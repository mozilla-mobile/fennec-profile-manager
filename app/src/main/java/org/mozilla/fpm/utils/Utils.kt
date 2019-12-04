/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fpm.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import org.mozilla.fpm.BuildConfig
import java.text.SimpleDateFormat
import java.util.Date

class Utils {

    companion object {
        private val TAG = Utils::class.java.canonicalName

        fun makeFirefoxPackageContext(context: Context): Context? {
            try {
                return context.createPackageContext(
                    BuildConfig.FIREFOX_PACKAGE_NAME,
                    Context.CONTEXT_RESTRICTED
                )
            } catch (e: PackageManager.NameNotFoundException) {
                Log.w(TAG, "No Firefox app installed. Please install one!")
            }

            return null
        }

        @SuppressLint("SimpleDateFormat")
        fun getFormattedDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("HH:mm - MM/dd/yyyy")
            val dateObj = Date(timestamp)
            return sdf.format(dateObj)
        }
    }
}
