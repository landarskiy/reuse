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

open class AsyncDiffAdapter(types: List<ViewHolderFactory<out DiffEntry>>) :
    ListAdapter<AsyncDiffAdapter.AdapterEntry, BaseViewHolder<out DiffEntry>>(ItemDiffCallback()) {

    private val viewTypeArray = SparseArray<ViewHolderFactory<out DiffEntry>>()
    val content: MutableList<AdapterEntry> = mutableListOf()

    init {
        types.forEach { registerViewType(it) }
    }

    fun registerViewType(viewType: ViewHolderFactory<out DiffEntry>) {
        viewTypeArray.put(viewType.typeId, viewType)
    }

    fun getViewType(typeId: Int): ViewHolderFactory<out DiffEntry> {
        return requireNotNull(viewTypeArray.get(typeId), { "Type $typeId not registered" })
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<out DiffEntry> {
        return getViewType(viewType).createViewHolder(parent.context, parent)
    }

    override fun getItemViewType(position: Int): Int {
        return content[position].viewType
    }

    override fun getItemCount(): Int {
        return content.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder<out DiffEntry>, position: Int) {
        holder.bindData(content[position].data)
    }

    data class AdapterEntry(val viewType: Int, val data: DiffEntry)

    open class ItemDiffCallback : DiffUtil.ItemCallback<AdapterEntry>() {

        override fun areItemsTheSame(oldItem: AdapterEntry, newItem: AdapterEntry): Boolean {
            return oldItem.viewType == newItem.viewType && oldItem.data.isSameEntry(newItem.data)
        }

        override fun areContentsTheSame(oldItem: AdapterEntry, newItem: AdapterEntry): Boolean {
            return oldItem.viewType == newItem.viewType && oldItem.data.isSameContent(newItem.data)
        }

        override fun getChangePayload(oldItem: AdapterEntry, newItem: AdapterEntry): Any? {
            return oldItem.data.getDiffPayload(newItem.data)
        }
    }
}