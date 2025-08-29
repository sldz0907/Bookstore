package model;

import java.time.LocalDate;
import java.util.List;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Invoice {
	private List<Book> books;
	
    private StringProperty invoiceId;
    private ObjectProperty<LocalDate> dateCreated;
    private StringProperty memberId;
    private StringProperty employeeId;
    private DoubleProperty totalPrice;
    private IntegerProperty discountApplied;
    private DoubleProperty finalPrice;

    public Invoice(String invoiceId, LocalDate dateCreated, String memberId, 
                   String employeeId, double totalPrice, int discountApplied) {
        this.invoiceId = new SimpleStringProperty(invoiceId);
        this.dateCreated = new SimpleObjectProperty<>(dateCreated);
        this.memberId = new SimpleStringProperty(memberId);
        this.employeeId = new SimpleStringProperty(employeeId);
        this.totalPrice = new SimpleDoubleProperty(totalPrice);
        this.discountApplied = new SimpleIntegerProperty(discountApplied);
        this.finalPrice = new SimpleDoubleProperty(totalPrice * (100 - discountApplied) / 100.0);
    }

    public Invoice(String invoiceId, LocalDate dateCreated, String memberId, 
            String employeeId, double totalPrice, int discountApplied,
            List<Book> books) {
		 this.invoiceId = new SimpleStringProperty(invoiceId);
		 this.dateCreated = new SimpleObjectProperty<>(dateCreated);
		 this.memberId = new SimpleStringProperty(memberId);
		 this.employeeId = new SimpleStringProperty(employeeId);
		 this.totalPrice = new SimpleDoubleProperty(totalPrice);
		 this.discountApplied = new SimpleIntegerProperty(discountApplied);
		 this.finalPrice = new SimpleDoubleProperty(totalPrice * (100 - discountApplied) / 100.0);
		 this.books = books;
    }
    

	public List<Book> getBooks() {
	    return books;
	}
	
	public void setBooks(List<Book> books) {
	    this.books = books;
	}
		 
    public StringProperty invoiceIdProperty() { return invoiceId; }
    public ObjectProperty<LocalDate> dateCreatedProperty() { return dateCreated; }
    public StringProperty memberIdProperty() { return memberId; }
    public StringProperty employeeIdProperty() { return employeeId; }
    public DoubleProperty totalPriceProperty() { return totalPrice; }
    public IntegerProperty discountAppliedProperty() { return discountApplied; }
    public DoubleProperty finalPriceProperty() { return finalPrice; }

    public String getInvoiceId() { return invoiceId.get(); }
    public LocalDate getDateCreated() { return dateCreated.get(); }
    public String getMemberId() { return memberId.get(); }
    public String getEmployeeId() { return employeeId.get(); }
    public double getTotalPrice() { return totalPrice.get(); }
    public int getDiscountApplied() { return discountApplied.get(); }
    public double getFinalPrice() { return finalPrice.get(); }
    
    public void setInvoiceId(String value) { invoiceId.set(value); }
    public void setDateCreated(LocalDate value) { dateCreated.set(value); }
    public void setMemberId(String value) { memberId.set(value); }
    public void setEmployeeId(String value) { employeeId.set(value); }
    public void setTotalPrice(double value) { 
        totalPrice.set(value); 
        updateFinalPrice();
    }
    public void setDiscountApplied(int value) { 
        discountApplied.set(value); 
        updateFinalPrice();
    }

    private void updateFinalPrice() {
        finalPrice.set(totalPrice.get() * (100 - discountApplied.get()) / 100.0);
    }
}

