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

package io.github.landarskiy.reuse.sample.screen.main.adapter.header

import android.view.View
import io.github.landarskiy.reuse.BaseViewHolder
import io.github.landarskiy.reuse.LayoutViewHolderFactory
import io.github.landarskiy.reuse.annotation.Factory
import io.github.landarskiy.reuse.sample.R
import io.github.landarskiy.reuse.sample.screen.main.adapter.SCOPE_MAIN

@Factory(name = "Header", scopes = [SCOPE_MAIN])
class HeaderViewHolderFactory : LayoutViewHolderFactory<HeaderEntry>(TYPE_ID) {

    override fun createViewHolder(view: View): BaseViewHolder<HeaderEntry> {
        return HeaderViewHolder(view)
    }

    companion object {
        const val TYPE_ID = R.layout.item_header
    }
}