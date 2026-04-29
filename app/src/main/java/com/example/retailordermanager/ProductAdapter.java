package com.example.retailordermanager;

import android.content.Context;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import java.util.List;

/**
 * ProductAdapter bridges a List<Product> to the Products ListView.
 * Each row uses product_card_item.xml — a shopping-app style card
 * with a photo thumbnail, name, price, and stock indicator.
 */
public class ProductAdapter extends ArrayAdapter<Product> {

    private final Context       context;
    private final List<Product> productList;

    public ProductAdapter(Context context, List<Product> productList) {
        super(context, 0, productList);
        this.context     = context;
        this.productList = productList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Reuse an existing card view to keep scrolling smooth
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.product_card_item, parent, false);
        }

        Product product = productList.get(position);

        // Find each view inside the card layout
        ImageView productImage = convertView.findViewById(R.id.productImage);
        TextView  productName  = convertView.findViewById(R.id.productName);
        TextView  productPrice = convertView.findViewById(R.id.productPrice);
        TextView  productStock = convertView.findViewById(R.id.productStock);

        // ── Product name ──────────────────────────────────────────────────────
        productName.setText(product.getProductName());

        // ── Price with comma thousands separator (e.g. "USh 10,000") ──────────
        productPrice.setText("USh " + String.format("%,.0f", product.getPrice()));

        // ── Stock — show red "Out of Stock" when qty is zero ──────────────────
        int stock = product.getQuantityInStock();
        if (stock <= 0) {
            productStock.setText("Out of Stock");
            // colorButtonDanger is the red color defined in colors.xml
            productStock.setTextColor(
                    ContextCompat.getColor(context, R.color.colorButtonDanger));
        } else {
            productStock.setText("Stock: " + stock);
            productStock.setTextColor(
                    ContextCompat.getColor(context, R.color.colorTextSecondary));
        }

        // ── Product image ─────────────────────────────────────────────────────
        String imagePath = product.getImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            // A photo URI was saved — try to load it
            try {
                Uri imageUri = Uri.parse(imagePath);
                productImage.setImageURI(imageUri);
                // Clear any tint so the real photo shows in its natural colors
                ImageViewCompat.setImageTintList(productImage, null);
            } catch (Exception e) {
                // URI no longer valid (e.g. file moved) — fall back to placeholder
                showPlaceholderIcon(productImage);
            }
        } else {
            // No image saved — show the navy placeholder icon
            showPlaceholderIcon(productImage);
        }

        return convertView;
    }

    /**
     * Sets the default navy placeholder icon.
     * The ic_products vector is white, so we tint it navy to make it
     * visible against the light gray rounded_image_background.
     */
    private void showPlaceholderIcon(ImageView imageView) {
        imageView.setImageResource(R.drawable.ic_products);
        ImageViewCompat.setImageTintList(imageView,
                ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.colorPrimary)));
    }
}
