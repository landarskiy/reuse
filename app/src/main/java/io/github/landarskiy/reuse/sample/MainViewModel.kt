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
import io.github.landarskiy.reuse.sample.dto.Repository
import io.github.landarskiy.reuse.sample.model.Content
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow


class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + Dispatchers.Main)

    private val _dataFlow: MutableStateFlow<List<Content>> = MutableStateFlow(emptyList())
    val dataFlow: Flow<List<Content>> = _dataFlow

    private val fullData: MutableList<Content> = mutableListOf()

    private val repository: Repository = Repository(app)

    var groupExpanded: Boolean = false
        set(value) {
            if (field == value) {
                return
            }
            field = value
            refreshData()
        }

    init {
        scope.launch {
            fullData.addAll(loadData())
            refreshData()
        }
    }

    fun onGroupClicked() {
        groupExpanded = !groupExpanded
    }

    private fun refreshData() {
        val displayData: MutableList<Content> = mutableListOf()
        var groupAdded = false
        fullData.forEach {
            if (it !is Content.Text) {
                displayData.add(it)
            } else if (it.style == Content.Text.Style.LIST_HEADER || it.style == Content.Text.Style.LIST_CONTENT) {
                if (!groupAdded) {
                    displayData.add(Content.GroupHeader(expanded = groupExpanded))
                    groupAdded = true
                }
                if (groupExpanded) {
                    displayData.add(it)
                }
            } else {
                displayData.add(it)
            }
        }
        _dataFlow.tryEmit(displayData)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun loadData() = withContext(Dispatchers.Default) {
        repository.loadData()
    }
}