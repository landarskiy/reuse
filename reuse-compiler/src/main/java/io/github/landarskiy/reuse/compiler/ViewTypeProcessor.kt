package io.github.landarskiy.reuse.compiler

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.landarskiy.reuse.annotation.ViewType
import io.github.landarskiy.reuse.annotation.ViewTypeModule
import java.io.File
import java.util.*
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.tools.Diagnostic


@AutoService(Processor::class)
class ViewTypeProcessor : AbstractProcessor() {

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(ViewType::class.java.name, ViewTypeModule::class.java.name)
    }

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment
    ): Boolean {
        val scopesResult = ScopesBuilder.buildScopes(roundEnv)
        if (scopesResult is ScopesBuilder.Result.Error) {
            printError(scopesResult.message)
            return true
        }
        val scopes = (scopesResult as ScopesBuilder.Result.Success).scopes
        if (scopes.isEmpty()) {
            return false
        }

        val viewTypeModule = getViewTypeModule(roundEnv) ?: return true

        val className = viewTypeModule.simpleName.toString()
        val pack = processingEnv.elementUtils.getPackageOf(viewTypeModule).toString()
        val fileName = "App${className}"
        val fileBuilder = FileSpec.builder(pack, fileName)
        val classBuilder = TypeSpec.objectBuilder(fileName)

        scopes.forEach { (scopeName, viewTypes) ->
            val scopeFactory: ClassName = createScopeFactory(
                "$pack.types",
                scopeName,
                viewTypes
            )
            classBuilder.addProperty(
                PropertySpec.builder(
                    scopeFactory.simpleName.decapitalize(Locale.ROOT),
                    scopeFactory
                ).initializer("%T()", scopeFactory)
                    .build()
            )
        }

        val file = fileBuilder.addType(classBuilder.build()).build()
        saveToFile(file)
        return false
    }

    private fun getViewTypeModule(roundEnv: RoundEnvironment): Element? {
        val viewTypeModules = roundEnv.getElementsAnnotatedWith(ViewTypeModule::class.java)
        if (viewTypeModules.isEmpty()) {
            printError("${ViewTypeModule::class.simpleName} annotated class have to be exists")
            return null
        }
        if (viewTypeModules.size > 1) {
            printError("Only one class with ${ViewTypeModule::class.simpleName} annotation have to be exists")
            return null
        }
        val viewTypeModule = viewTypeModules.first()
        if (viewTypeModule.kind != ElementKind.INTERFACE) {
            printError("Only interfaces can be annotated via ${ViewTypeModule::class.simpleName} annotation")
            return null
        }
        return viewTypeModule
    }

    private fun createScopeFactory(
        pack: String,
        scopeName: String,
        viewTypes: List<Element>
    ): ClassName {
        val fileName = "${scopeName}RecyclerContentFactory"
        val fileBuilder = FileSpec.builder(pack, fileName)
        val classBuilder = TypeSpec.classBuilder(fileName)
        val factoryClassName = ClassName(pack, fileName)

        val entryType = ClassName(PACKAGE_LIBRARY, "Adapter").nestedClass("AdapterEntry")

        val dataBuilderClassBuilder =
            TypeSpec.classBuilder(CLASS_DATA_BUILDER).addModifiers(KModifier.INNER)
        val resultClassName = ClassName(pack, fileName).nestedClass(CLASS_DATA_BUILDER)

        dataBuilderClassBuilder
            .addProperty(
                PropertySpec.builder(
                    PROPERTY_CONTENT,
                    MUTABLE_LIST.parameterizedBy(entryType),
                    KModifier.PRIVATE
                ).initializer("mutableListOf()").build()
            )

        val initializerBlock = CodeBlock.builder().addStatement(
            "val $PROPERTY_ITEM_VIEW_TYPES: %T = mutableListOf()",
            MUTABLE_LIST.parameterizedBy(CLASS_NAME_RECYCLER_ITEM_VIEW_TYPE)
        )

        viewTypes.forEach { viewType ->
            if (viewType !is TypeElement) {
                return@forEach
            }
            val viewTypeClassName = viewType.simpleName.toString()
            val viewTypePackage = processingEnv.elementUtils.getPackageOf(viewType).toString()
            val viewTypeClass = ClassName(viewTypePackage, viewTypeClassName)
            val viewTypePropertyName = viewTypeClassName.decapitalize(Locale.ROOT)

            val className = "$viewTypePackage.$viewTypeClassName"
            processingEnv.messager.printMessage(
                Diagnostic.Kind.NOTE,
                "Process $className class for $scopeName scope\n"
            )
            val superClass = viewType.superclass
            if (superClass !is DeclaredType) {
                return@forEach
            }
            initializerBlock.addStatement("$PROPERTY_ITEM_VIEW_TYPES.add(${viewTypePropertyName})")
            //get type for classes
            var dataType = superClass.typeArguments.firstOrNull()?.asTypeName()
            //TODO make search through hierarchy
            if (dataType == null) {
                //if dataType is null that can means that implemented directly interface
                val recyclerItemInterface = viewType.interfaces.find { typeMirror ->
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
                if (dataType == null) {
                    return@forEach
                }
            }
            val entryDataItemStatement =
                "${entryType.topLevelClassName().simpleName}.${entryType.simpleName}(${viewTypePropertyName}.$PROPERTY_TYPE_ID, $ARG_DATA_ITEM)"
            classBuilder
                .addProperty(
                    PropertySpec.builder(
                        viewTypePropertyName,
                        viewTypeClass,
                        KModifier.PRIVATE
                    ).initializer("%T()", viewTypeClass).build()
                )
            dataBuilderClassBuilder.addFunction(
                FunSpec.builder("with${viewTypeClassName}Item")
                    .addParameter(ParameterSpec.builder(ARG_DATA_ITEM, dataType).build())
                    .addStatement(
                        "$PROPERTY_CONTENT.add($entryDataItemStatement)"
                    ).addStatement("return this")
                    .returns(resultClassName)
                    .build()
            ).addFunction(
                FunSpec.builder("with${viewTypeClassName}Items")
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
                .returns(LIST.parameterizedBy(entryType))
                .build()
        )
        classBuilder
            .addProperty(
                PropertySpec.builder(
                    PROPERTY_ITEM_VIEW_TYPES,
                    LIST.parameterizedBy(CLASS_NAME_RECYCLER_ITEM_VIEW_TYPE),
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
        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, message)
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
        private const val INTERFACE_RECYCLER_ITEM_VIEW_TYPE = "RecyclerItemViewType"

        private val INTERFACE_DATA_ITEM = ClassName(PACKAGE_LIBRARY, "Entry")
        private val CLASS_NAME_RECYCLER_ITEM_VIEW_TYPE =
            ClassName(PACKAGE_LIBRARY, INTERFACE_RECYCLER_ITEM_VIEW_TYPE).parameterizedBy(
                WildcardTypeName.producerOf(INTERFACE_DATA_ITEM)
            )
    }
}