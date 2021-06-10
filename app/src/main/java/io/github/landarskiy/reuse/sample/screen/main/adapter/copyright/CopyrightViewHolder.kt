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

package io.github.landarskiy.reuse.sample.screen.main.adapter.copyright

import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.text.HtmlCompat
import io.github.landarskiy.reuse.BaseViewHolder
import io.github.landarskiy.reuse.sample.databinding.ItemCopyrightBinding

class CopyrightViewHolder(view: View) : BaseViewHolder<CopyrightEntry>(view) {

    private val binding: ItemCopyrightBinding = ItemCopyrightBinding.bind(view)

    init {
        binding.link.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun bind(data: CopyrightEntry) {
        with(binding) {
            text.text = data.content.text
            link.text = HtmlCompat.fromHtml(data.content.url, HtmlCompat.FROM_HTML_MODE_COMPACT)
        }
    }
}