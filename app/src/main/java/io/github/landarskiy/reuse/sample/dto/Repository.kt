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
import io.github.landarskiy.reuse.sample.model.Content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.lang.reflect.Type

class Repository(private val app: Application) {

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
                        ContentDto.HeaderDto::class.java,
                        ContentDto.ContentTypeDto.HEADER.dtoName
                    )
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
        (adapter.fromJson(data) ?: emptyList()).map {
            when (it) {
                is ContentDto.HeaderDto -> it.mapToModel()
                is ContentDto.TextDto -> it.mapToModel()
                is ContentDto.ImageDto -> it.mapToModel()
                is ContentDto.CopyrightDto -> it.mapToModel()
            }
        }
    }
}

private fun ContentDto.HeaderDto.mapToModel(): Content.Header {
    return Content.Header(text = text, assetsPath = assetsPath)
}

private fun ContentDto.TextDto.mapToModel(): Content.Text {
    return Content.Text(text = text, style = style.mapToModel())
}

private fun ContentDto.TextDto.StyleDto.mapToModel(): Content.Text.Style {
    return when (this) {
        ContentDto.TextDto.StyleDto.H3 -> Content.Text.Style.H3
        ContentDto.TextDto.StyleDto.H5 -> Content.Text.Style.H5
        ContentDto.TextDto.StyleDto.H6 -> Content.Text.Style.H6
        ContentDto.TextDto.StyleDto.BODY -> Content.Text.Style.BODY
        ContentDto.TextDto.StyleDto.LIST_CONTENT -> Content.Text.Style.LIST_CONTENT
        ContentDto.TextDto.StyleDto.LIST_HEADER -> Content.Text.Style.LIST_HEADER
    }
}

private fun ContentDto.ImageDto.mapToModel(): Content.Image {
    return Content.Image(assetsPath = assetsPath, width = width, height = height)
}

private fun ContentDto.CopyrightDto.mapToModel(): Content.Copyright {
    return Content.Copyright(text = text, url = url)
}