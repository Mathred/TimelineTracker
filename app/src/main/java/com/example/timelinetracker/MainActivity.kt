package com.example.timelinetracker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.timelinetracker.adapters.FrameAdapterData
import com.example.timelinetracker.adapters.FramesAdapter
import com.example.timelinetracker.adapters.FramesAdapter2
import com.example.timelinetracker.adapters.TestRvAdapter

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
        const val RV_MARGIN_HORIZONTAL_IN_DP = 28
    }

    private fun getDisplayMetrics(): DisplayMetrics {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics
    }

    private fun getScreenWidth(): Int {
        return Resources.getSystem().displayMetrics.widthPixels
    }

    private fun getRvWidth(): Float {
        return getScreenWidth() - RV_MARGIN_HORIZONTAL_IN_DP.dpToPx()*2
    }



    private val launcher = registerForActivityResult(SelectVideoContract()) {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(this, it)
        val vHeight = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt() ?: 0
        val vWidth = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: 0
        val frameWidthInDp = FRAME_HEIGHT_IN_DP * vWidth / vHeight
        val duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0

        Log.d("SAY123", "h x w = $vHeight x $vWidth, duration = $duration")

//        if (duration <= MAX_VIDEO_DURATION) {
            val rvWidthInDp = getScreenWidth().pxToDp() - 2 * RV_MARGIN_HORIZONTAL_IN_DP
            val fullFramesQuantity = rvWidthInDp / frameWidthInDp
            val lastFrameWidth = rvWidthInDp - fullFramesQuantity * frameWidthInDp
            Log.d("SAY123","rvWidthInDp: $rvWidthInDp, fullFramesQuantity $fullFramesQuantity, lastFrameWidth $lastFrameWidth")
//            it?.let {
//                rv?.adapter = FramesAdapter(frameWidthInDp, lastFrameWidth, fullFramesQuantity + 1, duration, it)
//                rv?.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
//            }
//        } else {
//            val threeMinutesSpanInDp = getScreenWidth().pxToDp() - 2 * RV_MARGIN_HORIZONTAL_IN_DP
//            val rvContentsWidthInDp = threeMinutesSpanInDp * duration / MAX_VIDEO_DURATION
//            val fullFramesAmount = (rvContentsWidthInDp / frameWidthInDp).toInt()
//            val lastFrameWidth = (rvContentsWidthInDp - fullFramesAmount*frameWidthInDp).toInt()

//            Log.d("SAY123","threeMinutesSpanInDp: $threeMinutesSpanInDp, frameWidthInDp: $frameWidthInDp, rvContentsWidthInDp: $rvContentsWidthInDp, fullFramesAmount $fullFramesAmount, lastFrameWidth $lastFrameWidth")

            it?.let {
//                rv?.adapter = FramesAdapter(frameWidthInDp, lastFrameWidth, fullFramesAmount + 1, duration, it)
                val data = FrameAdapterData(
                    duration = duration,
                    videoHeight = vHeight,
                    videoWidth = vWidth,
                    timelineWidthInDp = getRvWidth().toInt().pxToDp(),
                    timelineHeightInDp = FRAME_HEIGHT_IN_DP,
                    uri = it
                )
                rv?.adapter = FramesAdapter2(data)
                rv?.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
                setListener(duration)
            }
//        }
    }

    private var tracker: TrackerView2? = null
    private var btnChoose: Button? = null
    private var rv: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        tracker = findViewById(R.id.tracker)
//        tracker?.setVideoDuration(4_000L)
        btnChoose = findViewById(R.id.btn_choose)
        rv = findViewById(R.id.rv)
        btnChoose?.setOnClickListener {
            pickVideo()
        }
    }

    private fun setListener(duration: Long) {
        tracker?.setVideoDuration(duration)
        tracker?.setTimelineChangeListener(object : TrackerView2.OnTimelineChangeListener {
            override fun onNewVideoStartPosition(startPosition: Long) {
                Log.d("SAY123", "listener onNewVideoStartPosition $startPosition")
            }

            override fun onNewVideoEndPosition(endPosition: Long) {
                Log.d("SAY123", "listener onNewVideoEndPosition $endPosition")
            }

            override fun onNewTrackerPosition(trackerPosition: Long) {
                Log.d("SAY123", "listener onNewTrackerPosition $trackerPosition")
            }

            override fun onDragAndDrop(dx: Float) {
                Log.d("SAY123", "listener onDragAndDrop $dx")
                rv?.scrollBy(-dx.toInt(), 0)
            }

        })
    }

    private fun pickVideo() {
        launcher.launch("")
    }

}

fun Int.dpToPx() = (this * Resources.getSystem().displayMetrics.density)
fun Int.pxToDp() = (this / Resources.getSystem().displayMetrics.density).toInt()