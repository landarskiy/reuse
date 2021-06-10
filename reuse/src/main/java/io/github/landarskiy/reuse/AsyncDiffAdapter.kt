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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

/**
 * Async adapter which support [DiffUtil]
 */
open class AsyncDiffAdapter(types: List<ViewHolderFactory<out DiffEntry>>) :
    ListAdapter<AdapterEntry<DiffEntry>, BaseViewHolder<DiffEntry>>(ItemDiffCallback()) {

    private val viewTypeArray = SparseArray<ViewHolderFactory<out DiffEntry>>()

    init {
        types.forEach { registerViewType(it) }
    }

    fun registerViewType(viewType: ViewHolderFactory<out DiffEntry>) {
        viewTypeArray.put(viewType.typeId, viewType)
    }

    fun getViewType(typeId: Int): ViewHolderFactory<out DiffEntry> {
        return requireNotNull(viewTypeArray.get(typeId), { "Type $typeId not registered" })
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<DiffEntry> {
        return getViewType(viewType).createViewHolder(
            parent.context,
            parent
        ) as BaseViewHolder<DiffEntry>
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).viewType
    }

    override fun onBindViewHolder(holder: BaseViewHolder<DiffEntry>, position: Int) {
        holder.bind(getItem(position).data)
    }

    open class ItemDiffCallback : DiffUtil.ItemCallback<AdapterEntry<DiffEntry>>() {

        override fun areItemsTheSame(
            oldItem: AdapterEntry<DiffEntry>,
            newItem: AdapterEntry<DiffEntry>
        ): Boolean {
            return oldItem.viewType == newItem.viewType && oldItem.data.isSameEntry(newItem.data)
        }

        override fun areContentsTheSame(
            oldItem: AdapterEntry<DiffEntry>,
            newItem: AdapterEntry<DiffEntry>
        ): Boolean {
            return oldItem.viewType == newItem.viewType && oldItem.data.isSameContent(newItem.data)
        }

        override fun getChangePayload(
            oldItem: AdapterEntry<DiffEntry>,
            newItem: AdapterEntry<DiffEntry>
        ): Any? {
            return oldItem.data.getDiffPayload(newItem.data)
        }
    }
}