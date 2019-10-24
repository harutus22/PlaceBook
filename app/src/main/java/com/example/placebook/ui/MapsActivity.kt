package com.example.placebook.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProviders
import com.example.placebook.R
import com.example.placebook.adapter.BookmarkInfoWindowAdapter
import com.example.placebook.viewmodel.MapsViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mMap: GoogleMap
    private lateinit var placesClient: PlacesClient
    private lateinit var mapsViewModel: MapsViewModel
//    private var locationRequest: LocationRequest? = null

    companion object{
        private const val REQUEST_LOCATION = 1
        private const val TAG = "MapsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setUpLocationClient()
        setUpPlaces()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.isMyLocationEnabled = true
        setupListeners()
        setUpViewModel()
        getCurrentLocation()
    }

    private fun setUpLocationClient(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun setupListeners(){
        mMap.setInfoWindowAdapter(BookmarkInfoWindowAdapter(this))
        mMap.setOnPoiClickListener{
            displayPoi(it)
        }
        mMap.setOnInfoWindowClickListener {
            handleInfoWindowClick(it)
        }
    }

    private fun requestLocationPermission(){
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION
        )
    }

    private fun setUpPlaces(){
        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        placesClient = Places.createClient(this)
    }

    private fun displayPoi(pointOfInterest: PointOfInterest){
        displayPoiGetPlaceStep(pointOfInterest)
    }

    private fun displayPoiGetPhotoStep(place: Place){
        val photoMetadata = place.photoMetadatas?.get(0)
        if (photoMetadata == null) return

        val photoRequest = FetchPhotoRequest.builder(photoMetadata).
                setMaxWidth(resources.getDimensionPixelSize(R.dimen.default_image_width)).
            setMaxHeight(resources.getDimensionPixelSize(R.dimen.default_image_height)).build()

        placesClient.fetchPhoto(photoRequest).addOnSuccessListener { fetchPhotoResponse ->
            val bitmap = fetchPhotoResponse.bitmap
            displayPoiDisplayStep(place, bitmap)
        }.addOnFailureListener { exception ->
            if(exception is ApiException) {
                val statusCode = exception.statusCode
                Log.e(TAG, "Place not found: ${exception.message}, status code: $statusCode")
            }
        }
    }

    private fun displayPoiDisplayStep(place: Place, photo: Bitmap?){
//        val iconPhoto = if (photo == null) BitmapDescriptorFactory.defaultMarker()
//        else{ BitmapDescriptorFactory.fromBitmap(photo)}

        val marker = mMap.addMarker(MarkerOptions().position(place.latLng as LatLng)
//            .icon(iconPhoto)
            .title(place.name).snippet(place.phoneNumber))
//        marker?.tag = photo
        marker?.tag = PlaceInfo(place, photo)
    }

    private fun displayPoiGetPlaceStep(pointOfInterest: PointOfInterest) {
        val placeId = pointOfInterest.placeId
        val placeFields = listOf(
            Place.Field.ID, Place.Field.NAME, Place.Field.PHONE_NUMBER,
            Place.Field.PHOTO_METADATAS, Place.Field.ADDRESS, Place.Field.LAT_LNG
        )
        val request = FetchPlaceRequest.builder(placeId, placeFields).build()
        placesClient.fetchPlace(request).addOnSuccessListener {
            val place = it.place
//            Toast.makeText(
//                this, "${place.name}, ${place.phoneNumber}",
//                Toast.LENGTH_LONG
//            ).show()
            displayPoiGetPhotoStep(place)
        }.addOnFailureListener { exception ->
            if (exception is ApiException) {
                val statusCode = exception.statusCode
                Log.e(TAG, "Place not found ${exception.message}, status code: $statusCode ")
            }
        }
    }

    private fun setUpViewModel(){
        mapsViewModel = ViewModelProviders.of(this).get(MapsViewModel::class.java)
    }

    private fun getCurrentLocation(){
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestLocationPermission()
        } else {
//            if (locationRequest == null){
//                locationRequest = LocationRequest.create()
//                locationRequest?.let{
//                    it.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//                    it.interval = 5000
//                    it.fastestInterval = 1000
//                    val locationCallback = object : LocationCallback(){
//                        override fun onLocationResult(p0: LocationResult?) {
//                            getCurrentLocation()
//                        }
//                    }
//                    fusedLocationClient.requestLocationUpdates(locationRequest,
//                        locationCallback, null)
//                }
//            }
            fusedLocationClient.lastLocation.addOnCompleteListener {
                val location = it.result
                if (location != null){
                    val latLng = LatLng(location.latitude, location.longitude)
//                    mMap.addMarker(MarkerOptions().position(latLng).title("You are here!!"))
                    val update = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)
                    mMap.moveCamera(update)
                } else {
                    Log.e(TAG, "No location found")
                }
            }
        }
    }

    private fun handleInfoWindowClick(marker: Marker){
        val placeInfo = (marker.tag as PlaceInfo)
        if (placeInfo.place != null) {
            GlobalScope.launch {
                mapsViewModel.addBookmarkFromPlace(placeInfo.place, placeInfo.image)
            }
        }
        marker.remove()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Log.e(TAG, "Location permission denied")
            }
        }
    }

    class PlaceInfo(val place: Place? = null, val image: Bitmap? = null)
}
