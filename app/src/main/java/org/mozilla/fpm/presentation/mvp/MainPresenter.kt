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

    override fun applyBackup(backupName: String) {
        GlobalScope.launch(Dispatchers.Main) {
            view?.showLoading()
            withContext(Dispatchers.IO) { backupsRepository.deploy(backupName) }
            view?.hideLoading()
            view?.onBackupApplied()
        }
    }

    override fun createBackup(backupName: String) {
        var actualBackupName = backupName
        GlobalScope.launch(Dispatchers.Main) {
            view?.showLoading()
            val backup = withContext(Dispatchers.IO) {
                actualBackupName = getUniqueBackupFilename(backupName)
                backupsRepository.create(actualBackupName)
                backupsRepository.get("$actualBackupName.$MIME_TYPE")
            }

            view?.let {
                it.hideLoading()
                if (backup != null) {
                    it.onBackupCreated(backup)
                }
                if (backupName != actualBackupName) {
                    it.showBackupCreatedWithDifferentNameMessage(actualBackupName)
                }
            }
        }
    }

    override fun deleteBackup(backupName: String) {
        GlobalScope.launch(Dispatchers.Default) {
            backupsRepository.remove(backupName)
        }
    }

    override fun renameBackup(backup: Backup, newBackupName: String, position: Int) {
        val mimeSuffix = ".$MIME_TYPE"
        var actualBackupName = newBackupName
        view?.showLoading()
        GlobalScope.launch(Dispatchers.Main) {
            val renamedBackup = withContext(Dispatchers.IO) {
                actualBackupName = getUniqueBackupFilename(newBackupName).plus(".$MIME_TYPE")
                backupsRepository.update(backup, actualBackupName)
                backupsRepository.get(actualBackupName)
            }

            view?.let {
                it.hideLoading()
                if (renamedBackup != null) {
                    it.onBackupRenamed(renamedBackup, position)
                }
                if (newBackupName.plus(mimeSuffix) != actualBackupName) {
                    it.showBackupCreatedWithDifferentNameMessage(actualBackupName.substringBefore(mimeSuffix))
                }
            }
        }
    }

    override fun attachView(view: MainContract.View) {
        this.view = view
    }

    override fun detachView() {
        this.view = null
    }

    /**
     * Check if a backup with the *desiredName* does not already exists
     *  and add an increment suffix if needed to get a new unique name starting from the *desiredName*.
     *
     *  @param desiredName the name to check if a backup exists with
     *  @return <code>String</code>
     *  - *desiredName* if a backup with this name does not already exists
     *  - *desiredName* plus a digit suffix - for a new unique backup name
     */
    private suspend fun getUniqueBackupFilename(desiredName: String): String {
        return when (backupsRepository.get("$desiredName.$MIME_TYPE") == null) {
            true -> desiredName
            false -> getFirstNonDuplicateFilename(desiredName, backupsRepository.getAll())
        }
    }

    private suspend fun getFirstNonDuplicateFilename(
        duplicatedFilename: String,
        allBackups: List<Backup>
    ): String {
        // The desired filename could already follow our suffix system and end in a digit in parentheses.
        // If so, continue incrementing from there until we have a unique suffix
        // Or try to obtain a unique suffix starting with the digit 2.
        val dupeSuffix = Regex(".*?\\((\\d+)\\)+$")
        val matchResult = dupeSuffix.matchEntire(duplicatedFilename)
        return when (matchResult != null) {
            true -> {
                val currentDupeSuffix =
                    matchResult.groups[matchResult.groups.size - 1]!!.value.toInt()
                val uniqueIncrementedSuffix = getUniqueIncrementalSuffix(
                    duplicatedFilename.substringBeforeLast("($currentDupeSuffix)"),
                    currentDupeSuffix + 1,
                    allBackups
                )
                duplicatedFilename.replaceAfterLast("(", "$uniqueIncrementedSuffix)")
            }
            false -> {
                val uniqueIncrementedSuffix = getUniqueIncrementalSuffix(
                    duplicatedFilename, 2, allBackups
                )
                duplicatedFilename.plus("($uniqueIncrementedSuffix)")
            }
        }
    }

    private suspend fun getUniqueIncrementalSuffix(
        nonSufixedFilename: String,
        suffixValue: Int,
        allBackups: List<Backup>
    ): Int {
        // Validate that the starting value is not the incremental suffix of another existing backup
        // If it is, increment the suffix value until we have a unique one
        return when (allBackups.firstOrNull { backup ->
            // Backups names will come with our mime type. Don't care about that.
            with(backup.name.substringBefore(".$MIME_TYPE", backup.name)) {
                startsWith(nonSufixedFilename) && endsWith("($suffixValue)")
            }
        } == null) // there is no previous backup ending in suffixValue
        {
            true -> suffixValue
            false -> getUniqueIncrementalSuffix(nonSufixedFilename, suffixValue + 1, allBackups)
        }
    }
}
