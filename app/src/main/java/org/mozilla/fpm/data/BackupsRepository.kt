/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fpm.data

import org.mozilla.fpm.models.Backup

class BackupsRepository {

    fun getSampleData(): List<Backup> {
        return listOf(Backup(0, "Backup 1", "15 nov 2019"),
            Backup(1, "Backup 2", "17 nov 2019")
        )
    }
}