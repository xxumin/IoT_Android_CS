package com.example.pknu.asuhwasher;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by PKNU on 2017-07-21.
 */

public class SplashActivity extends AppCompatActivity {
    // 로딩 화면 만드는 Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Splash(Loading) 화면 만들기
        try {
            Thread.sleep(1500);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }


}
