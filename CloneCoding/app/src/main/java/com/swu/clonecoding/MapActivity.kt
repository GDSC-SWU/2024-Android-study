package com.swu.clonecoding

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.swu.clonecoding.databinding.ActivityMapBinding

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var binding: ActivityMapBinding

    private var mMap : GoogleMap? = null

    var currentLat : Double = 0.0
    var currentLng : Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //메인액티비티에서 보낸 위도, 경도 값 받음
        currentLat = intent.getDoubleExtra("currentLat",0.0)
        currentLng = intent.getDoubleExtra("currentLng",0.0)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)//지도가 준비되면 콜백 실행

        setButton()
    }

    //메인 액티비티 함수와 연동 되어야함. 네임값 일치, RESULT_OK 코드 동일하게 보내주기
    private fun setButton(){
        binding.btnCheckHere.setOnClickListener {
            mMap?.let {
                val intent = Intent()
                intent.putExtra("latitude",it.cameraPosition.target.latitude)
                intent.putExtra("longtitude",it.cameraPosition.target.longitude)
                setResult(Activity.RESULT_OK,intent)
                finish()
            }
        }
        binding.fabCurrentLocation.setOnClickListener{
            val locationProvider = LocationProvider(this)
            val latitude = locationProvider.getLocationLatitude()
            val longtitude = locationProvider.getLocationLongitude()

            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude!!,longtitude!!),16f))
            setMarker()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap?.let {
            val currentLocation = LatLng(currentLat,currentLng)
            it.setMaxZoomPreference(20.0f)
            it.setMinZoomPreference(12.0f)
            it.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,16f))
            setMarker()
        }
    }
    private fun setMarker(){
        mMap?.let {
            it.clear()
            val markerOption = MarkerOptions()
            markerOption.position(it.cameraPosition.target)
            markerOption.title("마커 위치")
            val marker = it.addMarker(markerOption)

            it.setOnCameraMoveListener {
                marker?.let {marker->
                    marker.position = it.cameraPosition.target
                }
            }
        }
    }
}