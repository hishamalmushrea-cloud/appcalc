package com.valdio.valdioveliu.recyclerview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.VH> {

    private final List<ReportRow> items;

    public ReportAdapter(List<ReportRow> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ReportRow row = items.get(position);
        h.title.setText(row.title);
        h.sub.setText(row.sub);
        h.sub.setVisibility(row.sub == null || row.sub.isEmpty() ? View.GONE : View.VISIBLE);
        h.c1.setText(row.c1);
        h.c2.setText(row.c2);
        h.c3.setText(row.c3);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, sub, c1, c2, c3;

        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            sub = itemView.findViewById(R.id.sub);
            c1 = itemView.findViewById(R.id.c1);
            c2 = itemView.findViewById(R.id.c2);
            c3 = itemView.findViewById(R.id.c3);
        }
    }
}
