package com.valdio.valdioveliu.recyclerview;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

public class SettingsActivity extends BaseActivity {

    private RadioGroup langGroup;
    private RadioButton langAr, langEn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle(R.string.settings_title);

        langGroup = findViewById(R.id.lang_group);
        langAr = findViewById(R.id.lang_ar);
        langEn = findViewById(R.id.lang_en);

        String lang = Prefs.get(this, "lang", Lang.AR);
        langAr.setChecked(Lang.AR.equals(lang));
        langEn.setChecked(Lang.EN.equals(lang));

        langGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.lang_en) {
                Lang.set(this, Lang.EN);
            } else {
                Lang.set(this, Lang.AR);
            }
            recreate();
        });

        findViewById(R.id.change_pin).setOnClickListener(v -> changePin());
        findViewById(R.id.backup).setOnClickListener(v -> backup());
        findViewById(R.id.restore).setOnClickListener(v -> restore());
        findViewById(R.id.about).setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle(R.string.menu_about)
                        .setMessage(R.string.about_text)
                        .setPositiveButton(R.string.ok, null)
                        .show());
    }

    private void changePin() {
        final String oldPin = Prefs.get(this, "pin", "");

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(20), dp(16), dp(20), 0);

        final EditText oldInput = new EditText(this);
        oldInput.setHint(R.string.old_pin);
        oldInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER |
                android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        final EditText newInput = new EditText(this);
        newInput.setHint(R.string.new_pin);
        newInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER |
                android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        box.addView(oldInput);
        box.addView(newInput);
        if (oldPin.isEmpty()) oldInput.setVisibility(android.view.View.GONE);

        new AlertDialog.Builder(this)
                .setTitle(R.string.change_pin)
                .setView(box)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String enteredOld = oldInput.getText().toString();
                    String newPin = newInput.getText().toString();
                    if (!oldPin.isEmpty() && !oldPin.equals(enteredOld)) {
                        Toast.makeText(this, R.string.pin_wrong, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (newPin.length() < 4) {
                        Toast.makeText(this, R.string.pin_min, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Prefs.put(this, "pin", newPin);
                    Toast.makeText(this, R.string.pin_changed, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void backup() {
        boolean ok = CsvBackup.exportBackup(this);
        Toast.makeText(this, ok ? R.string.backup_done : R.string.error, Toast.LENGTH_LONG).show();
    }

    private void restore() {
        boolean ok = CsvBackup.importBackup(this);
        Toast.makeText(this, ok ? R.string.restore_done : R.string.restore_failed, Toast.LENGTH_LONG).show();
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}
