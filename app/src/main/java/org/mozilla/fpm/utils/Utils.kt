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