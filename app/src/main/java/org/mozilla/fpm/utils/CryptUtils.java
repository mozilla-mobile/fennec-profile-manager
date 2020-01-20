package org.mozilla.fpm.utils;

import android.util.Log;

import org.mozilla.fpm.BuildConfig;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class CryptUtils {
    private static final String LOGTAG = "CryptUtils";
    private static final String KEY = BuildConfig.SECRET_KEY;
    private static final byte[] iv = {
            (byte) 0xB2, (byte) 0x12, (byte) 0xD5, (byte) 0xB2,
            (byte) 0x44, (byte) 0x21, (byte) 0xC3, (byte) 0xC3
    };

    public void encrypt(InputStream is, OutputStream os) {
        try {
            final Cipher ecipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            final SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), "DES");
            final AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
            ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);

            byte[] buf = new byte[1024];

            // bytes at this stream are first encoded
            os = new CipherOutputStream(os, ecipher);

            // read in the clear text and write to out to encrypt
            int numRead;
            while ((numRead = is.read(buf)) >= 0) {
                os.write(buf, 0, numRead);
            }

            // close all streams
            is.close();
            os.close();
        } catch (Exception e) {
            Log.e(LOGTAG, "Error: " + e.getMessage());
        }
    }

    public void decrypt(InputStream is, OutputStream os) {
        try {
            final Cipher dcipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            final SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), "DES");
            final AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
            dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);

            byte[] buf = new byte[1024];

            // bytes read from stream will be decrypted
            CipherInputStream cis = new CipherInputStream(is, dcipher);

            // read in the decrypted bytes and write the clear text to out
            int numRead;
            while ((numRead = cis.read(buf)) > 0) {
                os.write(buf, 0, numRead);
            }

            // close all streams
            cis.close();
            is.close();
            os.close();

        } catch (Exception e) {
            Log.e(LOGTAG, "Error: " + e.getMessage());
        }
    }
}
