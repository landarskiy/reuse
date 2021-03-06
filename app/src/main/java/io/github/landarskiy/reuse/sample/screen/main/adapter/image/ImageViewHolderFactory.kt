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

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import io.github.landarskiy.reuse.ReuseViewHolder
import io.github.landarskiy.reuse.ViewHolderFactory
import io.github.landarskiy.reuse.annotation.ReuseFactory
import io.github.landarskiy.reuse.sample.R
import io.github.landarskiy.reuse.sample.screen.main.adapter.SCOPE_MAIN

@ReuseFactory(name = "Image", scopes = [SCOPE_MAIN])
class ImageViewHolderFactory : ViewHolderFactory<ImageEntry>() {

    override val typeId: Int
        get() = TYPE_ID

    override fun createViewHolder(view: View): ReuseViewHolder<ImageEntry> {
        return ImageViewHolder(view)
    }

    override fun createView(context: Context, parent: ViewGroup?): View {
        return ImageView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            adjustViewBounds = true
        }
    }

    companion object {

        const val TYPE_ID = R.id.adapter_image
    }
}