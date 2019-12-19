/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fpm.presentation.mvp

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mozilla.fpm.data.BackupRepository
import org.mozilla.fpm.data.BackupRepositoryImpl
import org.mozilla.fpm.data.BackupRepositoryImpl.MIME_TYPE
import org.mozilla.fpm.models.Backup

class MainPresenter : MainContract.Presenter {

    private var view: MainContract.View? = null

    private val backupsRepository: BackupRepository = BackupRepositoryImpl

    override fun getBackups() {
        GlobalScope.launch(Dispatchers.Main) {
            view?.showLoading()
            val backups = withContext(Dispatchers.Default) { backupsRepository.getAll() }
            view?.hideLoading()
            view?.onBackupsLoaded(backups)
        }
    }

    override fun importBackup(fileUri: Uri, fileName: String) {
        GlobalScope.launch(Dispatchers.Main) {
            view?.showLoading()
            withContext(Dispatchers.IO) { backupsRepository.import(fileUri, fileName) }
            val importedBackup = withContext(Dispatchers.Default) { backupsRepository.get(fileName) }
            view?.hideLoading()
            view?.onBackupImported(importedBackup)
        }
    }

    override fun applyBackup(backupName: String) {
        GlobalScope.launch(Dispatchers.Main) {
            view?.showLoading()
            withContext(Dispatchers.IO) { backupsRepository.deploy(backupName) }
            view?.hideLoading()
            view?.onBackupApplied()
        }
    }

    override fun createBackup(backupName: String) {
        GlobalScope.launch(Dispatchers.Main) {
            view?.showLoading()
            withContext(Dispatchers.IO) { backupsRepository.create(backupName) }
            val backup = withContext(Dispatchers.Default) { backupsRepository.get("$backupName.$MIME_TYPE") }
            view?.hideLoading()
            view?.onBackupCreated(backup)
        }
    }

    override fun deleteBackup(backupName: String) {
        GlobalScope.launch(Dispatchers.Default) {
            backupsRepository.remove(backupName)
        }
    }

    override fun renameBackup(backup: Backup, newBackupName: String) {
        GlobalScope.launch(Dispatchers.Default) {
            backupsRepository.update(backup, newBackupName)
        }
    }

    override fun attachView(view: MainContract.View) {
        this.view = view
    }

    override fun detachView() {
        this.view = null
    }
}
