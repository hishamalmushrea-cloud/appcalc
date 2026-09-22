package com.valdio.valdioveliu.recyclerview;

import android.content.Context;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

/** نشاط أساسي يطبّق اللغة المختارة (عربي/إنجليزي) على كل الشاشات. */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(Lang.wrap(newBase));
    }

    protected boolean isRtl() {
        return getResources().getConfiguration().getLayoutDirection()
                == View.LAYOUT_DIRECTION_RTL;
    }
}
