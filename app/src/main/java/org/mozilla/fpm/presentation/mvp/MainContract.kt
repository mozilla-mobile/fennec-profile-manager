/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fpm.presentation.mvp

import org.mozilla.fpm.models.Backup

interface MainContract {
    interface View {
        fun onBackupsLoaded(data: List<Backup>)

        fun onBackupCreated(backup: Backup)

        fun onBackupApplied()

        fun showFirstrun()

        fun hideFirstrun()

        fun showLoading()

        fun hideLoading()
    }

    interface Presenter : BasePresenter<View> {
        fun getBackups()

        fun importBackup()

        fun createBackup(backupName: String)

        fun applyBackup(backupName: String)

        fun renameBackup(backupName: String)
    }
}
