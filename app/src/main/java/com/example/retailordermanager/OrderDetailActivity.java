package com.example.retailordermanager;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderDetailActivity — complete information for one specific order.
 *
 * Receives "order_id" via Intent extras, then fetches:
 *   • Order header: customer name, phone, date, total  (from getOrderById)
 *   • Line items:   product name, qty, unit price        (from getOrderItems)
 */
public class OrderDetailActivity extends AppCompatActivity {

    private TextView textViewDetailOrderId;
    private TextView textViewDetailCustomerName;
    private TextView textViewDetailPhone;
    private TextView textViewDetailDate;
    private TextView textViewDetailTotal;
    private ListView listViewOrderDetailItems;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        // ── Status bar colour ────────────────────────────────────────────────
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(
                    ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        // ── Toolbar setup ────────────────────────────────────────────────────
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Order Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Connect views
        textViewDetailOrderId      = findViewById(R.id.textViewDetailOrderId);
        textViewDetailCustomerName = findViewById(R.id.textViewDetailCustomerName);
        textViewDetailPhone        = findViewById(R.id.textViewDetailPhone);
        textViewDetailDate         = findViewById(R.id.textViewDetailDate);
        textViewDetailTotal        = findViewById(R.id.textViewDetailTotal);
        listViewOrderDetailItems   = findViewById(R.id.listViewOrderDetailItems);

        databaseHelper = new DatabaseHelper(this);

        // Get the order ID passed from OrderHistoryActivity
        int orderId = getIntent().getIntExtra("order_id", -1);
        if (orderId == -1) {
            Toast.makeText(this, "Error: order not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadOrderDetails(orderId);
        loadOrderItems(orderId);
    }

    /** Back arrow on toolbar closes this Activity. */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /**
     * Fetches the order header row and fills the summary TextViews.
     *
     * @param orderId The order to display
     */
    private void loadOrderDetails(int orderId) {
        Cursor cursor = databaseHelper.getOrderById(orderId);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int    id           = cursor.getInt(cursor.getColumnIndexOrThrow("order_id"));
                    String customerName = cursor.getString(cursor.getColumnIndexOrThrow("customer_name"));
                    String phone        = cursor.getString(cursor.getColumnIndexOrThrow("phone_number"));
                    String date         = cursor.getString(cursor.getColumnIndexOrThrow("order_date"));
                    double total        = cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount"));

                    textViewDetailOrderId.setText("Order #" + id);
                    textViewDetailCustomerName.setText("Customer: " + customerName);
                    textViewDetailPhone.setText("Phone: " + phone);
                    textViewDetailDate.setText("Date: " + date);
                    textViewDetailTotal.setText("Total: USh " + String.format("%,.0f", total));
                }
            } finally {
                cursor.close();
            }
        }
    }

    /**
     * Reads all line items for the order and populates the ListView.
     * Each entry shows: product name, quantity, unit price, and subtotal.
     *
     * @param orderId The order whose items to show
     */
    private void loadOrderItems(int orderId) {
        List<String> itemDisplayList = new ArrayList<>();

        Cursor cursor = databaseHelper.getOrderItems(orderId);
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    String productName = cursor.getString(cursor.getColumnIndexOrThrow("product_name"));
                    int    quantity    = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
                    double unitPrice   = cursor.getDouble(cursor.getColumnIndexOrThrow("unit_price"));
                    double subtotal    = quantity * unitPrice;

                    String itemText = productName + "\n"
                            + "  Qty: " + quantity
                            + "   Unit: USh " + String.format("%,.0f", unitPrice)
                            + "   Subtotal: USh " + String.format("%,.0f", subtotal);
                    itemDisplayList.add(itemText);
                }
            } finally {
                cursor.close();
            }
        }

        if (itemDisplayList.isEmpty()) {
            itemDisplayList.add("No items found for this order.");
        }

        // simple_list_item_1 is a built-in Android layout with one TextView per row
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, itemDisplayList);
        listViewOrderDetailItems.setAdapter(adapter);
    }
}
