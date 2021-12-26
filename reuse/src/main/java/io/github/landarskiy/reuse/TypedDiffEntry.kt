/*
 * Copyright (C) 2021 Yauhen Landarski.
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

package io.github.landarskiy.reuse

/**
 * Specific implementation of [DiffEntry] with auto cast comparable entries.
 * for same [LayoutViewHolderFactory.typeId].
 *
 * For use this subtype you have to be sure that object which will be
 * provided to [isSameEntry] and [isSameContent] will be [T] type.
 */
abstract class TypedDiffEntry<T : DiffEntry> : DiffEntry {

    @Suppress("UNCHECKED_CAST")
    override fun isSameEntry(other: DiffEntry): Boolean {
        return isSameEntryTyped(other as T)
    }

    open fun isSameEntryTyped(other: T): Boolean {
        return false
    }

    @Suppress("UNCHECKED_CAST")
    override fun isSameContent(other: DiffEntry): Boolean {
        return isSameContentTyped(other as T)
    }

    open fun isSameContentTyped(other: T): Boolean {
        return false
    }

    @Suppress("UNCHECKED_CAST")
    override fun getDiffPayload(other: DiffEntry): Any? {
        return getDiffPayloadTyped(other as T)
    }

    open fun getDiffPayloadTyped(other: T): Any? {
        return null
    }
}