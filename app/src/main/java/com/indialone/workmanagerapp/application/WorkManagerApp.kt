package com.indialone.workmanagerapp.application

import android.app.Application
import android.util.Log.*
import androidx.work.Configuration
import androidx.work.WorkManager

class WorkManagerApp : Application(), Configuration.Provider {
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(DEBUG)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        WorkManager.initialize(this, workManagerConfiguration)
    }

}