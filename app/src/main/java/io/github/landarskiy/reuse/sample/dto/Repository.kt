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

package io.github.landarskiy.reuse.sample.dto

import android.app.Application
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.lang.reflect.Type

class Repository(val app: Application) {

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun loadData() = withContext(Dispatchers.IO) {

        val type: Type = Types.newParameterizedType(
            MutableList::class.java,
            ContentDto::class.java
        )

        val moshi = Moshi.Builder()
            .add(
                PolymorphicJsonAdapterFactory.of(ContentDto::class.java, "type")
                    .withSubtype(
                        ContentDto.TextDto::class.java,
                        ContentDto.ContentTypeDto.TEXT.dtoName
                    )
                    .withSubtype(
                        ContentDto.ImageDto::class.java,
                        ContentDto.ContentTypeDto.IMAGE.dtoName
                    )
                    .withSubtype(
                        ContentDto.CopyrightDto::class.java,
                        ContentDto.ContentTypeDto.COPYRIGHT.dtoName
                    )
            )
            .add(StyleDtoAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()

        val stream = app.resources.assets.open("sample.json")
        val data = BufferedReader(stream.reader()).use { it.readText() }

        val adapter: JsonAdapter<List<ContentDto>> = moshi.adapter(type)
        adapter.fromJson(data) ?: emptyList()
    }
}