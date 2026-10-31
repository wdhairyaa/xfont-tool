package com.xfont.app

import android.app.Application
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class XFontApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Chaquopy in background IO thread for instantaneous app startup
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!Python.isStarted()) {
                    Python.start(AndroidPlatform(this@XFontApplication))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
