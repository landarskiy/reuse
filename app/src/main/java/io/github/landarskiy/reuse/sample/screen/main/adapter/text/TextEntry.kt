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

package io.github.landarskiy.reuse.sample.screen.main.adapter.text

import io.github.landarskiy.reuse.Entry

data class TextEntry(val text: String, val style: Style) : Entry {

    override fun isSameEntry(other: Entry): Boolean {
        if (other !is TextEntry) {
            return false
        }
        return text == other.text && style == other.style
    }

    override fun isSameContent(other: Entry): Boolean {
        return true
    }

    enum class Style {
        H3, H5, H6, BODY, LIST_HEADER, LIST_CONTENT
    }
}