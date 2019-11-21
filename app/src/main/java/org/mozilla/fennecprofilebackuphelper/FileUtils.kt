/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fennecprofilebackuphelper

import android.content.Context
import android.content.pm.PackageManager

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object FileUtils {
    private val BUFFER = 8192

    fun makeFirefoxPackageContext(context: Context): Context? {
        try {
            return context.createPackageContext("org.mozilla.firefox", Context.CONTEXT_RESTRICTED)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return null
    }

    @Throws(Exception::class)
    fun zip(files: Array<String>, zipFileName: String) {
        val dest = FileOutputStream(zipFileName)
        val out = ZipOutputStream(BufferedOutputStream(dest))
        var origin: BufferedInputStream
        val data = ByteArray(BUFFER)

        for (file in files) {
            val fileInputStream = FileInputStream(file)
            origin = BufferedInputStream(fileInputStream, BUFFER)

            val entry = ZipEntry(file.substring(file.lastIndexOf("/") + 1))
            out.putNextEntry(entry)
            var count: Int = origin.read(data, 0, BUFFER)

            while (count  != -1) {
                out.write(data, 0, count)
                count = origin.read(data, 0, BUFFER)
            }
            origin.close()
        }

        out.close()
    }

    @Throws(Exception::class)
    fun unzip(zipFileLocation: String, targetLocation: String) {
        val fileInputStream = FileInputStream(zipFileLocation)
        val zipInputStream = ZipInputStream(fileInputStream)
        var zipEntry: ZipEntry = zipInputStream.nextEntry
        while (zipEntry != null) {
            //create dir if required while unzipping
            if (zipEntry.isDirectory && !File(zipEntry.name).exists()) {
                File(zipEntry.name).mkdir()
            } else {
                val fileOutputStream = FileOutputStream(targetLocation + zipEntry.name)
                var c = zipInputStream.read()
                while (c != -1) {
                    fileOutputStream.write(c)
                    c = zipInputStream.read()
                }

                zipInputStream.closeEntry()
                fileOutputStream.close()
            }
            zipEntry= zipInputStream.nextEntry
        }
        zipInputStream.close()
    }
}
