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
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import io.github.landarskiy.reuse.annotation.ReuseFactory
import io.github.landarskiy.reuse.annotation.ReuseModule
import io.github.landarskiy.reuse.compiler.ksp.visitor.ReuseFactoryVisitor
import io.github.landarskiy.reuse.compiler.ksp.visitor.ReuseModuleVisitor

@KotlinPoetKspPreview
class ReuseProcessor(
    private val configuration: ReuseConfiguration,
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    private val factoryVisitor: ReuseFactoryVisitor = ReuseFactoryVisitor(logger, configuration)

    init {
        displayConfigurationInfo()
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        factoryVisitor.resolver = resolver
        val ret: MutableList<KSAnnotated> = mutableListOf()

        val moduleClassName = ReuseModule::class.java.name
        val moduleSymbols = resolver.getSymbolsWithAnnotation(moduleClassName)

        val moduleToProcessing = moduleSymbols.filter { it.validate() }
        ret.addAll(moduleSymbols.filter { !it.validate() })

        if (moduleToProcessing.count() == 0) {
            logger.info("Processing skipped, reason: not found interface annotated via '${ReuseModule::class.java.simpleName}'")
            return ret
        }

        val factoryClassName = ReuseFactory::class.java.name
        val factorySymbols = resolver.getSymbolsWithAnnotation(factoryClassName)
        val factoryToProcessing = factorySymbols.filter { it.validate() }
        ret.addAll(factorySymbols.filter { !it.validate() })

        val factories: Sequence<FactoryInfo> = factoryToProcessing.map {
            val factoryAnnotations =
                it.annotations.filter { annotation ->
                    factoryClassName == annotation.annotationType.resolve().declaration.qualifiedName?.asString()
                }
            check(factoryAnnotations.count() == 1) {
                "Only one 'Factory' declaration is allowed"
            }
            it.accept(factoryVisitor, factoryAnnotations.first())
        }

        check(moduleToProcessing.count() == 1) {
            "Only one interface can be declared by '${ReuseModule::class.simpleName}' annotation (found: ${moduleToProcessing.count()})"
        }
        with(moduleToProcessing.first()) {
            accept(ReuseModuleVisitor(codeGenerator, logger, configuration), factories)
        }
        return ret
    }

    private fun displayConfigurationInfo() {
        if(configuration.checkFactoryInstance) {
            logger.info("You can set ${ReuseConfiguration.ARG_CHECK_FACTORY_INSTANCE} to 'false' for speed optimization during code generation.")
        }
    }
}