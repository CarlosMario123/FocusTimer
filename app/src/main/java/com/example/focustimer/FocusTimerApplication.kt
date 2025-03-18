package com.example.focustimer

import android.app.Application
import com.example.focustimer.core.utils.NotificationHelper

class FocusTimerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.initialize(this)
    }
}