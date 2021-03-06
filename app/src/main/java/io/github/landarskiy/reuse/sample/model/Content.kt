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

package io.github.landarskiy.reuse.sample.model

sealed class Content {

    data class Header(val text: String, val assetsPath: String) : Content()

    data class Text(val text: String, val style: Style) : Content() {

        enum class Style {
            H3, H5, H6, BODY, LIST_HEADER, LIST_CONTENT
        }
    }

    data class Image(val assetsPath: String, val width: Int, val height: Int) : Content()

    data class Copyright(val text: String, val url: String) : Content()

    data class GroupHeader(val expanded: Boolean) : Content()
}