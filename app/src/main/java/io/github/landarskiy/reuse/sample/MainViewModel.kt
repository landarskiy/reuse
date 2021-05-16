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

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.github.landarskiy.reuse.Entry
import io.github.landarskiy.reuse.sample.dto.ContentDto
import io.github.landarskiy.reuse.sample.dto.Repository
import io.github.landarskiy.reuse.sample.screen.main.adapter.copyright.CopyrightEntry
import io.github.landarskiy.reuse.sample.screen.main.adapter.header.HeaderEntry
import io.github.landarskiy.reuse.sample.screen.main.adapter.image.ImageEntry
import io.github.landarskiy.reuse.sample.screen.main.adapter.text.TextEntry
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow


class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + Dispatchers.Main)

    private val _dataFlow: MutableStateFlow<List<Entry>> = MutableStateFlow(emptyList())
    val dataFlow: Flow<List<Entry>> = _dataFlow

    private val repository: Repository = Repository(app)

    init {
        scope.launch {
            _dataFlow.tryEmit(loadData())
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun loadData() = withContext(Dispatchers.Default) {
        val dto = repository.loadData()
        dto.map {
            when (it) {
                is ContentDto.HeaderDto -> {
                    HeaderEntry(it.text, it.assetsPath)
                }
                is ContentDto.TextDto -> {
                    TextEntry(it.text, it.style.toTextEntryStyle())
                }
                is ContentDto.ImageDto -> {
                    ImageEntry(it.assetsPath, it.width, it.height)
                }
                is ContentDto.CopyrightDto -> {
                    CopyrightEntry(it.text, it.url)
                }
            }
        }
    }
}

private fun ContentDto.TextDto.StyleDto.toTextEntryStyle(): TextEntry.Style {
    return when (this) {
        ContentDto.TextDto.StyleDto.H3 -> TextEntry.Style.H3
        ContentDto.TextDto.StyleDto.H5 -> TextEntry.Style.H5
        ContentDto.TextDto.StyleDto.H6 -> TextEntry.Style.H6
        ContentDto.TextDto.StyleDto.BODY -> TextEntry.Style.BODY
        ContentDto.TextDto.StyleDto.LIST_HEADER -> TextEntry.Style.LIST_HEADER
        ContentDto.TextDto.StyleDto.LIST_CONTENT -> TextEntry.Style.LIST_CONTENT
    }
}