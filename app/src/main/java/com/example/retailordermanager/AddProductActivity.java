package com.example.retailordermanager;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

/**
 * AddProductActivity — form for adding a new product or editing an existing one.
 *
 * New in this version: users can pick a photo from the phone gallery.
 * The image URI is stored in the database and displayed in the product list.
 *
 * If the Intent contains a "product_id" extra, the form loads existing data
 * for editing. Without the extra, the form starts blank for adding.
 */
public class AddProductActivity extends AppCompatActivity {

    // Request code we use when starting the gallery intent
    private static final int PICK_IMAGE_REQUEST = 1;

    private TextView  textViewFormTitle;
    private EditText  editTextProductName;
    private EditText  editTextDescription;
    private EditText  editTextPrice;
    private EditText  editTextQuantity;
    private Button    buttonSaveProduct;
    private ImageView imagePreview;    // shows the chosen or existing product photo
    private Button    btnChooseImage;  // opens the phone's gallery

    private DatabaseHelper databaseHelper;
    private int    productId         = -1; // -1 = add new mode; any other value = edit mode
    private String selectedImagePath = ""; // URI string of a newly chosen image (empty if none picked)
    private String existingImagePath = ""; // URI string already in the database (edit mode only)

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

        // ── Connect all views to their XML counterparts ───────────────────────
        textViewFormTitle   = findViewById(R.id.textViewFormTitle);
        editTextProductName = findViewById(R.id.editTextProductName);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextPrice       = findViewById(R.id.editTextPrice);
        editTextQuantity    = findViewById(R.id.editTextQuantity);
        buttonSaveProduct   = findViewById(R.id.buttonSaveProduct);
        imagePreview        = findViewById(R.id.imagePreview);
        btnChooseImage      = findViewById(R.id.btnChooseImage);

        databaseHelper = new DatabaseHelper(this);

        // ── Decide if we are adding a new product or editing an existing one ──
        if (getIntent().hasExtra("product_id")) {
            productId = getIntent().getIntExtra("product_id", -1);
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Edit Product");
            textViewFormTitle.setText("Edit Product");
            loadProductData(productId); // fill the form with existing values
        } else {
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Add Product");
            textViewFormTitle.setText("Add New Product");
        }

        // ── "Choose Image" opens the system file picker for photos ───────────
        btnChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ACTION_GET_CONTENT lets the user pick any image from
                // their gallery or file manager without needing special permissions
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });

        // ── "Save Product" validates and writes to the database ───────────────
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
     * Called automatically after the user picks an image from the gallery.
     * Saves the image URI and shows a preview at the top of the form.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                // Request a persistable permission so this URI still works
                // after the app is closed and reopened
                try {
                    getContentResolver().takePersistableUriPermission(
                            imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception e) {
                    // Not all content providers grant persistable permissions — OK to continue
                }

                // Store the URI as a string so we can save it in the database
                selectedImagePath = imageUri.toString();

                // Show the image in the preview ImageView
                imagePreview.setImageURI(imageUri);
                // Remove any color tint so the photo shows in natural colors
                ImageViewCompat.setImageTintList(imagePreview, null);
                imagePreview.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Reads the existing product from the database and fills in all form fields.
     * Also displays the saved photo if one exists.
     *
     * @param id The product_id to load
     */
    private void loadProductData(int id) {
        Cursor cursor = databaseHelper.getProductById(id);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    editTextProductName.setText(
                            cursor.getString(cursor.getColumnIndexOrThrow("product_name")));
                    editTextDescription.setText(
                            cursor.getString(cursor.getColumnIndexOrThrow("description")));
                    editTextPrice.setText(
                            String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow("price"))));
                    editTextQuantity.setText(
                            String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("quantity_in_stock"))));

                    // Load the existing product photo if one was saved
                    // getColumnIndex returns -1 if the column doesn't exist (safety check)
                    int imgColIndex = cursor.getColumnIndex("image_path");
                    if (imgColIndex >= 0) {
                        String savedPath = cursor.getString(imgColIndex);
                        if (savedPath != null && !savedPath.isEmpty()) {
                            existingImagePath = savedPath; // remember so we don't lose it on save
                            try {
                                Uri imageUri = Uri.parse(savedPath);
                                imagePreview.setImageURI(imageUri);
                                ImageViewCompat.setImageTintList(imagePreview, null);
                                imagePreview.setVisibility(View.VISIBLE);
                            } catch (Exception e) {
                                // URI might no longer point to a valid file — skip the preview
                            }
                        }
                    }
                }
            } finally {
                cursor.close();
            }
        }
    }

    /**
     * Validates all fields and saves (or updates) the product in the database.
     * Rules: all text fields required; price > 0; quantity >= 0.
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
                editTextQuantity.setError("Stock quantity cannot be negative");
                editTextQuantity.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            editTextQuantity.setError("Enter a whole number (e.g. 50)");
            editTextQuantity.requestFocus();
            return;
        }

        // ── Decide which image path to save ───────────────────────────────────
        // If the user picked a NEW image this session, use it.
        // If not, keep whatever image path was already stored in the database.
        String imageToSave = selectedImagePath.isEmpty() ? existingImagePath : selectedImagePath;

        // ── Write to database ─────────────────────────────────────────────────
        if (productId == -1) {
            // Adding a brand new product
            long result = databaseHelper.insertProduct(
                    name, description, price, quantity, imageToSave);
            if (result != -1) {
                Toast.makeText(this, "Product added successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error: failed to add product", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Updating an existing product
            int rows = databaseHelper.updateProduct(
                    productId, name, description, price, quantity, imageToSave);
            if (rows > 0) {
                Toast.makeText(this, "Product updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error: failed to update product", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
