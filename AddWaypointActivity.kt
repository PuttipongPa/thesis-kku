package com.example.aws_01


import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.text.SimpleDateFormat
import java.util.*


class AddWaypointActivity : AppCompatActivity(),OnMapReadyCallback,GoogleMap.OnMapClickListener   {
    private val locationEntries = mutableListOf<LocationEntry>()
    private lateinit var mMap: GoogleMap
    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var searchView: SearchView
    private lateinit var textdata: TextView
    private lateinit var textdata2 : TextView
    private val selectedCoordinates = mutableListOf<LatLng>()
    private val defaultAltitude = 0.0
    private val selectedPoints = mutableListOf<LocationEntry>()

    private lateinit var selectDateTimeButton: Button

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_waypoint)

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        selectDateTimeButton = findViewById(R.id.selectDateTimeButton)
        selectDateTimeButton.setOnClickListener {
            showDateTimePicker()
        }
//        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
//        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        findViewById<Button>(R.id.btnMyLocation).setOnClickListener {
            getCurrentLocation()
        }
        searchView = findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchLocation(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        val latitudeInput: EditText = findViewById(R.id.latitude_input)
        val longitudeInput: EditText = findViewById(R.id.longitude_input)
        val altitudeInput: EditText = findViewById(R.id.altitude_input)
        textdata = findViewById(R.id.TextDateTime)
        textdata.setText("Hello, world!")
        textdata2 = findViewById(R.id.TextDateTimetwo)
        val saveButton: Button = findViewById(R.id.save_button)
        val displayText: TextView = findViewById(R.id.display_text)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val addButton: Button = findViewById(R.id.add_button)
        val clearButton: Button = findViewById(R.id.clear_button)
        val locationList: RecyclerView = findViewById(R.id.location_list)
        val adapter = LocationListAdapter(locationEntries)
        locationList.layoutManager = LinearLayoutManager(this)
        locationList.adapter = adapter
        textdata.visibility = View.GONE
        val maxVal = 20
        val inputFilter = InputFilter { source, start, end, dest, dstart, dend ->
            try {
                val input = Integer.parseInt(dest.toString() + source.toString())
                if (input <= maxVal) {
                    null // Accept input
                } else {
                    "" // Reject input
                }
            } catch (e: NumberFormatException) {
                "" // Reject input
            }
        }
        altitudeInput.filters = arrayOf(inputFilter)


        addButton.setOnClickListener {
            val latitude = latitudeInput.text.toString().toDoubleOrNull() ?: 0.0
            val longitude = longitudeInput.text.toString().toDoubleOrNull() ?: 0.0
            val altitude = altitudeInput.text.toString().toDoubleOrNull() ?: 0.0
            val textdata_2 = textdata.text.toString().toIntOrNull() ?: 0

            Toast.makeText(applicationContext,"$textdata_2", Toast.LENGTH_SHORT).show()

            val name: String = altitudeInput.getText().toString()
            if (name.isEmpty()) { //ถ้าความสูงเป็นค่าว่างจะทำ124 แต่ถ้ามีค่าอยู่แล้ว มันจะเพิ่มค่าลงใน itemview
                Toast.makeText(applicationContext, "กรุณาใส่ค่าความสูง", Toast.LENGTH_SHORT).show()// Show an error message or take some other action
            } else {

                locationEntries.add(LocationEntry(latitude, longitude, altitude,textdata_2))
                adapter.notifyDataSetChanged()

                latitudeInput.text.clear()
                longitudeInput.text.clear()
                altitudeInput.text.clear()


                displayText.text = selectedPoints.joinToString(separator = "\n") {
                    "Latitude: ${it.latitude}, Longitude: ${it.longitude}, Altitude: ${it.altitude}"
                }// Proceed with the app logic using the user's input
            }

        }
        saveButton.setOnClickListener {
            var allDataSent = true

            for (entry in locationEntries) {
                if (!sendLocationData(entry.latitude, entry.longitude, entry.altitude, entry.unixtimestamp)) {
                    allDataSent = false
                }

            }
            Toast.makeText(applicationContext,"$locationEntries", Toast.LENGTH_SHORT).show()

            if (allDataSent) {
                Toast.makeText(applicationContext, "All data sent successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext, "Some data failed to send", Toast.LENGTH_SHORT).show()
            }

            locationEntries.clear()
            adapter.notifyDataSetChanged()

        }


        clearButton.setOnClickListener {
            locationEntries.clear()
            adapter.notifyDataSetChanged()
        }

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    // navigate to home page
                    startActivity(Intent(this, MainActivity::class.java)) //ถ้ากดแล้วจะเปลี่ยนไปหน้า navigation_add_waypoint
                    true
                }
                R.id.navigation_add_waypoint -> {
                    // navigate to add waypoint page
                    true
                }
//                R.id.navigation_add_mission -> {
//                    // navigate to home page
//                    startActivity(Intent(this, mission::class.java)) //ถ้ากดแล้วจะเปลี่ยนไปหน้า navigation_add_mission
//                    true
//                }
                else -> false
            }
        }
    }
//****เพิ่มเวลาวันที่
private fun showDateTimePicker() {
    val now = Calendar.getInstance()

    val datePicker = DatePickerDialog(
        this,
        { _, year, monthOfYear, dayOfMonth ->
            val selectedDateTime = Calendar.getInstance()
            selectedDateTime.set(year, monthOfYear, dayOfMonth)

            // Check if the selected date is in the past
            if (selectedDateTime.timeInMillis < System.currentTimeMillis()) {
                Toast.makeText(applicationContext, "Please select a valid date", Toast.LENGTH_SHORT).show()
                return@DatePickerDialog
            }

            // After the user selects a valid date, show a time picker
            val timePicker = TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    // Handle the selected date and time
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    selectedDateTime.set(Calendar.MINUTE, minute)

                    // Format the date and time for display
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val date = dateFormat.format(selectedDateTime.time)
                    val time = timeFormat.format(selectedDateTime.time)
                    val unixTimestamp = selectedDateTime.timeInMillis / 1000 //unixtimestamp

                    // Show a toast message with the selected date and time
                    val message = " $unixTimestamp"

                    // Check if the selected date and time are in the past
                    if (selectedDateTime.timeInMillis < System.currentTimeMillis()) {
                        Toast.makeText(applicationContext, "Please select a valid date and time", Toast.LENGTH_SHORT).show()
                        return@TimePickerDialog
                    }

                    textdata.setText("$unixTimestamp")
                    textdata2.setText("\uD83D\uDCC5 : $date ⏰ : $time")
                },
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                false
            )
            timePicker.show()
        },
        now.get(Calendar.YEAR),
        now.get(Calendar.MONTH),
        now.get(Calendar.DAY_OF_MONTH)
    )

    // Prevent selecting a backward date
    datePicker.datePicker.minDate = System.currentTimeMillis() - 1000

    datePicker.show()
}


    //****เพิ่มเวลาวันที่

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE); //เปลี่ยนโหมดgoogle map
        mMap.setOnMapClickListener(this)
        mMap.uiSettings.isMyLocationButtonEnabled = false
        enableMyLocation()

    }

    override fun onMapClick(latLng: LatLng) {
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(latLng).title("Clicked Location"))
        val message = "Latitude: ${latLng.latitude}, Longitude: ${latLng.longitude}"
        // Find the EditText view by its ID
        val myInput = findViewById<EditText>(R.id.latitude_input)
        val myInputlong = findViewById<EditText>(R.id.longitude_input)
        // Set a value to the EditText field
        myInput.setText( "${latLng.latitude}")
        //,${latLng.longitude}
        myInputlong.setText( "${latLng.longitude}")

    }

    private fun setupMapClick() {
        mMap.setOnMapClickListener { latLng ->
            val latitude = latLng.latitude
            val longitude = latLng.longitude

            // Add the selected coordinate to the list
            selectedCoordinates.add(latLng)

            // Add a marker for the selected coordinate
            mMap.addMarker(MarkerOptions().position(latLng).title("Selected Location"))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    private fun searchLocation(address: String?) {
        if (!address.isNullOrEmpty()) {
            val geocoder = Geocoder(this)
            try {
                val addresses = geocoder.getFromLocationName(address, 1)
                if (addresses != null) {
                    if (addresses.isNotEmpty()) {
                        val location = addresses[0]
                        val latLng = LatLng(location.latitude, location.longitude)
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    } else {
                        Toast.makeText(this, "ไม่พบสถานที่ที่คุณต้องการ", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }
    // Add MapView lifecycle methods
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}

    private fun sendLocationData(latitude: Double, longitude: Double, altitude: Double, unixtimestamp: Int): Boolean {
        val client = OkHttpClient()
        val url = "http://203.154.91.141"
        val jsonData = "{\"latitude\": $latitude, \"longitude\": $longitude, \"altitude\": $altitude ,\"unixtimestamp\": $unixtimestamp}"
        val requestBody = jsonData.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                println("Success: ${response.body?.string()}")
                println("request: $jsonData")
            }
            override fun onFailure(call: Call, e: IOException) {
                println("Error: $e")
            }
        })
        return true
    }

// Rest of the AddWaypointActivity code ...