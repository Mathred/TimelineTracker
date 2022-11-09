package com.example.timelinetracker.glide

import android.graphics.Bitmap
import android.util.Log
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.example.timelinetracker.dpToPx
import java.security.MessageDigest

class CropBitmapTransformation(
    val widthRatio: Float
): BitmapTransformation() {

    companion object {
        private val ID = CropBitmapTransformation::class.java.simpleName
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update((ID + widthRatio).toByteArray(CHARSET))
    }

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        return Bitmap.createBitmap(toTransform, 0, 0, (toTransform.width.toFloat() * widthRatio).toInt(), toTransform.height, null, true )
//        return Bitmap.createScaledBitmap(toTransform, (toTransform.width * widthRatio).toInt(), toTransform.height, true)
//        return Bitmap.createBitmap(
//            toTransform,
//            0,
//            0,
//            (toTransform.width.dpToPx()*widthRatio).toInt(),
//            toTransform.height.dpToPx().toInt()
//        )
    }
}