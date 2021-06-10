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

package io.github.landarskiy.reuse.sample.screen.main.adapter

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.github.landarskiy.reuse.sample.screen.main.adapter.copyright.CopyrightViewHolderFactory
import io.github.landarskiy.reuse.sample.screen.main.adapter.header.HeaderViewHolderFactory
import io.github.landarskiy.reuse.sample.screen.main.adapter.image.ImageViewHolderFactory
import io.github.landarskiy.reuse.sample.screen.main.adapter.text.types.TextBodyViewHolderFactory
import io.github.landarskiy.reuse.sample.screen.main.adapter.text.types.TextListContentViewHolderFactory
import io.github.landarskiy.reuse.sample.screen.main.adapter.text.types.TextListHeaderViewHolderFactory
import io.github.landarskiy.reuse.sample.screen.main.adapter.textgroup.TextGroupViewHolderFactory
import io.github.landarskiy.reuse.sample.utils.dp
import kotlin.math.roundToInt

class MainRecyclerItemDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val r = view.context.resources
        val itemType = parent.getChildViewHolder(view).itemViewType
        when (itemType) {
            TextBodyViewHolderFactory.TYPE_ID -> {
                outRect.bottom += r.dp(12).roundToInt()
            }
            ImageViewHolderFactory.TYPE_ID -> {
                outRect.top += r.dp(8).roundToInt()
                outRect.bottom += r.dp(8).roundToInt()
            }
            TextListHeaderViewHolderFactory.TYPE_ID -> {
                outRect.left += r.dp(24).roundToInt()
            }
            TextListContentViewHolderFactory.TYPE_ID -> {
                outRect.left += r.dp(24).roundToInt()
                outRect.bottom += r.dp(12).roundToInt()
            }
        }
        if (itemType != HeaderViewHolderFactory.TYPE_ID &&
            itemType != ImageViewHolderFactory.TYPE_ID &&
            itemType != CopyrightViewHolderFactory.TYPE_ID &&
            itemType != TextGroupViewHolderFactory.TYPE_ID
        ) {
            outRect.left += r.dp(16).roundToInt()
            outRect.right += r.dp(16).roundToInt()
        }
    }
}