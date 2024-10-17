package com.example.androidbasic

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        settingButton()
    }
    fun settingButton(){
        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener{
            val Intent = Intent(this, SubActivity::class.java)
            startActivity(Intent)
        }
    }
}