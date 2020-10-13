package com.example.screenshotdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    TextView text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = findViewById(R.id.text);

        //**ContentObserver**//
        
        ContentObserverManager contentObserverManager = ContentObserverManager.getInstance();
        contentObserverManager.registObserver(this, new ContentObserverManager.CallBack() {
            @Override
            public void ohShot(final String path) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        text.setText(path);
                    }
                });

            }
        });

        //**FileObserver**//

//        FileObserverManager ssm = new FileObserverManager();
//        ssm.initFileObserver();
//        ssm.fileObserver.startWatching();
//
//        ssm.setScreenShotCallback(new FileObserverManager.ScreenShotCallback() {
//            @Override
//            public void gotScreenShot(final String filePath) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(MainActivity.this,"你截了一張圖喔",Toast.LENGTH_SHORT).show();
//                        text.setText(filePath);
//                    }
//                });
//
//            }
//        });



    }
}
