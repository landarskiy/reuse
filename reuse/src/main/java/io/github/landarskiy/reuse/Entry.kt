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

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

interface Entry {
    /**
     * Used in [DiffUtil.Callback.areItemsTheSame] to decide whether two object represent the same Entry.
     * Same entry means that entries represent same object (e.g. 2 records for same employer)
     * but can have some difference in content (e.g. different job title or department)
     * Use default implementation only if you manually call one of
     * [RecyclerView.Adapter.notifyItemRangeRemoved],
     * [RecyclerView.Adapter.notifyItemRangeInserted],
     * [RecyclerView.Adapter.notifyItemRangeChanged],
     * [RecyclerView.Adapter.notifyItemRemoved],
     * [RecyclerView.Adapter.notifyItemInserted],
     * [RecyclerView.Adapter.notifyItemChanged]
     * method after set new data.
     *
     * @param other compared entry.
     * @return `true` if the two entries represent the same object or `false` if they are different.
     */
    fun isSameEntry(other: Entry): Boolean {
        return false
    }

    /**
     * Used in [DiffUtil.Callback.areContentsTheSame] to check whether two entries have the same content.
     * Will call only if [isSameEntry] return `true`.
     * Use default implementation only if you manually call one of
     * [RecyclerView.Adapter.notifyItemRangeRemoved],
     * [RecyclerView.Adapter.notifyItemRangeInserted],
     * [RecyclerView.Adapter.notifyItemRangeChanged],
     * [RecyclerView.Adapter.notifyItemRemoved],
     * [RecyclerView.Adapter.notifyItemInserted],
     * [RecyclerView.Adapter.notifyItemChanged]
     * method after set new data.
     *
     * @param other compared entry which represent the same object.
     * @return `true` if the contents of the entries are the same or `false` if they are different.
     */
    fun isSameContent(other: Entry): Boolean {
        return false
    }

    /**
     * Used in [DiffUtil.Callback.getChangePayload] to get a payload about the change.
     *
     * @param other compared entry which represent the same object.
     * @return a payload object that represents the change between the two entries.
     */
    fun getDiffPayload(other: Entry): Any? {
        return null
    }
}