package com.matthiaslapierre.spaceshooter

import android.app.Application

class App: Application() {

    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        // Load resources once to optimize performances.
        appContainer = AppContainer(applicationContext)
        appContainer.drawables.load()
        appContainer.typefaceHelper.load()
        appContainer.soundEngine.load()
    }

}