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

package io.github.landarskiy.reuse.adapter

import android.util.SparseArray
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.landarskiy.reuse.AdapterEntry
import io.github.landarskiy.reuse.ReuseViewHolder
import io.github.landarskiy.reuse.ViewHolderFactory

/**
 * Base adapter
 */
open class Adapter<T>(types: List<ViewHolderFactory<T>>) :
    RecyclerView.Adapter<ReuseViewHolder<T>>() {

    private val viewTypeArray = SparseArray<ViewHolderFactory<T>>()
    val content: MutableList<AdapterEntry<T>> = mutableListOf()

    init {
        types.forEach { registerViewType(it) }
    }

    fun registerViewType(viewType: ViewHolderFactory<T>) {
        viewTypeArray.put(viewType.typeId, viewType)
    }

    fun getViewType(typeId: Int): ViewHolderFactory<T> {
        return requireNotNull(viewTypeArray.get(typeId)) { "Type $typeId not registered" }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReuseViewHolder<T> {
        return getViewType(viewType).createViewHolder(parent.context, parent)
    }

    override fun getItemViewType(position: Int): Int {
        return content[position].viewType
    }

    override fun getItemCount(): Int {
        return content.size
    }

    override fun onBindViewHolder(holder: ReuseViewHolder<T>, position: Int) {
        holder.bind(content[position].data)
    }

    override fun onBindViewHolder(
        holder: ReuseViewHolder<T>, position: Int, payloads: MutableList<Any>
    ) {
        holder.bind(content[position].data, payloads)
    }
}