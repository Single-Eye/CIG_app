package com.example.retailordermanager;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

/**
 * DashboardActivity — main navigation hub of Inkify.
 *
 * Shows a 2×2 grid of coloured CardView cards. Each card opens a different
 * section of the app. A Logout button at the bottom clears the back-stack
 * and returns to LoginActivity.
 */
public class DashboardActivity extends AppCompatActivity {

    // The four navigation cards
    private CardView cardViewProducts;
    private CardView cardViewAddProduct;
    private CardView cardViewCreateOrder;
    private CardView cardViewOrderHistory;

    // Logout button at the bottom
    private Button buttonLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // ── Status bar colour ────────────────────────────────────────────────
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(
                    ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        // ── Toolbar setup ────────────────────────────────────────────────────
        // setSupportActionBar() tells AppCompat to use our custom Toolbar
        // instead of the default ActionBar (which is hidden by NoActionBar theme)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Inkify");
            // No back arrow on the Dashboard — it IS the home screen
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        // ── Connect CardViews and Logout button ──────────────────────────────
        cardViewProducts    = findViewById(R.id.cardViewProducts);
        cardViewAddProduct  = findViewById(R.id.cardViewAddProduct);
        cardViewCreateOrder = findViewById(R.id.cardViewCreateOrder);
        cardViewOrderHistory= findViewById(R.id.cardViewOrderHistory);
        buttonLogout        = findViewById(R.id.buttonLogout);

        // ── Card click listeners ─────────────────────────────────────────────

        // "View Products" card → ProductListActivity
        cardViewProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, ProductListActivity.class));
            }
        });

        // "Add Product" card → AddProductActivity (add-new mode, no extras)
        cardViewAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, AddProductActivity.class));
            }
        });

        // "Create Order" card → CreateOrderActivity
        cardViewCreateOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, CreateOrderActivity.class));
            }
        });

        // "Order History" card → OrderHistoryActivity
        cardViewOrderHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, OrderHistoryActivity.class));
            }
        });

        // Logout → clear the entire activity back-stack and go to LoginActivity
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
                // FLAG_ACTIVITY_CLEAR_TASK removes all activities so Back won't
                // bring the user back to the Dashboard after logging out
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * onResume is called every time this screen becomes visible —
     * including when the user returns from Create Order or Add Product.
     * This keeps the Business Summary numbers always up to date.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadSummaryData();
    }

    /**
     * Queries the database for total orders, revenue, and stock,
     * then displays the results in the Business Summary card.
     */
    private void loadSummaryData() {
        DatabaseHelper db = new DatabaseHelper(this);

        int    totalOrders  = db.getTotalOrderCount();
        double totalRevenue = db.getTotalRevenue();
        int    totalStock   = db.getTotalStock();

        // Find the three TextViews in the summary card
        TextView txtOrders  = findViewById(R.id.txtTotalOrders);
        TextView txtRevenue = findViewById(R.id.txtTotalRevenue);
        TextView txtStock   = findViewById(R.id.txtTotalStock);

        // Display the values — format revenue with comma separator (e.g. "USh 12,500")
        txtOrders.setText(String.valueOf(totalOrders));
        txtRevenue.setText("USh " + String.format("%,.0f", totalRevenue));
        txtStock.setText(String.valueOf(totalStock));
    }
}
