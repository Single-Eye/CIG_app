package com.example.retailordermanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * DatabaseHelper manages the entire SQLite database for Inkify.
 *
 * VERSION 2 changes: adds image_path column for product photos.
 * onUpgrade uses ALTER TABLE so existing data is preserved safely.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // ── Database configuration ────────────────────────────────────────────────
    private static final String DATABASE_NAME    = "retail_orders.db";
    private static final int    DATABASE_VERSION = 2; // bumped from 1 to add image_path

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
    private static final String COL_IMAGE_PATH        = "image_path"; // new in version 2

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

    // image_path is nullable — products without a photo just have NULL here
    private static final String CREATE_TABLE_PRODUCTS =
        "CREATE TABLE " + TABLE_PRODUCTS + " ("
        + COL_PRODUCT_ID        + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        + COL_PRODUCT_NAME      + " TEXT NOT NULL, "
        + COL_DESCRIPTION       + " TEXT, "
        + COL_PRICE             + " REAL NOT NULL, "
        + COL_QUANTITY_IN_STOCK + " INTEGER NOT NULL DEFAULT 0, "
        + COL_IMAGE_PATH        + " TEXT)";

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
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // ── Lifecycle callbacks ───────────────────────────────────────────────────

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_PRODUCTS);
        db.execSQL(CREATE_TABLE_CUSTOMERS);
        db.execSQL(CREATE_TABLE_ORDERS);
        db.execSQL(CREATE_TABLE_ORDER_ITEMS);

        // Default admin account so the user can log in immediately
        db.execSQL("INSERT INTO " + TABLE_USERS
                + " (" + COL_USERNAME + ", " + COL_PASSWORD + ")"
                + " VALUES ('admin', '1234')");

        // Sample products — image_path is omitted so it defaults to NULL
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
     * Called when DATABASE_VERSION is incremented.
     * Version 2: adds the image_path column using ALTER TABLE so no data is lost.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Existing products will have NULL for image_path — that's fine
            try {
                db.execSQL("ALTER TABLE " + TABLE_PRODUCTS
                        + " ADD COLUMN " + COL_IMAGE_PATH + " TEXT");
            } catch (Exception e) {
                // Column may already exist if a partial upgrade ran — safe to ignore
            }
        }
    }

    // ── USER methods ──────────────────────────────────────────────────────────

    public long insertUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result;
    }

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
     * Inserts a new product. Pass null or "" for imagePath if no photo was chosen.
     *
     * @param imagePath URI string from the gallery picker, or null
     * @return The new product_id, or -1 if the insert failed
     */
    public long insertProduct(String name, String description,
                              double price, int quantity, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PRODUCT_NAME,      name);
        values.put(COL_DESCRIPTION,       description);
        values.put(COL_PRICE,             price);
        values.put(COL_QUANTITY_IN_STOCK, quantity);
        values.put(COL_IMAGE_PATH,        imagePath); // null stored as SQL NULL
        long result = db.insert(TABLE_PRODUCTS, null, values);
        db.close();
        return result;
    }

    /**
     * Updates all fields of an existing product, including the image path.
     *
     * @param imagePath URI string, or null to clear the image
     * @return Rows updated (1 on success, 0 if id not found)
     */
    public int updateProduct(int id, String name, String description,
                             double price, int quantity, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PRODUCT_NAME,      name);
        values.put(COL_DESCRIPTION,       description);
        values.put(COL_PRICE,             price);
        values.put(COL_QUANTITY_IN_STOCK, quantity);
        values.put(COL_IMAGE_PATH,        imagePath);
        int result = db.update(TABLE_PRODUCTS, values,
                COL_PRODUCT_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return result;
    }

    public int deleteProduct(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_PRODUCTS,
                COL_PRODUCT_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return result;
    }

    /** Returns all products alphabetically. Caller must close the cursor. */
    public Cursor getAllProducts() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_PRODUCTS + " ORDER BY " + COL_PRODUCT_NAME, null);
    }

    /** Returns one product by ID. Caller must close the cursor. */
    public Cursor getProductById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_PRODUCTS + " WHERE " + COL_PRODUCT_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    /**
     * Returns the live stock quantity for a product directly from the database.
     * Always call this before adding to an order — never use cached values.
     */
    public int getProductStock(int productId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_QUANTITY_IN_STOCK + " FROM " + TABLE_PRODUCTS
                + " WHERE " + COL_PRODUCT_ID + " = ?",
                new String[]{String.valueOf(productId)});
        int stock = 0;
        if (cursor.moveToFirst()) {
            stock = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return stock;
    }

    /**
     * Subtracts quantityOrdered from a product's stock after an order is placed.
     * Uses Math.max(0, ...) so stock can never go below zero.
     */
    public int reduceProductStock(int productId, int quantityOrdered) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Read current stock so we can subtract from it
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_QUANTITY_IN_STOCK + " FROM " + TABLE_PRODUCTS
                + " WHERE " + COL_PRODUCT_ID + " = ?",
                new String[]{String.valueOf(productId)});
        int result = 0;
        if (cursor.moveToFirst()) {
            int currentStock = cursor.getInt(0);
            int newStock = Math.max(0, currentStock - quantityOrdered);
            ContentValues values = new ContentValues();
            values.put(COL_QUANTITY_IN_STOCK, newStock);
            result = db.update(TABLE_PRODUCTS, values,
                    COL_PRODUCT_ID + " = ?", new String[]{String.valueOf(productId)});
        }
        cursor.close();
        db.close();
        return result;
    }

    // ── CUSTOMER methods ──────────────────────────────────────────────────────

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

    /** Returns all orders newest-first, joined with customer name. Caller must close the cursor. */
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

    /** Returns all line items for one order with product names. Caller must close the cursor. */
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

    /** Returns the header of one order joined with customer info. Caller must close the cursor. */
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

    // ── SUMMARY methods (used by the Dashboard summary card) ──────────────────

    /**
     * Returns the total number of orders placed so far.
     */
    public int getTotalOrderCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ORDERS, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    /**
     * Returns the total revenue: sum of all order totals.
     * COALESCE ensures 0 is returned when no orders exist yet.
     */
    public double getTotalRevenue() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COALESCE(SUM(" + COL_TOTAL_AMOUNT + "), 0) FROM " + TABLE_ORDERS, null);
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return total;
    }

    /**
     * Returns the combined stock across all products.
     * COALESCE ensures 0 is returned when the products table is empty.
     */
    public int getTotalStock() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COALESCE(SUM(" + COL_QUANTITY_IN_STOCK + "), 0) FROM " + TABLE_PRODUCTS, null);
        int total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return total;
    }
}
