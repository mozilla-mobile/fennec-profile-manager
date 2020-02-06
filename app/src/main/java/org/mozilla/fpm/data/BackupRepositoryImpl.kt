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
import org.mozilla.fpm.utils.Utils.Companion.getBackupStoragePath
import org.mozilla.fpm.utils.Utils.Companion.getCryptedStoragePath
import org.mozilla.fpm.utils.Utils.Companion.makeFirefoxPackageContext
import org.mozilla.fpm.utils.ZipUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

@SuppressLint("StaticFieldLeak")
object BackupRepositoryImpl : BackupRepository {
    private const val LOGTAG = "BackupRepository"
    const val MIME_TYPE = "fpm"
    private lateinit var ctx: Context

    fun setContext(ctx: Context) {
        this.ctx = ctx
    }

    override fun create(k: String) {
        if (File(getBackupStoragePath(ctx)).mkdirs() || File(getCryptedStoragePath(ctx)).mkdirs()) {
            Log.d(javaClass.name, "Repository initialized!")
        }

        val deployPath = getBackupDeployPath()
        val signatureByteData = BuildConfig.FIREFOX_PACKAGE_NAME.toByteArray()

        if (deployPath != null) {
            // compress everything
            ZipUtils().compress(deployPath, "${getBackupStoragePath(ctx)}/${k}_arch.$MIME_TYPE")

            // now encrypt the archive
            CryptUtils().encrypt(
                FileInputStream("${getBackupStoragePath(ctx)}/${k}_arch.$MIME_TYPE"),
                FileOutputStream("${getBackupStoragePath(ctx)}/$k.$MIME_TYPE")
            )
            // delete the temporary file
            File("${getBackupStoragePath(ctx)}/${k}_arch.$MIME_TYPE").delete()

            // write the signature
            val fos = FileOutputStream("${getBackupStoragePath(ctx)}/$k.$MIME_TYPE", true)
            fos.write(signatureByteData)
            fos.close()

            return
        }
        Log.w(javaClass.name, "No Firefox app installed. Please install one!")
    }

    override fun getFileSignature(path: String): String? {
        val fileByteArray = FileInputStream(path).readBytes()
        val fileSize = fileByteArray.size

        BuildConfig.FENNEC_PACKAGE_NAMES.forEach {
            var matched = true
            val signatureByteArray = it.toByteArray()
            val signatureSize = signatureByteArray.size

            if (fileSize - signatureSize < 0) return@forEach

            for (i in 0 until signatureSize) {
                if (signatureByteArray[i].compareTo(fileByteArray[fileSize - signatureSize + i]) != 0) {
                    matched = false
                }
            }

            if (matched) {
                return it
            }
        }

        return null
    }

    override fun deploy(name: String) {
        if (File(getBackupStoragePath(ctx)).mkdirs() || File(getCryptedStoragePath(ctx)).mkdirs()) {
            Log.d(javaClass.name, "Repository initialized!")
        }

        val deployPath = getBackupDeployPath()

        if (deployPath != null) {
            // remove the current state of Firefox

            if (File(deployPath).deleteRecursively().not()) {
                Log.w(LOGTAG, "Could not clear Fennec's internal storage")
            }

            val sign = getFileSignature("${getBackupStoragePath(ctx)}/$name")
            val backupFile = File("${getBackupStoragePath(ctx)}/$name")
            if (sign != null) {
                val unsingedBytes =
                    backupFile.readBytes().dropLast(sign.toByteArray().size).toByteArray()
                FileOutputStream("${getBackupStoragePath(ctx)}/${name}_temp").write(unsingedBytes)
            }

            CryptUtils().decrypt(
                FileInputStream("${getBackupStoragePath(ctx)}/${name}_temp"),
                FileOutputStream("${getCryptedStoragePath(ctx)}/$name")
            )
            ZipUtils().extract("${getCryptedStoragePath(ctx)}/$name", getBackupDeployPath())

            // delete the temp files
            File("${getBackupStoragePath(ctx)}/${name}_temp").delete()
            File("${getCryptedStoragePath(ctx)}/$name").delete()
            return
        }
        Log.w(javaClass.name, "No Firefox app installed. Please install one!")
    }

    override fun remove(k: String) {
        File("${getBackupStoragePath(ctx)}/$k").delete()
    }

    override fun update(t: Backup, k: String) {
        File("${getBackupStoragePath(ctx)}/${t.name}").renameTo(File("${getBackupStoragePath(ctx)}/$k"))
    }

    override fun get(k: String): Backup? {
        File(getBackupStoragePath(ctx)).listFiles()?.forEach {
            if (it.name == k) {
                return Backup(
                    it.name,
                    Utils.getFormattedDate(it.lastModified()),
                    getFileSignature("${getBackupStoragePath(ctx)}/${it.name}"),
                    it.length()
                )
            }
        }

        return null
    }

    override fun getAll(): List<Backup> {
        val backupsList: MutableList<Backup> = arrayListOf()

        if (!File(getBackupStoragePath(ctx)).exists()) {
            File(getBackupStoragePath(ctx)).mkdirs()
        }

        File(getBackupStoragePath(ctx)).listFiles()?.forEach {
            backupsList.add(
                Backup(
                    it.name,
                    Utils.getFormattedDate(it.lastModified()),
                    getFileSignature("${getBackupStoragePath(ctx)}/${it.name}"),
                    it.length()
                )
            )
        }

        return backupsList
    }

    private fun getBackupDeployPath(): String? {
        return makeFirefoxPackageContext(ctx)?.applicationInfo?.dataDir
    }
}
