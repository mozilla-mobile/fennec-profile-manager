/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fpm.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.ContextThemeWrapper
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.mozilla.fpm.BuildConfig
import org.mozilla.fpm.R

class PermissionUtils {
    companion object {
        private val TAG = PermissionUtils::class.java.canonicalName

        /**
         * In order to allow for easier debugging and ease of access to backup files,
         * debug variants will store the backup archives in external storage.
         */
        @SuppressWarnings("ReturnCount")
        fun checkStoragePermission(activity: Activity, permissionsRequestCode: Int): Boolean {
            if (!BuildConfig.DEBUG) {
                return true
            }

            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    permissionsRequestCode
                )
                return false
            }

            return true
        }

        @SuppressWarnings("all")
        fun validateStoragePermissionOrShowRationale(
            activity: Activity,
            permissions: Array<out String>,
            grantResults: IntArray
        ): Boolean {
            if (permissions.isEmpty()) {
                return false
            }

            var allPermissionsGranted = true
            if (grantResults.isNotEmpty()) {
                for (grantResult in grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        allPermissionsGranted = false
                        break
                    }
                }
            }

            if (!allPermissionsGranted) {
                var somePermissionsForeverDenied = false
                for (permission in permissions) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                        // denied
                        Log.i(TAG, "Permission denied")
                    } else {
                        if (ActivityCompat.checkSelfPermission(
                                activity, permission
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            // allowed
                            Log.i(TAG, "Permission allowed")
                        } else {
                            // set to never ask again
                            Log.e(TAG, "Permision set to never ask again")
                            somePermissionsForeverDenied = true
                        }
                    }
                }

                if (somePermissionsForeverDenied) {
                    showRationaleDialog(activity)
                }

                return false
            }

            return true
        }

        fun showRationaleDialog(context: Context) {
            val alertDialogBuilder = AlertDialog.Builder(ContextThemeWrapper(context, R.style.AlertDialogTheme))
            alertDialogBuilder
                .setTitle(context.getString(R.string.permission_required))
                .setMessage(context.getString(R.string.storage_permission_denied))
                .setPositiveButton(context.getString(R.string.settings)) { dialog, which ->
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
                .setNegativeButton(
                    context.getString(R.string.cancel)
                ) { _, _ -> }
                .setCancelable(false)
                .create()
                .show()
        }
    }
}
