/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fpm.presentation

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.mozilla.fpm.R
import java.io.File

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.canonicalName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        title = getString(R.string.app_name)

        try {
        val fennecContext = createPackageContext("org.mozilla.firefox", Context.CONTEXT_RESTRICTED)
        val fennecInternalStorage = File(fennecContext.filesDir.parent!!)

            @Suppress("MagicNumber")
            add_fab.setOnClickListener {
                //description.text = getString(R.string.poc_results_description)
                //description.textSize = 16f
                //results.text = fennecInternalStorage.list()!!.joinToString("\n")
            }
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }
}
