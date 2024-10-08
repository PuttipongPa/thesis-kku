package com.example.aws_01

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class mission : AppCompatActivity() {
    private val profileRecyclerView = mutableListOf<Profile>()
    private val selectedPoints_mission = mutableListOf<Profile>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mission)
        showOnActivity()
        val adapter_mission = ProfileAdapter(profileRecyclerView)
        val profileRecyclerView: RecyclerView = findViewById(R.id.profileRecyclerView)
        val profiles = mutableListOf<Profile>()
        val name_mission: EditText = findViewById(R.id.name_mission)
        val latitude_mission: EditText = findViewById(R.id.latitude_mission)
        val longitude_mission: EditText = findViewById(R.id.longitude_mission)
        val altitude_mission: EditText = findViewById(R.id.altitude_mission)
//        val add_mission: Button = findViewById(R.id.add_mission)

        val displayText_mission: TextView = findViewById(R.id.display_text_mission)

        val latitudeText = latitude_mission.text.toString()
        val latitude = if (latitudeText.isNotEmpty()) latitudeText.toDouble() else 0.0
        val latitude_1: String = latitude.toString()
        val longitudeText = longitude_mission.text.toString()
        val longitude = if (longitudeText.isNotEmpty()) longitudeText.toDouble() else 0.0

        val altitudeText = altitude_mission.text.toString()
        val altitude = if (altitudeText.isNotEmpty()) altitudeText.toDouble() else 0.0


        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    // navigate to home page
                    true
                }
                R.id.navigation_add_waypoint -> {
                    // navigate to add waypoint page
                    startActivity(Intent(this, AddWaypointActivity::class.java)) //ถ้ากดแล้วจะเปลี่ยนไปหน้า navigation_add_waypoint
                    true
                }

//                R.id.navigation_add_mission -> {
//                    // navigate to add waypoint page
//                    startActivity(Intent(this, mission::class.java)) //ถ้ากดแล้วจะเปลี่ยนไปหน้า navigation_add_waypoint
//                    true
//                }
                else -> false
            }
        }
/*
        add_mission.setOnClickListener {
            val missionName = name_mission.text.toString()
//            val latitude = latitude_mission.text.toString().toDouble()
//            val longitude = longitude_mission.text.toString().toDouble()
//            val altitude = altitude_mission.text.toString().toDouble()
            //val newProfile = Profile(latitude, longitude, altitude)
            profiles.add(Profile(latitude, longitude, altitude))
            adapter_mission.notifyDataSetChanged()
            // Save the data to cache memory
            //saveToCache(this, missionName, latitude, longitude, altitude)
            // Show the data on the activity XML
            displayText_mission = selectedPoints_mission.joinToString(separator = "\n") {
                "Latitude: ${it.latitude}, Longitude: ${it.longitude}, Altitude: ${it.altitude}"
            }// Proceed with the app logic using the user's input
            showOnActivity()
        }

 */
    }

    private fun saveToCache(context: Context, missionName: String, latitude: String, longitude: String, altitude: String) {
        val sharedPreferences = context.getSharedPreferences("MissionData", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("MissionName", missionName)
        editor.putString("Latitude", latitude)
        editor.putString("Longitude", longitude)
        editor.putString("Altitude", altitude)
        editor.apply()
    }

    private fun showOnActivity() {
        val textView: TextView = findViewById(R.id.textView)
        val textView2: TextView = findViewById(R.id.textView2)
        val textView3: TextView = findViewById(R.id.textView3)
        val textView4: TextView = findViewById(R.id.textView4)

        // Retrieve values from cache
        val sharedPreferences = getSharedPreferences("MissionData", Context.MODE_PRIVATE)
        val missionName = sharedPreferences.getString("MissionName", "")
        val latitude = sharedPreferences.getString("Latitude", "")
        val longitude = sharedPreferences.getString("Longitude",  "")
        val altitude = sharedPreferences.getString("Altitude",  "")

        // Set values to views
        textView.setText(missionName)
        textView2.setText(latitude.toString())
        textView3.setText(longitude.toString())
        textView4.setText(altitude.toString())
    }
}