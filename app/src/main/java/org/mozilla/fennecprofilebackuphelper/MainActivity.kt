/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fennecprofilebackuphelper

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val fennecContext = createPackageContext("org.mozilla.firefox", Context.CONTEXT_RESTRICTED)
        val fennecInternalStorage = File(fennecContext.filesDir.parent)

        fab.setOnClickListener {
            description.text = getString(R.string.poc_results_description)
            description.textSize = 16f
            results.text = fennecInternalStorage.list().joinToString("\n")
            fab.hide()
        }
    }
}
