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
    private int    quantityInStock;
    private String imagePath; // URI string of the product photo, or null if no image

    /**
     * Constructor without imagePath — used when image is not needed (e.g. Spinner dropdown).
     * Defaults imagePath to null.
     */
    public Product(int productId, String productName, String description, double price, int quantityInStock) {
        this(productId, productName, description, price, quantityInStock, null);
    }

    /**
     * Full constructor including imagePath for display in the product list cards.
     */
    public Product(int productId, String productName, String description,
                   double price, int quantityInStock, String imagePath) {
        this.productId       = productId;
        this.productName     = productName;
        this.description     = description;
        this.price           = price;
        this.quantityInStock = quantityInStock;
        this.imagePath       = imagePath;
    }

    // Getters — used to read the product's data
    public int getProductId()       { return productId; }
    public String getProductName()  { return productName; }
    public String getDescription()  { return description; }
    public double getPrice()        { return price; }
    public int getQuantityInStock() { return quantityInStock; }
    public String getImagePath()    { return imagePath; }

    // Setters — used to update the product's data
    public void setProductId(int productId)             { this.productId = productId; }
    public void setProductName(String productName)      { this.productName = productName; }
    public void setDescription(String description)      { this.description = description; }
    public void setPrice(double price)                  { this.price = price; }
    public void setQuantityInStock(int quantityInStock) { this.quantityInStock = quantityInStock; }
    public void setImagePath(String imagePath)          { this.imagePath = imagePath; }

    /**
     * Returns a formatted string shown in the Spinner dropdown when creating an order.
     */
    @Override
    public String toString() {
        return productName + "  —  USh " + String.format("%,.0f", price);
    }
}
