/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fpm.presentation

import android.app.Application
import org.mozilla.fpm.data.PrefsManager

class FpmApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        PrefsManager.init(this@FpmApplication)
    }
}
