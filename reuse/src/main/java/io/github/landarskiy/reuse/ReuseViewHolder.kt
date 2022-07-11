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
 * Base ViewHolder implementation for any type of data.
 *
 * @param view related view
 *
 * @see [ViewHolderFactory]
 */
abstract class ReuseViewHolder<T>(view: View) : RecyclerView.ViewHolder(view) {

    val context: Context = view.context

    /**
     * Bind data to view.
     * In cases when used in custom adapters should be called from [RecyclerView.Adapter.onBindViewHolder]
     *
     * @param data data to bind
     */
    abstract fun bind(data: T)

    /**
     * Bind data to view.
     * In cases when used in custom adapters should be called from [RecyclerView.Adapter.onBindViewHolder]
     *
     * @param data data to bind
     * @param payload diff payload when presented
     */
    open fun bind(data: T, payload: MutableList<Any>) {
        bind(data)
    }
}