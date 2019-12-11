/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fpm.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import org.mozilla.fpm.BuildConfig
import java.text.SimpleDateFormat
import java.util.Date

class Utils {

    companion object {
        private val TAG = Utils::class.java.canonicalName
        private const val BACKUP_STORAGE_RELATIVE_PATH = "/backups"
        private const val CRYPTO_STORAGE_RELATIVE_PATH = "/crypt"

        fun showMessage(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }

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

        fun getCryptedStoragePath(ctx: Context): String {
            return "${ctx.applicationInfo.dataDir}/$CRYPTO_STORAGE_RELATIVE_PATH"
        }

        /**
         * Debug variants use the external storage in order to allow for better debugging and
         * ease of access to the Backups.
         */
        fun getBackupStoragePath(ctx: Context): String {
            return if (BuildConfig.DEBUG) {
                "${ctx.getExternalFilesDir(null)?.absolutePath}$BACKUP_STORAGE_RELATIVE_PATH"
            } else {
                "${ctx.applicationInfo.dataDir}$BACKUP_STORAGE_RELATIVE_PATH"
            }
        }

        fun getFileNameFromUri(context: Context, uri: Uri?): String? {
            var fileName: String? = null
            if (uri != null && uri.scheme.equals("content")) {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor.use {
                    if (it != null && it.moveToFirst()) {
                        fileName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    }
                }
            }
            if (fileName.isNullOrEmpty()) {
                fileName = uri?.path
                val cut = fileName?.lastIndexOf('/')
                if (cut != -1) {
                    fileName = cut?.plus(1)?.let { fileName?.substring(it) }
                }
            }
            return fileName
        }
    }
}
