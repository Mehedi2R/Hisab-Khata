package com.hisabkhata.app

import android.app.Application

class HisabKhataApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Language Manager
        com.hisabkhata.app.data.local.AppLanguageManager.init(this)
    }
}
