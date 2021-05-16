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

import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import androidx.core.text.HtmlCompat
import io.github.landarskiy.reuse.ItemViewHolder

class TextItemViewHolder(view: View) : ItemViewHolder<TextEntry>(view) {

    private val textView: TextView = view as TextView

    init {
        textView.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun bind(entry: TextEntry) {
        textView.text = HtmlCompat.fromHtml(entry.text, HtmlCompat.FROM_HTML_MODE_COMPACT)
    }
}