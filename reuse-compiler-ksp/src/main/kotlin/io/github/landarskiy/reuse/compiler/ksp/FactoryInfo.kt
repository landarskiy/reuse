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

import com.google.devtools.ksp.symbol.KSClassDeclaration
import io.github.landarskiy.reuse.compiler.ksp.utils.titleCaseIgnoreLocale

data class FactoryInfo(
    val name: String,
    val factoryClass: KSClassDeclaration,
    val dataClass: KSClassDeclaration,
    val diffEntryGeneric: Boolean,
    val scopes: Set<String>
) {
    internal data class Builder(
        var name: String,
        var factoryClass: KSClassDeclaration,
        var dataClass: KSClassDeclaration? = null,
        var diffEntryGeneric: Boolean = false,
        var scopes: MutableSet<String> = mutableSetOf()
    ) {
        fun build(): FactoryInfo {
            return FactoryInfo(
                name = name,
                factoryClass = factoryClass,
                dataClass = requireNotNull(dataClass) { "Data class have to be specified" },
                diffEntryGeneric = diffEntryGeneric,
                scopes = scopes.map { it.titleCaseIgnoreLocale() }.toSet()
            )
        }
    }
}