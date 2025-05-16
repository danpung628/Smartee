// SmarteeApplication.kt
package com.example.smartee

import android.app.Application
import com.google.android.libraries.places.api.Places
import com.google.firebase.FirebaseApp

class SmarteeApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Firebase 초기화
        FirebaseApp.initializeApp(this)

        // Places API 초기화 - google-services.json에서 자동으로 생성된 API 키 사용
        val apiKey = getString(R.string.maps_api_key)
        Places.initialize(applicationContext, apiKey)
    }
}