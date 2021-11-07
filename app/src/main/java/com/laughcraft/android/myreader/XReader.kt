package com.laughcraft.android.myreader

import android.app.Application
import android.os.StrictMode
import androidx.appcompat.app.AppCompatDelegate
import com.laughcraft.android.myreader.di.DaggerMainComponent
import com.laughcraft.android.myreader.di.MainComponent
import android.os.Build
import android.util.Log
import java.lang.Exception
import java.lang.reflect.Method


class XReader: Application() {

    companion object {
        lateinit var dagger: MainComponent
        lateinit var app: Application
    }

    override fun onCreate() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate()
        dagger = DaggerMainComponent.create()

        if (Build.VERSION.SDK_INT >= 24) {
            try {
                val m: Method = StrictMode::class.java.getMethod("disableDeathOnFileUriExposure")
                m.invoke(null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        getExternalFilesDirs(null).forEach {
            Log.i("XReader", "External Dirs: ${it.absolutePath}")
        }
        app = this
    }
}