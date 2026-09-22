package com.valdio.valdioveliu.recyclerview;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private Dao dao;
    private Spinner groupFilter;
    private RecyclerView list;
    private TextView empty;
    private List<Group> groups = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.customers);

        dao = new Dao(this);
        list = findViewById(R.id.list);
        empty = findViewById(R.id.empty);
        groupFilter = findViewById(R.id.group_filter);

        list.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.fab).setOnClickListener(v ->
                startActivity(new Intent(this, CustomerEditActivity.class)));

        setupGroups();
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void setupGroups() {
        groups = new ArrayList<>();
        groups.add(new Group(-1, getString(R.string.all_groups)));
        groups.addAll(dao.groups());

        ArrayAdapter<Group> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, groups);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupFilter.setAdapter(adapter);
        groupFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                load();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void load() {
        Group g = (Group) groupFilter.getSelectedItem();
        Integer groupId = (g == null || g.id == -1) ? null : g.id;
        List<Customer> customers = dao.customers(groupId);

        list.setAdapter(new CustomerAdapter(customers, new CustomerAdapter.Listener() {
            @Override
            public void onClick(Customer c) {
                Intent i = new Intent(MainActivity.this, AccountActivity.class);
                i.putExtra("id", c.id);
                i.putExtra("name", c.name);
                startActivity(i);
            }

            @Override
            public boolean onLongClick(Customer c) {
                showOptions(c);
                return true;
            }
        }, isRtl()));

        empty.setVisibility(customers.isEmpty() ? View.VISIBLE : View.GONE);
        list.setVisibility(customers.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showOptions(Customer c) {
        String[] options = {
                getString(R.string.statement),
                getString(R.string.edit_customer),
                getString(R.string.delete_customer)
        };
        new AlertDialog.Builder(this)
                .setTitle(c.name)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent i = new Intent(this, AccountActivity.class);
                        i.putExtra("id", c.id);
                        i.putExtra("name", c.name);
                        startActivity(i);
                    } else if (which == 1) {
                        Intent i = new Intent(this, CustomerEditActivity.class);
                        i.putExtra("id", c.id);
                        startActivity(i);
                    } else {
                        confirmDelete(c);
                    }
                })
                .show();
    }

    private void confirmDelete(Customer c) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_customer)
                .setMessage(R.string.delete_customer_confirm)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    dao.deleteCustomer(c.id);
                    load();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            startActivity(new Intent(this, SearchActivity.class));
            return true;
        } else if (id == R.id.action_reports) {
            startActivity(new Intent(this, ReportsActivity.class));
            return true;
        } else if (id == R.id.action_calculator) {
            startActivity(new Intent(this, CalculatorActivity.class));
            return true;
        } else if (id == R.id.action_reminders) {
            startActivity(new Intent(this, RemindersActivity.class));
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_about) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.menu_about)
                    .setMessage(R.string.about_text)
                    .setPositiveButton(R.string.ok, null)
                    .show();
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
