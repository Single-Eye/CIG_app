package com.example.retailordermanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * DatabaseHelper manages the entire SQLite database for RetailOrderManager.
 *
 * It creates all five tables on first run and provides every method the
 * Activities need to read and write data. Extend this class to add new
 * queries — never write raw SQL directly in an Activity.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // ── Database configuration ────────────────────────────────────────────────
    private static final String DATABASE_NAME    = "retail_orders.db";
    private static final int    DATABASE_VERSION = 1;

    // ── Table names ───────────────────────────────────────────────────────────
    private static final String TABLE_USERS       = "users";
    private static final String TABLE_PRODUCTS    = "products";
    private static final String TABLE_CUSTOMERS   = "customers";
    private static final String TABLE_ORDERS      = "orders";
    private static final String TABLE_ORDER_ITEMS = "order_items";

    // ── Column names: users ───────────────────────────────────────────────────
    private static final String COL_USER_ID  = "user_id";
    private static final String COL_USERNAME = "username";
    private static final String COL_PASSWORD = "password";

    // ── Column names: products ────────────────────────────────────────────────
    private static final String COL_PRODUCT_ID       = "product_id";
    private static final String COL_PRODUCT_NAME     = "product_name";
    private static final String COL_DESCRIPTION      = "description";
    private static final String COL_PRICE             = "price";
    private static final String COL_QUANTITY_IN_STOCK = "quantity_in_stock";

    // ── Column names: customers ───────────────────────────────────────────────
    private static final String COL_CUSTOMER_ID   = "customer_id";
    private static final String COL_CUSTOMER_NAME = "customer_name";
    private static final String COL_PHONE_NUMBER  = "phone_number";

    // ── Column names: orders ──────────────────────────────────────────────────
    private static final String COL_ORDER_ID     = "order_id";
    private static final String COL_ORDER_DATE   = "order_date";
    private static final String COL_TOTAL_AMOUNT = "total_amount";

    // ── Column names: order_items ─────────────────────────────────────────────
    private static final String COL_ITEM_ID    = "item_id";
    private static final String COL_QUANTITY   = "quantity";
    private static final String COL_UNIT_PRICE = "unit_price";

    // ── CREATE TABLE statements ───────────────────────────────────────────────
    private static final String CREATE_TABLE_USERS =
        "CREATE TABLE " + TABLE_USERS + " ("
        + COL_USER_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        + COL_USERNAME + " TEXT NOT NULL, "
        + COL_PASSWORD + " TEXT NOT NULL)";

    private static final String CREATE_TABLE_PRODUCTS =
        "CREATE TABLE " + TABLE_PRODUCTS + " ("
        + COL_PRODUCT_ID        + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        + COL_PRODUCT_NAME      + " TEXT NOT NULL, "
        + COL_DESCRIPTION       + " TEXT, "
        + COL_PRICE             + " REAL NOT NULL, "
        + COL_QUANTITY_IN_STOCK + " INTEGER NOT NULL)";

    private static final String CREATE_TABLE_CUSTOMERS =
        "CREATE TABLE " + TABLE_CUSTOMERS + " ("
        + COL_CUSTOMER_ID   + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        + COL_CUSTOMER_NAME + " TEXT NOT NULL, "
        + COL_PHONE_NUMBER  + " TEXT)";

    private static final String CREATE_TABLE_ORDERS =
        "CREATE TABLE " + TABLE_ORDERS + " ("
        + COL_ORDER_ID     + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        + COL_CUSTOMER_ID  + " INTEGER NOT NULL, "
        + COL_ORDER_DATE   + " TEXT NOT NULL, "
        + COL_TOTAL_AMOUNT + " REAL NOT NULL, "
        + "FOREIGN KEY(" + COL_CUSTOMER_ID + ") REFERENCES "
        + TABLE_CUSTOMERS + "(" + COL_CUSTOMER_ID + "))";

    private static final String CREATE_TABLE_ORDER_ITEMS =
        "CREATE TABLE " + TABLE_ORDER_ITEMS + " ("
        + COL_ITEM_ID    + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        + COL_ORDER_ID   + " INTEGER NOT NULL, "
        + COL_PRODUCT_ID + " INTEGER NOT NULL, "
        + COL_QUANTITY   + " INTEGER NOT NULL, "
        + COL_UNIT_PRICE + " REAL NOT NULL, "
        + "FOREIGN KEY(" + COL_ORDER_ID   + ") REFERENCES " + TABLE_ORDERS   + "(" + COL_ORDER_ID   + "), "
        + "FOREIGN KEY(" + COL_PRODUCT_ID + ") REFERENCES " + TABLE_PRODUCTS + "(" + COL_PRODUCT_ID + "))";

    // ── Constructor ───────────────────────────────────────────────────────────

    /**
     * Creates (or opens) the database. Call this once in each Activity's onCreate().
     *
     * @param context The Activity or Application context
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // ── Lifecycle callbacks ───────────────────────────────────────────────────

    /**
     * Called automatically the very first time the database is created on this device.
     * Creates all tables and seeds a default admin user plus two sample products.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_PRODUCTS);
        db.execSQL(CREATE_TABLE_CUSTOMERS);
        db.execSQL(CREATE_TABLE_ORDERS);
        db.execSQL(CREATE_TABLE_ORDER_ITEMS);

        // Default admin account so the user can log in right away
        db.execSQL("INSERT INTO " + TABLE_USERS
                + " (" + COL_USERNAME + ", " + COL_PASSWORD + ")"
                + " VALUES ('admin', '1234')");

        // Sample products to demonstrate the product list on first launch
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS
                + " (" + COL_PRODUCT_NAME + ", " + COL_DESCRIPTION + ", " + COL_PRICE + ", " + COL_QUANTITY_IN_STOCK + ")"
                + " VALUES ('Notebook A4', 'Pack of 100 pages lined notebook', 2.50, 200)");
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS
                + " (" + COL_PRODUCT_NAME + ", " + COL_DESCRIPTION + ", " + COL_PRICE + ", " + COL_QUANTITY_IN_STOCK + ")"
                + " VALUES ('Ballpoint Pen', 'Blue ink, medium tip', 0.75, 500)");
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS
                + " (" + COL_PRODUCT_NAME + ", " + COL_DESCRIPTION + ", " + COL_PRICE + ", " + COL_QUANTITY_IN_STOCK + ")"
                + " VALUES ('Stapler', 'Desktop stapler with 100 staples', 5.99, 50)");
    }

    /**
     * Called when DATABASE_VERSION is incremented in a future update.
     * For now it simply drops all tables and recreates them.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // ── USER methods ──────────────────────────────────────────────────────────

    /**
     * Inserts a new user account into the database.
     *
     * @param username The login username
     * @param password The login password (plain text for this demo)
     * @return The new row's ID, or -1 if the insert failed
     */
    public long insertUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result;
    }

    /**
     * Checks whether a username + password pair exists in the database.
     *
     * @param username The entered username
     * @param password The entered password
     * @return true if the credentials match a row in users, false otherwise
     */
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS
                + " WHERE " + COL_USERNAME + " = ? AND " + COL_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username, password});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        db.close();
        return exists;
    }

    // ── PRODUCT methods ───────────────────────────────────────────────────────

    /**
     * Inserts a new product into the products table.
     *
     * @param name        Product display name
     * @param description Short description of the product
     * @param price       Selling price (must be > 0)
     * @param quantity    Initial quantity in stock
     * @return The new product_id, or -1 if the insert failed
     */
    public long insertProduct(String name, String description, double price, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PRODUCT_NAME,      name);
        values.put(COL_DESCRIPTION,       description);
        values.put(COL_PRICE,             price);
        values.put(COL_QUANTITY_IN_STOCK, quantity);
        long result = db.insert(TABLE_PRODUCTS, null, values);
        db.close();
        return result;
    }

    /**
     * Updates every field of an existing product.
     *
     * @param id          The product_id to update
     * @param name        New product name
     * @param description New description
     * @param price       New price
     * @param quantity    New stock quantity
     * @return Number of rows updated (1 on success, 0 if id not found)
     */
    public int updateProduct(int id, String name, String description, double price, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PRODUCT_NAME,      name);
        values.put(COL_DESCRIPTION,       description);
        values.put(COL_PRICE,             price);
        values.put(COL_QUANTITY_IN_STOCK, quantity);
        int result = db.update(TABLE_PRODUCTS, values,
                COL_PRODUCT_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return result;
    }

    /**
     * Permanently removes a product from the database.
     *
     * @param id The product_id to delete
     * @return Number of rows deleted (1 on success, 0 if id not found)
     */
    public int deleteProduct(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_PRODUCTS,
                COL_PRODUCT_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return result;
    }

    /**
     * Returns all products ordered alphabetically by name.
     * IMPORTANT: The caller must call cursor.close() when finished.
     *
     * Columns returned: product_id, product_name, description, price, quantity_in_stock
     */
    public Cursor getAllProducts() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_PRODUCTS + " ORDER BY " + COL_PRODUCT_NAME, null);
    }

    /**
     * Returns the single product matching the given ID.
     * IMPORTANT: The caller must call cursor.close() when finished.
     *
     * Columns returned: product_id, product_name, description, price, quantity_in_stock
     */
    public Cursor getProductById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_PRODUCTS + " WHERE " + COL_PRODUCT_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    // ── CUSTOMER methods ──────────────────────────────────────────────────────

    /**
     * Inserts a new customer record. A new customer row is created each time
     * an order is submitted (customers are not de-duplicated in this version).
     *
     * @param name  Customer's full name
     * @param phone Customer's phone number
     * @return The new customer_id, or -1 if the insert failed
     */
    public long insertCustomer(String name, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CUSTOMER_NAME, name);
        values.put(COL_PHONE_NUMBER,  phone);
        long result = db.insert(TABLE_CUSTOMERS, null, values);
        db.close();
        return result;
    }

    // ── ORDER methods ─────────────────────────────────────────────────────────

    /**
     * Inserts a new order header record.
     *
     * @param customerId  The customer_id this order belongs to
     * @param date        Order date/time as a formatted string (yyyy-MM-dd HH:mm:ss)
     * @param totalAmount The pre-calculated total amount for this order
     * @return The new order_id, or -1 if the insert failed
     */
    public long insertOrder(long customerId, String date, double totalAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CUSTOMER_ID,  customerId);
        values.put(COL_ORDER_DATE,   date);
        values.put(COL_TOTAL_AMOUNT, totalAmount);
        long result = db.insert(TABLE_ORDERS, null, values);
        db.close();
        return result;
    }

    /**
     * Inserts one line item under an existing order.
     *
     * @param orderId   The parent order_id
     * @param productId The product being ordered
     * @param quantity  How many units
     * @param unitPrice The price at the time of the order (snapshot — price may change later)
     * @return The new item_id, or -1 if the insert failed
     */
    public long insertOrderItem(long orderId, int productId, int quantity, double unitPrice) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ORDER_ID,   orderId);
        values.put(COL_PRODUCT_ID, productId);
        values.put(COL_QUANTITY,   quantity);
        values.put(COL_UNIT_PRICE, unitPrice);
        long result = db.insert(TABLE_ORDER_ITEMS, null, values);
        db.close();
        return result;
    }

    /**
     * Returns all orders joined with their customer name, newest first.
     * IMPORTANT: The caller must call cursor.close() when finished.
     *
     * Columns returned: order_id, customer_name, order_date, total_amount
     */
    public Cursor getAllOrders() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query =
            "SELECT o." + COL_ORDER_ID     + ", "
                 + "c." + COL_CUSTOMER_NAME + ", "
                 + "o." + COL_ORDER_DATE    + ", "
                 + "o." + COL_TOTAL_AMOUNT  + " "
            + "FROM " + TABLE_ORDERS + " o "
            + "INNER JOIN " + TABLE_CUSTOMERS + " c "
            +   "ON o." + COL_CUSTOMER_ID + " = c." + COL_CUSTOMER_ID + " "
            + "ORDER BY o." + COL_ORDER_DATE + " DESC";
        return db.rawQuery(query, null);
    }

    /**
     * Returns all line items for one order, joined with product names.
     * IMPORTANT: The caller must call cursor.close() when finished.
     *
     * Columns returned: item_id, product_name, quantity, unit_price
     */
    public Cursor getOrderItems(int orderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query =
            "SELECT oi." + COL_ITEM_ID    + ", "
                 + "p."  + COL_PRODUCT_NAME + ", "
                 + "oi." + COL_QUANTITY    + ", "
                 + "oi." + COL_UNIT_PRICE  + " "
            + "FROM " + TABLE_ORDER_ITEMS + " oi "
            + "INNER JOIN " + TABLE_PRODUCTS + " p "
            +   "ON oi." + COL_PRODUCT_ID + " = p." + COL_PRODUCT_ID + " "
            + "WHERE oi." + COL_ORDER_ID + " = ?";
        return db.rawQuery(query, new String[]{String.valueOf(orderId)});
    }

    /**
     * Returns the header of one specific order, joined with customer details.
     * IMPORTANT: The caller must call cursor.close() when finished.
     *
     * Columns returned: order_id, customer_name, phone_number, order_date, total_amount
     */
    public Cursor getOrderById(int orderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query =
            "SELECT o." + COL_ORDER_ID     + ", "
                 + "c." + COL_CUSTOMER_NAME + ", "
                 + "c." + COL_PHONE_NUMBER  + ", "
                 + "o." + COL_ORDER_DATE    + ", "
                 + "o." + COL_TOTAL_AMOUNT  + " "
            + "FROM " + TABLE_ORDERS + " o "
            + "INNER JOIN " + TABLE_CUSTOMERS + " c "
            +   "ON o." + COL_CUSTOMER_ID + " = c." + COL_CUSTOMER_ID + " "
            + "WHERE o." + COL_ORDER_ID + " = ?";
        return db.rawQuery(query, new String[]{String.valueOf(orderId)});
    }
}
