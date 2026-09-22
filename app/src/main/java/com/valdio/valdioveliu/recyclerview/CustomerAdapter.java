package com.valdio.valdioveliu.recyclerview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.VH> {

    public interface Listener {
        void onClick(Customer c);

        boolean onLongClick(Customer c);
    }

    private final List<Customer> items;
    private final Listener listener;
    private final boolean rtl;

    public CustomerAdapter(List<Customer> items, Listener listener, boolean rtl) {
        this.items = items;
        this.listener = listener;
        this.rtl = rtl;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_customer, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Customer c = items.get(position);
        h.name.setText(c.name);
        String sub = (c.groupName == null ? "" : c.groupName)
                + (c.gsm == null || c.gsm.isEmpty() ? "" : "  •  " + c.gsm);
        h.sub.setText(sub.trim());
        h.balance.setText(Fmt.signed(c.balance));
        h.balance.setTextColor(ContextCompat.getColor(h.balance.getContext(),
                c.balance >= 0 ? R.color.green : R.color.red));
        h.itemView.setOnClickListener(v -> listener.onClick(c));
        h.itemView.setOnLongClickListener(v -> listener.onLongClick(c));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, sub, balance;

        VH(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            sub = itemView.findViewById(R.id.sub);
            balance = itemView.findViewById(R.id.balance);
        }
    }
}
