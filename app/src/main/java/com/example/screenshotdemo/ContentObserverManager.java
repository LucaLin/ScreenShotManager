package com.example.screenshotdemo;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;

/*
* 2020/10/13
* 使用ContentObserver攔截截圖事件
* 做用該Observer可以監聽手機系統資料夾內容的變化
* 比FileObserver更能攔截到事件
* 參考來源https://www.twblogs.net/a/5cff5e37bd9eee14029f8379
* */


public class ContentObserverManager {

    public static final String TAG = ContentObserverManager.class.getSimpleName();

    //要讀取哪些Column的資料
    private static final String[] MEDIA_PROJECTIONS = {
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.ImageColumns.DATE_TAKEN,
            MediaStore.Images.ImageColumns.DATE_ADDED
    };

    interface CallBack {
        void ohShot(String path);
    }

    //各種截圖資料夾的關鍵字
    private static final String[] SCREENSHOTS_KEYWORDS = {
            "screenshot", "screen_shot", "screen-shot", "screen shot", "screencapture",
            "screen_capture", "screen-capture", "screen capture", "screencap", "screen_cap",
            "screen-cap", "screen cap"
    };

    private ContentResolver contentResolver;
    private CallBack callBack;
    private MediaContentObserver observer_Internal;
    private MediaContentObserver observer_External;
    private static ContentObserverManager instance;

    private ContentObserverManager() {
    }

    public static ContentObserverManager getInstance() {
        if (instance == null) {
            instance = new ContentObserverManager();
        }
        return instance;
    }

    //註冊observer
    public void registObserver(Context context, CallBack callBack) {
        contentResolver = context.getContentResolver();
        this.callBack = callBack;

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        Handler handler = new Handler(handlerThread.getLooper());

        observer_Internal = new MediaContentObserver(handler, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        observer_External = new MediaContentObserver(handler, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        contentResolver.registerContentObserver(MediaStore.Images.Media.INTERNAL_CONTENT_URI, false, observer_Internal);
        contentResolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, observer_External);

    }

    //取消observer
    private void unregist() {
        if (contentResolver != null) {
            contentResolver.unregisterContentObserver(observer_Internal);
            contentResolver.unregisterContentObserver(observer_External);
        }
    }

    //使用cursor獲取新圖位置資訊
    private void handleMediaContent(Uri uri) {
        Cursor c = null;

        try {
            //抓最後一個位置的資料
            c = contentResolver.query(uri, MEDIA_PROJECTIONS, null, null,
                    MediaStore.Images.ImageColumns.DATE_ADDED + " desc limit 1");
            if (c == null)
                return;

            if (!c.moveToFirst())
                return;

            int index = c.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            int addIndex = c.getColumnIndex(MediaStore.Images.ImageColumns.DATE_ADDED);

            //處理數據
            handleMediaData(c.getString(index),c.getLong(addIndex));


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
    }

    //處理獲取到的資料
    private void handleMediaData(String path, long dateAdded) {
        long duration = 0;
        long step = 100;

        //範例說某些手機會誤認其它動作為截圖動作，所以判斷操作時間，但目前不用
//        if (!isTimeOk(dateAdded))
//            return;

        //緩衝時間
        while (!checkScreenShot(path) && duration <= 500) {
            try {
                duration += step;
                Thread.sleep(step);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (checkScreenShot(path)) {
            if (callBack != null)
                callBack.ohShot(path);
        }
    }


    private boolean isTimeOk(long dateAdded) {
        return Math.abs(System.currentTimeMillis() / 1000 - dateAdded) <= 1;
    }

    //判斷是不是截圖動作
    private boolean checkScreenShot(String path) {
        if (path == null)
            return false;

        path = path.toLowerCase();
        for (String keyword : SCREENSHOTS_KEYWORDS) {
            if (path.contains(keyword))
                return true;
        }
        return false;
    }


    private class MediaContentObserver extends ContentObserver {
        private Uri uri;

        public MediaContentObserver(Handler handler, Uri uri) {
            super(handler);
            this.uri = uri;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            handleMediaContent(uri);
        }
    }
}
