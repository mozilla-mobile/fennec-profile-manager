/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fpm.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

@SuppressLint("StaticFieldLeak")
object PrefsManager {
    private const val APP_PREFS = "fpm_prefs"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE)
    }

    fun checkFirstRun(): Boolean {
        return prefs.getBoolean("is_firstrun", false)
    }

    fun setFirstRunComplete() {
        prefs.edit().putBoolean("is_firstrun", true).apply()
    }
}
