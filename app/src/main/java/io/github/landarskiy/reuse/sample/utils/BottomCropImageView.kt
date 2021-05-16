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

package io.github.landarskiy.reuse.sample.utils

import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView


class BottomCropImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
        if (drawable == null) return super.setFrame(l, t, r, b)
        val matrix: Matrix = imageMatrix
        val scale: Float
        val viewWidth = measuredWidth - paddingLeft - paddingRight
        val viewHeight = measuredHeight - paddingTop - paddingBottom
        val drawableWidth = drawable.intrinsicWidth
        val drawableHeight = drawable.intrinsicHeight

        scale = if (drawableWidth * viewHeight > drawableHeight * viewWidth) {
            viewHeight.toFloat() / drawableHeight.toFloat()
        } else {
            viewWidth.toFloat() / drawableWidth.toFloat()
        }

        val drawableRect = RectF(
            0f, drawableHeight - viewHeight / scale,
            drawableWidth.toFloat(), drawableHeight.toFloat()
        )
        val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
        matrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.FILL)
        imageMatrix = matrix
        return super.setFrame(l, t, r, b)
    }
}