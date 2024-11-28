package com.withoutsummer.misemise

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.app.ActivityCompat

class LocationProvider(val context : Context) {
    //현재 위치 저장하기 위한 변수
    private var location : Location? = null
    //위치 서비스에 접근하기 위해 사용
    private var locationManager : LocationManager?= null

    //클래스가 초기화될 때 자동으로 getLocation 호출하여 위치를 가져옴
    init{
        getLocation()
    }

    private fun getLocation() : Location? {
        try{
            //LOCATION_SERVICE를 사용하여 LocationManager 객체를 초기화 -> 위치 정보를 가져옴
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            var gpsLocation : Location? = null
            var networkLocation : Location? = null

            //isProviderEnabled 메서드를 사용하여 GPS와 네트워크 위치 서비스가 활성화되었는지 확인
            val isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            //둘다 사용 불가일 경우
            if(!isGPSEnabled && !isNetworkEnabled){
                return null
            }else{
                //위치 권한이 부여되었는지 확인
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    //권한이 없다면 null을 반환.
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
                        //location = networkLocation
                        location=gpsLocation
                    }
                    return location
                }else{
                    if(gpsLocation != null){
                        location=gpsLocation
                    }
                    if(networkLocation != null){
                        location = networkLocation
                    }
                }
            }
//문제 발생 시 에러 로그 출력
        }catch (e : Exception){
            e.printStackTrace()
        }
        return location
    }

    //위도 및 경도 반환 함수
    //location 객체의 latitude 값을 반환.
    //위치 정보가 없으면 null을 반환.
    fun getLocationLatitude() : Double? {
        return location?.latitude
    }

    //location 객체의 longitude 값을 반환.
    //위치 정보가 없으면 null을 반환.
    fun getLocationLongitude() : Double? {
        return location?.longitude
    }

}