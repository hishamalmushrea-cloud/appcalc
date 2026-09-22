package com.valdio.valdioveliu.recyclerview;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AccountActivity extends BaseActivity {

    private Dao dao;
    private int id;
    private TextView credit, debit, net;
    private RecyclerView list;
    private TextView empty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        dao = new Dao(this);
        id = getIntent().getIntExtra("id", 0);
        String name = getIntent().getStringExtra("name");
        setTitle(getString(R.string.statement) + (name == null ? "" : ": " + name));

        credit = findViewById(R.id.credit);
        debit = findViewById(R.id.debit);
        net = findViewById(R.id.net);
        list = findViewById(R.id.list);
        empty = findViewById(R.id.empty);

        list.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.fab).setOnClickListener(v -> {
            Intent i = new Intent(this, TransactionEditActivity.class);
            i.putExtra("cusId", id);
            startActivity(i);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        double bal = dao.balance(id);
        double cr = 0, db = 0;
        List<Transaction> txs = dao.transactions(id);
        for (Transaction t : txs) {
            if (t.signed >= 0) cr += t.signed;
            else db += -t.signed;
        }
        credit.setText(Fmt.num(cr));
        debit.setText(Fmt.num(db));
        net.setText(Fmt.signed(bal));

        list.setAdapter(new TransactionAdapter(txs, t -> {
            Intent i = new Intent(this, TransactionEditActivity.class);
            i.putExtra("cusId", id);
            i.putExtra("txId", t.id);
            startActivity(i);
        }));

        empty.setVisibility(txs.isEmpty() ? View.VISIBLE : View.GONE);
        list.setVisibility(txs.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_account, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_export) {
            export();
            return true;
        } else if (itemId == R.id.action_edit) {
            Intent i = new Intent(this, CustomerEditActivity.class);
            i.putExtra("id", id);
            startActivity(i);
            return true;
        } else if (itemId == R.id.action_close) {
            closeAccount();
            return true;
        } else if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void export() {
        boolean ok = CsvBackup.exportBackup(this);
        Toast.makeText(this, ok ? R.string.export_done : R.string.error, Toast.LENGTH_LONG).show();
    }

    private void closeAccount() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.close_account)
                .setMessage(R.string.close_confirm)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    dao.closeAccount(id, getString(R.string.opening_balance));
                    load();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
