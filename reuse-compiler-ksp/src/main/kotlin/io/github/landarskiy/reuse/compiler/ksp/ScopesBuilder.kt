/*
 * Copyright (C) 2022 Yauhen Landarski.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.landarskiy.reuse.compiler.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo

@KotlinPoetKspPreview
class ScopesBuilder(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
    private val reuseConfiguration: ReuseConfiguration
) {

    fun buildScopes(pack: String, data: Sequence<FactoryInfo>): List<ScopeInfo> {
        val scopes: Map<String, List<FactoryInfo>> = mergeToScopes(data)
        return scopes.filter { it.value.isNotEmpty() }.map { (scope, factories) ->
            createScope(pack, scope, factories)
        }
    }

    private fun createScope(
        pack: String, scopeName: String, factories: List<FactoryInfo>
    ): ScopeInfo {
        val fileName = "Reuse${scopeName}ContentScope"
        val fileBuilder = FileSpec.builder(pack, fileName)
        val classBuilder = TypeSpec.classBuilder(fileName)
        val scopeFactoryClassName = ClassName(pack, fileName)

        val entryType = ClassName(PACKAGE_LIBRARY, "AdapterEntry")

        val dataBuilderClassBuilder =
            TypeSpec.classBuilder(CLASS_DATA_BUILDER).addModifiers(KModifier.INNER)
        val resultClassName = ClassName(pack, fileName).nestedClass(CLASS_DATA_BUILDER)

        //Check is all view types implement DiffEntry or not.
        val allParametrizedByDiffEntry = isAllParametrizedByDiffEntry(factories)
        val contentClass = if (allParametrizedByDiffEntry) {
            CLASS_NAME_DIFF_ENTRY
        } else {
            ClassName("kotlin", "Any")
        }

        val contentReturnType = entryType.parameterizedBy(contentClass)

        dataBuilderClassBuilder
            .primaryConstructor(
                FunSpec.constructorBuilder().addParameter(
                    ParameterSpec.builder(
                        PROPERTY_CONTENT,
                        MUTABLE_LIST.parameterizedBy(contentReturnType)
                    ).build()
                ).build()
            )
            .addProperty(
                PropertySpec.builder(
                    PROPERTY_CONTENT,
                    MUTABLE_LIST.parameterizedBy(contentReturnType),
                    KModifier.PRIVATE
                ).initializer(PROPERTY_CONTENT).build()
            )

        val viewTypePropertyNames = mutableListOf<String>()

        factories.forEach { typeInfo ->
            val factoryVariableName = typeInfo.name
            val factoryPackage = typeInfo.factoryClass.packageName.asString()
            val factoryClassName =
                ClassName(factoryPackage, typeInfo.factoryClass.simpleName.getShortName())
            val factoryPropertyName = factoryVariableName.replaceFirstChar { it.lowercaseChar() }

            viewTypePropertyNames.add(factoryPropertyName)
            //get type for classes
            val entryDataItemStatement =
                "${entryType.simpleName}(${factoryPropertyName}.$PROPERTY_TYPE_ID, $ARG_DATA_ITEM)"
            val mapFunction = "map${factoryVariableName}"
            classBuilder
                .addProperty(
                    PropertySpec.builder(
                        factoryPropertyName,
                        factoryClassName
                    ).initializer("%T()", factoryClassName).build()
                )
            val dataType = typeInfo.dataClass.toClassName()
            dataBuilderClassBuilder.addFunction(
                FunSpec.builder("with${factoryVariableName}")
                    .addParameter(ParameterSpec.builder(ARG_DATA_ITEM, dataType).build())
                    .addStatement(
                        "$PROPERTY_CONTENT.add($mapFunction($ARG_DATA_ITEM))"
                    ).addStatement("return this")
                    .returns(resultClassName)
                    .build()
            ).addFunction(
                FunSpec.builder("with${factoryVariableName}")
                    .addParameter(
                        ParameterSpec.builder(ARG_DATA_ITEMS, LIST.parameterizedBy(dataType))
                            .build()
                    ).addStatement(
                        "$PROPERTY_CONTENT.addAll($ARG_DATA_ITEMS.map { $ARG_DATA_ITEM -> $mapFunction($ARG_DATA_ITEM)})"
                    ).addStatement("return this")
                    .returns(resultClassName)
                    .build()
            ).addFunction(
                FunSpec.builder(mapFunction)
                    .addParameter(ParameterSpec.builder(ARG_DATA_ITEM, dataType).build())
                    .addStatement("return $entryDataItemStatement")
                    .returns(contentReturnType)
                    .build()
            )
        }

        dataBuilderClassBuilder.addFunction(
            FunSpec.builder("build")
                .addStatement("return $PROPERTY_CONTENT.toList()")
                .returns(LIST.parameterizedBy(contentReturnType))
                .build()
        )
        classBuilder
            .addProperty(
                PropertySpec.builder(
                    PROPERTY_FACTORIES,
                    LIST.parameterizedBy(
                        CLASS_NAME_RECYCLER_ITEM_VIEW_TYPE.parameterizedBy(
                            WildcardTypeName.producerOf(contentClass)
                        )
                    )
                ).build()
            ).addFunction(
                FunSpec.builder("newDataBuilder")
                    .addParameter(
                        ParameterSpec.builder(
                            PROPERTY_CONTENT,
                            MUTABLE_LIST.parameterizedBy(contentReturnType)
                        ).defaultValue("mutableListOf()").build()
                    )
                    .addStatement("return %T($PROPERTY_CONTENT)", resultClassName)
                    .returns(resultClassName)
                    .build()
            )
            .addType(dataBuilderClassBuilder.build())

        val initializerBlock =
            CodeBlock.builder().addStatement("this.$PROPERTY_FACTORIES = listOf(")
        viewTypePropertyNames.forEachIndexed { index, propertyName ->
            if (index == viewTypePropertyNames.size - 1) {
                initializerBlock.addStatement(propertyName)
            } else {
                initializerBlock.addStatement("$propertyName,")
            }
        }
        initializerBlock.addStatement(")")
        classBuilder.addInitializerBlock(initializerBlock.build())

        val factoriesFiles = factories.mapNotNull { it.factoryClass.containingFile }
        val file = fileBuilder.addType(classBuilder.build()).build()
        logger.info("$scopeName scope dependencies: $factoriesFiles")
        file.writeTo(codeGenerator, Dependencies(true, *factoriesFiles.toTypedArray()))

        return ScopeInfo(scopeFactoryClassName, file)
    }

    private fun isAllParametrizedByDiffEntry(factories: List<FactoryInfo>): Boolean {
        return factories.all { it.diffEntryGeneric }
    }

    private fun mergeToScopes(factories: Sequence<FactoryInfo>): Map<String, List<FactoryInfo>> {
        val scopes: MutableMap<String, MutableList<FactoryInfo>> = mutableMapOf()
        val defaultScope: MutableList<FactoryInfo> = mutableListOf()
        scopes[SCOPE_DEFAULT] = defaultScope

        factories.forEach { factoryInfo ->
            when (reuseConfiguration.defaultScopeGenerateMode) {
                ReuseConfiguration.DefaultScopeGenerateMode.ALWAYS -> defaultScope.add(factoryInfo)
                ReuseConfiguration.DefaultScopeGenerateMode.EMPTY_SCOPES -> {
                    if (factoryInfo.scopes.isEmpty()) {
                        defaultScope.add(factoryInfo)
                    }
                }
                ReuseConfiguration.DefaultScopeGenerateMode.NEVER -> {}
            }
            factoryInfo.scopes.forEach { scopeName ->
                var scope = scopes[scopeName]
                if (scope == null) {
                    scope = mutableListOf()
                    scopes[scopeName] = scope
                }
                scope.add(factoryInfo)
            }
        }
        return scopes
    }

    data class ScopeInfo(val scopeFactoryClassName: ClassName, val file: FileSpec)

    companion object {
        private const val SCOPE_DEFAULT = io.github.landarskiy.reuse.annotation.SCOPE_DEFAULT

        private const val PROPERTY_TYPE_ID = "typeId"
        private const val PROPERTY_FACTORIES = "factories"
        private const val PROPERTY_CONTENT = "content"
        private const val ARG_DATA_ITEM = "dataItem"
        private const val ARG_DATA_ITEMS = "dataItems"
        private const val CLASS_DATA_BUILDER = "DataBuilder"
    }
}