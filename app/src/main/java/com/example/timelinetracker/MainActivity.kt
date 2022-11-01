package com.example.timelinetracker

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View

class MainActivity : AppCompatActivity() {

    private var tracker: TrackerView2? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tracker = findViewById(R.id.tracker)
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
    }

}