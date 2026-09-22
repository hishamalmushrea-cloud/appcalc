package com.valdio.valdioveliu.recyclerview;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionEditActivity extends BaseActivity {

    private Dao dao;
    private int cusId = 0;
    private int txId = 0;
    private RadioButton dirCredit, dirDebit;
    private EditText amount, remarks;
    private Spinner currency, toAccount;
    private Button dateBtn;
    private String date = Fmt.today();

    private List<Currency> currencies = new ArrayList<>();
    private List<Customer> customers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_edit);
        setTitle(R.string.add_txn_title);

        dao = new Dao(this);
        cusId = getIntent().getIntExtra("cusId", 0);
        txId = getIntent().getIntExtra("txId", 0);

        dirCredit = findViewById(R.id.dir_credit);
        dirDebit = findViewById(R.id.dir_debit);
        amount = findViewById(R.id.amount);
        remarks = findViewById(R.id.remarks);
        currency = findViewById(R.id.currency);
        toAccount = findViewById(R.id.to_account);
        dateBtn = findViewById(R.id.date);
        dateBtn.setText(date);
        dateBtn.setOnClickListener(v -> pickDate());

        currencies = dao.currencies();
        ArrayAdapter<Currency> ca = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, currencies);
        ca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currency.setAdapter(ca);

        // قائمة "إلى حساب" (بدون الحساب الحالي)
        List<Customer> all = dao.customers(null);
        customers = new ArrayList<>();
        customers.add(new Customer(0, getString(R.string.none), "", "", "", 0));
        for (Customer c : all) {
            if (c.id != cusId) customers.add(c);
        }
        ArrayAdapter<Customer> ta = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, customers);
        ta.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toAccount.setAdapter(ta);

        if (txId != 0) {
            setTitle(R.string.edit_txn_title);
            Transaction t = dao.transaction(txId);
            if (t != null) {
                if (t.dir >= 0) dirCredit.setChecked(true);
                else dirDebit.setChecked(true);
                amount.setText(Fmt.num(t.amount));
                remarks.setText(t.remarks);
                date = t.date;
                dateBtn.setText(date);
                selectById(currency, t.currId);
                selectById(toAccount, t.tCusId);
            }
        }

        findViewById(R.id.save).setOnClickListener(v -> save());
    }

    private void selectById(Spinner spinner, int id) {
        for (int i = 0; i < spinner.getCount(); i++) {
            Object o = spinner.getItemAtPosition(i);
            int cur = (o instanceof Currency) ? ((Currency) o).id : ((Customer) o).id;
            if (cur == id) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void pickDate() {
        Calendar cal = parseDate(date);
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            date = String.format(Locale.US, "%02d-%02d-%04d", dayOfMonth, month + 1, year);
            dateBtn.setText(date);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private Calendar parseDate(String d) {
        Calendar cal = Calendar.getInstance();
        try {
            Date parsed = new SimpleDateFormat("dd-MM-yyyy", Locale.US).parse(d);
            if (parsed != null) cal.setTime(parsed);
        } catch (Exception ignored) {
        }
        return cal;
    }

    private void save() {
        double amt = Fmt.parse(amount.getText().toString());
        if (amt <= 0) {
            Toast.makeText(this, R.string.amount_required, Toast.LENGTH_SHORT).show();
            return;
        }
        int dir = dirCredit.isChecked() ? 1 : -1;
        int currId = currencies.get(currency.getSelectedItemPosition()).id;
        int tCusId = customers.get(toAccount.getSelectedItemPosition()).id;
        String rem = remarks.getText().toString().trim();

        if (txId == 0) {
            dao.addTransaction(cusId, tCusId, dir, amt, currId, date, rem);
        } else {
            dao.updateTransaction(txId, cusId, tCusId, dir, amt, currId, date, rem);
        }
        Toast.makeText(this, R.string.done, Toast.LENGTH_SHORT).show();
        finish();
    }
}
