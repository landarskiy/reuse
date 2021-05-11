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

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

sealed class ContentDto(
    @Json(name = "type")
    val contentType: ContentTypeDto
) {

    @JsonClass(generateAdapter = true)
    data class TextDto(
        @Json(name = "content")
        val text: String,
        @Json(name = "style")
        val style: StyleDto
    ) : ContentDto(ContentTypeDto.TEXT) {

        enum class StyleDto(val dtoName: String) {
            H3("h3"),
            H5("h5"),
            H6("h6"),
            BODY("body1"),
            LIST_HEADER("lih"),
            LIST_CONTENT("lic")
        }
    }

    @JsonClass(generateAdapter = true)
    data class ImageDto(
        @Json(name = "content")
        val assetsPath: String,
        @Json(name = "w")
        val width: Int,
        @Json(name = "h")
        val height: Int
    ) : ContentDto(ContentTypeDto.IMAGE)

    data class CopyrightDto(
        @Json(name = "content")
        val text: String,
        @Json(name = "link")
        val url: String
    ) : ContentDto(ContentTypeDto.COPYRIGHT)

    enum class ContentTypeDto(val dtoName: String) {
        TEXT("text"), IMAGE("image"), COPYRIGHT("copyright")
    }
}