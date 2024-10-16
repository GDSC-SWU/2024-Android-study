package com.example.androidbasic

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.findViewTreeViewModelStoreOwner

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        settingButton()
    }

    //버튼을 눌렀을 때 서브 액티비티로 움직일 수 있게끔 로직 작성
    fun settingButton(){
        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener{ //버튼을 눌렀을 때 어떤 작용을 할 것 인지 //서브 액티비티로 이동
            val intent = Intent(this, Subactivity::class.java) //this->서브액티비티로 이동
            startActivity(intent)
        }
    }
}