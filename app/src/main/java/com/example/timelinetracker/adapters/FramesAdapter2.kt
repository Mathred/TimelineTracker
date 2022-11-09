package com.example.timelinetracker.adapters

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.timelinetracker.MainActivity.Companion.FRAME_HEIGHT_IN_DP
import com.example.timelinetracker.R
import com.example.timelinetracker.dpToPx
import com.example.timelinetracker.glide.CropBitmapTransformation
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

data class FrameAdapterData(
    val duration: Long,
    val videoHeight: Int,
    val videoWidth: Int,
    val timelineWidthInDp: Int,
    val timelineHeightInDp: Int,
    val uri: Uri
) {
    companion object {
        const val MAX_VIDEO_DURATION = 3 * 60_000L
    }

    private fun isShortVideo(): Boolean = duration <= MAX_VIDEO_DURATION
    private fun getTotalFramesWidthInDp(): Int = if (isShortVideo()) timelineWidthInDp else (timelineWidthInDp * duration / MAX_VIDEO_DURATION).toInt()
    fun getFrameWidthInDp(): Int = (timelineHeightInDp * videoWidth / videoHeight).coerceIn()
    fun getAmountOfFrames(): Int = getTotalFramesWidthInDp() / getFrameWidthInDp() + 1
    fun getLastFrameWidthInDp(): Int = getTotalFramesWidthInDp() - (getAmountOfFrames() - 1) * getFrameWidthInDp()

}

class FramesAdapter2(
    val data: FrameAdapterData,
): RecyclerView.Adapter<FramesAdapter2.ViewHolder>() {


    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val ivFrame: ImageView
        init {
            ivFrame = view.findViewById(R.id.iv_frame)
        }
    }

    private fun isLastFrame(position: Int): Boolean = position == itemCount - 1
    private fun getTimeStampInUs(position: Int) = TimeUnit.MILLISECONDS.toMicros(data.duration) * position /data.getAmountOfFrames()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.frame_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.ivFrame.updateLayoutParams {
            width = (if (isLastFrame(position)) data.getLastFrameWidthInDp() else data.getFrameWidthInDp()).dpToPx().toInt()
        }
        if (isLastFrame(position)) {
            Glide.with(holder.itemView)
                .asBitmap()
                .load(data.uri)
                .apply(RequestOptions().frame(getTimeStampInUs(position)))
                .apply(RequestOptions.bitmapTransform(CropBitmapTransformation(data.getLastFrameWidthInDp().toFloat() / data.getFrameWidthInDp())))
                .override(data.getFrameWidthInDp().dpToPx().toInt(), data.timelineHeightInDp.dpToPx().toInt())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(holder.ivFrame)
//                .into(object : CustomTarget<Bitmap>() {
//                    override fun onResourceReady(
//                        resource: Bitmap,
//                        transition: Transition<in Bitmap>?
//                    ) {
//                        val croppedBitmap = Bitmap.createBitmap(resource, 0, 0, data.getLastFrameWidthInDp(), FRAME_HEIGHT_IN_DP)
//                        holder.ivFrame.setImageBitmap(croppedBitmap)
//                    }
//
//                    override fun onLoadCleared(placeholder: Drawable?) {
//                    }
//
//                })
        } else {
            Glide.with(holder.itemView)
                .load(data.uri)
                .apply(RequestOptions().frame(getTimeStampInUs(position)))
                .into(holder.ivFrame)
        }

    }

    override fun getItemCount() = data.getAmountOfFrames()

}