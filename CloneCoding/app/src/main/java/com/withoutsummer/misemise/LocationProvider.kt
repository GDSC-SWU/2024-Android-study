package com.withoutsummer.misemise

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.app.ActivityCompat

class LocationProvider(val context : Context) {
    private var location : Location? = null
    private var locationManager : LocationManager?= null

    init{
        getLocation()
    }

    private fun getLocation() : Location? {
        try{
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            var gpsLocation : Location? = null
            var networkLocation : Location? = null

            val isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            //둘다 사용 불가일 경우
            if(!isGPSEnabled && !isNetworkEnabled){
                return null
            }else{
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return null
                }

                //네트워크 위치를 사용 가능한 경우
                if(isNetworkEnabled){
                    networkLocation = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                }

                //gps 위치를 사용 가능한 경우
                if(isGPSEnabled){
                    gpsLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                }

                //둘다 사용 가능하다면 정확도 높은 것을 선택
                if(gpsLocation != null && networkLocation != null){
                    if(gpsLocation.accuracy >  networkLocation.accuracy){
                        location=gpsLocation
                    }else{
//                        location=networkLocation
                        location = gpsLocation

                    }
                    return location
                }else{
                    if(gpsLocation != null){
                        location=gpsLocation
                    }
                    if(networkLocation != null){
                        location=networkLocation
                    }
                }
            }
//문제 발생 시 에러 로그 출력
        }catch (e : Exception){
            e.printStackTrace()
        }
        return location
    }

    fun getLocationLatitude() : Double? {
        return location?.latitude
    }

    fun getLocationLongitude() : Double? {
        return location?.longitude
    }

}