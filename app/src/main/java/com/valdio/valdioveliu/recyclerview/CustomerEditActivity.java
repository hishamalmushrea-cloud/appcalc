package com.valdio.valdioveliu.recyclerview;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class CustomerEditActivity extends BaseActivity {

    private Dao dao;
    private EditText name, phone;
    private Spinner group, type;
    private int id = 0;
    private List<Group> groups = new ArrayList<>();
    private List<CustomerType> types = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_edit);
        setTitle(R.string.add_customer);

        dao = new Dao(this);
        name = findViewById(R.id.name);
        phone = findViewById(R.id.phone);
        group = findViewById(R.id.group);
        type = findViewById(R.id.type);

        groups = dao.groups();
        types = dao.types();

        ArrayAdapter<Group> ga = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, groups);
        ga.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        group.setAdapter(ga);

        ArrayAdapter<CustomerType> ta = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, types);
        ta.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type.setAdapter(ta);

        id = getIntent().getIntExtra("id", 0);
        if (id != 0) {
            setTitle(R.string.edit_customer);
            Customer c = dao.customer(id);
            if (c != null) {
                name.setText(c.name);
                phone.setText(c.gsm);
                selectById(group, c.gId);
                selectById(type, c.typeId);
            }
        }

        findViewById(R.id.save).setOnClickListener(v -> save());
    }

    private void selectById(Spinner spinner, int id) {
        for (int i = 0; i < spinner.getCount(); i++) {
            Object o = spinner.getItemAtPosition(i);
            int cur = (o instanceof Group) ? ((Group) o).id : ((CustomerType) o).id;
            if (cur == id) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void save() {
        String n = name.getText().toString().trim();
        if (n.isEmpty()) {
            Toast.makeText(this, R.string.name_required, Toast.LENGTH_SHORT).show();
            return;
        }
        int gid = groups.get(group.getSelectedItemPosition()).id;
        int tid = types.get(type.getSelectedItemPosition()).id;
        String gsm = phone.getText().toString().trim();

        if (id == 0) {
            dao.addCustomer(n, gsm, gid, tid);
        } else {
            dao.updateCustomer(id, n, gsm, gid, tid);
        }
        Toast.makeText(this, R.string.done, Toast.LENGTH_SHORT).show();
        finish();
    }
}
