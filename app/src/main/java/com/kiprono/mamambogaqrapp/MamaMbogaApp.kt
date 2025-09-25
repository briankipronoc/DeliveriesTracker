package com.kiprono.mamambogaqrapp

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

class MamaMbogaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
    }
}