package com.withoutsummer.misemise

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
import androidx.activity.result.contract.ActivityResultContract
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
import com.withoutsummer.misemise.databinding.ActivityMainBinding
import com.withoutsummer.misemise.retrofit.AirQualityResponse
import com.withoutsummer.misemise.retrofit.AirQualityService
import com.withoutsummer.misemise.retrofit.RetrofitConnection
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.Locale
import com.jakewharton.threetenabp.AndroidThreeTen // 추가
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {

    var mInterstitialAd : InterstitialAd? = null

    lateinit var binding : ActivityMainBinding

    //사용자의 위치를 가져오기 위해 LocationProvider 클래스의 인스턴스를 생성
    lateinit var locationProvider: LocationProvider

    private val PERMISSIONS_REQUEST_CODE = 100

    //ver2 위도, 경로를 저장하기 위한 클래스 객체 변수 생성
    var latitude:Double? = 0.0
    var longitude:Double? = 0.0

    //앱이 요청할 런타임 권한 배열
    val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    //위치 서비스(GPS) 설정 화면으로 이동하기 위해 사용하는 ActivityResultLauncher 객체
    lateinit var getGPSPermissionLauncher : ActivityResultLauncher<Intent>

    //ver2.맵액티비티가 시작되고 기능이 수행된 후 다시 메인 액티비티로 돌아왔을때 onActivityResult 함수가 실행됨
    val startMapActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
        object : ActivityResultCallback<ActivityResult>{
            //지도 액티비티에서 사용자가 위치를 선택한 후 반환된 데이터를 처리
            override fun onActivityResult(result: ActivityResult) {
                //resultCode가 OK일 경우 위도 경도 정보 조회 후 클래스 객체 변수에 넣어준다
                if(result?.resultCode?:0 == Activity.RESULT_OK){
                    latitude = result?.data?.getDoubleExtra("latitude",0.0) ?: 0.0
                    longitude = result?.data?.getDoubleExtra("longitude",0.0) ?: 0.0
                    //선택된 위치를 기반 UI를 갱신
                    updateUI()
                }
            }
        }
        )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //ThreeTenABP 라이브러리를 초기화하여 날짜 및 시간 작업에 사용
        AndroidThreeTen.init(this)

        //위치 권한 및 GPS 활성화 여부를 확인
        checkAllPermission()
        //현재 위치 및 미세먼지 데이터를 기반으로 UI를 업데이트
        updateUI()
        //새로 고침 버튼을 설정하여 클릭 시 데이터를 갱신
        setRefreshButton()

        //ver.2 플로팅 액션 버튼(FAB)을 설정하여 클릭 시 지도 액티비티로 이동
        setFab()
        //배너 광고 불러오는 함수
        setBannerAds()
    }

    //전면 광고는 재사용이 불가능하기 때문에 초기화해야됨.
    override fun onResume() {
        super.onResume()
        setInterstitialAds()
    }

    private fun setInterstitialAds(){
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this,"/21775744923/example/interstitial",adRequest,
            object :InterstitialAdLoadCallback(){
                override fun onAdLoaded(p0: InterstitialAd) {
                    super.onAdLoaded(p0)
                    Log.d("Ads Log","전면 광고가 로드 되었습니다.")

                    mInterstitialAd=p0
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    Log.d("Ads Log","전면 광고가 로드 실패 되었습니다.")
                }
        })
    }

    private fun setBannerAds(){
        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()
        binding.adsBanner.loadAd(adRequest)
        binding.adsBanner.adListener = object :AdListener(){
            override fun onAdLoaded() {
                super.onAdLoaded()
                Log.d("Ads Log","배너 광고가 로드 되었습니다.")
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                Log.d("Ads Log","배너 광고가 로드 실패되었습니다.")
            }

            override fun onAdClicked() {
                super.onAdClicked()
                Log.d("Ads Log","배너 광고가 클릭 되었습니다.")
            }
        }
    }

    private fun updateUI(){

        //현재 위치를 가져오기 위해 LocationProvider 클래스 인스턴스를 생성
        locationProvider=LocationProvider(this@MainActivity)

        //ver2.위도와 경도가 없을 경우 LocationProvider를 통해 위도, 경로를 받아온다
        if(latitude == 0.0 && longitude == 0.0){
            latitude = locationProvider.getLocationLatitude()
            longitude = locationProvider.getLocationLongitude()
        }

        if(latitude != null && longitude != null){

            //1.현재 위치 가져와서 UI 업데이트(지오 코딩)
            //getCurrentAddress를 호출하여 위도와 경도를 주소로 변환.
            val address = getCurrentAddress(latitude!!,longitude!!)

            //주소를 텍스트뷰에 표시
            address?.let {
                binding.tvLocationTitle.text="${it.thoroughfare}"
                binding.tvLocationSubtitle.text="${it.countryName} ${it.adminArea}"

            }

            //2.미세먼지 농도 가져와서 UI 업데이트
            //getAirQualityData를 호출하여 미세먼지 데이터를 요청하고 UI를 갱신.
            getAirQualityData(latitude!!,longitude!!)

        }else{
            //위도 및 경도가 null인 경우 오류 메시지를 표시.
            Toast.makeText(this,"위도 및 경도 정보를 가져올 수 없습니다.",Toast.LENGTH_SHORT).show()
        }
    }

    private fun getAirQualityData(latitude : Double,longitude :Double){
        //RetrofitConnection을 통해 AirQualityService 인터페이스 생성.
        var retrofitAPI = RetrofitConnection.getInstance().create(
            AirQualityService::class.java
        )

        //API 키와 위도 및 경도를 전달하여 미세먼지 데이터를 요청.
        retrofitAPI.getAirQualityData(
            latitude.toString(),
            longitude.toString(),
            "${BuildConfig.API_KEY}"
        ).enqueue(object :Callback<AirQualityResponse>{
            override fun onResponse(
                call: Call<AirQualityResponse>,
                response: Response<AirQualityResponse>
            ) {
                if(response.isSuccessful){
                    Toast.makeText(this@MainActivity,"최신 데이터 업데이트 완료!",Toast.LENGTH_SHORT).show()
                    response.body()?.let{updateAirUI(it)}


                }else{
                    Toast.makeText(this@MainActivity,"데이터를 가져오는데 실패했습니다.",Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AirQualityResponse>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(this@MainActivity,"데이터를 가져오는데 실패했습니다.",Toast.LENGTH_SHORT).show()
            }
        })
    }

    //미세먼지 농도(aqius), 측정 시간, 상태(좋음/보통/나쁨/매우 나쁨)를 UI에 표시.
    private fun updateAirUI(airQualityData : AirQualityResponse){
        val pollutionData = airQualityData.data.current.pollution

        //수치를 지정
        binding.tvCount.text=pollutionData.aqius.toString()

        // 측정된 날짜를 ThreeTenABP로 변환
        val dateTime = ZonedDateTime.parse(pollutionData.ts) // 기존 날짜 문자열 파싱
            .withZoneSameInstant(ZoneId.of("Asia/Seoul"))    // 서울 시간대로 변환
            .toLocalDateTime()                               // 로컬 시간으로 변환

        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm") // 포맷 지정

        // 포맷 적용 후 텍스트 설정
        binding.tvCheckTime.text = dateTime.format(dateFormatter)

        when(pollutionData.aqius){
            in 0 .. 50->{
                binding.tvTitle.text="좋음"
                binding.imgBg.setImageResource(R.drawable.bg_good)
            }

            in 51 .. 150->{
                binding.tvTitle.text="보통"
                binding.imgBg.setImageResource(R.drawable.bg_soso)
            }

            in 151 .. 200->{
                binding.tvTitle.text="나쁨"
                binding.imgBg.setImageResource(R.drawable.bg_bad)
            }

            else->{
                binding.tvTitle.text="매우 나쁨"
                binding.imgBg.setImageResource(R.drawable.bg_worst)
            }
        }
    }

    //새로 고침 버튼 클릭 시 updateUI를 호출하여 데이터를 갱신.
    private fun setRefreshButton(){
        binding.btnRefresh.setOnClickListener { updateUI() }
    }

    //ver2
    private fun setFab(){
        //fab 버튼을 눌렀을 경우,
        binding.fab.setOnClickListener {

            if(mInterstitialAd!=null){
                mInterstitialAd!!.fullScreenContentCallback = object  : FullScreenContentCallback(){
                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()

                        Log.d("Ads Log","전면 광고가 닫혔습니다.")
                        val intent = Intent(this@MainActivity,MapActivity::class.java)
                        //액티비티로 이동시 위도, 경도 값을 넘겨줌. -> 맵 액티비티에서 지도를 보여줄 것
                        intent.putExtra("currentLat",latitude)
                        intent.putExtra("currentLng",longitude)
                        //launcher
                        startMapActivityResult.launch(intent)
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        super.onAdFailedToShowFullScreenContent(p0)
                        Log.d("Ads Log","전면 광고가 열기 실패했습니다.")
                    }

                    override fun onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent()
                        Log.d("Ads Log","전면 광고가 열기 성공했습니다.")
                        mInterstitialAd = null
                    }
                }
                mInterstitialAd!!.show(this@MainActivity)
            }else{
                Log.d("Ads Log","전면 광고가 로딩이 안되었습니다.")
                Toast.makeText(this,"잠시 후 다시 시도해주세요.",Toast.LENGTH_LONG).show()
            }
        }
    }

    //getCurrentAddress 함수는 사용자의 위도(latitude)와 경도(longitude)를 기반으로 주소를 가져옴 -> Geocoder API를 사용
    private fun getCurrentAddress(latitude:Double,longitude : Double) : Address? {

        //Geocoder 객체를 생성하여 위도와 경도를 한국어 주소로 변환할 준비
        val geoCoder = Geocoder(this, Locale.KOREA)
        val addresses : List<Address>?

        addresses = try{
            //위도와 경도를 기반으로 주소를 반환.
            //최대 7개의 주소를 반환하도록 설정.
            geoCoder.getFromLocation(latitude,longitude,7)

        }catch (ioExeption : IOException){
            Toast.makeText(this,"지오코더 서비스 이용 불가합니다.",Toast.LENGTH_SHORT).show()
            return null
        }catch (illegalArgumentExeption : IllegalArgumentException){
            Toast.makeText(this,"잘못된 위도, 경도 입니다.",Toast.LENGTH_SHORT).show()
            return null
        }

        if(addresses == null|| addresses.size == 0){
            Toast.makeText(this,"주소가 발견되지 않았습니다.",Toast.LENGTH_SHORT).show()
            return null
        }

        //주소 목록 중 첫 번째 주소를 반환
        return addresses[0]
    }

    //위치 서비스(GPS 또는 네트워크 위치)가 활성화되어 있는지 확인. onCreate에서 호출
    private fun checkAllPermission(){
        //활성화되지 않았으면 설정 화면으로 이동하도록 다이얼로그를 표시.
        if(!isLocationServicesAvailable()) {
            showDialogForLocationServiceSetting()

        }else{
            //위치 서비스가 활성화되어 있으면 위치 권한(FINE/COARSE)을 확인하고 요청.
            isRunTimePermissionGranted()
        }
    }

    //LocationManager를 사용하여 GPS 및 네트워크 제공자가 활성 상태인지 반환.
    //checkAllPermission에서 호출, howDialogForLocationServiceSetting에서 다시 호출
    private fun isLocationServicesAvailable() : Boolean{
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        //GPS(GPS_PROVIDER) 또는 네트워크(NETWORK_PROVIDER)가 활성화되었는지 확인.
        return(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)|| locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
    }

    //위치 권한이 부여되었는지 확인. 권한이 없다면 런타임 권한 요청. checkAllPermission에서 호출
    private fun isRunTimePermissionGranted(){
        //ACCESS_FINE_LOCATION 및 ACCESS_COARSE_LOCATION 권한 상태를 확인.
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(this@MainActivity,Manifest.permission.ACCESS_FINE_LOCATION)
        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this@MainActivity,Manifest.permission.ACCESS_COARSE_LOCATION)

        //권한이 없으면 ActivityCompat.requestPermissions를 호출하여 권한 요청 다이얼로그 표시.
        if(hasFineLocationPermission != PackageManager.PERMISSION_GRANTED || hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this@MainActivity,REQUIRED_PERMISSIONS,PERMISSIONS_REQUEST_CODE)
        }
    }

    //사용자가 권한을 허용했는지 여부에 따라 동작 결정.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //PERMISSIONS_REQUEST_CODE와 동일한 요청인지 확인.
        //grantResults 배열의 모든 값이 PERMISSION_GRANTED인지 확인
        if(requestCode == PERMISSIONS_REQUEST_CODE && grantResults.size == REQUIRED_PERMISSIONS.size){
            var checkResult = true

            for(result in grantResults){
                if(result != PackageManager.PERMISSION_GRANTED){
                    checkResult=false
                    break;
                }
            }

            //모든 권한 허용시, updateUI() 호출하여 위치 데이터를 가져와 UI를 갱신.
            if(checkResult){
                //위치값을 가져올 수 있게 됨.
                updateUI()
            }else{
                //권한 거부시, 토스트 메시지 표시 후 앱 종료.
                Toast.makeText(this@MainActivity,"퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요",Toast.LENGTH_SHORT).show()
                finish()  //액티비티 종료
            }
        }
    }


    //위치 서비스가 꺼져 있을 때 설정 화면으로 이동하도록 사용자에게 다이얼로그를 표시.
    private fun showDialogForLocationServiceSetting(){
        //설정 화면에서 돌아왔을 때 위치 서비스 상태를 재확인.

        //특정 작업(예: 설정 화면 진입)의 결과를 처리하기 위해 Activity Result API를 등록
        getGPSPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){ //설정 화면에서 돌아왔을 때 실행
            result ->
            //사용자가 설정 화면에서 작업을 완료한 경우
            if (result.resultCode == Activity.RESULT_OK){
                //위치 서비스가 활성화되었는지 확인
                if(isLocationServicesAvailable()){
                    //위치 권한을 다시 확인.
                    isRunTimePermissionGranted()
                }else{
                    Toast.makeText(this@MainActivity,"퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요",Toast.LENGTH_SHORT).show()
                    finish()  //액티비티 종료
                }
            }
        }

        //알림 다이얼로그 생성
        val builder : AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("위치 서비스 비활성화")
        builder.setMessage("위치 서비스가 꺼져있습니다. 설정해야 앱을 사용할 수 있습니다.")
        builder.setCancelable(true)
        builder.setPositiveButton("설정",DialogInterface.OnClickListener{dialogInterface, i ->
            //사용자를 위치 설정 화면으로 이동시킵니다.
            val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            //위에서 등록한 getGPSPermissionLauncher를 사용해 설정 화면을 실행.
            getGPSPermissionLauncher.launch(callGPSSettingIntent)
        })
        builder.setNegativeButton("취소",DialogInterface.OnClickListener{dialogInterface, i ->
            dialogInterface.cancel()
            Toast.makeText(this@MainActivity,"위치 서비스를 사용할 수 없습니다.",Toast.LENGTH_SHORT).show()
            finish()  //액티비티 종료
        })
        //AlertDialog 객체를 생성.
        builder.create().show()
    }
}