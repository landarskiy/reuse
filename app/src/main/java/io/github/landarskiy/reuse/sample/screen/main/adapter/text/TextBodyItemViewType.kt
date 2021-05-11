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
import io.github.landarskiy.reuse.annotation.ViewType
import io.github.landarskiy.reuse.sample.R

@ViewType
class TextBodyItemViewType : TextItemViewType() {

    override val typeId: Int
        get() = TYPE_ID

    override fun textAppearanceResId(): Int {
        return R.style.TextAppearance_MaterialComponents_Body1
    }

    companion object {
        const val TYPE_ID = R.id.adapter_text_body
    }
}