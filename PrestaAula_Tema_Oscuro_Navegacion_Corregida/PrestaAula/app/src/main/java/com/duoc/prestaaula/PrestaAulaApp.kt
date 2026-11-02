package com.duoc.prestaaula

import android.app.Application
import com.duoc.prestaaula.di.AppContainer
import com.duoc.prestaaula.nativeaccess.NotificationHelper

class PrestaAulaApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationHelper.createChannel(this)
        container.seedIfEmpty()
    }
}
