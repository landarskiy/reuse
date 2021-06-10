package io.github.landarskiy.reuse.compiler

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.classinspector.elements.ElementsClassInspector
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import io.github.landarskiy.reuse.annotation.Factory
import io.github.landarskiy.reuse.annotation.ReuseModule
import java.io.File
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic


@KotlinPoetMetadataPreview
@AutoService(Processor::class)
class ViewTypeProcessor : AbstractProcessor() {

    private lateinit var types: Types
    private lateinit var elements: Elements
    private lateinit var filer: Filer
    private lateinit var messager: Messager
    private lateinit var classInspector: ElementsClassInspector

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(Factory::class.java.name, ReuseModule::class.java.name)
    }

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        this.types = processingEnv.typeUtils
        this.elements = processingEnv.elementUtils
        this.filer = processingEnv.filer
        this.messager = processingEnv.messager
        classInspector = ElementsClassInspector.create(elements, types) as ElementsClassInspector
    }

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment
    ): Boolean {
        //Step 1 - collect all scopes
        val scopesResult = ScopesBuilder.buildScopes(roundEnv)
        if (scopesResult is ScopesBuilder.Result.Error) {
            printError(scopesResult.message)
            return true
        }
        val scopes = (scopesResult as ScopesBuilder.Result.Success).scopes
        if (scopes.isEmpty()) {
            return false
        }

        //Step 2 - check module exists
        val viewTypeModule = getViewTypeModule(roundEnv) ?: return true

        val className = viewTypeModule.simpleName.toString()
        val pack = processingEnv.elementUtils.getPackageOf(viewTypeModule).toString()
        val fileName = "App${className}"
        val fileBuilder = FileSpec.builder(pack, fileName)
        val classBuilder = TypeSpec.objectBuilder(fileName)

        scopes.forEach { (scopeName, typeInfoList) ->
            val scopeFactory: ClassName = createScopeFactory(
                "$pack.types",
                scopeName,
                typeInfoList
            )
            classBuilder.addProperty(
                PropertySpec.builder(
                    scopeFactory.simpleName.replaceFirstChar { it.lowercaseChar() },
                    scopeFactory
                ).initializer("%T()", scopeFactory)
                    .build()
            )
        }

        val file = fileBuilder.addType(classBuilder.build()).build()
        saveToFile(file)
        return false
    }

    /**
     * Step 2 - check module interface exists
     */
    private fun getViewTypeModule(roundEnv: RoundEnvironment): Element? {
        val viewTypeModules = roundEnv.getElementsAnnotatedWith(ReuseModule::class.java)
        if (viewTypeModules.isEmpty()) {
            printError("${ReuseModule::class.simpleName} annotated class have to be exists")
            return null
        }
        if (viewTypeModules.size > 1) {
            printError("Only one class with ${ReuseModule::class.simpleName} annotation have to be exists")
            return null
        }
        val viewTypeModule = viewTypeModules.first()
        if (viewTypeModule.kind != ElementKind.INTERFACE) {
            printError("Only interfaces can be annotated via ${ReuseModule::class.simpleName} annotation")
            return null
        }
        return viewTypeModule
    }

    private fun createScopeFactory(
        pack: String,
        scopeName: String,
        typeInfoList: List<ScopesBuilder.TypeInfo>
    ): ClassName {
        val fileName = "${scopeName}RecyclerContentFactory"
        val fileBuilder = FileSpec.builder(pack, fileName)
        val classBuilder = TypeSpec.classBuilder(fileName)
        val factoryClassName = ClassName(pack, fileName)

        val entryType = ClassName(PACKAGE_LIBRARY, "AdapterEntry")

        val dataBuilderClassBuilder =
            TypeSpec.classBuilder(CLASS_DATA_BUILDER).addModifiers(KModifier.INNER)
        val resultClassName = ClassName(pack, fileName).nestedClass(CLASS_DATA_BUILDER)

        //Check is all view types implement DiffEntry or not.
        val allParametrizedByDiffEntry = isAllParametrizedByDiffEntry(typeInfoList)
        val contentClass = if (allParametrizedByDiffEntry) {
            INTERFACE_DATA_ITEM
        } else {
            ClassName("kotlin", "Any")
        }

        dataBuilderClassBuilder
            .addProperty(
                PropertySpec.builder(
                    PROPERTY_CONTENT,
                    MUTABLE_LIST.parameterizedBy(entryType.parameterizedBy(contentClass)),
                    KModifier.PRIVATE
                ).initializer("mutableListOf()").build()
            )

        val initializerBlock = CodeBlock.builder().addStatement(
            "val $PROPERTY_ITEM_VIEW_TYPES: %T = mutableListOf()",
            MUTABLE_LIST.parameterizedBy(
                CLASS_NAME_RECYCLER_ITEM_VIEW_TYPE.parameterizedBy(
                    WildcardTypeName.producerOf(contentClass)
                )
            )
        )

        typeInfoList.forEach { typeInfo ->
            if (typeInfo.element !is TypeElement) {
                return@forEach
            }
            val viewTypeClassName = typeInfo.name
            val viewTypePackage =
                processingEnv.elementUtils.getPackageOf(typeInfo.element).toString()
            val viewTypeClass = ClassName(viewTypePackage, typeInfo.element.simpleName.toString())
            val viewTypePropertyName = viewTypeClassName.replaceFirstChar { it.lowercaseChar() }

            val superClass = typeInfo.element.superclass
            if (superClass !is DeclaredType) {
                return@forEach
            }
            initializerBlock.addStatement("$PROPERTY_ITEM_VIEW_TYPES.add(${viewTypePropertyName})")
            //get type for classes
            val dataType = findViewTypeParametrizedType(typeInfo.element) ?: return@forEach
            val entryDataItemStatement =
                "${entryType.simpleName}(${viewTypePropertyName}.$PROPERTY_TYPE_ID, $ARG_DATA_ITEM)"
            classBuilder
                .addProperty(
                    PropertySpec.builder(
                        viewTypePropertyName,
                        viewTypeClass,
                        KModifier.PRIVATE
                    ).initializer("%T()", viewTypeClass).build()
                )
            dataBuilderClassBuilder.addFunction(
                FunSpec.builder("with${viewTypeClassName}")
                    .addParameter(ParameterSpec.builder(ARG_DATA_ITEM, dataType).build())
                    .addStatement(
                        "$PROPERTY_CONTENT.add($entryDataItemStatement)"
                    ).addStatement("return this")
                    .returns(resultClassName)
                    .build()
            ).addFunction(
                FunSpec.builder("with${viewTypeClassName}")
                    .addParameter(
                        ParameterSpec.builder(ARG_DATA_ITEMS, LIST.parameterizedBy(dataType))
                            .build()
                    ).addStatement(
                        "$PROPERTY_CONTENT.addAll($ARG_DATA_ITEMS.map { $ARG_DATA_ITEM -> $entryDataItemStatement})"
                    ).addStatement("return this")
                    .returns(resultClassName)
                    .build()
            )
        }

        dataBuilderClassBuilder.addFunction(
            FunSpec.builder("build")
                .addStatement("return $PROPERTY_CONTENT.toList()")
                .returns(LIST.parameterizedBy(entryType.parameterizedBy(contentClass)))
                .build()
        )
        classBuilder
            .addProperty(
                PropertySpec.builder(
                    PROPERTY_ITEM_VIEW_TYPES,
                    LIST.parameterizedBy(
                        CLASS_NAME_RECYCLER_ITEM_VIEW_TYPE.parameterizedBy(
                            WildcardTypeName.producerOf(contentClass)
                        )
                    ),
                    KModifier.PUBLIC
                ).build()
            ).addFunction(
                FunSpec.builder("newDataBuilder")
                    .addStatement("return %T()", resultClassName)
                    .returns(resultClassName)
                    .build()
            )
            .addType(dataBuilderClassBuilder.build())

        initializerBlock.addStatement("this.$PROPERTY_ITEM_VIEW_TYPES = $PROPERTY_ITEM_VIEW_TYPES.toList()")
        classBuilder.addInitializerBlock(initializerBlock.build())
        saveToFile(fileBuilder.addType(classBuilder.build()).build())
        return factoryClassName
    }

    private fun findViewTypeParametrizedType(element: TypeElement): TypeName? {
        val superClass = element.superclass
        if (superClass !is DeclaredType) {
            return null
        }
        var dataType = superClass.typeArguments.firstOrNull()?.asTypeName()
        if (dataType == null) {
            //if dataType is null that can means that implemented directly interface
            val recyclerItemInterface = element.interfaces.find { typeMirror ->
                if (typeMirror !is DeclaredType) {
                    return@find false
                }
                val typeElement = typeMirror.asElement()
                val interfacePackage =
                    processingEnv.elementUtils.getPackageOf(typeElement).toString()
                val interfaceName = typeElement.simpleName.toString()
                interfacePackage == PACKAGE_LIBRARY && interfaceName == INTERFACE_RECYCLER_ITEM_VIEW_TYPE
            }
            if (recyclerItemInterface is DeclaredType) {
                dataType = recyclerItemInterface.typeArguments.firstOrNull()?.asTypeName()
            }
        }
        return dataType ?: findViewTypeParametrizedType(superClass.asElement() as TypeElement)
    }

    private fun isAllParametrizedByDiffEntry(typeInfoList: List<ScopesBuilder.TypeInfo>): Boolean {
        return typeInfoList.all {
            if (it.element !is TypeElement) {
                return@all false
            }
            val parametrizedType = findViewTypeParametrizedType(it.element) ?: return@all false
            isImplementDiffEntry(parametrizedType)
        }
    }

    private fun isImplementDiffEntry(tn: TypeName): Boolean {
        return isImplementDiffEntry(elements.getTypeElement(tn.toString()).asType())
    }

    private fun isImplementDiffEntry(tm: TypeMirror): Boolean {
        val serializable: TypeMirror =
            elements.getTypeElement(INTERFACE_DATA_ITEM.canonicalName).asType()
        return types.isAssignable(tm, serializable)
    }

    private fun saveToFile(fileSpec: FileSpec): Boolean {
        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        if (kaptKotlinGeneratedDir == null) {
            printError("Can't find kapt output directory")
            return true
        }
        fileSpec.writeTo(File(kaptKotlinGeneratedDir))
        return false
    }

    private fun printError(message: String) {
        messager.printMessage(Diagnostic.Kind.ERROR, "$message\r\n")
    }

    private fun printNote(message: String) {
        messager.printMessage(Diagnostic.Kind.NOTE, "$message\r\n")
    }

    private fun printDebug(message: String) {
        println(message)
    }

    companion object {

        private const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
        private const val PACKAGE_LIBRARY = "io.github.landarskiy.reuse"
        private const val PROPERTY_TYPE_ID = "typeId"
        private const val PROPERTY_ITEM_VIEW_TYPES = "types"
        private const val PROPERTY_CONTENT = "content"
        private const val ARG_DATA_ITEM = "dataItem"
        private const val ARG_DATA_ITEMS = "dataItems"
        private const val CLASS_DATA_BUILDER = "DataBuilder"
        private const val INTERFACE_RECYCLER_ITEM_VIEW_TYPE = "ViewHolderFactory"

        private val INTERFACE_DATA_ITEM = ClassName(PACKAGE_LIBRARY, "DiffEntry")
        private val CLASS_NAME_RECYCLER_ITEM_VIEW_TYPE =
            ClassName(PACKAGE_LIBRARY, INTERFACE_RECYCLER_ITEM_VIEW_TYPE)
    }
}