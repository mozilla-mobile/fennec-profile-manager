/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fpm.data

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import org.mozilla.fpm.BuildConfig
import org.mozilla.fpm.models.Backup
import org.mozilla.fpm.utils.Utils
import org.mozilla.fpm.utils.Utils.Companion.makeFirefoxPackageContext
import org.mozilla.fpm.utils.ZipUtils
import java.io.File

@SuppressLint("StaticFieldLeak")
object BackupRepositoryImpl : BackupRepository {
    private const val BACKUP_STORAGE_RELATIVE_PATH = "/backups"
    private lateinit var ctx: Context

    fun setContext(ctx: Context) {
        this.ctx = ctx
    }

    override fun create(k: String) {
        if (File(getBackupStoragePath()).mkdirs()) {
            Log.d(javaClass.name, "Repository initialized!")
        }

        val deployPath = getBackupDeployPath()

        if (deployPath != null) {
            ZipUtils().compress(deployPath, "${getBackupStoragePath()}/$k.zip")
            return
        }
        Log.w(javaClass.name, "No Firefox app installed. Please install one!")
    }

    override fun deploy(name: String) {
        val deployPath = getBackupDeployPath()

        if (deployPath != null) {
            File(deployPath).deleteRecursively()
            ZipUtils().extract("${getBackupStoragePath()}/$name", getBackupDeployPath())
            return
        }
        Log.w(javaClass.name, "No Firefox app installed. Please install one!")
    }

    override fun remove(k: String) {
        File("${getBackupStoragePath()}/$k").delete()
    }

    override fun update(t: Backup, k: String) {
        File("${getBackupStoragePath()}/${t.name}").delete()
        // TODO copy data through stream with simple renaming
    }

    override fun get(k: String): Backup {
        File(getBackupStoragePath()).listFiles()?.forEach {
            if (it.name == k) {
                return Backup(it.name, Utils.getFormattedDate(it.lastModified()))
            }
        }

        return Backup("", "")
    }

    override fun getAll(): List<Backup> {
        val backupsList: MutableList<Backup> = arrayListOf()

        if (!File(getBackupStoragePath()).exists()) {
            File(getBackupStoragePath()).mkdirs()
        }

        File(getBackupStoragePath()).listFiles()?.forEach {
            backupsList.add(Backup(it.name, Utils.getFormattedDate(it.lastModified())))
        }

        return backupsList
    }

    /**
     * Debug variants use the external storage in order to allow for better debugging and ease of access to the Backups.
     */
    private fun getBackupStoragePath(): String {
        return if (BuildConfig.DEBUG) {
            "${ctx.getExternalFilesDir(null)?.absolutePath}$BACKUP_STORAGE_RELATIVE_PATH"
        } else {
            "${ctx.applicationInfo.dataDir}$BACKUP_STORAGE_RELATIVE_PATH"
        }
    }

    private fun getBackupDeployPath(): String? {
        return makeFirefoxPackageContext(ctx)?.applicationInfo?.dataDir
    }
}
