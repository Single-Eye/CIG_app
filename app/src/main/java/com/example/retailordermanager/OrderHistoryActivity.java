package com.example.retailordermanager;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderHistoryActivity — scrollable list of every order ever submitted.
 *
 * Each row shows: order number, customer name, date, and total.
 * Tap a row → OrderDetailActivity for the full breakdown.
 * List refreshes automatically every time this screen becomes visible.
 */
public class OrderHistoryActivity extends AppCompatActivity {

    private ListView      listViewOrders;
    private DatabaseHelper databaseHelper;
    private List<Order>   orderList;
    private OrderAdapter  orderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        // ── Status bar colour ────────────────────────────────────────────────
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(
                    ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        // ── Toolbar setup ────────────────────────────────────────────────────
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Order History");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        databaseHelper = new DatabaseHelper(this);
        orderList      = new ArrayList<>();

        listViewOrders = findViewById(R.id.listViewOrders);
        orderAdapter   = new OrderAdapter(this, orderList);
        listViewOrders.setAdapter(orderAdapter);

        // Tap a row → open OrderDetailActivity passing only the order ID
        listViewOrders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Order selected = orderList.get(position);
                Intent intent  = new Intent(OrderHistoryActivity.this, OrderDetailActivity.class);
                intent.putExtra("order_id", selected.getOrderId());
                startActivity(intent);
            }
        });
    }

    /** Back arrow on toolbar closes this Activity. */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /** Refresh the list every time the user navigates back to this screen. */
    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }

    /** Queries the database for all orders (newest first) and refreshes the ListView. */
    private void loadOrders() {
        orderList.clear();
        Cursor cursor = databaseHelper.getAllOrders();
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    int    orderId      = cursor.getInt(cursor.getColumnIndexOrThrow("order_id"));
                    String customerName = cursor.getString(cursor.getColumnIndexOrThrow("customer_name"));
                    String orderDate    = cursor.getString(cursor.getColumnIndexOrThrow("order_date"));
                    double totalAmount  = cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount"));
                    orderList.add(new Order(orderId, 0, customerName, orderDate, totalAmount));
                }
            } finally {
                cursor.close();
            }
        }
        orderAdapter.notifyDataSetChanged();
        if (orderList.isEmpty()) {
            Toast.makeText(this, "No orders yet. Create your first order!", Toast.LENGTH_SHORT).show();
        }
    }
}
