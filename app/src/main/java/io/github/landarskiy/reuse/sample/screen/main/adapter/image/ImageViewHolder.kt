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

import android.net.Uri
import android.view.View
import android.widget.ImageView
import coil.load
import io.github.landarskiy.reuse.BaseViewHolder

class ImageViewHolder(view: View) : BaseViewHolder<ImageEntry>(view) {

    private val imageView: ImageView = view as ImageView

    override fun bind(data: ImageEntry) {
        imageView.load(Uri.parse("file:///android_asset/${data.content.assetsPath}")) {
            crossfade(true)
            placeholder(null)
        }
    }
}