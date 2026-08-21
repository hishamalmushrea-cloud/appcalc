package com.valdio.valdioveliu.recyclerview;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RemindersActivity extends BaseActivity {

    private Dao dao;
    private RecyclerView list;
    private TextView empty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);
        setTitle(R.string.reminders_title);

        dao = new Dao(this);
        list = findViewById(R.id.list);
        empty = findViewById(R.id.empty);
        list.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.fab).setOnClickListener(v -> showAddDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        List<Reminder> reminders = dao.reminders();
        list.setAdapter(new ReminderAdapter(reminders, r -> confirmDelete(r)));
        empty.setVisibility(reminders.isEmpty() ? View.VISIBLE : View.GONE);
        list.setVisibility(reminders.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showAddDialog() {
        List<Customer> customers = dao.customers(null);
        if (customers.isEmpty()) {
            Toast.makeText(this, R.string.empty_customers, Toast.LENGTH_SHORT).show();
            return;
        }

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(20), dp(16), dp(20), 0);

        final Spinner customer = new Spinner(this);
        ArrayAdapter<Customer> a = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, customers);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        customer.setAdapter(a);
        box.addView(customer);

        final EditText date = new EditText(this);
        date.setHint(getString(R.string.reminder_date) + " (dd-MM-yyyy)");
        date.setText(Fmt.today());
        box.addView(date);

        final EditText time = new EditText(this);
        time.setHint(getString(R.string.reminder_time) + " (HH:mm)");
        time.setText("09:00");
        box.addView(time);

        final EditText notes = new EditText(this);
        notes.setHint(R.string.reminder_notes);
        box.addView(notes);

        new AlertDialog.Builder(this)
                .setTitle(R.string.add_reminder)
                .setView(box)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    int cusId = customers.get(customer.getSelectedItemPosition()).id;
                    dao.addReminder(cusId, date.getText().toString().trim(),
                            time.getText().toString().trim(), notes.getText().toString().trim());
                    load();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmDelete(Reminder r) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete)
                .setMessage(R.string.delete_confirm)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    dao.deleteReminder(r.id);
                    load();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}
