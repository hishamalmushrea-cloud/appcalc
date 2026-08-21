package com.valdio.valdioveliu.recyclerview;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SearchActivity extends BaseActivity {

    private Dao dao;
    private EditText query;
    private RecyclerView list;
    private TextView empty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setTitle(R.string.search_title);

        dao = new Dao(this);
        query = findViewById(R.id.query);
        list = findViewById(R.id.list);
        empty = findViewById(R.id.empty);
        list.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.search).setOnClickListener(v -> doSearch());
        query.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch();
                return true;
            }
            return false;
        });
    }

    private void doSearch() {
        String q = query.getText().toString().trim();
        if (q.isEmpty()) return;
        List<Customer> result = dao.search(q);

        list.setAdapter(new CustomerAdapter(result, new CustomerAdapter.Listener() {
            @Override
            public void onClick(Customer c) {
                Intent i = new Intent(SearchActivity.this, AccountActivity.class);
                i.putExtra("id", c.id);
                i.putExtra("name", c.name);
                startActivity(i);
            }

            @Override
            public boolean onLongClick(Customer c) {
                return false;
            }
        }, isRtl()));

        empty.setVisibility(result.isEmpty() ? View.VISIBLE : View.GONE);
        list.setVisibility(result.isEmpty() ? View.GONE : View.VISIBLE);
    }
}
