package com.valdio.valdioveliu.recyclerview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.VH> {

    public interface Listener {
        void onClick(Reminder r);
    }

    private final List<Reminder> items;
    private final Listener listener;

    public ReminderAdapter(List<Reminder> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reminder, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Reminder r = items.get(position);
        h.customer.setText(r.cusName);
        h.when.setText((r.date == null ? "" : r.date) + "   " + (r.time == null ? "" : r.time));
        h.notes.setText(r.notes);
        h.itemView.setOnClickListener(v -> listener.onClick(r));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView customer, when, notes;

        VH(@NonNull View itemView) {
            super(itemView);
            customer = itemView.findViewById(R.id.customer);
            when = itemView.findViewById(R.id.when);
            notes = itemView.findViewById(R.id.notes);
        }
    }
}
