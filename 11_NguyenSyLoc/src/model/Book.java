package model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class Book {
    private String bookID;
    private String title;
    private String author;
    private String genre;
    private double price;
    private int quantity;
    
    
    private IntegerProperty selectedQuantity = new SimpleIntegerProperty(0);
    public Book(String title, double price) {
        this.title = title;
        this.price = price;
    }
    
    public Book(String bookID, int quantity, double price) {
        this.bookID = bookID;
        this.quantity = quantity;
        this.price = price;
    }
    
    public int getSelectedQuantity() { return selectedQuantity.get(); }
    public void setSelectedQuantity(int qty) { selectedQuantity.set(qty); }
    public IntegerProperty selectedQuantityProperty() { return selectedQuantity; }

    
    public Book(String bookID, String title, String author, String genre, int quantity, double price) {
        this.bookID = bookID;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.quantity = quantity;
        this.price = price;
    }
    
    public Book(String bookID, String title, String author, int quantity, double price) {
        this.bookID = bookID;
        this.title = title;
        this.author = author;
        this.quantity = quantity;
        this.price = price;
    }

    public String getBookID() { return bookID; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getGenre() { return genre; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }

    public void setBookID(String bookID) { this.bookID = bookID; }
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setPrice(double price) { this.price = price; }
}


