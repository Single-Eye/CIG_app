package com.example.retailordermanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

/**
 * OrderAdapter bridges a List<Order> to the Order History ListView.
 * Each row uses order_history_card_item.xml — a card with a gold circle
 * icon, order details, and the total amount.
 */
public class OrderAdapter extends ArrayAdapter<Order> {

    private final Context     context;
    private final List<Order> orderList;

    public OrderAdapter(Context context, List<Order> orderList) {
        super(context, 0, orderList);
        this.context   = context;
        this.orderList = orderList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Inflate the card layout once, then reuse it for subsequent rows
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.order_history_card_item, parent, false);
        }

        Order order = orderList.get(position);

        // Find the TextViews inside the card
        TextView orderTitle    = convertView.findViewById(R.id.orderTitle);
        TextView orderCustomer = convertView.findViewById(R.id.orderCustomer);
        TextView orderDate     = convertView.findViewById(R.id.orderDate);
        TextView orderTotal    = convertView.findViewById(R.id.orderTotal);

        // Fill in the order data
        orderTitle.setText("Order #" + order.getOrderId());
        orderCustomer.setText(order.getCustomerName());
        orderDate.setText(order.getOrderDate());
        // Format total with comma thousands separator (e.g. "USh 12,500")
        orderTotal.setText("USh " + String.format("%,.0f", order.getTotalAmount()));

        return convertView;
    }
}
