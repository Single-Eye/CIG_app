package com.example.retailordermanager;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * CreateOrderActivity — build and submit a new customer order.
 *
 * Workflow:
 *  1. Enter customer name and phone.
 *  2. Pick a product from the Spinner and type a quantity.
 *  3. "Add to Order" appends the item to a temporary list; running total updates.
 *  4. Repeat steps 2–3 for every product needed.
 *  5. "Submit Order" saves customer, order header, and all line items to SQLite.
 */
public class CreateOrderActivity extends AppCompatActivity {

    private EditText editTextCustomerName;
    private EditText editTextPhoneNumber;
    private Spinner  spinnerProduct;
    private EditText editTextOrderQuantity;
    private Button   buttonAddToOrder;
    private ListView listViewOrderItems;
    private TextView textViewTotal;
    private Button   buttonSubmitOrder;

    private DatabaseHelper          databaseHelper;
    private List<Product>           productList;    // products loaded into the Spinner
    private List<OrderItem>         orderItemList;  // items added to the current order
    private ArrayAdapter<OrderItem> orderItemAdapter;
    private double totalAmount = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_order);

        // ── Status bar colour ────────────────────────────────────────────────
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(
                    ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        // ── Toolbar setup ────────────────────────────────────────────────────
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Create Order");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Connect views
        editTextCustomerName  = findViewById(R.id.editTextCustomerName);
        editTextPhoneNumber   = findViewById(R.id.editTextPhoneNumber);
        spinnerProduct        = findViewById(R.id.spinnerProduct);
        editTextOrderQuantity = findViewById(R.id.editTextOrderQuantity);
        buttonAddToOrder      = findViewById(R.id.buttonAddToOrder);
        listViewOrderItems    = findViewById(R.id.listViewOrderItems);
        textViewTotal         = findViewById(R.id.textViewTotal);
        buttonSubmitOrder     = findViewById(R.id.buttonSubmitOrder);

        databaseHelper = new DatabaseHelper(this);
        productList    = new ArrayList<>();
        orderItemList  = new ArrayList<>();

        // Use our card-style row layout; R.id.textOrderItem is the TextView inside it.
        // ArrayAdapter calls toString() on each OrderItem to fill that TextView.
        orderItemAdapter = new ArrayAdapter<>(this,
                R.layout.list_item_create_order_item, R.id.textOrderItem, orderItemList);
        listViewOrderItems.setAdapter(orderItemAdapter);

        loadProductsIntoSpinner();

        buttonAddToOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItemToOrder();
            }
        });

        buttonSubmitOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitOrder();
            }
        });
    }

    /** Back arrow on toolbar closes this Activity. */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /** Loads all products from the database into the Spinner dropdown. */
    private void loadProductsIntoSpinner() {
        productList.clear();
        Cursor cursor = databaseHelper.getAllProducts();
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    int    id    = cursor.getInt(cursor.getColumnIndexOrThrow("product_id"));
                    String name  = cursor.getString(cursor.getColumnIndexOrThrow("product_name"));
                    String desc  = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                    double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
                    int    qty   = cursor.getInt(cursor.getColumnIndexOrThrow("quantity_in_stock"));
                    productList.add(new Product(id, name, desc, price, qty));
                }
            } finally {
                cursor.close();
            }
        }
        ArrayAdapter<Product> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, productList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProduct.setAdapter(spinnerAdapter);
    }

    /**
     * Validates the chosen product and quantity, then adds the line item
     * to the temporary list and updates the running total.
     */
    private void addItemToOrder() {
        if (productList.isEmpty()) {
            Toast.makeText(this, "No products available. Please add products first.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String quantityStr = editTextOrderQuantity.getText().toString().trim();
        if (quantityStr.isEmpty()) {
            editTextOrderQuantity.setError("Enter a quantity");
            editTextOrderQuantity.requestFocus();
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                editTextOrderQuantity.setError("Quantity must be at least 1");
                editTextOrderQuantity.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            editTextOrderQuantity.setError("Enter a valid whole number");
            editTextOrderQuantity.requestFocus();
            return;
        }

        // Get the selected product from the Spinner
        Product selected = productList.get(spinnerProduct.getSelectedItemPosition());

        // Query the database for the LIVE stock count.
        // We never rely on the value stored in the Product object because it was
        // loaded when the spinner was populated and may be stale if another order
        // was placed in the same session.
        int availableStock = databaseHelper.getProductStock(selected.getProductId());
        if (quantity > availableStock) {
            Toast.makeText(this,
                    "Not enough stock. Only " + availableStock + " available.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the line item and add it to the temporary order list
        OrderItem item = new OrderItem(
                selected.getProductId(), selected.getProductName(),
                quantity, selected.getPrice());
        orderItemList.add(item);
        orderItemAdapter.notifyDataSetChanged();

        // Update the running total
        totalAmount += item.getSubtotal();
        textViewTotal.setText("Total: USh " + String.format("%,.0f", totalAmount));

        editTextOrderQuantity.setText(""); // clear for the next item
        Toast.makeText(this, "Item added to order", Toast.LENGTH_SHORT).show();
    }

    /**
     * Saves the customer, order header, and all line items to the database.
     * Called when the employee taps "Submit Order".
     */
    private void submitOrder() {
        String customerName = editTextCustomerName.getText().toString().trim();
        String phoneNumber  = editTextPhoneNumber.getText().toString().trim();

        if (customerName.isEmpty()) {
            editTextCustomerName.setError("Customer name is required");
            editTextCustomerName.requestFocus();
            return;
        }
        if (phoneNumber.isEmpty()) {
            editTextPhoneNumber.setError("Phone number is required");
            editTextPhoneNumber.requestFocus();
            return;
        }
        if (orderItemList.isEmpty()) {
            Toast.makeText(this, "Please add at least one product to the order",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Step 0: Re-check live stock for every item before committing.
            // Stock could have changed between when the item was added to the
            // temporary list and when the user tapped Submit Order.
            for (OrderItem item : orderItemList) {
                int available = databaseHelper.getProductStock(item.getProductId());
                if (item.getQuantity() > available) {
                    Toast.makeText(this,
                            "\"" + item.getProductName() + "\" only has "
                                    + available + " left in stock. Please remove it and re-add.",
                            Toast.LENGTH_LONG).show();
                    return;
                }
            }

            // Step 1: Save the customer record
            long customerId = databaseHelper.insertCustomer(customerName, phoneNumber);
            if (customerId == -1) {
                Toast.makeText(this, "Error saving customer data", Toast.LENGTH_SHORT).show();
                return;
            }

            // Step 2: Save the order header with today's date/time
            String orderDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(new Date());
            long orderId = databaseHelper.insertOrder(customerId, orderDate, totalAmount);
            if (orderId == -1) {
                Toast.makeText(this, "Error saving order", Toast.LENGTH_SHORT).show();
                return;
            }

            // Step 3: Save each line item
            boolean allSaved = true;
            for (OrderItem item : orderItemList) {
                long result = databaseHelper.insertOrderItem(
                        orderId, item.getProductId(), item.getQuantity(), item.getUnitPrice());
                if (result == -1) allSaved = false;
            }

            if (allSaved) {
                // Step 4: Reduce the stock of every product that was ordered.
                // This is the bug fix — without this loop, stock never decreases.
                for (OrderItem item : orderItemList) {
                    databaseHelper.reduceProductStock(item.getProductId(), item.getQuantity());
                }
                Toast.makeText(this,
                        "Order #" + orderId + " submitted!  Total: USh "
                                + String.format("%,.0f", totalAmount),
                        Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, "Order saved but some items may not have been recorded.",
                        Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Unexpected error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
