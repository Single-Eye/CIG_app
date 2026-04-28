package com.example.retailordermanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

/**
 * OrderAdapter bridges a List<Order> to the ListView in OrderHistoryActivity.
 *
 * Each row shows: Order ID, customer name, date, and total amount.
 * It inflates list_item_order.xml for every visible row.
 */
public class OrderAdapter extends ArrayAdapter<Order> {

    private final Context     context;
    private final List<Order> orderList;

    /**
     * Creates a new OrderAdapter.
     *
     * @param context   The Activity that owns the ListView
     * @param orderList The list of orders to display
     */
    public OrderAdapter(Context context, List<Order> orderList) {
        super(context, 0, orderList);
        this.context   = context;
        this.orderList = orderList;
    }

    /**
     * Called by the ListView for every visible row.
     * Inflates (or reuses) list_item_order.xml and fills in the order data.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.list_item_order, parent, false);
        }

        Order order = orderList.get(position);

        TextView textViewOrderId     = convertView.findViewById(R.id.textViewOrderId);
        TextView textViewCustomer    = convertView.findViewById(R.id.textViewOrderCustomerName);
        TextView textViewDate        = convertView.findViewById(R.id.textViewOrderDate);
        TextView textViewTotal       = convertView.findViewById(R.id.textViewOrderTotal);

        textViewOrderId.setText("Order #" + order.getOrderId());
        textViewCustomer.setText(order.getCustomerName());
        textViewDate.setText(order.getOrderDate());
        textViewTotal.setText("USh " + String.format("%,.0f", order.getTotalAmount()));

        return convertView;
    }
}
