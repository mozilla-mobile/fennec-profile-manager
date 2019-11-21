/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fennecprofilebackuphelper

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.mozilla.fennecprofilebackuphelper.FileUtils.makeFirefoxPackageContext
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        @Suppress("MagicNumber")
        fab.setOnClickListener {
            val fennecContext = makeFirefoxPackageContext(applicationContext)

            if (fennecContext == null) {
                description.text = getString(R.string.poc_results_description_package_not_found)
                description.textSize = 16f
            } else {
                val fennecInternalStorage = File(fennecContext.filesDir.parent!!)
                description.text = getString(R.string.poc_results_description)
                description.textSize = 16f

                with(fennecInternalStorage.list()) {
                    when (this) {
                        null -> {

                        }
                        else -> {
                            FileUtils.zip(this, "tmp_" + System.currentTimeMillis())
                            results.text = this.joinToString("\n")
                        }
                    }
                }
            }
        }
    }
}
