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

package io.github.landarskiy.reuse.sample.screen.main.adapter.image

import io.github.landarskiy.reuse.DiffEntry
import io.github.landarskiy.reuse.sample.model.Content

data class ImageEntry(val content: Content.Image) : DiffEntry {

    override fun isSameEntry(other: DiffEntry): Boolean {
        if (other !is ImageEntry) {
            return false
        }
        return content.assetsPath == other.content.assetsPath
    }

    override fun isSameContent(other: DiffEntry): Boolean {
        return true
    }
}