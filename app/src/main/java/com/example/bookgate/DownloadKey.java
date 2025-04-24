package com.example.bookgate;

public class DownloadKey {
    private int id;
    private int bookId;
    private String keyValue;
    private String bookTitle; // For displaying in the admin panel

    public DownloadKey(int id, int bookId, String keyValue, String bookTitle) {
        this.id = id;
        this.bookId = bookId;
        this.keyValue = keyValue;
        this.bookTitle = bookTitle;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }
}
