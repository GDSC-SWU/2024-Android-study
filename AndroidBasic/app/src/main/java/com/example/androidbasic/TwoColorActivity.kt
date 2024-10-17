package com.example.androidbasic

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class TwoColorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_two_color)

        settingButtons()
    }
    fun settingButtons(){
        val button_red=findViewById<Button>(R.id.button_red_fragment)
        val button_blue=findViewById<Button>(R.id.button_blue_fragment)

        button_red.setOnClickListener{
            val fragmentTransaction=supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.frame_layout,RedFragment())
            fragmentTransaction.commit()
        }

        button_blue.setOnClickListener{
            val fragmentTransaction=supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.frame_layout,BlueFragment())
            fragmentTransaction.commit()
        }
    }
}