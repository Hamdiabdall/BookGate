package com.example.bookgate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "bookgate.db";
    private static final int DATABASE_VERSION = 1;

    // User table
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USER_NAME = "name";
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_PASSWORD = "password";
    public static final String COLUMN_USER_ROLE = "role";

    // Books table
    public static final String TABLE_BOOKS = "books";
    public static final String COLUMN_BOOK_ID = "id";
    public static final String COLUMN_BOOK_TITLE = "title";
    public static final String COLUMN_BOOK_AUTHOR = "author";
    public static final String COLUMN_BOOK_DESCRIPTION = "description";
    public static final String COLUMN_BOOK_IMAGE_PATH = "image_path";
    public static final String COLUMN_BOOK_PDF_PATH = "pdf_path";

    // Download Keys table
    public static final String TABLE_DOWNLOAD_KEYS = "download_keys";
    public static final String COLUMN_KEY_ID = "id";
    public static final String COLUMN_KEY_BOOK_ID = "book_id";
    public static final String COLUMN_KEY_VALUE = "key_value";

    // Admin Key for registering new admins
    public static final String ADMIN_KEY = "ADMIN2024";

    // Default admin credentials
    public static final String DEFAULT_ADMIN_NAME = "Admin";
    public static final String DEFAULT_ADMIN_EMAIL = "admin@bookgate.com";
    public static final String DEFAULT_ADMIN_PASSWORD = "admin123";

    // User roles
    public static final String ROLE_MEMBER = "Member";
    public static final String ROLE_LIBRARIAN = "Librarian";

    // Create table queries
    private static final String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_USER_NAME + " TEXT, "
            + COLUMN_USER_EMAIL + " TEXT UNIQUE, "
            + COLUMN_USER_PASSWORD + " TEXT, "
            + COLUMN_USER_ROLE + " TEXT)";

    private static final String CREATE_BOOKS_TABLE = "CREATE TABLE " + TABLE_BOOKS + "("
            + COLUMN_BOOK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_BOOK_TITLE + " TEXT, "
            + COLUMN_BOOK_AUTHOR + " TEXT, "
            + COLUMN_BOOK_DESCRIPTION + " TEXT, "
            + COLUMN_BOOK_IMAGE_PATH + " TEXT, "
            + COLUMN_BOOK_PDF_PATH + " TEXT)";

    private static final String CREATE_DOWNLOAD_KEYS_TABLE = "CREATE TABLE " + TABLE_DOWNLOAD_KEYS + "("
            + COLUMN_KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_KEY_BOOK_ID + " INTEGER, "
            + COLUMN_KEY_VALUE + " TEXT UNIQUE, "
            + "FOREIGN KEY(" + COLUMN_KEY_BOOK_ID + ") REFERENCES " + TABLE_BOOKS + "(" + COLUMN_BOOK_ID + "))";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_BOOKS_TABLE);
        db.execSQL(CREATE_DOWNLOAD_KEYS_TABLE);
        
        // Add default admin account
        addDefaultAdmin(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOWNLOAD_KEYS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
    
    private void addDefaultAdmin(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, DEFAULT_ADMIN_NAME);
        values.put(COLUMN_USER_EMAIL, DEFAULT_ADMIN_EMAIL);
        values.put(COLUMN_USER_PASSWORD, DEFAULT_ADMIN_PASSWORD);
        values.put(COLUMN_USER_ROLE, ROLE_LIBRARIAN);
        
        db.insert(TABLE_USERS, null, values);
    }
    
    // User management methods
    public long addUser(String name, String email, String password, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, name);
        values.put(COLUMN_USER_EMAIL, email);
        values.put(COLUMN_USER_PASSWORD, password);
        values.put(COLUMN_USER_ROLE, role);
        
        long id = db.insert(TABLE_USERS, null, values);
        db.close();
        return id;
    }
    
    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_USERS, 
                new String[]{COLUMN_USER_ID, COLUMN_USER_NAME, COLUMN_USER_EMAIL, COLUMN_USER_PASSWORD, COLUMN_USER_ROLE},
                COLUMN_USER_EMAIL + "=?",
                new String[]{email}, null, null, null);
        
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new User(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PASSWORD)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ROLE))
            );
            cursor.close();
        }
        
        return user;
    }
    
    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USER_ID},
                COLUMN_USER_EMAIL + "=? AND " + COLUMN_USER_PASSWORD + "=?",
                new String[]{email, password},
                null, null, null);
        
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }
    
    public boolean isUserAdmin(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USER_ROLE},
                COLUMN_USER_EMAIL + "=? AND " + COLUMN_USER_ROLE + "=?",
                new String[]{email, ROLE_LIBRARIAN},
                null, null, null);
        
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }
    
    // Book management methods
    public long addBook(String title, String author, String description, String imagePath, String pdfPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BOOK_TITLE, title);
        values.put(COLUMN_BOOK_AUTHOR, author);
        values.put(COLUMN_BOOK_DESCRIPTION, description);
        values.put(COLUMN_BOOK_IMAGE_PATH, imagePath);
        values.put(COLUMN_BOOK_PDF_PATH, pdfPath);
        
        long id = db.insert(TABLE_BOOKS, null, values);
        db.close();
        return id;
    }
    
    public List<Book> getAllBooks() {
        List<Book> booksList = new ArrayList<>();
        
        String selectQuery = "SELECT * FROM " + TABLE_BOOKS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
            do {
                Book book = new Book(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BOOK_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOK_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOK_AUTHOR)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOK_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOK_IMAGE_PATH)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOK_PDF_PATH))
                );
                booksList.add(book);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        return booksList;
    }
    
    public Book getBookById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_BOOKS,
                new String[]{COLUMN_BOOK_ID, COLUMN_BOOK_TITLE, COLUMN_BOOK_AUTHOR, 
                        COLUMN_BOOK_DESCRIPTION, COLUMN_BOOK_IMAGE_PATH, COLUMN_BOOK_PDF_PATH},
                COLUMN_BOOK_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        
        Book book = null;
        if (cursor != null && cursor.moveToFirst()) {
            book = new Book(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BOOK_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOK_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOK_AUTHOR)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOK_DESCRIPTION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOK_IMAGE_PATH)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOK_PDF_PATH))
            );
            cursor.close();
        }
        
        return book;
    }
    
    public boolean deleteBook(int bookId) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // First delete associated download keys
        db.delete(TABLE_DOWNLOAD_KEYS, COLUMN_KEY_BOOK_ID + "=?", new String[]{String.valueOf(bookId)});
        
        // Then delete the book
        int result = db.delete(TABLE_BOOKS, COLUMN_BOOK_ID + "=?", new String[]{String.valueOf(bookId)});
        db.close();
        
        return result > 0;
    }
    
    public boolean updateBook(Book book) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(COLUMN_BOOK_TITLE, book.getTitle());
        values.put(COLUMN_BOOK_AUTHOR, book.getAuthor());
        values.put(COLUMN_BOOK_DESCRIPTION, book.getDescription());
        if (book.getImagePath() != null) {
            values.put(COLUMN_BOOK_IMAGE_PATH, book.getImagePath());
        }
        if (book.getPdfPath() != null) {
            values.put(COLUMN_BOOK_PDF_PATH, book.getPdfPath());
        }
        
        // Update the book
        int result = db.update(TABLE_BOOKS, values, COLUMN_BOOK_ID + "=?", 
                new String[]{String.valueOf(book.getId())});
        db.close();
        
        return result > 0;
    }
    
    // Download key management methods
    public long addDownloadKey(int bookId, String keyValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_KEY_BOOK_ID, bookId);
        values.put(COLUMN_KEY_VALUE, keyValue);
        
        long id = db.insert(TABLE_DOWNLOAD_KEYS, null, values);
        db.close();
        return id;
    }
    
    public List<DownloadKey> getAllKeys() {
        List<DownloadKey> keysList = new ArrayList<>();
        
        String selectQuery = "SELECT * FROM " + TABLE_DOWNLOAD_KEYS + " k JOIN " + TABLE_BOOKS + 
                " b ON k." + COLUMN_KEY_BOOK_ID + " = b." + COLUMN_BOOK_ID;
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
            do {
                DownloadKey key = new DownloadKey(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_KEY_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_KEY_BOOK_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_KEY_VALUE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOK_TITLE))
                );
                keysList.add(key);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        return keysList;
    }
    
    public boolean isValidKey(String keyValue, int bookId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DOWNLOAD_KEYS,
                new String[]{COLUMN_KEY_ID},
                COLUMN_KEY_VALUE + "=? AND " + COLUMN_KEY_BOOK_ID + "=?",
                new String[]{keyValue, String.valueOf(bookId)},
                null, null, null);
        
        boolean isValid = cursor.getCount() > 0;
        cursor.close();
        return isValid;
    }
    
    public void deleteKey(String keyValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DOWNLOAD_KEYS, COLUMN_KEY_VALUE + "=?", new String[]{keyValue});
        db.close();
    }
}
