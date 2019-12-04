package org.mozilla.fpm.presentation

import android.app.Application
import org.mozilla.fpm.data.PrefsManager

class FpmApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        PrefsManager.init(this@FpmApplication)
    }
}
