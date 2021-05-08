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

package io.github.landarskiy.reuse

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Base ViewHolder implementation for support [Entry] items.
 *
 * @param view related view
 */
abstract class EntryViewHolder<T : Entry>(view: View) : RecyclerView.ViewHolder(view) {

    val context: Context = view.context

    /**
     * Should be called from [RecyclerView.Adapter.onBindViewHolder]
     *
     * @param entry bindable entry
     */
    @Suppress("UNCHECKED_CAST")
    fun bindData(entry: Entry) {
        bind(entry as T)
    }

    /**
     * Should be call from [bindData], do not use this method directly.
     *
     * @param entry bindable entry
     */
    abstract fun bind(entry: T)
}