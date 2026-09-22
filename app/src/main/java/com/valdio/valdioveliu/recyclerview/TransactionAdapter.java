package com.valdio.valdioveliu.recyclerview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.VH> {

    public interface Listener {
        void onClick(Transaction t);
    }

    private final List<Transaction> items;
    private final Listener listener;

    public TransactionAdapter(List<Transaction> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Transaction t = items.get(position);
        String remarks = t.remarks == null ? "" : t.remarks;
        String line = remarks;
        if (t.tCusId != 0) {
            String tag = h.itemView.getContext().getString(R.string.transfer_tag);
            line = tag + " — " + remarks;
        }
        if (t.currName != null && !t.currName.isEmpty()) {
            line += "  (" + t.currName + ")";
        }
        h.remarks.setText(line);
        h.date.setText(t.date);
        h.amount.setText(Fmt.signed(t.signed));
        h.amount.setTextColor(ContextCompat.getColor(h.amount.getContext(),
                t.signed >= 0 ? R.color.green : R.color.red));
        h.itemView.setOnClickListener(v -> listener.onClick(t));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView remarks, date, amount;

        VH(@NonNull View itemView) {
            super(itemView);
            remarks = itemView.findViewById(R.id.remarks);
            date = itemView.findViewById(R.id.date);
            amount = itemView.findViewById(R.id.amount);
        }
    }
}
