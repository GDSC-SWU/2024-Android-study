package com.swu.clonecoding

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.swu.clonecoding.databinding.ActivityMainBinding
import com.swu.clonecoding.retrofit.AirQualityResponse
import com.swu.clonecoding.retrofit.AirQualityService
import com.swu.clonecoding.retrofit.RetrofitConnection
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : AppCompatActivity() {

    var mInterstitialAd : InterstitialAd? = null

    lateinit var binding : ActivityMainBinding

    private val PERMISSIONS_REQUEST_CODE = 100

    val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    //지오코딩
    lateinit var locationProvider : com.swu.clonecoding.LocationProvider

    //위도 경도 변수
    var latitude : Double? = 0.0
    var longtitude : Double? = 0.0

    lateinit var getGPSPermissionLauncher : ActivityResultLauncher<Intent>

    val startMapActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
        object : ActivityResultCallback<ActivityResult> {
            override fun onActivityResult(result: ActivityResult) {
                if(result?.resultCode?:0 == Activity.RESULT_OK){
                    latitude = result?.data?.getDoubleExtra("latitude",0.0)?:0.0
                    longtitude = result?.data?.getDoubleExtra("longtitude",0.0)?:0.0
                    updateUI();
                }
            }
        })

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

    //전면 광고는 한번만 사용가능한데, 돌아와도 다시 로딩되도록
    override fun onResume() {
        super.onResume()
        setInterstitialAds()
    }

    private fun setInterstitialAds(){
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this,"/21775744923/example/interstitial",adRequest,object :InterstitialAdLoadCallback(){
            override fun onAdLoaded(p0: InterstitialAd) {
                super.onAdLoaded(p0)
                Log.d("Ads Log","전면 광고가 로드 되었습니다.")
                mInterstitialAd = p0
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                Log.d("Ads Log","전면 광고가 로드 실패했습니다.")
            }
        })
    }
    private fun setBannerAds(){
        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()
        binding.adsBanner.loadAd(adRequest)//배너 광고에 광고를 로딩

        binding.adsBanner.adListener = object: AdListener(){
            override fun onAdLoaded() {
                super.onAdLoaded()
                Log.d("Ads Log","배너 광고가 로드되었습니다.")
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                Log.d("Ads Log","배너 광고 로드가 실패되었습니다.")
            }

            override fun onAdClicked() {
                super.onAdClicked()
                Log.d("Ads Log","배너 광고가 클릭되었습니다.")
            }
        }
    }

    private fun updateUI(){
        locationProvider = LocationProvider(this)

        if(latitude==0.0 && longtitude==0.0){
            latitude = locationProvider.getLocationLatitude()
            longtitude = locationProvider.getLocationLongitude()
        }

        if(latitude != null && longtitude != null){
            //1. 현재 위치가져오고 UI 업데이트
            val address = getCurrentAddress(latitude!!, longtitude!!)

            //address가 null이 아니면 let{..} 실행
            address?.let {
                binding.tvLocationTitle.text = "${it.thoroughfare}"
                //it => address address의 thoroughfare은 동네 지명 나타냄 예)역삼1동
                binding.tvLocationSubtitle.text = "${it.countryName} ${it.adminArea}"
                //it => address address의 contryName은 나라 이름 나타냄
            }
            //2. 미세먼지 농도 가져오고 UI 업데이트
            getAirQualityData(latitude!!,longtitude!!)
        }else{
            Toast.makeText(this,"위도, 경도 정보를 가져올 수 없습니다.",Toast.LENGTH_LONG).show()
        }
    }

    private fun getAirQualityData(latitude: Double, longtitude: Double){
        var retrofitAPI = RetrofitConnection.getInstance().create(
            AirQualityService::class.java
        ) //AirQualityService 구현체를 레트로핏이 생성해줌 => getAirQualityData 함수 사용 가능

        retrofitAPI.getAirQualityData(
            latitude.toString(),
            longtitude.toString(),
            "${BuildConfig.API_KEY}"

            //excute() : 동기 실행(함수가 실행되는 스레드에서 실행되어 데이터가 오기를 기다림)
            // enqueue() : 비동기 실행(백그라운드 스레드에서 요청을 보내고 응답이 오면 등록한 콜백 함수 실행
            //메인스레드에서 실행하더라도 백그라운드 스레드에서 요청이 실행되기 때문에 UI막지 않음. enqueue() 사용 권장
        ).enqueue(object : Callback<AirQualityResponse>{
            //응답이 왔을 때 실행되는 함수
            override fun onResponse(
                call: Call<AirQualityResponse>,
                response: Response<AirQualityResponse>
            ) {
                if (response.isSuccessful) {
                    //응답이 성공적이라면
                    Toast.makeText(this@MainActivity, "최신 데이터 업데이트 완료!", Toast.LENGTH_LONG).show()
                    response.body()?.let{updateAirUI(it)} //응답을 성공적으로 가져오고 바디값이 null이 아니라면 UI 업데이트
                } else {
                    Toast.makeText(this@MainActivity, "데이터를 가져오는 데 실패했습니다.", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<AirQualityResponse>, t: Throwable) {
                t.printStackTrace() //에러 출력
                Toast.makeText(this@MainActivity,"데이터를 가져오는 데 실패했습니다.",Toast.LENGTH_LONG).show()
            }
        }
        )
    }

    private fun updateAirUI(airQualityData : AirQualityResponse){
        val pollutionData = airQualityData.data.current.pollution
        //데이터 클래스 데이터 중 pollution 값 가져옴.

        //aqius 데이터 가져와서 수치를 설정
        binding.tvCount.text = pollutionData.aqius.toString()

        //측정된 날짜 설정
        //한국 시에 맞춰서 가져와서 년도-월-일 시:분 형태로 설정
        val dateTime = ZonedDateTime.parse(pollutionData.ts).withZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDateTime()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-mm-dd HH:mm")
        binding.tvCheckTime.text = dateTime.format(dateFormatter).toString()

        when(pollutionData.aqius){
            in 0..50 ->{
                binding.tvTitle.text = "좋음"
                binding.imgBg.setImageResource(R.drawable.bg_good)
            }
            in 51..150 ->{
                binding.tvTitle.text = "보통"
                binding.imgBg.setImageResource(R.drawable.bg_soso)
            }
            in 151..200 ->{
                binding.tvTitle.text = "나쁨"
                binding.imgBg.setImageResource(R.drawable.bg_bad)
            }
            else ->{
                binding.tvTitle.text = "매우나쁨"
                binding.imgBg.setImageResource(R.drawable.bg_worst)
            }
        }
    }

    private fun setRefreshButton(){
        binding.btnRefresh.setOnClickListener{
            updateUI()
        }
    }

    private fun setFab(){
        binding.fab.setOnClickListener{

            if(mInterstitialAd!=null){
                mInterstitialAd!!.fullScreenContentCallback=object :FullScreenContentCallback(){
                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()
                        Log.d("Ads Log","전면 광고 닫혔습니다.")
                        val intent = Intent(this@MainActivity, MapActivity::class.java)
                        intent.putExtra("currentLat",latitude)
                        intent.putExtra("currentLng",longtitude)
                        startMapActivityResult.launch(intent)
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        super.onAdFailedToShowFullScreenContent(p0)
                        Log.d("Ads Log","전면 광고 열기 실패했습니다.")
                    }

                    override fun onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent()
                        Log.d("Ads Log","전면 광고 열기 성공했습니다.")
                        mInterstitialAd = null
                    }
                }
                mInterstitialAd!!.show(this@MainActivity)
            }else{
                Log.d("Ads Log","전면 광고가 로딩이 안 되었습니다. ")
                Toast.makeText(this, "잠시후 시도해주세요.",Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun getCurrentAddress (latitude : Double, longtitude : Double): Address?{
        val geoCoder = Geocoder(this, Locale.KOREA) //지오 코드 객체 생성해서 위치를 휴대폰 한국어를 기반으로 가져옴.

        val addresses: List<Address>? // Nullable 타입으로 선언
        //지오 코딩 객체는 값이 여러개가 넘어오기 때문에 리스트 사용

        addresses = try {
            geoCoder.getFromLocation(latitude,longtitude,7)
        }catch (ioException : IOException){
            Toast.makeText(this,"지오코더 서비스를 이용불가 합니다.",Toast.LENGTH_LONG).show()
            return null
        }catch (illegalArgumentException : IllegalArgumentException){
            Toast.makeText(this,"잘못된 위도, 경도 입니다.",Toast.LENGTH_LONG).show()
            return null
        } //catch문 에러 발생하면 null 값 반환

        //주소값 가져오지 못한 경우
        if(addresses ==null || addresses.size == 0)
        { Toast.makeText(this,"주소가 발견되지 않았습니다.",Toast.LENGTH_LONG).show()
            return null
        }
        //정상적으로 주소를 가져 왔다면
        return addresses[0]
    }

    private fun checkAllPermissions(){
        if(!isLocationServicesAvailable()){
                showDialogForLocationServiceSetting()
        }else{
            isRunTimePermissionsGranted()
        }
    }
    private fun isLocationServicesAvailable() : Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager //LocationManager 타입 캐스팅

        //네크워크나 GPS 중 하나를 프로바이더로 설정. GPS는 위성신호로 위치 판독, 네트워크 프로바이더는 wifi나 네트워크 기지국으로 판독
        //하나라도 사용되면 true 반환
        return(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))

    }

    private fun isRunTimePermissionsGranted(){
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        if(hasFineLocationPermission != PackageManager.PERMISSION_GRANTED || hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE)
            //허가 요청할 코드와 리스트 넣어줌. 코드를 넣어주는 이유는 : response로 돌아왔을 때 어떤 request를 요청한 것인지 확인하는 용도
        }

    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==PERMISSIONS_REQUEST_CODE && grantResults.size == REQUIRED_PERMISSIONS.size)
        {
            var checkResult = true

            for(result in grantResults){
                if(result != PackageManager.PERMISSION_GRANTED){
                    checkResult = false
                    break;
                }
            }
            if(checkResult){
                //위치값 가져올 수 있음
                updateUI()
            }else{Toast.makeText(this,"퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.",
                Toast.LENGTH_LONG).show()
                finish()}
        }
    }

        private fun showDialogForLocationServiceSetting(){
        getGPSPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){
            result ->
            Log.d("LOG : :: : : : : ",result.resultCode.toString())
                if(isLocationServicesAvailable()){
                    isRunTimePermissionsGranted()
                }else{Toast.makeText(this,"위치 서비스를 사용할 수 없습니다.",
                    Toast.LENGTH_LONG).show()
                    finish()
                }

        }
        val builder : AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("위치 서비스 비활성화")
        builder.setMessage("위치 서비스가 꺼져있습니다. 설정해야 앱을 사용할 수 있습니다.")
        builder.setCancelable(true)
        builder.setPositiveButton("설정",DialogInterface.OnClickListener{ dialogInterface, i ->
            val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            getGPSPermissionLauncher.launch(callGPSSettingIntent)
        } )
        builder.setNegativeButton("취소",DialogInterface.OnClickListener{ dialogInterface, i ->
            dialogInterface.cancel()
            Toast.makeText(this,"위치 서비스를 사용할 수 없습니다.",
                Toast.LENGTH_LONG).show()
            finish()
        })
        builder.create().show()
    }

}
