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

import android.util.SparseArray
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * Base adapter
 */
open class Adapter(vararg types: RecyclerItemViewType<out Entry>) :
    RecyclerView.Adapter<ItemViewHolder<out Entry>>() {

    private val viewTypeArray = SparseArray<RecyclerItemViewType<out Entry>>()
    val content: MutableList<AdapterEntry> = mutableListOf()

    init {
        types.forEach { registerViewType(it) }
    }

    fun registerViewType(viewType: RecyclerItemViewType<out Entry>) {
        viewTypeArray.put(viewType.typeId, viewType)
    }

    fun getViewType(typeId: Int): RecyclerItemViewType<out Entry> {
        return requireNotNull(viewTypeArray.get(typeId), { "Type $typeId not registered" })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder<out Entry> {
        return getViewType(viewType).createViewHolder(parent.context, parent)
    }

    override fun getItemViewType(position: Int): Int {
        return content[position].viewType
    }

    override fun getItemCount(): Int {
        return content.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder<out Entry>, position: Int) {
        holder.bindData(content[position].data)
    }

    data class AdapterEntry(val viewType: Int, val data: Entry)
}