package com.example.aws_01

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Profile(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
)
class ProfileAdapter(private val profiles: MutableList<Profile>) :
    RecyclerView.Adapter<ProfileAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val latitudeTextView: TextView = view.findViewById(R.id.textView2)
        val longitudeTextView: TextView = view.findViewById(R.id.textView3)
        val altitudeTextView: TextView = view.findViewById(R.id.textView4)
        val textView: TextView = view.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_mission, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val profile = profiles[position]
        holder.textView.text = "Lat: ${profile.latitude}, Lon: ${profile.longitude}, Alt: ${profile.altitude}"
//        holder.latitudeTextView.text = profile.latitude.toString()
//        holder.longitudeTextView.text = profile.longitude.toString()
//        holder.altitudeTextView.text = profile.altitude.toString()
    }

    override fun getItemCount() = profiles.size


}