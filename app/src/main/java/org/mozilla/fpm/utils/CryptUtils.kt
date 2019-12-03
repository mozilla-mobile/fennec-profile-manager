/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fpm.utils

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec

object CryptUtils {
    private var ecipher: Cipher? = null
    private var dcipher: Cipher? = null

    // 8-byte initialization vector
    private val iv = byteArrayOf(
        0xB2.toByte(),
        0x12.toByte(),
        0xD5.toByte(),
        0xB2.toByte(),
        0x44.toByte(),
        0x21.toByte(),
        0xC3.toByte(),
        0xC3.toByte()
    )

    @Throws(Exception::class)
    fun call() {
        val key = KeyGenerator.getInstance("AES/CBC/NoPadding").generateKey()
        val paramSpec = IvParameterSpec(iv)

        ecipher = Cipher.getInstance("AES/CBC/NoPadding")
        dcipher = Cipher.getInstance("AES/CBC/NoPadding")

        ecipher!!.init(Cipher.ENCRYPT_MODE, key, paramSpec)
        dcipher!!.init(Cipher.DECRYPT_MODE, key, paramSpec)
    }

    /**
     *
     */
    @Throws(IOException::class)
    fun encrypt(`is`: InputStream, ostream: OutputStream) {
        call()

        val buf = ByteArray(1024)

        // bytes at this stream are first encoded
        val os = CipherOutputStream(ostream, ecipher)

        // read in the clear text and write to out to encrypt
        var numRead = 0
        while (numRead >= 0) {
            os.write(buf, 0, numRead)
            numRead = `is`.read(buf)
        }

        // close all streams
        os.close()
    }

    /**
     *
     */
    @Throws(IOException::class)
    fun decrypt(`is`: InputStream, os: OutputStream) {
        call()

        val buf = ByteArray(1024)
        // bytes read from stream will be decrypted
        val cis = CipherInputStream(`is`, dcipher)
        // read in the decrypted bytes and write the clear text to out
        var numRead = 0
        while (numRead > 0) {
            os.write(buf, 0, numRead)
            numRead = cis.read(buf)
        }

        // close all streams
        cis.close()
        `is`.close()
        os.close()
    }
}
