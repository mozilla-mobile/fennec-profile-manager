package org.mozilla.fpm.data

import org.mozilla.fpm.models.Backup
import org.mozilla.fpm.utils.FileUtils

object BackupRepositoryImpl: BackupRepository {
    private const val BACKUP_STORAGE_RELATIVE_PATH = "/backups"

    override fun add(t: Backup) {
        FileUtils.createFileNameAtPath(BACKUP_STORAGE_RELATIVE_PATH, t.name)
    }

    override fun remove(k: Int) {
        var index = 0

        FileUtils.listFilesAtPath(BACKUP_STORAGE_RELATIVE_PATH)!!.forEach {
            if (index == k) {
                FileUtils.removeFileAtPath("$BACKUP_STORAGE_RELATIVE_PATH/$it")
            }
            index++
        }
    }

    override fun update(t: Backup, k: Int) {
        var index = 0

        FileUtils.listFilesAtPath(BACKUP_STORAGE_RELATIVE_PATH)!!.forEach {
            if (index == k) {
                FileUtils.removeFileAtPath("$BACKUP_STORAGE_RELATIVE_PATH/$it")
                FileUtils.createFileNameAtPath(BACKUP_STORAGE_RELATIVE_PATH, t.name)
            }
            index++
        }
    }

    override fun get(k: Int): Backup {
        val backupsList : MutableList<Backup> = arrayListOf()
        var index = 0

        FileUtils.listFilesAtPath(BACKUP_STORAGE_RELATIVE_PATH)!!.forEach {
            backupsList.add(Backup(index, it, "00:00:00"))
            index++
        }

        return backupsList[k]
    }

    override fun getAll(): List<Backup> {
        val backupsList : MutableList<Backup> = arrayListOf()
        var index = 0

        FileUtils.listFilesAtPath(BACKUP_STORAGE_RELATIVE_PATH)!!.forEach {
            backupsList.add(Backup(index, it, "00:00:00"))
            index++
        }

        return backupsList
    }
}