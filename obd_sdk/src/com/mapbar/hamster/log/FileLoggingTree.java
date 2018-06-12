package com.mapbar.hamster.log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import timber.log.Timber;

/**
 * Created by guomin on 2018/6/11.
 */

public class FileLoggingTree extends Timber.Tree {


    private String filePath;

    public FileLoggingTree(String filePath) {
        this.filePath = filePath;
        File logFile = new File(filePath);
        if (!new File(filePath).exists()) {
            logFile.mkdirs();
        }
    }

    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        if ("".equals(filePath) || null == filePath) {
            return;
        }

        File file = new File(filePath, "/obd_log.txt");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file, true);
            fos.write(message.getBytes());
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
