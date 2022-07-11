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

import com.squareup.kotlinpoet.ClassName

internal const val PACKAGE_LIBRARY = "io.github.landarskiy.reuse"
internal const val INTERFACE_VIEW_HOLDER_FACTORY = "ViewHolderFactory"
internal const val INTERFACE_DIFF_ENTRY = "DiffEntry"

internal val CLASS_NAME_DIFF_ENTRY = ClassName(PACKAGE_LIBRARY, INTERFACE_DIFF_ENTRY)
internal val CLASS_NAME_RECYCLER_ITEM_VIEW_TYPE =
    ClassName(PACKAGE_LIBRARY, INTERFACE_VIEW_HOLDER_FACTORY)