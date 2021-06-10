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

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import io.github.landarskiy.reuse.BaseViewHolder
import io.github.landarskiy.reuse.ViewHolderFactory

abstract class TextViewHolderFactory : ViewHolderFactory<TextEntry>() {

    override fun createViewHolder(view: View): BaseViewHolder<TextEntry> {
        return TextViewHolder(view)
    }

    override fun createView(context: Context, parent: ViewGroup?): View {
        return TextView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }.apply {
            TextViewCompat.setTextAppearance(this, textAppearanceResId())
        }
    }

    abstract fun textAppearanceResId(): Int
}