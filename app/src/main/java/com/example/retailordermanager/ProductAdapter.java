package com.example.retailordermanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

/**
 * ProductAdapter bridges a List<Product> to a ListView.
 *
 * Each row in the list shows the product name, price, and current stock.
 * It inflates list_item_product.xml for every visible row and recycles
 * old View objects to keep scrolling fast.
 */
public class ProductAdapter extends ArrayAdapter<Product> {

    private final Context      context;
    private final List<Product> productList;

    /**
     * Creates a new ProductAdapter.
     *
     * @param context     The Activity that owns the ListView
     * @param productList The data to display — update this list and call notifyDataSetChanged()
     */
    public ProductAdapter(Context context, List<Product> productList) {
        super(context, 0, productList);
        this.context     = context;
        this.productList = productList;
    }

    /**
     * Called by the ListView for every visible row.
     * Inflates (or reuses) list_item_product.xml and fills in the product data.
     *
     * @param position    The index of this item in productList
     * @param convertView A recycled row view to reuse (may be null on first pass)
     * @param parent      The ListView that owns this row
     * @return The fully populated row View
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Reuse an existing row View instead of inflating a brand-new one each time
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.list_item_product, parent, false);
        }

        // Get the Product object for this row
        Product product = productList.get(position);

        // Find each TextView inside the row layout and set its text
        TextView textViewName  = convertView.findViewById(R.id.textViewProductName);
        TextView textViewPrice = convertView.findViewById(R.id.textViewProductPrice);
        TextView textViewStock = convertView.findViewById(R.id.textViewProductStock);

        textViewName.setText(product.getProductName());
        textViewPrice.setText("Price: USh " + String.format("%,.0f", product.getPrice()));
        textViewStock.setText("Stock: " + product.getQuantityInStock());

        return convertView;
    }
}
