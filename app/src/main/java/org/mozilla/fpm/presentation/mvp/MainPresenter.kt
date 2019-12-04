/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fpm.presentation.mvp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mozilla.fpm.data.BackupRepository
import org.mozilla.fpm.data.BackupRepositoryImpl

class MainPresenter : MainContract.Presenter {

    private var view: MainContract.View? = null

    private val backupsRepository: BackupRepository = BackupRepositoryImpl

    override fun getBackups() {
        GlobalScope.launch(Dispatchers.Main) {
            val backups = withContext(Dispatchers.Default) { backupsRepository.getAll() }
            view?.onBackupsLoaded(backups)
        }
    }

    override fun importBackup() {
        // todo
    }

    override fun createBackup() {
        // todo
    }

    override fun attachView(view: MainContract.View) {
        this.view = view
    }

    override fun detachView() {
        this.view = null
    }
}
