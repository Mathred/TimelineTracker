package com.example.timelinetracker

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import java.lang.ref.WeakReference
import kotlin.math.abs

class TrackerView2(
    context: Context,
    attrs: AttributeSet
): View(context, attrs) {

    companion object {
        const val MAX_VIDEO_DURATION = 3 * 60_000L
        const val MIN_DISTANCE_IN_MILLISECONDS = 2_000L
    }

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
    private var overlayVerticalPadding = dpToPx(6f)

    private var listener: WeakReference<OnTimelineChangeListener>? = null

    private var videoDuration = 0L
    private var maxTimeLineInMillis = 0L
    private var maxTimelineWidth = 0f

    private val horizontalTouchPadding = dpToPx(32f)

    private val leftBound = XScrollable(
        leftTouchPadding = boundWidth + horizontalTouchPadding,
        rightTouchPadding = horizontalTouchPadding,
        touchOffset = - boundWidth / 2,
        onNewX = { listener?.get()?.onNewVideoStartPosition(it) }
    )
    private val rightBound = XScrollable(
        leftTouchPadding = horizontalTouchPadding,
        rightTouchPadding = boundWidth + horizontalTouchPadding,
        touchOffset = boundWidth / 2,
        onNewX = { listener?.get()?.onNewVideoEndPosition(it) }
    )
    private val tracker = XScrollable(
        leftTouchPadding = trackerWidth / 2 + horizontalTouchPadding,
        rightTouchPadding = trackerWidth / 2 + horizontalTouchPadding,
        onNewX = { listener?.get()?.onNewTrackerPosition(it) }
    )
    private val xScrollables = listOf(
        tracker,
        leftBound,
        rightBound
    )

    private fun dpToPx(dp: Float): Float =
        dp * context.resources.displayMetrics.density

    private fun updateXAxisLimits() {
        leftBound.maxX = rightBound.x - minimumDistance
        rightBound.minX = leftBound.x + minimumDistance
        tracker.minX = leftBound.x
        tracker.maxX = rightBound.x
    }

    init {
        setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    performClick()
                    updateXAxisLimits()
                    xScrollables
                        .filter { it.isTouched(motionEvent.x) }
                        .minByOrNull { it.getTouchDistance(motionEvent.x) }
                        ?.let {
                            it.beingScrolled = true
                            if (!tracker.beingScrolled) animateTrackerVisibility(false)
                            invalidate()
                        }
                }
                MotionEvent.ACTION_UP -> {
                    if (leftBound.beingScrolled) tracker.x = leftBound.x
                    if (rightBound.beingScrolled) tracker.x = rightBound.x
                    xScrollables.forEach { it.beingScrolled = false }
                    animateTrackerVisibility(true)
                }
                else -> {
                    xScrollables.firstOrNull { it.beingScrolled }?.let {
                        it.setDesiredX(motionEvent.x)
                        invalidate()
                    }
                }
            }
            true
        }
    }

    fun setTimelineChangeListener(listener: OnTimelineChangeListener) {
        this.listener = WeakReference(listener)
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
        val animator = ObjectAnimator.ofInt(this, "trackerAlpha", if (show) trackerAlpha else 255, if (show) 255 else 0).apply {
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
    private val trackerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFFFFF")
        style = Paint.Style.FILL
    }
    private val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#99000000")
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.apply {
            drawLeftOverlay()
            drawRightOverlay()
            drawBounds()
            drawTracker()
        }
        super.onDraw(canvas)
    }

    private fun Canvas.drawLeftOverlay() {
        drawRect(0f, overlayVerticalPadding, leftBound.x - boundCornerRadius, overlayHeight + overlayVerticalPadding, overlayPaint)
    }

    private fun Canvas.drawRightOverlay() {
        drawRect(rightBound.x, overlayVerticalPadding, width.toFloat(), overlayVerticalPadding + overlayHeight, overlayPaint)
    }

    private fun Canvas.drawBounds() {
        val path = Path().apply {
            moveTo(leftBound.x, boundsVerticalPadding)
            lineTo(rightBound.x + boundWidth - boundCornerRadius, boundsVerticalPadding)
            arcTo(rightBound.x + boundWidth - 2 * boundCornerRadius, boundsVerticalPadding, rightBound.x + boundWidth, boundsVerticalPadding + 2 * boundCornerRadius, -90f, 90f, false)
            lineTo(rightBound.x + boundWidth, boundsVerticalPadding + boundHeight - boundCornerRadius)
            arcTo(rightBound.x + boundWidth - 2 * boundCornerRadius, boundsVerticalPadding + boundHeight - 2 * boundCornerRadius, rightBound.x + boundWidth, boundsVerticalPadding + boundHeight, 0f, 90f, false)
            lineTo(leftBound.x, boundsVerticalPadding + boundHeight)
            arcTo(leftBound.x - boundWidth, boundsVerticalPadding + boundHeight - 2 * boundCornerRadius, leftBound.x - boundWidth + 2 * boundCornerRadius, boundsVerticalPadding + boundHeight, 90f, 90f, false)
            lineTo(leftBound.x - boundWidth, boundsVerticalPadding + boundCornerRadius)
            arcTo(leftBound.x - boundWidth, boundsVerticalPadding, leftBound.x - boundWidth + 2 * boundCornerRadius, boundsVerticalPadding + 2 * boundCornerRadius, 180f, 90f, false)
            moveTo(leftBound.x, boundsVerticalPadding + borderStrokeW)
            lineTo(leftBound.x, boundsVerticalPadding + boundHeight - borderStrokeW)
            lineTo(rightBound.x, boundsVerticalPadding + boundHeight - borderStrokeW)
            lineTo(rightBound.x, boundsVerticalPadding + borderStrokeW)
            lineTo(leftBound.x, boundsVerticalPadding + borderStrokeW)
        }
        drawPath(path, fillPaintBlue)
    }

    private fun Canvas.drawTracker() {
        drawRoundRect(
            tracker.x - trackerWidth / 2,
            0f,
            tracker.x + trackerWidth / 2,
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

        leftBound.x = horizontalPadding + boundWidth
        leftBound.minX = horizontalPadding + boundWidth
        rightBound.x = w.toFloat() - horizontalPadding - boundWidth
        rightBound.maxX = rightBound.x
        tracker.x = leftBound.x
        maxTimeLineInMillis = videoDuration.coerceAtMost(MAX_VIDEO_DURATION)
        minimumDistance = (rightBound.x - leftBound.x) * MIN_DISTANCE_IN_MILLISECONDS / maxTimeLineInMillis
        maxTimelineWidth = w.toFloat() - horizontalPadding - boundWidth - (horizontalPadding + boundWidth)

        setMeasuredDimension(w, h)
    }

    open inner class XScrollable(
        var x: Float = 0f,
        var minX: Float = 0f,
        var maxX: Float = 0f,
        var touchOffset: Float = 0f,
        var beingScrolled: Boolean = false,
        var leftTouchPadding: Float = 0f,
        var rightTouchPadding: Float = 0f,
        val onNewX: (Long) -> Unit,
    ) {
        fun getTouchDistance(x: Float) = abs((x - this.x - touchOffset))
        fun isTouched(x: Float) = (x >= this.x - leftTouchPadding && x <= this.x + rightTouchPadding)
        fun setDesiredX(x: Float) {
            when {
                x < minX -> this.x = minX
                x > maxX -> this.x = maxX
                x in minX..maxX -> this.x = x
                else -> {}
            }
            onNewX.invoke(getCurrentPositionInMillis())
        }

        private fun getCurrentPositionInMillis() =
            (maxTimeLineInMillis * (x - horizontalPadding - boundWidth) / maxTimelineWidth).toLong()
    }

    interface OnTimelineChangeListener {

        fun onNewVideoStartPosition(startPosition: Long)
        fun onNewVideoEndPosition(endPosition: Long)
        fun onNewTrackerPosition(trackerPosition: Long)

    }

}