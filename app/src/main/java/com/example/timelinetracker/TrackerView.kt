package com.example.timelinetracker

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator

class TrackerView(
    context: Context,
    attrs: AttributeSet
): View(context, attrs) {

    companion object {
        const val MAX_VIDEO_DURATION = 3 * 60_000L
        const val MIN_DISTANCE_IN_MILLISECONDS = 2_000L
    }

    private var leftBoundX = 0f
    private var rightBoundX = 0f
    private var trackerX = 0f

    private var leftBoundTouched = false
    private var rightBoundTouched = false
    private var trackerTouched = false

    private var minimumDistance = 0f

    private val trackerHeight = dpToPx(56f)
    private val boundCornerRadius = dpToPx(6f)
    private val boundWidth = dpToPx(11.5f)
    private val boundHeight = dpToPx(48f)
    private val borderStrokeW = dpToPx(2f)
    private val trackerWidth = dpToPx(4f)
    private val trackerCornerRadius = dpToPx(4f)
    private val overlayHeight = dpToPx(44f)

    private val boundsVerticalPadding = dpToPx(4f)
    private val horizontalPadding = dpToPx(16f)
//    private var trackerVerticalPadding = 0f
    private var overlayVerticalPadding = dpToPx(6f)

    private var listener: OnTimelineChangeListener? = null

    private var videoDuration = 0L
    private var maxTimeLineInMillis = 0L
    private var maxTimelineWidth = 0f

    private val horizontalTouchPadding = dpToPx(4f)

    private fun dpToPx(dp: Float): Float =
        dp * context.resources.displayMetrics.density

    init {
        setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    performClick()
                    when {
                        checkIfTrackerTouched(motionEvent.x, motionEvent.y) -> trackerTouched = true
                        checkIfLeftBoundTouched(motionEvent.x, motionEvent.y) -> {
                            leftBoundTouched = true
                            animateTrackerVisibility(false)
                            invalidate()
                        }
                        checkIfRightBoundTouched(motionEvent.x, motionEvent.y) -> {
                            rightBoundTouched = true
                            animateTrackerVisibility(false)
                            invalidate()
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (leftBoundTouched) trackerX = leftBoundX
                    if (rightBoundTouched) trackerX = rightBoundX
                    leftBoundTouched = false
                    rightBoundTouched = false
                    trackerTouched = false
                    animateTrackerVisibility(true)
                }
                else -> {
                    when {
                        leftBoundTouched -> {
                            when {
                                rightBoundX - motionEvent.x >= minimumDistance && motionEvent.x >= horizontalPadding + boundWidth -> {
                                    leftBoundX = motionEvent.x
                                    invalidate()
                                }
                                rightBoundX - motionEvent.x < minimumDistance -> {
                                    leftBoundX = rightBoundX - minimumDistance
                                    invalidate()
                                }
                                motionEvent.x < horizontalPadding -> {
                                    leftBoundX = horizontalPadding + boundWidth
                                    invalidate()
                                }
                            }
                            listener?.onNewVideoStart(getCurrentLeftBoundTime())
                        }
                        rightBoundTouched -> {
                            when {
                                motionEvent.x - leftBoundX >= minimumDistance && motionEvent.x + boundWidth <= width - horizontalPadding -> {
                                    rightBoundX = motionEvent.x
                                    invalidate()
                                }
                                motionEvent.x - leftBoundX < minimumDistance -> {
                                    rightBoundX = leftBoundX + minimumDistance
                                    invalidate()
                                }
                                motionEvent.x + boundWidth > width - horizontalPadding -> {
                                    rightBoundX = width - horizontalPadding - boundWidth
                                    invalidate()
                                }
                            }
                            listener?.onNewVideoEnd(getCurrentRightBoundTime())
                        }
                        trackerTouched -> {
                            when {
                                (motionEvent.x >= leftBoundX) && (motionEvent.x <= rightBoundX) -> {
                                    trackerX = motionEvent.x
                                    invalidate()
                                }
                                motionEvent.x < leftBoundX -> {
                                    trackerX = leftBoundX
                                    invalidate()
                                }
                                motionEvent.x + trackerWidth > rightBoundX -> {
                                    trackerX = rightBoundX
                                    invalidate()
                                }
                            }
                            listener?.onNewTrackerPosition(getCurrentTrackerTime())
                        }
                    }
                }
            }
            true
        }
    }

    private fun getCurrentLeftBoundTime(): Long =
        (maxTimeLineInMillis * (leftBoundX - horizontalPadding - boundWidth) / maxTimelineWidth).toLong()

    private fun getCurrentRightBoundTime(): Long =
        (maxTimeLineInMillis * (rightBoundX - horizontalPadding - boundWidth) / maxTimelineWidth).toLong()

    private fun getCurrentTrackerTime(): Long =
        (maxTimeLineInMillis * (trackerX - horizontalPadding - boundWidth) / maxTimelineWidth).toLong()

    fun setTimelineChangeListener(listener: OnTimelineChangeListener) {
        this.listener = listener
    }

    fun setVideoDuration(duration: Long) {
        videoDuration = duration
    }

    private var trackerAlpha = 255

    private fun setTrackerAlpha(alpha: Int) {
        trackerAlpha = alpha
    }

    private fun getTrackerAlpha(): Int = trackerAlpha

    private fun animateTrackerVisibility(show: Boolean) {
        val animator = ObjectAnimator.ofInt(this, "trackerAlpha", if (show) 0 else 255, if (show) 255 else 0).apply {
            duration = 100
            interpolator = LinearInterpolator()
        }.apply {
            addUpdateListener { invalidate() }
        }
        animator.start()
    }

    private val fillPaintBlue = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4765FF")
        style = Paint.Style.FILL
    }
    private val clipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
    }
    private val trackerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFFFFF")
        style = Paint.Style.FILL
    }
    private val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#99000000")
        style = Paint.Style.FILL
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4765FF")
        style = Paint.Style.STROKE
        strokeWidth = borderStrokeW
    }

    override fun onDraw(canvas: Canvas?) {

        //Отрисовка без аппаратного ускорения
        /*setLayerType(LAYER_TYPE_SOFTWARE, null)
        canvas?.apply {
            drawOverlay()
            drawOuterRectangle()
            clipInnerRectangle()
            drawTracker()
        }*/


        //Отрисовка с аппаратным ускорением
        /*canvas?.apply {
            drawLeftOverlay()
            drawRightOverlay()
            drawLeftBound()
            drawRightBound()
            drawTopBorder()
            drawBottomBorder()
            drawTracker()
        }*/

        canvas?.apply {
            drawBounds()
            drawTracker()
        }

        super.onDraw(canvas)
    }

    private fun Canvas.drawBounds() {
        val path = Path().apply {
            moveTo(leftBoundX, boundsVerticalPadding)
            lineTo(rightBoundX + boundWidth - boundCornerRadius, boundsVerticalPadding)
            arcTo(rightBoundX + boundWidth - 2 * boundCornerRadius, boundsVerticalPadding, rightBoundX + boundWidth, boundsVerticalPadding + 2 * boundCornerRadius, -90f, 90f, false)
            lineTo(rightBoundX + boundWidth, boundsVerticalPadding + boundHeight - boundCornerRadius)
            arcTo(rightBoundX + boundWidth - 2 * boundCornerRadius, boundsVerticalPadding + boundHeight - 2 * boundCornerRadius, rightBoundX + boundWidth, boundsVerticalPadding + boundHeight, 0f, 90f, false)
            lineTo(leftBoundX, boundsVerticalPadding + boundHeight)
            arcTo(leftBoundX - boundWidth, boundsVerticalPadding + boundHeight - 2 * boundCornerRadius, leftBoundX - boundWidth + 2 * boundCornerRadius, boundsVerticalPadding + boundHeight, 90f, 90f, false)
            lineTo(leftBoundX - boundWidth, boundsVerticalPadding + boundCornerRadius)
            arcTo(leftBoundX - boundWidth, boundsVerticalPadding, leftBoundX - boundWidth + 2 * boundCornerRadius, boundsVerticalPadding + 2 * boundCornerRadius, 180f, 90f, false)
            moveTo(leftBoundX, boundsVerticalPadding + borderStrokeW)
            lineTo(leftBoundX, boundsVerticalPadding + boundHeight - borderStrokeW)
            lineTo(rightBoundX, boundsVerticalPadding + boundHeight - borderStrokeW)
            lineTo(rightBoundX, boundsVerticalPadding + borderStrokeW)
            lineTo(leftBoundX, boundsVerticalPadding + borderStrokeW)
        }
        drawPath(path, fillPaintBlue)
    }

    private fun Canvas.drawLeftOverlay() {
        drawRect(0f, overlayVerticalPadding, leftBoundX - boundCornerRadius, overlayHeight + overlayVerticalPadding, overlayPaint)
    }

    private fun Canvas.drawRightOverlay() {
        drawRect(rightBoundX, overlayVerticalPadding, width.toFloat(), overlayVerticalPadding + overlayHeight, overlayPaint)
    }

    private fun Canvas.drawLeftBound() {
        val topOval = RectF(leftBoundX - boundWidth, boundsVerticalPadding, leftBoundX - boundWidth + boundCornerRadius * 2, boundsVerticalPadding + boundCornerRadius * 2)
        val bottomOval = RectF(leftBoundX - boundWidth, boundsVerticalPadding + boundHeight - 2 * boundCornerRadius, leftBoundX - boundWidth + 2 * boundCornerRadius, boundsVerticalPadding + boundHeight)
        val path = Path().apply {
            moveTo(leftBoundX, boundsVerticalPadding)
            lineTo(leftBoundX - (boundWidth - boundCornerRadius), boundsVerticalPadding)
            arcTo(topOval, -90f, -90f)
            lineTo(leftBoundX - boundWidth, boundHeight + boundsVerticalPadding)
            arcTo(bottomOval, 180f, -90f)
            lineTo(leftBoundX, boundHeight + boundsVerticalPadding)
            lineTo(leftBoundX, boundsVerticalPadding)
        }
        drawPath(path, fillPaintBlue)
    }

    private fun Canvas.drawRightBound() {
        val topOval = RectF(rightBoundX + boundWidth - 2 * boundCornerRadius, boundsVerticalPadding, rightBoundX + boundWidth, 2 * boundCornerRadius + boundsVerticalPadding)
        val bottomOval = RectF(rightBoundX + boundWidth - 2 * boundCornerRadius, boundsVerticalPadding + boundHeight - 2*boundCornerRadius, rightBoundX + boundWidth, boundsVerticalPadding + boundHeight)
        val path = Path().apply {
            moveTo(rightBoundX, boundsVerticalPadding)
            lineTo(rightBoundX + boundCornerRadius, boundsVerticalPadding)
            arcTo(topOval, -90f, 90f)
            lineTo(rightBoundX + 2 * boundCornerRadius, boundsVerticalPadding + boundHeight - boundCornerRadius)
            arcTo(bottomOval, 0f, 90f)
            lineTo(rightBoundX, boundHeight + boundsVerticalPadding)
        }
        drawPath(path, fillPaintBlue)
    }

    private fun Canvas.drawTopBorder() {
        drawLine(leftBoundX, boundsVerticalPadding + borderStrokeW/2, rightBoundX, boundsVerticalPadding + borderStrokeW/2, strokePaint)
    }

    private fun Canvas.drawBottomBorder() {
        drawLine(leftBoundX, boundsVerticalPadding + boundHeight - borderStrokeW/2, rightBoundX, boundsVerticalPadding + boundHeight - borderStrokeW/2, strokePaint)
    }

    private fun Canvas.drawOuterRectangle() {
        drawRoundRect(leftBoundX - boundWidth, boundsVerticalPadding, rightBoundX + boundWidth, boundsVerticalPadding + boundHeight, boundCornerRadius, boundCornerRadius, fillPaintBlue)
    }

    private fun Canvas.clipInnerRectangle() {
        drawRect(leftBoundX, boundsVerticalPadding + borderStrokeW, rightBoundX, boundsVerticalPadding + boundHeight - borderStrokeW, clipPaint)
    }

    private fun Canvas.drawOverlay() {
        drawRect(0f, overlayVerticalPadding, width.toFloat(), overlayHeight + overlayVerticalPadding, overlayPaint)
    }

    private fun Canvas.drawTracker() {
        drawRoundRect(
            trackerX - trackerWidth / 2,
            0f,
            trackerX + trackerWidth / 2,
            trackerHeight,
            trackerCornerRadius,
            trackerCornerRadius,
            trackerPaint.apply {
                alpha = trackerAlpha
            }
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)

        val minh: Int = MeasureSpec.getSize(w) + paddingBottom + paddingTop
        val h: Int = resolveSizeAndState(minh, heightMeasureSpec, 0)

        leftBoundX = horizontalPadding + boundWidth
        rightBoundX = w.toFloat() - horizontalPadding - boundWidth
        trackerX = leftBoundX
        overlayVerticalPadding = (h - overlayHeight) / 2
        maxTimeLineInMillis = videoDuration.coerceAtMost(MAX_VIDEO_DURATION)
        minimumDistance = (rightBoundX - leftBoundX) * MIN_DISTANCE_IN_MILLISECONDS / maxTimeLineInMillis
        maxTimelineWidth = w.toFloat() - horizontalPadding - boundWidth - (horizontalPadding + boundWidth)

        setMeasuredDimension(w, h)
    }

    private fun checkIfLeftBoundTouched(x: Float, y: Float): Boolean {
        val touchRect = RectF(leftBoundX - boundWidth - horizontalTouchPadding, boundsVerticalPadding, leftBoundX + horizontalTouchPadding, boundHeight)
        return touchRect.contains(x, y)
    }

    private fun checkIfRightBoundTouched(x: Float, y: Float): Boolean {
        val touchRect = RectF(rightBoundX - horizontalTouchPadding, boundsVerticalPadding, rightBoundX + boundWidth + horizontalTouchPadding, boundsVerticalPadding + boundHeight)
        return touchRect.contains(x, y)
    }

    private fun checkIfTrackerTouched(x: Float, y: Float): Boolean {
        val touchRect = RectF(trackerX - horizontalTouchPadding, 0f, trackerX + trackerWidth + horizontalTouchPadding, boundHeight)
        return touchRect.contains(x, y)
    }

    interface OnTimelineChangeListener {

        fun onNewVideoStart(start: Long)
        fun onNewVideoEnd(end: Long)
        fun onNewTrackerPosition(tracker: Long)

    }

}