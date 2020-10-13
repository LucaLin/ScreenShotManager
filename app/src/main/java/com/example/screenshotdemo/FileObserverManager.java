package com.example.screenshotdemo;

import android.os.Environment;
import android.os.FileObserver;
import android.util.Log;
import androidx.annotation.Nullable;

import java.io.File;

/*
* 使用FileObserver攔截截圖事件
* 做法上較為簡單
* 但有可能會攔截不到截圖事件
* 參考來源https://www.twblogs.net/a/5b8d7d122b717718833e5ffe
* */

public class FileObserverManager {

    public FileObserver fileObserver;
    public static final String TAG = FileObserverManager.class.getSimpleName();

    private static final String SCREENSHOT_PATH = Environment.getExternalStorageDirectory()
            + File.separator + Environment.DIRECTORY_DCIM
            + File.separator + "Screenshots" + File.separator;

    public static String lastShotPath;
    public static final int MAX_RETRY = 5;


    interface ScreenShotCallback {
        void gotScreenShot(String filePath);
    }

    public ScreenShotCallback screenShotCallback;

    public void setScreenShotCallback(ScreenShotCallback screenShotCallback) {
        this.screenShotCallback = screenShotCallback;
    }


    void initFileObserver() {
        fileObserver = new FileObserver(SCREENSHOT_PATH, FileObserver.ALL_EVENTS) {
            @Override
            public void onEvent(int event, @Nullable String path) {
                if (path != null && event == FileObserver.CREATE && (!path.equals(lastShotPath))) {
                    Log.d(TAG, "screenshots path: " + path);
                    lastShotPath = path;
                    String filePath = SCREENSHOT_PATH + path;
                    int retryCount = 0;
                    try {
                        if (screenShotCallback != null) {
                            screenShotCallback.gotScreenShot(filePath);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        retryCount++;
                        if (retryCount >= MAX_RETRY) {
                            return;
                        }
                    }
                }
                //callback
            }

        };
    }


}
