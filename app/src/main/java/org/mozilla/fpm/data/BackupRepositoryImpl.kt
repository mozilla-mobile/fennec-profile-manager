/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fpm.data

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import org.mozilla.fpm.BuildConfig
import org.mozilla.fpm.models.Backup
import org.mozilla.fpm.utils.CryptUtils
import org.mozilla.fpm.utils.Utils
import org.mozilla.fpm.utils.Utils.Companion.makeFirefoxPackageContext
import org.mozilla.fpm.utils.ZipUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

@SuppressLint("StaticFieldLeak")
object BackupRepositoryImpl : BackupRepository {
    private const val BACKUP_STORAGE_RELATIVE_PATH = "/backups"
    private const val CRYPTO_STORAGE_RELATIVE_PATH = "/crypt"
    private lateinit var ctx: Context

    fun setContext(ctx: Context) {
        this.ctx = ctx
    }

    override fun create(k: String) {
        if (File(getBackupStoragePath()).mkdirs() || File(getCryptedStoragePath()).mkdirs()) {
            Log.d(javaClass.name, "Repository initialized!")
        }

        val deployPath = getBackupDeployPath()

        if (deployPath != null) {
            ZipUtils().compress(deployPath, "${getBackupStoragePath()}/${k}_arch.zip")
            CryptUtils.encrypt(
                FileInputStream("${getBackupStoragePath()}/${k}_arch.zip"),
                FileOutputStream("${getBackupStoragePath()}/$k.zip")
            )
            File("${getBackupStoragePath()}/${k}_arch.zip").delete()
            return
        }
        Log.w(javaClass.name, "No Firefox app installed. Please install one!")
    }

    override fun deploy(name: String) {
        if (File(getBackupStoragePath()).mkdirs() || File(getCryptedStoragePath()).mkdirs()) {
            Log.d(javaClass.name, "Repository initialized!")
        }

        val deployPath = getBackupDeployPath()

        if (deployPath != null) {
            File(deployPath).deleteRecursively()
            CryptUtils.decrypt(
                FileInputStream("${getBackupStoragePath()}/$name"),
                FileOutputStream("${getCryptedStoragePath()}/$name")
            )
            ZipUtils().extract("${getCryptedStoragePath()}/$name", getBackupDeployPath())
            File("${getCryptedStoragePath()}/$name").delete()
            return
        }
        Log.w(javaClass.name, "No Firefox app installed. Please install one!")
    }

    override fun remove(k: String) {
        File("${getBackupStoragePath()}/$k").delete()
    }

    override fun update(t: Backup, k: String) {
        File("${getBackupStoragePath()}/${t.name}").renameTo(File("${getBackupStoragePath()}/$k"))
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

    private fun getCryptedStoragePath(): String {
        return "${ctx.applicationInfo.dataDir}/$CRYPTO_STORAGE_RELATIVE_PATH"
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
