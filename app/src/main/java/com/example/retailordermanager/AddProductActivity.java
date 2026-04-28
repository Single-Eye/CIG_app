package com.example.retailordermanager;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

/**
 * AddProductActivity — form for adding a new product or editing an existing one.
 *
 * If the Intent contains a "product_id" extra, the form loads that product's
 * data from the database and switches to edit mode.
 * Without the extra, the form starts blank in add-new mode.
 */
public class AddProductActivity extends AppCompatActivity {

    private TextView textViewFormTitle;
    private EditText editTextProductName;
    private EditText editTextDescription;
    private EditText editTextPrice;
    private EditText editTextQuantity;
    private Button   buttonSaveProduct;

    private DatabaseHelper databaseHelper;

    // productId = -1 means "add new"; any other value means "edit existing"
    private int productId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // ── Status bar colour ────────────────────────────────────────────────
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(
                    ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        // ── Toolbar setup ────────────────────────────────────────────────────
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Connect views
        textViewFormTitle   = findViewById(R.id.textViewFormTitle);
        editTextProductName = findViewById(R.id.editTextProductName);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextPrice       = findViewById(R.id.editTextPrice);
        editTextQuantity    = findViewById(R.id.editTextQuantity);
        buttonSaveProduct   = findViewById(R.id.buttonSaveProduct);

        databaseHelper = new DatabaseHelper(this);

        // Check whether we were opened with a product to edit
        if (getIntent().hasExtra("product_id")) {
            productId = getIntent().getIntExtra("product_id", -1);
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Edit Product");
            textViewFormTitle.setText("Edit Product");
            loadProductData(productId);
        } else {
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Add Product");
            textViewFormTitle.setText("Add New Product");
        }

        buttonSaveProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProduct();
            }
        });
    }

    /** Back arrow on toolbar closes this Activity. */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /**
     * Reads an existing product from the database and pre-fills all form fields.
     *
     * @param id The product_id to load
     */
    private void loadProductData(int id) {
        Cursor cursor = databaseHelper.getProductById(id);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    editTextProductName.setText(cursor.getString(cursor.getColumnIndexOrThrow("product_name")));
                    editTextDescription.setText(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                    editTextPrice.setText(String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow("price"))));
                    editTextQuantity.setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("quantity_in_stock"))));
                }
            } finally {
                cursor.close();
            }
        }
    }

    /**
     * Validates all fields and then inserts (or updates) the product in the database.
     *
     * Rules: all fields required; price > 0; quantity >= 0.
     */
    private void saveProduct() {
        String name        = editTextProductName.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String priceStr    = editTextPrice.getText().toString().trim();
        String quantityStr = editTextQuantity.getText().toString().trim();

        // ── Field presence checks ─────────────────────────────────────────────
        if (name.isEmpty()) {
            editTextProductName.setError("Product name is required");
            editTextProductName.requestFocus();
            return;
        }
        if (description.isEmpty()) {
            editTextDescription.setError("Description is required");
            editTextDescription.requestFocus();
            return;
        }
        if (priceStr.isEmpty()) {
            editTextPrice.setError("Price is required");
            editTextPrice.requestFocus();
            return;
        }
        if (quantityStr.isEmpty()) {
            editTextQuantity.setError("Quantity is required");
            editTextQuantity.requestFocus();
            return;
        }

        // ── Parse and validate price ──────────────────────────────────────────
        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) {
                editTextPrice.setError("Price must be greater than 0");
                editTextPrice.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            editTextPrice.setError("Enter a valid price (e.g. 9.99)");
            editTextPrice.requestFocus();
            return;
        }

        // ── Parse and validate quantity ───────────────────────────────────────
        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity < 0) {
                editTextQuantity.setError("Quantity cannot be negative");
                editTextQuantity.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            editTextQuantity.setError("Enter a whole number (e.g. 50)");
            editTextQuantity.requestFocus();
            return;
        }

        // ── Save to database ──────────────────────────────────────────────────
        if (productId == -1) {
            long result = databaseHelper.insertProduct(name, description, price, quantity);
            if (result != -1) {
                Toast.makeText(this, "Product added successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error: failed to add product", Toast.LENGTH_SHORT).show();
            }
        } else {
            int rows = databaseHelper.updateProduct(productId, name, description, price, quantity);
            if (rows > 0) {
                Toast.makeText(this, "Product updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error: failed to update product", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
