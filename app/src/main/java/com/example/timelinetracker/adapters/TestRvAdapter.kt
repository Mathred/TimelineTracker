package com.example.timelinetracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.timelinetracker.R

class TestRvAdapter: RecyclerView.Adapter<TestRvAdapter.ViewHolder>() {

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val textView: TextView
        init {
            textView = view.findViewById(R.id.tvTestRv)
        }
    }

    val dataSet: MutableList<Int> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.test_rv_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = position.toString()
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    fun setData(data: List<Int>) {
        dataSet.clear()
        dataSet.addAll(data)
        notifyDataSetChanged()
    }

}