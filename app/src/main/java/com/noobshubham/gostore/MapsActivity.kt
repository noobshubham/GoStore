package com.noobshubham.gostore

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.mancj.materialsearchbar.MaterialSearchBar
import com.noobshubham.gostore.databinding.ActivityMapsBinding
import com.noobshubham.gostore.grocery.GroceryActivity
import com.noobshubham.gostore.model.MyPlaces
import com.noobshubham.gostore.remote.IGoogleAPIService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsBinding
    private var map: GoogleMap? = null

    // A default location (India)
    private val india = LatLng(22.80720609595059, 79.637510102395)

    // Material Search Bar
    private lateinit var materialSearchBar: MaterialSearchBar

    // from reference video
    private var latitude: Double = 0.toDouble()
    private var longitude: Double = 0.toDouble()

    private var locationPermissionGranted = false
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

    private lateinit var mLastLocation: Location
    private var marker: Marker? = null

    //location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    companion object {
        private const val MY_PERMISSION_CODE: Int = 2000
    }

    private lateinit var mServices: IGoogleAPIService
    internal lateinit var currentPlace: MyPlaces

    // [START maps_current_place_on_create]
    @SuppressLint("VisibleForTests")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(binding.root)

        binding.showList.setOnClickListener { startActivity(Intent(this, GroceryActivity::class.java)) }

        materialSearchBar = binding.searchbar

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // init service
        mServices = Common.googleApiService

        getLocationPermission()

        binding.btnRestaurants.setOnClickListener { nearByPlace("Restaurants") }
        binding.btnHospitals.setOnClickListener { nearByPlace("Hospitals") }
        binding.btnATMs.setOnClickListener { nearByPlace("ATMs") }
        binding.btnBanks.setOnClickListener { nearByPlace("Banks") }
        binding.btnSchools.setOnClickListener { nearByPlace("Schools") }
        binding.btnHotels.setOnClickListener { nearByPlace("Hotels") }
    }

    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.applicationContext, ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    private fun nearByPlace(typePlace: String) {

        //clear all marker from map
        map?.clear()

        //build url based on location
        val url = getUrl(latitude, longitude, typePlace)
        mServices.getNearbyPlaces(url)
            .enqueue(object : Callback<MyPlaces> {
                override fun onResponse(call: Call<MyPlaces>, response: Response<MyPlaces>) {

                    currentPlace = response.body()!!
                    if (response.isSuccessful) {
                        for (i in 0 until response.body()!!.results!!.size) {
                            val markerOptions = MarkerOptions()
                            val googlePlaces = response.body()!!.results!![i]
                            val lat = googlePlaces.geometry!!.location!!.lat
                            val lng = googlePlaces.geometry!!.location!!.lng
                            val placeName = googlePlaces.name
                            val latLng = LatLng(lat, lng)
                            markerOptions.position(latLng)
                            markerOptions.title(placeName)

                            when (typePlace) {
                                "restaurants" -> markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_restaurants)).title(placeName)
                                "hospitals" -> markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_doctor)).title(placeName)
                                "ATMs" -> markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_credit_card)).title(placeName)
                                "banks" -> markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bank)).title(placeName)
                                "schools" -> markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_school)).title(placeName)
                                "hotels" -> markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bed)).title(placeName)
                                else -> markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                            }
                            // add marker to map
                            markerOptions.snippet(latLng.toString())
                            // to get the lat/lng of place
                            map?.addMarker(markerOptions)
                            // move camera
                            map?.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                            map?.animateCamera(CameraUpdateFactory.zoomTo(12f))
                        }
                    }
                }

                override fun onFailure(call: Call<MyPlaces>, t: Throwable) {
                    Toast.makeText(baseContext, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun getUrl(latitude: Double, longitude: Double, typePlace: String): String {
        val googlePlaceUrl =
            StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
        googlePlaceUrl.append("?keyword=cruise&location=$latitude,$longitude")
        googlePlaceUrl.append("&radius=20000") // 20km
        googlePlaceUrl.append("&type=$typePlace")
        googlePlaceUrl.append(("&key=${this.getString(R.string.maps_api_key)}"))
        Log.d("url_debug", googlePlaceUrl.toString())
        return googlePlaceUrl.toString()
    }

    private fun buildLocationCallBack() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                mLastLocation = p0.locations[p0.locations.size - 1] // Get last location
                if (marker != null) {
                    marker!!.remove()
                }
                latitude = mLastLocation.latitude
                longitude = mLastLocation.longitude

                val latlng = LatLng(latitude, longitude)
                val markerOptions = MarkerOptions()
                    .position(latlng)
                    .title("Your Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                marker = map?.addMarker(markerOptions)

                //Move Camera
                map?.moveCamera(CameraUpdateFactory.newLatLng(latlng))
                map?.animateCamera(CameraUpdateFactory.zoomTo(11f))
            }
        }
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        locationRequest.smallestDisplacement = 10f
    }

    private fun checkLocationPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, ACCESS_FINE_LOCATION))
                ActivityCompat.requestPermissions(
                    this, arrayOf(ACCESS_FINE_LOCATION), MY_PERMISSION_CODE)
            else
                ActivityCompat.requestPermissions(
                    this, arrayOf(ACCESS_FINE_LOCATION), MY_PERMISSION_CODE)
            false
        } else
            true
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    )
                        if (checkLocationPermission()) {
                            buildLocationRequest()
                            buildLocationCallBack()
                            fusedLocationProviderClient =
                                LocationServices.getFusedLocationProviderClient(this)
                            fusedLocationProviderClient.requestLocationUpdates(
                                locationRequest,
                                locationCallback,
                                Looper.myLooper()
                            )
                            map?.isMyLocationEnabled
                        }
                } else
                    Toast.makeText(this, "location Service Not Enabled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStop() {
        // fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(map: GoogleMap) {
        this.map = map

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) { map.isMyLocationEnabled }
        } else
            map.isMyLocationEnabled

        map.animateCamera(CameraUpdateFactory.newLatLng(india))
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(india, 7.4F))
    }
}
