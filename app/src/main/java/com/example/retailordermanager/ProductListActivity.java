package com.example.retailordermanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

/**
 * ProductListActivity — shows all products in a scrollable ListView.
 *
 * Long-press any row → Edit or Delete dialog.
 * FAB (bottom-right) → opens AddProductActivity to add a new product.
 * List refreshes automatically every time this screen becomes visible.
 */
public class ProductListActivity extends AppCompatActivity {

    private ListView              listViewProducts;
    private FloatingActionButton  fabAddProduct;
    private DatabaseHelper        databaseHelper;
    private ProductAdapter        productAdapter;
    private List<Product>         productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        // ── Status bar colour ────────────────────────────────────────────────
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(
                    ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        // ── Toolbar setup ────────────────────────────────────────────────────
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Products");
            // Show a back arrow so the user can return to the Dashboard
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        databaseHelper = new DatabaseHelper(this);
        productList    = new ArrayList<>();

        listViewProducts = findViewById(R.id.listViewProducts);
        fabAddProduct    = findViewById(R.id.fabAddProduct);

        // Attach the custom adapter — it reads from productList at all times
        productAdapter = new ProductAdapter(this, productList);
        listViewProducts.setAdapter(productAdapter);

        // FAB → open AddProductActivity with no extras (add-new mode)
        fabAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProductListActivity.this, AddProductActivity.class));
            }
        });

        // Long-press a row → show Edit / Delete dialog
        listViewProducts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showOptionsDialog(position);
                return true;
            }
        });
    }

    /**
     * Pressing the toolbar back arrow calls this method.
     * finish() closes this Activity and returns to the previous screen.
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /**
     * Refresh the product list every time the user returns to this screen
     * (e.g. after adding or editing a product in AddProductActivity).
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }

    /**
     * Queries the database for all products and refreshes the ListView.
     */
    private void loadProducts() {
        productList.clear();
        Cursor cursor = databaseHelper.getAllProducts();
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    int    id        = cursor.getInt(cursor.getColumnIndexOrThrow("product_id"));
                    String name      = cursor.getString(cursor.getColumnIndexOrThrow("product_name"));
                    String desc      = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                    double price     = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
                    int    qty       = cursor.getInt(cursor.getColumnIndexOrThrow("quantity_in_stock"));
                    // getColumnIndex returns -1 if the column is missing (safe for old DB)
                    int    imgCol    = cursor.getColumnIndex("image_path");
                    String imgPath   = (imgCol >= 0) ? cursor.getString(imgCol) : null;
                    productList.add(new Product(id, name, desc, price, qty, imgPath));
                }
            } finally {
                cursor.close(); // always close the Cursor to free resources
            }
        }
        productAdapter.notifyDataSetChanged();
    }

    /**
     * Shows an AlertDialog with "Edit" and "Delete" options for the tapped product.
     *
     * @param position Index of the product in productList
     */
    private void showOptionsDialog(final int position) {
        final Product product = productList.get(position);
        String[] options = {"Edit", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(product.getProductName());
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    // Edit — pass the product ID so AddProductActivity loads existing data
                    Intent intent = new Intent(ProductListActivity.this, AddProductActivity.class);
                    intent.putExtra("product_id", product.getProductId());
                    startActivity(intent);
                } else {
                    // Delete — ask for confirmation first
                    confirmDelete(product);
                }
            }
        });
        builder.show();
    }

    /**
     * Shows a confirmation dialog before permanently deleting a product.
     *
     * @param product The product the user wants to remove
     */
    private void confirmDelete(final Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Product");
        builder.setMessage("Are you sure you want to delete \"" + product.getProductName() + "\"?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int rows = databaseHelper.deleteProduct(product.getProductId());
                if (rows > 0) {
                    Toast.makeText(ProductListActivity.this,
                            "\"" + product.getProductName() + "\" deleted", Toast.LENGTH_SHORT).show();
                    loadProducts();
                } else {
                    Toast.makeText(ProductListActivity.this,
                            "Failed to delete product", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
