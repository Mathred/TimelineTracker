package com.example.timelinetracker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class BoundView(
    context: Context,
    attrs: AttributeSet
): View(context, attrs) {

    private val boundRadius = dpToPx(6f)
    private val boundHeight = dpToPx(48f)

    private val rightBoundOffset = dpToPx(20f)

    private val fillPaintBlue = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4765FF")
        style = Paint.Style.FILL
    }

    private fun dpToPx(dp: Float): Float =
        dp * context.resources.displayMetrics.density

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        canvas?.apply {
            val topOval = RectF(0f, 0f, boundRadius * 2, boundRadius * 2)
            val bottomOval = RectF(0f, boundHeight - boundRadius * 2, boundRadius * 2, boundHeight)

            val path = Path().apply {
                moveTo(boundRadius*2, 0f)
                lineTo(boundRadius, 0f)
                arcTo(topOval, -90f, -90f)
                lineTo(0f,boundHeight - boundRadius)
                arcTo(bottomOval, 180f, -90f)
                lineTo(boundRadius*2, boundHeight)
                lineTo(boundRadius*2, 0f)
//                arcTo(0f, boundHeight - boundRadius, boundRadius, boundHeight, 90f, 90f, false)
//                lineTo(boundRadius*2, boundHeight)
//                lineTo(boundRadius*2, 0f)
            }
            drawPath(path, fillPaintBlue)


        }
    }

}