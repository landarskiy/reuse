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

package io.github.landarskiy.reuse.sample.screen.main.adapter.textgroup

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import io.github.landarskiy.reuse.BaseViewHolder
import io.github.landarskiy.reuse.sample.R
import io.github.landarskiy.reuse.sample.databinding.ItemTextGroupBinding
import io.github.landarskiy.reuse.sample.utils.dp

class TextGroupViewHolder(view: View) : BaseViewHolder<TextGroupEntry>(view) {

    private val binding: ItemTextGroupBinding = ItemTextGroupBinding.bind(view)

    private var arrowAnimator: Animator? = null

    override fun bind(data: TextGroupEntry) {
        with(binding) {
            if (data.content.expanded) {
                action.setText(R.string.text_group_action_collapse)
                icon.setImageResource(R.drawable.ic_collapse)
                startAnimation(-1)
            } else {
                action.setText(R.string.text_group_action_expand)
                icon.setImageResource(R.drawable.ic_expand)
                startAnimation(1)
            }
            root.setOnClickListener {
                data.clickListener.invoke(data)
            }
        }
    }

    private fun startAnimation(direction: Int) {
        arrowAnimator?.cancel()
        val r = binding.root.context.resources

        arrowAnimator =
            ObjectAnimator.ofFloat(binding.icon, View.TRANSLATION_Y, 0f, r.dp(4) * direction)
                .apply {
                    duration = 600
                    interpolator = AccelerateDecelerateInterpolator()
                    repeatCount = ObjectAnimator.INFINITE
                    repeatMode = ObjectAnimator.REVERSE
                    start()
                }
    }
}