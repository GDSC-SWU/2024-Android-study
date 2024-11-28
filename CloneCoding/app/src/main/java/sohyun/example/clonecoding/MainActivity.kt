package sohyun.example.clonecoding

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import android.util.Log
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResult
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.sohyun.clonecoding.BuildConfig
import com.sohyun.clonecoding.R
import com.sohyun.clonecoding.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import sohyun.example.clonecoding.retrofit.AirQualityResponse
import sohyun.example.clonecoding.retrofit.AirQualityService
import sohyun.example.clonecoding.retrofit.RetrofitConnection
import java.io.IOException
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : AppCompatActivity() {

    var mInterstitalAd : InterstitialAd? = null

    lateinit var binding: ActivityMainBinding
    lateinit var locationProvider: LocationProvider


    private val PERMISSIONS_REQUEST_CODE = 100

    var latitude : Double? = 0.0
    var longitude : Double? = 0.0

    val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION

    )

    lateinit var getGPSPermissionLauncher: ActivityResultLauncher<Intent>




    val startMapActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult> {
            @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
            override fun onActivityResult(result: ActivityResult) {
                if (result?.resultCode ?: Activity.RESULT_CANCELED == Activity.RESULT_OK) {
                    latitude = result?.data?.getDoubleExtra("latitude", 0.0) ?: 0.0
                    longitude = result?.data?.getDoubleExtra("longitude", 0.0) ?: 0.0
                    updateUI()
                }
            }
        })



    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        checkAllPermissions()
        updateUI()
        setRefreshButton()

        setFab()
        setBannerAds()
    }

    override fun onResume() {
        super.onResume()
        setInterstitialAds()

    }

    private fun setInterstitialAds(){
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this, "/21775744923/example/interstitial",adRequest, object : InterstitialAdLoadCallback(){
            override fun onAdLoaded(p0: InterstitialAd) {
                super.onAdLoaded(p0)
                Log.d("Ads Log", "전면 광고가 로드 되었습니다.")
                mInterstitalAd = p0
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                Log.d("Ads Log", "전면 광고가 로드 실패 되었습니다.")
            }
        })
    }

    private fun setBannerAds(){
        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()
        binding.adsBanner.loadAd(adRequest)

        binding.adsBanner.adListener = object : AdListener() {

            override fun onAdLoaded() {
                super.onAdLoaded()
                Log.d("Ads Log", "배너 광고가 로드 되었습니다.")
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                Log.d("Ads Log", "배너 광고가 로드 실패 되었습니다.")
            }

            override fun onAdClicked() {
                super.onAdClicked()
                Log.d("Ads Log", "배너 광고가 로드 클릭 되었습니다.")
            }



        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun updateUI(){
        locationProvider = LocationProvider(this@MainActivity)

        if (latitude == 0.0 && longitude == 0.0) {

            val latitude = locationProvider.getLocationLatitude()
            val longitude = locationProvider.getLocationLongitude()

        }

        if(latitude != null && longitude != null){
            // 1. 현재 위치 가져오고 UI 업데이트
            val address = getCurrentAddress(latitude!!, longitude!!)

            address?.let {
                binding.tvLocationTitle.text = "${it.thoroughfare}"
                binding.tvLocationSubtitle.text = "${it.countryName} ${it.adminArea}"
            }

            // 2. 미세먼지 농도 가져오고 UI 업데이트

            getAirQualityData(latitude!!, longitude!!)
        }else{
            Toast.makeText(this,"위도, 경도 정보를 가져올 수 없습니다.", Toast.LENGTH_LONG).show()
        }
    }

    private fun getAirQualityData(latitude: Double, longitude: Double){
        var retrofitAPI = RetrofitConnection.getInstance().create(
            AirQualityService::class.java
        )
        retrofitAPI.getAitQualityData(
            latitude.toString(),
            longitude.toString(),
            "${BuildConfig.API_KEY}")

        .enqueue( object : Callback<AirQualityResponse>{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<AirQualityResponse>,
                response: Response<AirQualityResponse>
            ) {
                if (response.isSuccessful){
                    Toast.makeText(this@MainActivity, "최신 데이터 업데이트 완료!", Toast.LENGTH_LONG).show()
                    response.body()?.let {updateAirUI(it)}

                }else{
                    Toast.makeText(this@MainActivity, "데이터를 가져오는 데 실패했습니다.", Toast.LENGTH_LONG).show()
                }
            }


            override fun onFailure(call: Call<AirQualityResponse>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(this@MainActivity, "데이터를 가져오는 데 실패했습니다.", Toast.LENGTH_LONG).show()

            }
        }


        )

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateAirUI(airQualityData: AirQualityResponse){
        val pollutionData = airQualityData.data.current.pollution

        //수치를 지정
        binding.tvCount.text = pollutionData.aqius.toString()

        //측정된 날짜

        val dateTime = ZonedDateTime.parse(pollutionData.ts).withZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDateTime()
        val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        binding.tvCheckTime.text = dateTime.format(dateFormatter).toString()


        when(pollutionData.aqius){
            in 0..50 -> {
                binding.tvTitle.text = "좋음"
                binding.imgBg.setImageResource(R.drawable.bg_good)
            }
            in 51..150 -> {
                binding.tvTitle.text = "보통"
                binding.imgBg.setImageResource(R.drawable.bg_soso)
            }

            in 151..200 -> {
                binding.tvTitle.text = "나쁨"
                binding.imgBg.setImageResource(R.drawable.bg_bad)
            }

            else -> {
                binding.tvTitle.text = "매우나쁨"
                binding.imgBg.setImageResource(R.drawable.bg_worst)
            }


        }

    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun setRefreshButton(){
        binding.btnRefresh.setOnClickListener{
            updateUI()
        }
    }


    private fun setFab(){

        binding.fab.setOnClickListener{

        if (mInterstitalAd != null){

            mInterstitalAd!!.fullScreenContentCallback = object : FullScreenContentCallback(){
                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    Log.d("Ads Log","전면 광고 닫혔습니다.")

                    val intent = Intent(this@MainActivity, MapActivity::class.java)
                    intent.putExtra("currentLat",latitude)
                    intent.putExtra("currentLng", longitude)
                    startMapActivityResult.launch(intent)
                }


                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)
                    Log.d("Ads Log","전면 광고 열기 실패했습니다.")
                }


                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                    Log.d("Ads Log","전면 광고 열기 성공했습니다.")
                    mInterstitalAd = null



                }





            }

            mInterstitalAd!!.show(this@MainActivity)


        }else{
            Log.d("Ads Log", "전면 광고가 로딩이 안되었습니다.")
            Toast.makeText(this, "잠시 후 시도해주세요.", Toast.LENGTH_LONG).show()

        }










        }
    }
    private fun getCurrentAddress(latitude: Double, longitude: Double): Address? {
        val geoCoder = Geocoder(this, Locale.KOREA)
        val addresses: List<Address>? = try {
            geoCoder.getFromLocation(latitude, longitude, 7)
        } catch (ioException: IOException) {
            Toast.makeText(this, "지오코더 서비스 이용이 불가합니다.", Toast.LENGTH_LONG).show()
            null
        } catch (illegalArgumentException: IllegalArgumentException) {
            Toast.makeText(this, "잘못된 위도, 경도입니다.", Toast.LENGTH_LONG).show()
            null
        }

        if (addresses.isNullOrEmpty()) {
            Toast.makeText(this, "주소가 발견되지 않았습니다.", Toast.LENGTH_LONG).show()
            return null
        }

        return addresses[0]
    }


    private fun checkAllPermissions(){
        if (!isLocationServicesAvailable()){
            showDialogForLocationServiceSetting()
        }else{
            isRunTimePermissionsGranted()
        }
    }
    private fun isLocationServicesAvailable() : Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))

    }

    private fun isRunTimePermissionsGranted() {
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.ACCESS_FINE_LOCATION)
        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.ACCESS_COARSE_LOCATION)

        if (hasFineLocationPermission!= PackageManager.PERMISSION_GRANTED || hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@MainActivity, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE)
        }
    }


    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode==PERMISSIONS_REQUEST_CODE && grantResults.size == REQUIRED_PERMISSIONS.size){
            var checkResult = true

            for (result in grantResults){
                if (result != PackageManager.PERMISSION_GRANTED){
                    checkResult = false
                    break;
                }
            }

            if (checkResult){
                //위치값을 가져올 수 있음
                updateUI()
            }else{
                Toast.makeText(this@MainActivity, "퍼미션이 거부 되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_SHORT).show()
                finish()
            }

        }

    }

    private fun  showDialogForLocationServiceSetting(){
        getGPSPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){
            result->
            if(result.resultCode== Activity.RESULT_OK){
                if (isLocationServicesAvailable()){
                    isRunTimePermissionsGranted()
                }else{
                    Toast.makeText(this@MainActivity, "위치 서비스를 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        val builder : AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("위치 서비스 비활성화")
        builder.setMessage("위치 서비스가 꺼져있습니다. 실행해야 앱을 사용할 수 있습니다.")
        builder.setCancelable(true)
        builder.setPositiveButton("설정", DialogInterface.OnClickListener{DialogInterface, i ->
            val callGPSSetingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            getGPSPermissionLauncher.launch(callGPSSetingIntent)
        })
        builder.setNegativeButton("취소", DialogInterface.OnClickListener{dialogInterface, i -> dialogInterface.cancel()
            Toast.makeText(this@MainActivity, "위치 서비스를 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
        })
        builder.create().show()
    }








}












