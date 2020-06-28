package com.bigbang.myplacecompass

import android.app.Application
import com.bridge.androidtechnicaltest.di.*
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module

class App : Application(){


    private val appComponent : MutableList<Module> = mutableListOf(networkModule, repositoriesModule, viewModelModule)

    override fun onCreate() {
        super.onCreate()
        // start Koin!
        startKoin {
            // declare used Android context
            androidContext(applicationContext)
            // declare modules
            modules(appComponent)
        }
    }

}