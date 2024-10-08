package com.example.aws_01

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        println("*******************************A1")
        fetchDataFromAPI()
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
    }

    private fun fetchDataFromAPI() {
        println("*******************************")
        GlobalScope.launch(Dispatchers.IO) {
            val response = fetchDataFromAPIInternal()
            val items = parseAPIResponse(response)
            withContext(Dispatchers.Main) {
// Set up the RecyclerView

                val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
                recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
                recyclerView.adapter = ItemAdapter(items)
            }
        }
    }

    private fun fetchDataFromAPIInternal(): String {
        val apiURL = "http://203.154.91.141"
        //"http://203.154.91.141"
        val url = URL(apiURL)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connect()

        val responseCode = connection.responseCode
        return if (responseCode == HttpURLConnection.HTTP_OK) {
            val inputStream = connection.inputStream
            val reader = BufferedReader(InputStreamReader(inputStream))
            val response = reader.readText()
            reader.close()
            inputStream.close()
            response
        } else {
            "Error: $responseCode"
        }
    }

    private fun parseAPIResponse(response: String): List<Item> {
        val jsonArray = JSONArray(response)
        val items = mutableListOf<Item>()

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            items.add(
                Item(
                    id = jsonObject.getString("id"),
                    latitude = jsonObject.getString("latitude"),
                    longitude = jsonObject.getString("longitude"),
                    altitude = jsonObject.getString("altitude"),
                    satellites_visible = jsonObject.getString("satellites_visible"),
                    gps_fix = jsonObject.getString("gps_fix"),
                    timestamp = jsonObject.getString("timestamp"),
                    imageData = jsonObject.getString("image_data")
                )
            )
        }
        return items
    }
}







