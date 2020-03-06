package com.duzhaokun123.bilibilihd.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.duzhaokun123.bilibilihd.R;
import com.duzhaokun123.bilibilihd.pbilibiliapi.api.PBilibiliClient;
import com.duzhaokun123.bilibilihd.ui.main.MainActivity;
import com.duzhaokun123.bilibilihd.utils.OtherUtils;
import com.duzhaokun123.bilibilihd.utils.SettingsManager;

public class WelcomeActivity extends AppCompatActivity {

    private PBilibiliClient pBilibiliClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        pBilibiliClient = PBilibiliClient.Companion.getPBilibiliClient();

        new Thread(new Runnable() {
            @Override
            public void run() {
                SettingsManager.init(getApplicationContext());
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                OtherUtils.loadLoginResponse(WelcomeActivity.this, pBilibiliClient);
            }
        }).start();

         new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 2000);
    }
}
