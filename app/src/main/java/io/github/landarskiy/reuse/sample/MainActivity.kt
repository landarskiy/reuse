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

package io.github.landarskiy.reuse.sample

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.landarskiy.reuse.DiffAdapter
import io.github.landarskiy.reuse.sample.databinding.ActivityMainBinding
import io.github.landarskiy.reuse.sample.screen.main.adapter.AppViewTypeModule
import io.github.landarskiy.reuse.sample.screen.main.adapter.MainRecyclerItemDecoration
import io.github.landarskiy.reuse.sample.screen.main.adapter.copyright.CopyrightEntry
import io.github.landarskiy.reuse.sample.screen.main.adapter.header.HeaderEntry
import io.github.landarskiy.reuse.sample.screen.main.adapter.image.ImageEntry
import io.github.landarskiy.reuse.sample.screen.main.adapter.text.TextEntry
import io.github.landarskiy.reuse.sample.screen.main.adapter.types.DefaultRecyclerContentFactory
import kotlinx.coroutines.flow.collect

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val typeFactory: DefaultRecyclerContentFactory =
        AppViewTypeModule.defaultRecyclerContentFactory
    private val listAdapter: DiffAdapter =
        DiffAdapter(*typeFactory.types.toTypedArray())

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding.recyclerView) {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = listAdapter
            addItemDecoration(MainRecyclerItemDecoration())
        }
        lifecycleScope.launchWhenCreated {
            viewModel.dataFlow.collect {
                val dataBuilder = typeFactory.newDataBuilder()
                it.forEach { entry ->
                    when (entry) {
                        is HeaderEntry -> dataBuilder.withHeaderItemViewTypeItem(entry)
                        is TextEntry -> {
                            when (entry.style) {
                                TextEntry.Style.H3 -> dataBuilder.withTextH3ItemViewTypeItem(entry)
                                TextEntry.Style.H5 -> dataBuilder.withTextH5ItemViewTypeItem(entry)
                                TextEntry.Style.H6 -> dataBuilder.withTextH6ItemViewTypeItem(entry)
                                TextEntry.Style.BODY -> dataBuilder.withTextBodyItemViewTypeItem(
                                    entry
                                )
                                TextEntry.Style.LIST_HEADER -> dataBuilder.withTextListHeaderItemViewTypeItem(
                                    entry
                                )
                                TextEntry.Style.LIST_CONTENT -> dataBuilder.withTextListContentItemViewTypeItem(
                                    entry
                                )
                            }

                        }
                        is ImageEntry -> dataBuilder.withImageItemViewTypeItem(entry)
                        is CopyrightEntry -> dataBuilder.withCopyrightItemViewTypeItem(entry)
                    }
                }
                listAdapter.setItems(dataBuilder.build())
            }
        }
    }
}