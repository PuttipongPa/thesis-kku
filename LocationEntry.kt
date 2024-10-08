package com.example.aws_01

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class LocationEntry(val latitude: Double, val longitude: Double, val altitude: Double, val unixtimestamp: Int)

class LocationListAdapter(private val locationEntries: MutableList<LocationEntry>) :
    RecyclerView.Adapter<LocationListAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = locationEntries[position]
        holder.textView.text = "Lat: ${entry.latitude}, Lon: ${entry.longitude}, Alt: ${entry.altitude}, uni: ${entry.unixtimestamp}"
    } //แสดงตอนกด add

    override fun getItemCount(): Int = locationEntries.size
}

