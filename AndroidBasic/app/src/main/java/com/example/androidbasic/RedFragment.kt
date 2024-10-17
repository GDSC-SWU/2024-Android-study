package com.example.androidbasic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class RedFragment:Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       return inflater.inflate(R.layout.fragment_red,container,false)
        //프레임 레이아웃이 컨테이너 역할, 프래그먼트를 자동으로 추가할 지에 대해 false
    }
}