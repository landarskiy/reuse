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

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSDefaultVisitor
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import io.github.landarskiy.reuse.annotation.ReuseModule
import io.github.landarskiy.reuse.compiler.ksp.FactoryInfo
import io.github.landarskiy.reuse.compiler.ksp.ReuseConfiguration
import io.github.landarskiy.reuse.compiler.ksp.ScopesBuilder

@KotlinPoetKspPreview
class ReuseModuleVisitor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val configuration: ReuseConfiguration
) : KSDefaultVisitor<Sequence<FactoryInfo>, Unit>() {

    private fun annotatedError(node: KSNode): String {
        return "Only interfaces can be annotated via ${ReuseModule::class.simpleName} annotation. Check ${node.location}"
    }

    override fun defaultHandler(node: KSNode, data: Sequence<FactoryInfo>) {
        error(annotatedError(node))
    }

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration, data: Sequence<FactoryInfo>
    ) {
        check(classDeclaration.classKind == ClassKind.INTERFACE) {
            annotatedError(classDeclaration)
        }
        val packageName = classDeclaration.packageName.asString()
        val declaredInterfaceName = classDeclaration.simpleName.getShortName()

        logger.info("Declared module interface name: $declaredInterfaceName, package: $packageName")
        ScopesBuilder(logger, codeGenerator, configuration).buildScopes("$packageName.types", data)
    }
}