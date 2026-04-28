package com.example.retailordermanager;

/**
 * Order represents a completed customer order stored in the database.
 * It holds the order summary used in the OrderHistoryActivity list.
 */
public class Order {

    private int orderId;
    private int customerId;
    private String customerName;
    private String orderDate;
    private double totalAmount;

    /**
     * Constructor that creates an Order with all its summary details.
     */
    public Order(int orderId, int customerId, String customerName, String orderDate, double totalAmount) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
    }

    // Getters
    public int getOrderId()         { return orderId; }
    public int getCustomerId()      { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getOrderDate()    { return orderDate; }
    public double getTotalAmount()  { return totalAmount; }
}
