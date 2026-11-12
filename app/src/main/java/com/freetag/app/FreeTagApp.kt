package com.freetag.app

import android.app.Application
import com.freetag.app.data.PreferenceManager

/**
 * Global Application instance for FreeTag.
 * Initializes core singleton services like PreferenceManager.
 */
class FreeTagApp : Application() {

    lateinit var preferenceManager: PreferenceManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        preferenceManager = PreferenceManager(this)
    }

    companion object {
        lateinit var instance: FreeTagApp
            private set
    }
}
