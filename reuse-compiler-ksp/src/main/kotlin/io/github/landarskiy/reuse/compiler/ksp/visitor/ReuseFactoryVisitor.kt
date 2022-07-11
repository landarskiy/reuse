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

package io.github.landarskiy.reuse.compiler.ksp.visitor

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.visitor.KSDefaultVisitor
import io.github.landarskiy.reuse.annotation.ReuseFactory
import io.github.landarskiy.reuse.compiler.ksp.*
import io.github.landarskiy.reuse.compiler.ksp.utils.titleCaseIgnoreLocale
import io.github.landarskiy.reuse.compiler.ksp.utils.typedValue

/**
 * Prepare classes annotated via [ReuseFactory] interface:
 * -extract name
 * -extract class declaration
 * -extract generic declaration
 * -extract scopes
 */
class ReuseFactoryVisitor(
    private val logger: KSPLogger,
    private val configuration: ReuseConfiguration
) :
    KSDefaultVisitor<KSAnnotation, FactoryInfo>() {

    var factoryGenericType: FactoryGenericType = FactoryGenericType.AUTO
    lateinit var resolver: Resolver

    private val factoryName: KSName by lazy {
        resolver.getKSNameFromString("$PACKAGE_LIBRARY.$INTERFACE_VIEW_HOLDER_FACTORY")
    }
    private val factoryClassDeclaration: KSClassDeclaration by lazy {
        requireNotNull(resolver.getClassDeclarationByName(factoryName)) {
            "Can't find base factory class $factoryName. Check your dependencies."
        }
    }
    private val diffEntryName: KSName by lazy {
        resolver.getKSNameFromString("$PACKAGE_LIBRARY.$INTERFACE_DIFF_ENTRY")
    }
    private val diffEntryClassDeclaration: KSClassDeclaration by lazy {
        requireNotNull(resolver.getClassDeclarationByName(diffEntryName)) {
            "Can't find base diff entry class $diffEntryName. Check your dependencies."
        }
    }

    private fun annotatedError(node: KSNode): String {
        return "Only classes can be annotated via ${ReuseFactory::class.simpleName} annotation. Check ${node.location}"
    }

    override fun defaultHandler(node: KSNode, data: KSAnnotation): FactoryInfo {
        error(annotatedError(node))
    }

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration, data: KSAnnotation
    ): FactoryInfo {
        logger.info("Start handle ${classDeclaration.qualifiedName?.asString()} factory class.")
        check(classDeclaration.classKind == ClassKind.CLASS) {
            annotatedError(classDeclaration)
        }
        val builder = FactoryInfo.Builder(
            name = defaultFactoryName(classDeclaration),
            factoryClass = classDeclaration
        )
        data.arguments.forEach {
            when (it.name?.asString()) {
                "name" -> {
                    val name: String = it.typedValue()
                    if (name.isNotBlank()) {
                        builder.name = name.titleCaseIgnoreLocale()
                    }
                }
                "scopes" -> {
                    builder.scopes.addAll(it.typedValue())
                }
            }
        }
        check(isFactoryDeclaration(classDeclaration)) {
            "Only $INTERFACE_VIEW_HOLDER_FACTORY subclasses can be annotated by ${ReuseFactory::class.java.simpleName} declaration."
        }
        val param = findParametrizedType(classDeclaration)
        with(builder) {
            dataClass = param.first
            diffEntryGeneric = param.second
        }
        return builder.build()
    }

    private fun defaultFactoryName(classDeclaration: KSClassDeclaration): String {
        return classDeclaration.simpleName.getShortName().titleCaseIgnoreLocale()
    }

    private fun isFactoryDeclaration(classDeclaration: KSClassDeclaration): Boolean {
        if (!configuration.checkFactoryInstance) {
            return true
        }
        return classDeclaration.isInstanceOf(factoryClassDeclaration)
    }

    private fun findParametrizedType(factoryClassDeclaration: KSClassDeclaration): Pair<KSClassDeclaration, Boolean> {
        val params = factoryClassDeclaration.getAllSuperTypes()
            .map { it.arguments }
            .flatten()
            .mapNotNull { it.type }
            .map { it.resolve().declaration }
            .filterIsInstance(KSClassDeclaration::class.java)
        val diffEntry = params.find { it.isInstanceOf(diffEntryClassDeclaration) }
        return if (diffEntry != null) {
            Pair(diffEntry, true)
        } else {
            val entry = requireNotNull(params.first()) {
                "Factory have to be specified generic argument. Check ${factoryClassDeclaration.qualifiedName?.asString()}"
            }
            Pair(entry, false)
        }
    }

    private fun KSClassDeclaration.isInstanceOf(target: KSClassDeclaration): Boolean {
        val targetName = target.qualifiedName
        return getAllSuperTypes().mapNotNull {
            it.declaration.qualifiedName
        }.any {
            it == targetName
        }
    }

    enum class FactoryGenericType {
        DIFF_ENTRY, ANY, AUTO
    }
}