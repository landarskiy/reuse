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

package io.github.landarskiy.reuse.sample.screen.main.adapter.text.types

import io.github.landarskiy.reuse.annotation.ViewHolderType
import io.github.landarskiy.reuse.sample.R
import io.github.landarskiy.reuse.sample.screen.main.adapter.SCOPE_MAIN
import io.github.landarskiy.reuse.sample.screen.main.adapter.text.TextViewHolderFactory

@ViewHolderType(scopes = [SCOPE_MAIN])
class TextH3ViewHolderFactory : TextViewHolderFactory() {

    override val typeId: Int
        get() = TYPE_ID

    override fun textAppearanceResId(): Int {
        return R.style.TextAppearance_MaterialComponents_Headline3
    }

    companion object {
        const val TYPE_ID = R.id.adapter_text_h3
    }
}