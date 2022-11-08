package com.example.timelinetracker.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.OPTION_CLOSEST
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.timelinetracker.MainActivity.Companion.FRAME_HEIGHT_IN_DP
import com.example.timelinetracker.R
import com.example.timelinetracker.dpToPx
import com.example.timelinetracker.glide.CropBitmapTransformation

class FramesAdapter(
    val frameWidth: Int,
    val lastFrameWidth: Int,
    val amountOfFrames: Int,
    val videoDuration: Long,
    val videoUri: Uri
): RecyclerView.Adapter<FramesAdapter.ViewHolder>() {

    inner class ViewHolder(val parentContext: Context, view: View): RecyclerView.ViewHolder(view) {
        val ivFrame: ImageView
        init {
            ivFrame = view.findViewById(R.id.iv_frame)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.frame_item, parent, false)
        return ViewHolder(parent.context, view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        if (position == itemCount - 1) {
//            holder.ivFrame.scaleType = ImageView.ScaleType.CENTER_CROP
//            holder.ivFrame.updateLayoutParams { width = lastFrameWidth }
//        } else {
//            holder.ivFrame.scaleType = ImageView.ScaleType.FIT_XY
//            holder.ivFrame.updateLayoutParams {
//                Log.d("SAY123","updating width $frameWidth")
//                width = frameWidth
//            }
//        }

//        holder.ivFrame.layoutParams.width = frameWidth

        holder.ivFrame.updateLayoutParams {
            val w = (if (position == itemCount - 1) lastFrameWidth else frameWidth).dpToPx().toInt()
            Log.d("SAY123", "setting width $w")
            width = w
        }

        if (position == itemCount - 1) {

            Glide.with(holder.itemView)
                .asBitmap()
                .load(videoUri)
                .apply(RequestOptions().frame(videoDuration / amountOfFrames * position*1000))
                .into(object: CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        val croppedBitmap = Bitmap.createBitmap(resource, 0, 0, lastFrameWidth, FRAME_HEIGHT_IN_DP)
                        holder.ivFrame.setImageBitmap(croppedBitmap)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {

                    }

                })
        } else {
            Glide.with(holder.itemView)
//                .asBitmap()
                .load(videoUri)
                .apply(RequestOptions().frame(videoDuration / amountOfFrames * position*1000))
                .into(holder.ivFrame)
        }




        /*val retr = MediaMetadataRetriever()
        retr.setDataSource(holder.parentContext, videoUri)
        val timeStamp = videoDuration / amountOfFrames * position * 1000L
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            holder.ivFrame.setImageBitmap(retr.getScaledFrameAtTime(timeStamp, OPTION_CLOSEST, frameWidth, FRAME_HEIGHT_IN_DP))
        } else {
            holder.ivFrame.setImageBitmap(retr.getFrameAtTime(timeStamp))
        }
        retr.release()*/
    }

    override fun getItemCount() = amountOfFrames

}