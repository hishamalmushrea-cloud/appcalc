package com.valdio.valdioveliu.recyclerview;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReportsActivity extends BaseActivity {

    private Dao dao;
    private Spinner type, customer;
    private TextView hTitle, hC1, hC2, hC3;
    private RecyclerView list;
    private TextView empty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);
        setTitle(R.string.reports_title);

        dao = new Dao(this);
        type = findViewById(R.id.type);
        customer = findViewById(R.id.customer);
        hTitle = findViewById(R.id.h_title);
        hC1 = findViewById(R.id.h_c1);
        hC2 = findViewById(R.id.h_c2);
        hC3 = findViewById(R.id.h_c3);
        list = findViewById(R.id.list);
        empty = findViewById(R.id.empty);
        list.setLayoutManager(new LinearLayoutManager(this));

        ArrayAdapter<CharSequence> ta = ArrayAdapter.createFromResource(this,
                R.array.report_types, android.R.layout.simple_spinner_item);
        ta.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type.setAdapter(ta);
        type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setupCustomerSpinner(position == 2);
                load(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupCustomerSpinner(boolean visible) {
        customer.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (!visible) return;
        ArrayAdapter<Customer> a = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, dao.customers(null));
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        customer.setAdapter(a);
    }

    private void load(int position) {
        List<ReportRow> rows;
        if (position == 0) {
            hTitle.setText(R.string.col_customer);
            hC1.setText(R.string.col_credit);
            hC2.setText(R.string.col_debit);
            hC3.setText(R.string.col_net);
            hC1.setVisibility(View.VISIBLE);
            hC2.setVisibility(View.VISIBLE);
            hC3.setVisibility(View.VISIBLE);
            rows = dao.reportBalances();
        } else if (position == 1) {
            hTitle.setText(R.string.col_customer);
            hC1.setText(R.string.col_net);
            hC2.setText(R.string.col_last);
            hC3.setText(R.string.col_days);
            hC1.setVisibility(View.VISIBLE);
            hC2.setVisibility(View.VISIBLE);
            hC3.setVisibility(View.VISIBLE);
            rows = dao.reportLate();
        } else {
            hTitle.setText(R.string.col_month);
            hC1.setText(R.string.col_net);
            hC2.setVisibility(View.GONE);
            hC3.setVisibility(View.GONE);
            Customer cu = (Customer) customer.getSelectedItem();
            rows = (cu == null) ? dao.reportMonthly(0) : dao.reportMonthly(cu.id);
        }
        list.setAdapter(new ReportAdapter(rows));
        empty.setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
        list.setVisibility(rows.isEmpty() ? View.GONE : View.VISIBLE);
    }
}
