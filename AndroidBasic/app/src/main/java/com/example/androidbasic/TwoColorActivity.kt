package com.example.androidbasic

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class TwoColorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_two_color)
    }

    fun settingButtons(){
        val button_red = findViewById<Button>(R.id.button_red_fragment)
        val button_blue = findViewById<Button>(R.id.button_blue_fragment)

        button_red.setOnClickListener {

            val fragmenTransaction = supportFragmentManager.beginTransaction()
            fragmenTransaction.replace(R.id.frame_layout,RedFragment())
            fragmenTransaction.commit()
        }

        button_blue.setOnClickListener {
            val fragmenTransaction = supportFragmentManager.beginTransaction()
            fragmenTransaction.replace(R.id.frame_layout,BlueFragment())
            fragmenTransaction.commit()

        }
    }
}