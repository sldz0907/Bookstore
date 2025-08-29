package model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty; 
import javafx.beans.property.SimpleStringProperty;

public class Member {
    private final SimpleStringProperty accountId;
    private final SimpleStringProperty fullName;
    private final SimpleStringProperty email;
    private final SimpleStringProperty phone;
    private final SimpleStringProperty address;
    private final SimpleStringProperty rank;
    private final SimpleStringProperty status;
    private final SimpleDoubleProperty totalSpending; // <<< Thêm thuộc tính mới

    // Constructor cũ của bạn có thể chỉ có 7 tham số
    // Chúng ta sẽ tạo một constructor mới có 8 tham số
    public Member(String accountId, String fullName, String email, String phone, String address, String rank, String status, double totalSpending) {
        this.accountId = new SimpleStringProperty(accountId);
        this.fullName = new SimpleStringProperty(fullName);
        this.email = new SimpleStringProperty(email);
        this.phone = new SimpleStringProperty(phone);
        this.address = new SimpleStringProperty(address);
        this.rank = new SimpleStringProperty(rank);
        this.status = new SimpleStringProperty(status);
        this.totalSpending = new SimpleDoubleProperty(totalSpending); // <<< Khởi tạo thuộc tính mới
    }
    
    // Bạn có thể cần giữ lại constructor cũ nếu có nơi khác đang dùng
    public Member(String accountId, String fullName, String email, String phone, String address, String rank, String status) {
        this(accountId, fullName, email, phone, address, rank, status, 0.0); // Gọi constructor mới với totalSpending mặc định là 0
    }


    // --- Getter và Property Getter ---
    // Các getter cũ giữ nguyên
    public String getAccountId() { return accountId.get(); }
    public String getFullName() { return fullName.get(); }
    public String getEmail() { return email.get(); }
    public String getPhone() { return phone.get(); }
    public String getAddress() { return address.get(); }
    public String getRank() { return rank.get(); }
    public String getStatus() { return status.get(); }

    // <<< Thêm getter mới cho totalSpending
    public double getTotalSpending() { return totalSpending.get(); }


    // Các property getter cũ giữ nguyên
    public SimpleStringProperty accountIdProperty() { return accountId; }
    public SimpleStringProperty fullNameProperty() { return fullName; }
    public SimpleStringProperty emailProperty() { return email; }
    public SimpleStringProperty phoneProperty() { return phone; }
    public SimpleStringProperty addressProperty() { return address; }
    public SimpleStringProperty rankProperty() { return rank; }
    public SimpleStringProperty statusProperty() { return status; }
    
    // <<< Thêm property getter mới cho totalSpending
    public SimpleDoubleProperty totalSpendingProperty() { return totalSpending; }
}
