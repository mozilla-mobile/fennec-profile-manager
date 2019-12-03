package org.mozilla.fpm.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import org.mozilla.fpm.models.Backup
import org.mozilla.fpm.utils.ZipUtils
import java.io.File

@SuppressLint("StaticFieldLeak")
object BackupRepositoryImpl : BackupRepository {
    private const val FENNEC_PACKAGE_NAME = "org.mozilla.firefox"
    private const val BACKUP_STORAGE_RELATIVE_PATH = "/backups"
    private lateinit var ctx: Context

    fun setContext(ctx: Context) {
        this.ctx = ctx
    }

    override fun create(k: String) {
        if (File(getBackupStoragePath()).mkdirs()) {
            Log.d(javaClass.name, "Repository initialized!")
        }

        val zde = ZipUtils()
        zde.compress(getBackupDeployPath(), "${getBackupStoragePath()}/$k.zip")
    }

    override fun deploy(name: String) {
        val zde = ZipUtils()
        File(getBackupDeployPath()).deleteRecursively()
        zde.extract("${getBackupStoragePath()}/$name")
    }

    override fun remove(k: String) {
        File("${getBackupStoragePath()}/$k").delete()
    }

    override fun update(t: Backup, k: String) {
        File("${getBackupStoragePath()}/${t.name}").delete()
        //TODO copy data through stream with simple renaming
    }

    override fun get(k: String): Backup {
        File(getBackupStoragePath()).listFiles()!!.forEach {
            if (it.name == k) {
                return Backup(it.name, it.lastModified().toString())
            }
        }

        return Backup("NOT_FOUND_ERR", "")
    }

    override fun getAll(): List<Backup> {
        val backupsList: MutableList<Backup> = arrayListOf()

        if (!File(getBackupStoragePath()).exists()) {
            File(getBackupStoragePath()).mkdirs()
        }

        File(getBackupStoragePath()).listFiles()!!.forEach {
            backupsList.add(Backup(it.name, it.lastModified().toString()))
        }

        return backupsList
    }

    private fun makeFirefoxPackageContext(context: Context): Context? {
        try {
            return context.createPackageContext(FENNEC_PACKAGE_NAME, Context.CONTEXT_RESTRICTED)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return null
    }

    private fun getBackupStoragePath(): String {
        return "${ctx.applicationInfo.dataDir}$BACKUP_STORAGE_RELATIVE_PATH"
    }

    private fun getBackupDeployPath(): String {
        return makeFirefoxPackageContext(ctx)!!.applicationInfo.dataDir
    }
}