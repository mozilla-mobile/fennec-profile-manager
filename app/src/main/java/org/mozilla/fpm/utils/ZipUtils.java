/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fpm.utils;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
    private static final String LOGTAG = "ZipUtils";
    private List<String> fileList = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void compress(String source, String destination) {
        File directory = new File(source);

        getFileList(directory);

        try (FileOutputStream fos = new FileOutputStream(destination);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            for (String filePath : fileList) {
                // Creates a zip entry.
                String name = filePath.substring(directory.getAbsolutePath().length() + 1);

                ZipEntry zipEntry = new ZipEntry(name);
                zos.putNextEntry(zipEntry);

                // Read file content and write to zip output stream.
                try (FileInputStream fis = new FileInputStream(filePath)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }

                    // Close the zip entry.
                    zos.closeEntry();
                } catch (Exception e) {
                    Log.e(LOGTAG, "Error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            Log.e(LOGTAG, "Error: " + e.getMessage());
        }
    }

    public void extract(String zipFile, String destinationDir) throws IOException {
        final int BUFFER = 2048;
        final File file = new File(zipFile);
        final ZipFile zip = new ZipFile(file);

        new File(destinationDir).mkdirs();
        Enumeration zipFileEntries = zip.entries();

        // Process each entry
        while (zipFileEntries.hasMoreElements()) {
            // grab a zip file entry
            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
            String currentEntry = entry.getName();
            File destFile = new File(destinationDir, currentEntry);
            File destinationParent = destFile.getParentFile();

            // create the parent directory structure if needed
            destinationParent.mkdirs();

            if (!entry.isDirectory()) {
                BufferedInputStream is = new BufferedInputStream(zip
                        .getInputStream(entry));
                int currentByte;
                // establish buffer for writing file
                byte[] data = new byte[BUFFER];

                // write the current file to disk
                try (FileOutputStream fos = new FileOutputStream(destFile);
                     BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER)){

                    // read and write until last byte is encountered
                    while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, currentByte);
                    }
                } catch (FileNotFoundException e) {
                    // On some devices the data/data/app_package/lib directory is just a symlink
                    // while on other devices that directory contains the actual extracted libs
                    // The actual files can be overwritten by us
                    // The symlinked files (and directory) is only readable by us and so when trying to
                    // create a new FileOutputStream for such a file we would get a FileNotFoundException
                    // Do a speculative check that the file that caused the exception is one such symlinked file.
                    if (destFile.exists() && destFile.length() > 0 && !destFile.canWrite()) {
                        Log.w(LOGTAG, "Could not write " + destFile.getName() + ". Ignoring.");
                    } else {
                        throw e;
                    }
                } finally {
                    is.close();
                }
            }
        }
    }

    /**
     * Get files list from the directory recursive to the sub directory.
     */
    private void getFileList(File directory) {
        File[] files = directory.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isFile()) {
                    fileList.add(file.getAbsolutePath());
                } else {
                    getFileList(file);
                }
            }
        }

    }
}
