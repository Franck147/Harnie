package com.harnie.app

import android.app.Application
import com.harnie.app.core.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class HarnieApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@HarnieApp)
            modules(appModule)
        }
    }
}