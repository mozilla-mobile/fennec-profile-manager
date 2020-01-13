package org.mozilla.fpm.utils;

import org.mozilla.fpm.BuildConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class CryptUtils {
    private static Cipher ecipher;
    private static Cipher dcipher;
    private static final String KEY = BuildConfig.SECRET_KEY;

    // 8-byte initialization vector
    private static byte[] iv = {
            (byte) 0xB2, (byte) 0x12, (byte) 0xD5, (byte) 0xB2,
            (byte) 0x44, (byte) 0x21, (byte) 0xC3, (byte) 0xC3
    };

    public static void call() {
        try {
            SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), "DES");

            AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);

            ecipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            dcipher = Cipher.getInstance("DES/CBC/PKCS5Padding");

            ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
            dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void encrypt(InputStream is, OutputStream os) {
        try {

            call();

            byte[] buf = new byte[1024];

            // bytes at this stream are first encoded
            os = new CipherOutputStream(os, ecipher);

            // read in the clear text and write to out to encrypt
            int numRead;
            while ((numRead = is.read(buf)) >= 0) {
                os.write(buf, 0, numRead);
            }

            // close all streams
            os.close();

        } catch (IOException e) {
            System.out.println("I/O Error:" + e.getMessage());
        }

    }

    public static void decrypt(InputStream is, OutputStream os) {
        try {
            call();

            byte[] buf = new byte[1024];

            // bytes read from stream will be decrypted
            CipherInputStream cis = new CipherInputStream(is, dcipher);

            // read in the decrypted bytes and write the clear text to out
            int numRead = 0;
            while ((numRead = cis.read(buf)) > 0) {
                os.write(buf, 0, numRead);
            }

            // close all streams
            cis.close();
            is.close();
            os.close();

        } catch (IOException e) {
            System.out.println("I/O Error:" + e.getMessage());
        }
    }
}
