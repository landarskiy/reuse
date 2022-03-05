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

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.google.devtools.ksp.visitor.KSDefaultVisitor
import io.github.landarskiy.reuse.annotation.Factory
import io.github.landarskiy.reuse.annotation.ReuseModule

class ViewTypeProcessor(val codeGenerator: CodeGenerator, val logger: KSPLogger) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        //val moduleDeclaration = findModuleAnnotation(resolver)
        val factoryName = Factory::class.java.name
        val factorySymbols = resolver.getSymbolsWithAnnotation(factoryName)
        val ret = factorySymbols.filter { !it.validate() }.toList()
        factorySymbols
            .filter { it.validate() }
            .forEach {
                val annotation = it.annotations.find { annotation ->
                    annotation.shortName.asString() == factoryName
                }
                checkNotNull(annotation) {
                    "${(it as KSClassDeclaration).location} not annotated"
                }
                it.accept(FactoryVisitor(), annotation)
            }
        return ret
    }

    private fun findModuleAnnotation(resolver: Resolver): KSClassDeclaration {
        val moduleSymbols = resolver.getSymbolsWithAnnotation(ReuseModule::class.java.name).toList()
        check(moduleSymbols.isNotEmpty()) {
            "${ReuseModule::class.simpleName} annotated interface have to be exists"
        }
        check(moduleSymbols.size == 1) {
            "Only one interface with ${ReuseModule::class.simpleName} annotation have to be specified"
        }
        val moduleDeclaration: KSAnnotated = moduleSymbols.first()
        check(moduleDeclaration !is KSClassDeclaration || moduleDeclaration.classKind != ClassKind.INTERFACE) {
            "Only interfaces can be annotated via ${ReuseModule::class.simpleName} annotation"
        }
        return moduleDeclaration as KSClassDeclaration
    }

    inner class ModuleVisitor : KSDefaultVisitor<Unit, Unit>() {
        override fun defaultHandler(node: KSNode, data: Unit) {
            "Only interfaces can be annotated via ${ReuseModule::class.simpleName} annotation. Check ${node.location}"
        }

        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            check(classDeclaration.classKind != ClassKind.INTERFACE) {
                "Only interfaces can be annotated via ${ReuseModule::class.simpleName} annotation. Check ${classDeclaration.location}"
            }
        }
    }

    inner class FactoryVisitor : KSDefaultVisitor<KSAnnotation, FactoryInfo>() {
        override fun defaultHandler(node: KSNode, data: KSAnnotation): FactoryInfo {
            error("Only classes can be annotated via ${Factory::class.simpleName} annotation. Check ${node.location}")
        }

        override fun visitClassDeclaration(
            classDeclaration: KSClassDeclaration, data: KSAnnotation
        ): FactoryInfo {
            check(classDeclaration.classKind == ClassKind.CLASS) {
                "Only classes can be annotated via ${Factory::class.simpleName} annotation. Check ${classDeclaration.location}"
            }
            val builder = FactoryInfo.Builder(factory = classDeclaration)
            data.arguments.forEach {
                when (it.name?.asString()) {
                    "name" -> {}
                    "scopes" -> {}
                }
            }
            return builder.build()
        }
    }

    data class FactoryInfo(
        val name: String, val factory: KSClassDeclaration, val scopes: Set<String>
    ) {
        internal data class Builder(
            var name: String? = null,
            var factory: KSClassDeclaration? = null,
            var scopes: MutableSet<String> = mutableSetOf()
        ) {
            fun build(): FactoryInfo {
                return FactoryInfo(
                    name = checkNotNull(name) { "'name' have to be specified" },
                    factory = checkNotNull(factory) { "Factory not found" },
                    scopes = scopes.toSet()
                )
            }
        }
    }
}