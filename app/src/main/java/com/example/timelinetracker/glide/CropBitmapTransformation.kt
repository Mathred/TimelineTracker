package com.example.timelinetracker.glide

import android.graphics.Bitmap
import android.util.Log
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest

class CropBitmapTransformation(
    val widthRatio: Float
): BitmapTransformation() {
    override fun updateDiskCacheKey(messageDigest: MessageDigest) {}

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        Log.d("SAY123", "applying transform $widthRatio")
        return Bitmap.createBitmap(
            toTransform,
            0,
            0,
            toTransform.width*widthRatio.toInt(),
            toTransform.height
        )
    }
}