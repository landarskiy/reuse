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
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * Makes relation between data and [BaseViewHolder]
 */
abstract class ViewHolderFactory<T> {

    /**
     * Unique type id
     */
    abstract val typeId: Int

    /**
     * Create View instance for ViewHolder for specific [typeId]
     */
    abstract fun createView(context: Context, parent: ViewGroup?): View

    /**
     * Create view holder for specific [typeId]
     */
    abstract fun createViewHolder(view: View): BaseViewHolder<T>

    /**
     * Create view holder for specific [typeId]
     * In cases when used in custom adapters should be called from [RecyclerView.Adapter.onCreateViewHolder]
     */
    fun createViewHolder(context: Context, parent: ViewGroup?): BaseViewHolder<T> {
        return createViewHolder(createView(context, parent))
    }
}