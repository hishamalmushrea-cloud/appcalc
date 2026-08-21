package com.valdio.valdioveliu.recyclerview;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends BaseActivity {

    private EditText pin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle(R.string.login_title);

        pin = findViewById(R.id.pin);
        findViewById(R.id.login).setOnClickListener(v -> check());
    }

    private void check() {
        String saved = Prefs.get(this, "pin", "");
        if (pin.getText().toString().equals(saved)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, R.string.login_wrong, Toast.LENGTH_SHORT).show();
        }
    }
}
