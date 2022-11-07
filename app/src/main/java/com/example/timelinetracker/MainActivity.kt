package com.example.timelinetracker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class MainActivity : AppCompatActivity() {

    inner class SelectVideoContract: ActivityResultContract<Any, Uri?>() {
        override fun createIntent(context: Context, input: Any): Intent {
            return Intent().apply {
                action = Intent.ACTION_GET_CONTENT
                type = "video/*"
            }
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return intent.takeIf { resultCode == Activity.RESULT_OK }?.data
        }

    }

    var amountOfFramesInRv = 0
    companion object {
        const val MAX_VIDEO_DURATION = 3 * 60_000L
        const val FRAME_HEIGHT_IN_DP = 44
    }

    private fun getDisplayMetrics(): DisplayMetrics {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics
    }

    /*private fun getScreenWidth(): Int {

    }*/

    private val launcher = registerForActivityResult(SelectVideoContract()) {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(this, it)
        val height = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt() ?: 0
        val width = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: 0
        val frameWidthInDp = FRAME_HEIGHT_IN_DP * width / height

        val duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt() ?: 0
        if (duration <= MAX_VIDEO_DURATION) {

        }



        Log.d("SAY123", "h x w = $height x $width, duration = $duration frameWidthInDp = $frameWidthInDp")

        it?.let {
            setThumbnailsViaGlide(it)
        }
    }

    private fun setThumbnailsViaMetadataRetriever(uri: Uri) {
        val retr = MediaMetadataRetriever()
        retr.setDataSource(this, uri)
        val bmp = retr.getFrameAtTime(0)
        iv1?.setImageBitmap(retr.getFrameAtTime(0))
        iv2?.setImageBitmap(retr.getFrameAtTime(10000000))
        iv3?.setImageBitmap(retr.getFrameAtTime(20000000))
    }

    private fun setThumbnailsViaGlide(uri: Uri) {
        val options = RequestOptions().frame(1_000_000).centerCrop()
        iv1?.let {
            Glide.with(this)
                .load(uri)
                .apply(options)
                .into(it)
        }
    }

    private var tracker: TrackerView2? = null
    private var btnChoose: Button? = null
    private var iv1: ImageView? = null
    private var iv2: ImageView? = null
    private var iv3: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tracker = findViewById(R.id.tracker)
        btnChoose = findViewById(R.id.btn_choose)
        iv1 = findViewById(R.id.iv1)
        iv2 = findViewById(R.id.iv2)
        iv3 = findViewById(R.id.iv3)

        tracker?.setVideoDuration(60_000L)
        tracker?.setTimelineChangeListener(object : TrackerView2.OnTimelineChangeListener {
            override fun onNewVideoStartPosition(startPosition: Long) {
                Log.d("SAY123", "onNewVideoStartPosition $startPosition")

            }

            override fun onNewVideoEndPosition(endPosition: Long) {
                Log.d("SAY123", "onNewVideoEndPosition $endPosition")

            }

            override fun onNewTrackerPosition(trackerPosition: Long) {
                Log.d("SAY123", "onNewTrackerPosition $trackerPosition")
            }

        })

        btnChoose?.setOnClickListener {
            pickVideo()
        }
    }

    private fun pickVideo() {
        launcher.launch("")
    }

// /storage/self/primary/Movies/Telegram/VID_20220822_152245_580.mp4

}