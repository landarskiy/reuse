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
import io.github.landarskiy.reuse.*
import io.github.landarskiy.reuse.adapter.AsyncDiffAdapter
import io.github.landarskiy.reuse.sample.databinding.ActivityMainBinding
import io.github.landarskiy.reuse.sample.model.Content
import io.github.landarskiy.reuse.sample.screen.main.adapter.AppReuseModule
import io.github.landarskiy.reuse.sample.screen.main.adapter.MainRecyclerItemDecoration
import io.github.landarskiy.reuse.sample.screen.main.adapter.copyright.CopyrightEntry
import io.github.landarskiy.reuse.sample.screen.main.adapter.header.HeaderEntry
import io.github.landarskiy.reuse.sample.screen.main.adapter.image.ImageEntry
import io.github.landarskiy.reuse.sample.screen.main.adapter.text.TextEntry
import io.github.landarskiy.reuse.sample.screen.main.adapter.textgroup.TextGroupEntry
import io.github.landarskiy.reuse.sample.screen.main.adapter.types.MainRecyclerContentFactory
import kotlinx.coroutines.flow.collect

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val typeFactory: MainRecyclerContentFactory =
        AppReuseModule.mainRecyclerContentFactory
    private val listAdapter: AsyncDiffAdapter = AsyncDiffAdapter(typeFactory.types)

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
                listAdapter.submitList(mapData(it))
            }
        }
    }

    private fun mapData(content: List<Content>): List<AdapterEntry<DiffEntry>> {
        val dataBuilder = typeFactory.newDataBuilder()
        content.forEach { item ->
            when (item) {
                is Content.Header -> {
                    dataBuilder.withHeader(HeaderEntry(item))
                }

                is Content.Text -> {
                    val entry = TextEntry(item)
                    when (item.style) {
                        Content.Text.Style.H3 -> dataBuilder.withH3(entry)
                        Content.Text.Style.H5 -> dataBuilder.withH5(entry)
                        Content.Text.Style.H6 -> dataBuilder.withH6(entry)
                        Content.Text.Style.BODY -> dataBuilder.withBody(entry)
                        Content.Text.Style.LIST_HEADER -> dataBuilder.withListHeader(entry)
                        Content.Text.Style.LIST_CONTENT -> dataBuilder.withListContent(entry)
                    }

                }

                is Content.GroupHeader -> {
                    dataBuilder.withGroup(TextGroupEntry(item) {
                        viewModel.onGroupClicked()
                    })
                }

                is Content.Image -> dataBuilder.withImage(ImageEntry(item))
                is Content.Copyright -> dataBuilder.withCopyright(CopyrightEntry(item))
            }
        }
        return dataBuilder.build()
    }
}