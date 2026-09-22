package com.valdio.valdioveliu.recyclerview;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                String pin = Prefs.get(SplashActivity.this, "pin", "");
                Class<?> target = pin.isEmpty() ? MainActivity.class : LoginActivity.class;
                startActivity(new Intent(SplashActivity.this, target));
                finish();
            }
        }, 300);
    }
}
