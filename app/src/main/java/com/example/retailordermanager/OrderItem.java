package com.example.retailordermanager;

/**
 * OrderItem represents a single product line within an order.
 * It is used both for the temporary list while building an order
 * and for displaying items in OrderDetailActivity.
 */
public class OrderItem {

    private int productId;
    private String productName;
    private int quantity;
    private double unitPrice;

    /**
     * Constructor that creates an OrderItem with product and pricing details.
     */
    public OrderItem(int productId, String productName, int quantity, double unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Getters
    public int getProductId()      { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity()       { return quantity; }
    public double getUnitPrice()   { return unitPrice; }

    /** Returns the line total: quantity × unit price */
    public double getSubtotal()    { return quantity * unitPrice; }

    /**
     * Returns a string shown in the order items ListView while building an order.
     */
    @Override
    public String toString() {
        return productName + "  ×" + quantity + "  =  USh " + String.format("%,.0f", getSubtotal());
    }
}
