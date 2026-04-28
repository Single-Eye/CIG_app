package com.example.retailordermanager;

/**
 * Product represents a single item sold by the retail business.
 * It stores all product details and is used throughout the app.
 */
public class Product {

    private int productId;
    private String productName;
    private String description;
    private double price;
    private int quantityInStock;

    /**
     * Constructor that creates a Product with all its details.
     */
    public Product(int productId, String productName, String description, double price, int quantityInStock) {
        this.productId = productId;
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.quantityInStock = quantityInStock;
    }

    // Getters — used to read the product's data
    public int getProductId()       { return productId; }
    public String getProductName()  { return productName; }
    public String getDescription()  { return description; }
    public double getPrice()        { return price; }
    public int getQuantityInStock() { return quantityInStock; }

    // Setters — used to update the product's data
    public void setProductId(int productId)             { this.productId = productId; }
    public void setProductName(String productName)      { this.productName = productName; }
    public void setDescription(String description)      { this.description = description; }
    public void setPrice(double price)                  { this.price = price; }
    public void setQuantityInStock(int quantityInStock) { this.quantityInStock = quantityInStock; }

    /**
     * Returns a formatted string shown in the Spinner dropdown when creating an order.
     */
    @Override
    public String toString() {
        return productName + "  —  USh " + String.format("%,.0f", price);
    }
}
