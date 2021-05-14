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

import androidx.annotation.MainThread
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback

/**
 * Adapter which support [DiffUtil]
 */
open class DiffAdapter(vararg types: RecyclerItemViewType<out Entry>) : Adapter(*types) {

    @MainThread
    fun setItems(newItems: List<AdapterEntry>) {
        val callback = createDiffCallback(newItems)
        updateItemsInternal(callback, newItems)
    }

    private fun createDiffCallback(newItems: List<AdapterEntry>): DiffUtil.Callback {
        return RecyclerContentDiffCallback(content, newItems)
    }

    private fun updateItemsInternal(diffCallback: DiffUtil.Callback, newItems: List<AdapterEntry>) {
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        content.replace(newItems)
        diffResult.dispatchUpdatesTo(createListUpdateCallback())
    }

    open fun createListUpdateCallback(): ListUpdateCallback {
        return AdapterListUpdateCallback(this)
    }

    open class RecyclerContentDiffCallback(
        private val oldItems: List<AdapterEntry>,
        private val newItems: List<AdapterEntry>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return oldItems.size
        }

        override fun getNewListSize(): Int {
            return newItems.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldItems[oldItemPosition].viewType == newItems[newItemPosition].viewType &&
                    oldItems[oldItemPosition].data.isSameEntry(newItems[newItemPosition].data)
        }

        override fun areContentsTheSame(
            oldItemPosition: Int,
            newItemPosition: Int
        ): Boolean {
            return oldItems[oldItemPosition].data.isSameContent(newItems[newItemPosition].data)
        }

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            return oldItems[oldItemPosition].data.getDiffPayload(newItems[newItemPosition].data)
        }
    }
}