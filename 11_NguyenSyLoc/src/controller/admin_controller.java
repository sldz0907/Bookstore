package controller;

import javafx.fxml.Initializable;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import dao.connectDB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import model.Book;
import model.Invoice;
import model.Member;
import java.sql.CallableStatement;
public class admin_controller implements Initializable {

    @FXML
    private Button exitBtn;

    @FXML
    private BorderPane main_form;

    @FXML
    private Button manageBooksBtn;

    @FXML
    private AnchorPane manageBooksForm;

    @FXML
    private TextField mb_author;

    @FXML
    private TextField mb_bookID;

    @FXML
    private Button mb_clearBtn;

    @FXML
    private TableColumn<Book, String> mb_col_author;

    @FXML
    private TableColumn<Book, String> mb_col_bookID;

    @FXML
    private TableColumn<Book, String> mb_col_genre;

    @FXML
    private TableColumn<Book, Double> mb_col_price;

    @FXML
    private TableColumn<Book, Integer> mb_col_quantity;

    @FXML
    private TableColumn<Book, String> mb_col_title;

    @FXML
    private Button mb_deleteBtn;

    @FXML
    private ComboBox<String> mb_genre;

    @FXML
    private ComboBox<String> mb_genreFilter;

    @FXML
    private Button mb_insertBtn;

    @FXML
    private TextField mb_price;

    @FXML
    private TextField mb_quantity;

    @FXML
    private Button mb_resetBtn;

    @FXML
    private TextField mb_search;

    @FXML
    private TableView<Book> mb_tableView;

    @FXML
    private TextField mb_title;

    @FXML
    private Button mb_updateBtn;

    @FXML
    private TextField mem_address;

    @FXML
    private Button mem_clearBtn;

    @FXML
    private TableColumn<Member, String> mem_col_ID;

    @FXML
    private TableColumn<Member, String> mem_col_address;

    @FXML
    private TableColumn<Member, String> mem_col_email;

    @FXML
    private TableColumn<Member, String> mem_col_fullName;

    @FXML
    private TableColumn<Member, String> mem_col_phone;

    @FXML
    private TableColumn<Member, String> mem_col_rank;
    @FXML
    private TableColumn<Member, Number> mem_col_totalSpending;
    @FXML
    private TableColumn<Member, String> mem_col_status;

    @FXML
    private Button mem_deleteBtn;

    @FXML
    private TextField mem_email;

    @FXML
    private TextField mem_fullName;

    @FXML
    private TextField mem_id;

    @FXML
    private TextField mem_phone;

    @FXML
    private ComboBox<String> mem_rank;

    @FXML
    private Button mem_registerBtn;

    @FXML
    private TextField mem_search;

    @FXML
    private TableView<Member> mem_tableView;

    @FXML
    private Button mem_updateBtn;

    @FXML
    private Button membersBtn;

    @FXML
    private AnchorPane membersForm;

    @FXML
    private Button minimizeBtn;

    @FXML
    private TableColumn<Invoice, String> od_col_ID;

    @FXML
    private TableColumn<Invoice, LocalDate> od_col_dateCreated;

    @FXML
    private TableColumn<Invoice, String> od_col_member;

    @FXML
    private TableColumn<Invoice, String> od_col_employee;

    @FXML
    private TableColumn<Invoice, Double> od_col_totalPrice;

    @FXML
    private TableColumn<Invoice, Integer> od_col_discount;

    @FXML
    private TableColumn<Invoice, Double> od_col_finalPrice;
    
    @FXML
    private TableColumn <Invoice, Void> od_col_viewDetails;

    @FXML
    private Button od_deleteBtn;

    @FXML
    private Button od_newBtn;

    @FXML
    private TextField od_search;

    @FXML
    private TableView<Invoice> od_tableView;

    @FXML
    private AnchorPane ordersForm;

    @FXML
    private Button recordsBtn;

    @FXML
    private Button signOutBtn;

    @FXML
    private Label st_availableBooks;

    @FXML
    private BarChart<String, Number> st_incomeChart;

    @FXML
    private BarChart<String, Number> st_rankBarChart;

    @FXML
    private Label st_totalIncomes;

    @FXML
    private Label st_totalMembers;

    @FXML
    private Button staticsBtn;

    @FXML
    private AnchorPane staticsForm;

    
	
	private Connection connect;
	private String originalBookID = null;
	private ObservableList<Invoice> invoiceList = FXCollections.observableArrayList();
	private FilteredList<Invoice> filteredList;
	private ObservableList<Book> bookList = FXCollections.observableArrayList();
	private ObservableList<Member> memberList = FXCollections.observableArrayList();
	private final ObservableList<String> rankOptions = FXCollections.observableArrayList("Bronze", "Silver", "Gold", "Platinum", "Emerald", "Diamond");
	private String originalAccountID = null;
	
	private ObservableList<Book> mb_getBookListFromDB() {
		ObservableList<Book> list = FXCollections.observableArrayList();

		String query = "SELECT * FROM books";
		try (Connection conn = connectDB.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(query)) {

			while (rs.next()) {
				list.add(new Book(
						rs.getString("book_id"), 
						rs.getString("title"), 
						rs.getString("author"),
						rs.getString("genre"), 
						rs.getInt("quantity"),
						rs.getDouble("price") 
						));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	private final ObservableList<String> genreOptions = FXCollections.observableArrayList("Self-Help", "Fiction",
			"Non-fiction", "Science", "History", "Fantasy", "Biography", "Romance", "Horror", "Mystery", "Children");

	public void loadBooks() {
	    bookList.clear(); // Xóa dữ liệu cũ trong danh sách

	    String query = "SELECT * FROM Books";

	    try {
	        connect = connectDB.getConnection();
	        PreparedStatement stmt = connect.prepareStatement(query);
	        ResultSet rs = stmt.executeQuery();

	        while (rs.next()) {
	            String id = rs.getString("book_id");
	            String title = rs.getString("title");
	            String author = rs.getString("author");
	            String genre = rs.getString("genre");
	            int quantity = rs.getInt("quantity");
	            double price = rs.getDouble("price");

	            Book book = new Book(id, title, author, genre, quantity, price);
	            bookList.add(book);
	        }

	        // Sau khi cập nhật bookList, TableView sẽ tự động hiển thị lại nếu bạn đã gắn dữ liệu qua tableView.setItems(bookList);
	    } catch (SQLException e) {
	        e.printStackTrace();
	        showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load books from database.");
	    }
	}
     /**
	 * Nó gọi Stored Procedure sp_AddOrUpdateBookStock để thêm mới hoặc cập nhật thông tin sách.
	 */
	/**
	 * Phương thức này xử lý cả việc Thêm và Cập nhật sách.
	 * Nó được gọi bởi cả hai nút "Insert" và "Update".
	 * Tên phương thức là "mb_insertBook" để khớp với onAction trong file FXML.
	 */
	@FXML
	private void mb_insertBook() {
	    String id = mb_bookID.getText().trim();
	    String title = mb_title.getText().trim();
	    String author = mb_author.getText().trim();
	    String genre = mb_genre.getValue();
	    double price;
	    int quantity;

	    // 1. Kiểm tra dữ liệu đầu vào từ UI
	    if (id.isEmpty() || title.isEmpty() || author.isEmpty() || genre == null || mb_quantity.getText().trim().isEmpty() || mb_price.getText().trim().isEmpty()) {
	        showAlert(Alert.AlertType.ERROR, "Thiếu thông tin", "Vui lòng điền đầy đủ các trường.");
	        return;
	    }

	    try {
	        price = Double.parseDouble(mb_price.getText().trim());
	        quantity = Integer.parseInt(mb_quantity.getText().trim());
	    } catch (NumberFormatException e) {
	        showAlert(Alert.AlertType.ERROR, "Định dạng sai", "Số lượng và giá phải là số.");
	        return;
	    }
	    
	    // 2. Xác định xem đây là thêm mới (INSERT) hay cập nhật (UPDATE)
	    boolean isUpdate = (originalBookID != null && !originalBookID.isEmpty());
	    int quantityChange;

	    if (isUpdate) {
	        // Đây là trường hợp UPDATE
	        Book selectedBook = mb_tableView.getSelectionModel().getSelectedItem();
	        // Kiểm tra xem người dùng có thực sự chọn một cuốn sách hợp lệ không
	        if (selectedBook == null || !selectedBook.getBookID().equals(originalBookID)) {
	             showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng chọn lại sách cần cập nhật từ bảng.");
	             return;
	        }
	        int oldQuantity = selectedBook.getQuantity();
	        quantityChange = quantity - oldQuantity; // quantity là số lượng mới từ UI
	    } else {
	        // Đây là trường hợp INSERT
	        quantityChange = quantity;
	    }

	    // 3. Gọi Stored Procedure sp_AddOrUpdateBookStock
	    String sql = "{CALL dbo.sp_AddOrUpdateBookStock(?, ?, ?, ?, ?, ?)}";

	    try (Connection conn = connectDB.getConnection();
	         CallableStatement cstmt = conn.prepareCall(sql)) {

	        cstmt.setString(1, id);
	        cstmt.setString(2, title);
	        cstmt.setString(3, author);
	        cstmt.setString(4, genre);
	        cstmt.setInt(5, quantityChange); // Tham số quan trọng: số lượng thay đổi
	        cstmt.setDouble(6, price);

	        cstmt.executeUpdate();

	        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã lưu thông tin sách thành công.");
	        
	        // 4. Cập nhật lại giao diện
	        bookList = mb_getBookListFromDB(); // Gọi lại SP lấy danh sách sách
	        mb_tableView.setItems(bookList);
	        mb_clearBookFields();
	        originalBookID = null; // Reset lại ID gốc sau khi hoàn tất

	    } catch (SQLException e) {
	        // Hiển thị lỗi trả về từ RAISERROR của SQL Server
	        showAlert(Alert.AlertType.ERROR, "Lỗi cơ sở dữ liệu", e.getMessage());
	        e.printStackTrace();
	    }
	}

	/**
	 * Phương thức này được gọi bởi nút "Update" trong file FXML.
	 * Nó chỉ đơn giản là gọi lại phương thức xử lý chính là mb_insertBook().
	 */
	@FXML
	private void mb_updateBook() {
	    // Gọi phương thức chung để xử lý logic
	    mb_insertBook();
	}

	

	private void mb_clearBookFields() {
		mb_bookID.clear();
		mb_title.clear();
		mb_author.clear();
		mb_genre.getSelectionModel().clearSelection();
		mb_quantity.clear();
		mb_price.clear();
	}

	@FXML
	private void mb_handleClearBtn(ActionEvent event) {
		mb_clearBookFields();
	}

	@FXML
	private void mb_selectBook() {
		Book selectedBook = mb_tableView.getSelectionModel().getSelectedItem();
		if (selectedBook != null) {
			mb_bookID.setText(selectedBook.getBookID());
			mb_title.setText(selectedBook.getTitle());
			mb_author.setText(selectedBook.getAuthor());
			mb_genre.setValue(selectedBook.getGenre());
			mb_quantity.setText(String.valueOf(selectedBook.getQuantity()));
			mb_price.setText(String.valueOf(selectedBook.getPrice()));
			originalBookID = selectedBook.getBookID();
		}
	}

	
	@FXML
	private void mb_deleteBook() {
		String id = mb_bookID.getText().trim();
		if (id.isEmpty()) {
			showAlert(Alert.AlertType.ERROR, "No Selection", "Please select a book to delete.");
			return;
		}

		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Confirmation");
		alert.setHeaderText(null);
		alert.setContentText("Are you sure you want to delete this book?");
		Optional<ButtonType> option = alert.showAndWait();

		if (option.isPresent() && option.get() == ButtonType.OK) {
			try {
				connect = connectDB.getConnection();
				String sql = "DELETE FROM Books WHERE book_id=?";
				PreparedStatement stmt = connect.prepareStatement(sql);
				stmt.setString(1, id);
				stmt.executeUpdate();

				bookList = mb_getBookListFromDB();
				mb_tableView.setItems(bookList);
				mb_clearBookFields();
				originalBookID = null;

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	@FXML
	private void mb_resetFilter() {
		mb_genreFilter.getSelectionModel().clearSelection();
		mb_search.clear();
		mb_tableView.setItems(bookList);
	}

	private void mb_applyFilters() {
		String selectedGenre = mb_genreFilter.getValue();
		String searchText = mb_search.getText().toLowerCase().trim();

		ObservableList<Book> filteredList = bookList.filtered(book -> {
			boolean matchesGenre = (selectedGenre == null || selectedGenre.isEmpty())
					|| book.getGenre().equalsIgnoreCase(selectedGenre);
			boolean matchesTitle = searchText.isEmpty() || book.getTitle().toLowerCase().contains(searchText);
			return matchesGenre && matchesTitle;
		});

		mb_tableView.setItems(filteredList);
	}
	
	private void showAlert(Alert.AlertType type, String title, String message) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
	
	// Members
	private ObservableList<Member> mem_getMemberList() {
	    ObservableList<Member> list = FXCollections.observableArrayList();

	    String query = "SELECT m.account_id, m.full_name, m.email, m.phone, m.address, m.rank, m.total_spending, a.is_active " +
                "FROM Members m JOIN Accounts a ON m.account_id = a.account_id";
 
 try (Connection conn = connectDB.getConnection();
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(query)) {

     // Lặp qua từng dòng kết quả
     while (rs.next()) {
         // Tách việc lấy dữ liệu ra các biến riêng để code rõ ràng hơn
         String accountId = rs.getString("account_id");
         String fullName = rs.getString("full_name");
         String email = rs.getString("email");
         String phone = rs.getString("phone");
         String address = rs.getString("address");
         String rank = rs.getString("rank");
         double totalSpending = rs.getDouble("total_spending");
         boolean isActive = rs.getBoolean("is_active");

         // Chuyển đổi trạng thái từ boolean sang chuỗi để hiển thị
         String status = isActive ? "Active" : "Inactive";
         
         // Tạo đối tượng Member mới với đầy đủ thông tin
         Member member = new Member(accountId, fullName, email, phone, address, rank, status, totalSpending);
         
         // Thêm thành viên vào danh sách
         list.add(member);
     }
   } catch (SQLException e) {
     e.printStackTrace();
   }
 return list;
}  

	@FXML
	private void mem_selectMember(ActionEvent event) {
		mem_selectMember();
	}
	
	private void mem_selectMember() {
	    Member selected = mem_tableView.getSelectionModel().getSelectedItem();
	    if (selected != null) {
	        mem_id.setText(selected.getAccountId());
	        mem_fullName.setText(selected.getFullName());
	        mem_email.setText(selected.getEmail());
	        mem_phone.setText(selected.getPhone());
	        mem_address.setText(selected.getAddress());
	        mem_rank.setValue(selected.getRank());
	        originalAccountID = selected.getAccountId();
	    }
	}
	
	@FXML
	private void mem_clearMemberFields(ActionEvent event) {
	    mem_clearMemberFields();
	}
	
	private void mem_clearMemberFields() {
	    mem_id.clear();
	    mem_fullName.clear();
	    mem_email.clear();
	    mem_phone.clear();
	    mem_address.clear();
	    mem_rank.getSelectionModel().clearSelection();
	}
	
	@FXML
	private void mb_insertMember(ActionEvent event) {
	    String id = mem_id.getText().trim();
	    String fullName = mem_fullName.getText().trim();
	    String email = mem_email.getText().trim();
	    String phone = mem_phone.getText().trim();
	    String address = mem_address.getText().trim();
	    String rank = mem_rank.getValue();

	    if (id.isEmpty() || fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty() || rank == null) {
	        showAlert(Alert.AlertType.ERROR, "Missing Fields", "Please fill in all fields.");
	        return;
	    }

	    try (Connection conn = connectDB.getConnection()) {
	        String checkSql = "SELECT account_id FROM Members WHERE account_id = ?";
	        PreparedStatement checkStmt = conn.prepareStatement(checkSql);
	        checkStmt.setString(1, id);
	        ResultSet rs = checkStmt.executeQuery();

	        if (rs.next()) {
	            showAlert(Alert.AlertType.ERROR, "Duplicate ID", "A member with this ID already exists.");
	            return;
	        }

	        String insertAccount = "INSERT INTO Accounts (account_id, username, password_hash, role, is_active) VALUES (?, ?, ?, 'member', 1)";
	        PreparedStatement accStmt = conn.prepareStatement(insertAccount);
	        accStmt.setString(1, id);
	        accStmt.setString(2, id);
	        accStmt.setString(3, "123");
	        accStmt.executeUpdate();

	        String insertMember = "INSERT INTO Members (account_id, full_name, phone, email, address, rank) VALUES (?, ?, ?, ?, ?, ?)";
	        PreparedStatement memStmt = conn.prepareStatement(insertMember);
	        memStmt.setString(1, id);
	        memStmt.setString(2, fullName);
	        memStmt.setString(3, phone);
	        memStmt.setString(4, email);
	        memStmt.setString(5, address);
	        memStmt.setString(6, rank);
	        memStmt.executeUpdate();
	        
	        memberList.add(new Member(id, fullName, email, phone, address, rank, "Active"));
	        showAlert(Alert.AlertType.INFORMATION, "Success", "Member inserted successfully.");
	        mem_clearMemberFields();
	    } catch (SQLException e) {
	        e.printStackTrace();
	        showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to insert member.");
	    }
	}

	@FXML
	private void mb_updateMember(ActionEvent event) {
	    String newID = mem_id.getText().trim();
	    String fullName = mem_fullName.getText().trim();
	    String email = mem_email.getText().trim();
	    String phone = mem_phone.getText().trim();
	    String address = mem_address.getText().trim();
	    String rank = mem_rank.getValue();

	    if (newID.isEmpty() || fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty() || rank == null) {
	        showAlert(Alert.AlertType.ERROR, "Missing Fields", "Please fill in all fields.");
	        return;
	    }

	    try (Connection conn = connectDB.getConnection()) {
	        if (!newID.equals(originalAccountID)) {
	            String checkSql = "SELECT account_id FROM Members WHERE account_id = ?";
	            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
	            checkStmt.setString(1, newID);
	            ResultSet rs = checkStmt.executeQuery();
	            if (rs.next()) {
	                showAlert(Alert.AlertType.ERROR, "Duplicate ID", "Member ID already exists.");
	                return;
	            }
	        }

	        String updateMember = "UPDATE Members SET account_id=?, full_name=?, phone=?, email=?, address=?, rank=? WHERE account_id=?";
	        PreparedStatement stmt = conn.prepareStatement(updateMember);
	        stmt.setString(1, newID);
	        stmt.setString(2, fullName);
	        stmt.setString(3, phone);
	        stmt.setString(4, email);
	        stmt.setString(5, address);
	        stmt.setString(6, rank);
	        stmt.setString(7, originalAccountID);
	        stmt.executeUpdate();

	        showAlert(Alert.AlertType.INFORMATION, "Success", "Member updated successfully.");
	        memberList = mem_getMemberList();
	        mem_tableView.setItems(memberList);
	        mem_clearMemberFields();
	        originalAccountID = null;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update member.");
	    }
	}

	@FXML
	private void mem_deleteMember(ActionEvent event) {
	    String id = mem_id.getText().trim();
	    if (id.isEmpty()) {
	        showAlert(Alert.AlertType.ERROR, "No Selection", "Please select a member.");
	        return;
	    }

	    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete this member?", ButtonType.YES, ButtonType.NO);
	    Optional<ButtonType> result = alert.showAndWait();

	    if (result.isPresent() && result.get() == ButtonType.YES) {
	        try (Connection conn = connectDB.getConnection()) {
	            conn.setAutoCommit(false);

	            String deleteMemberSQL = "DELETE FROM Members WHERE account_id = ?";
	            try (PreparedStatement ps1 = conn.prepareStatement(deleteMemberSQL)) {
	                ps1.setString(1, id);
	                ps1.executeUpdate();
	            }

	            String deleteAccountSQL = "DELETE FROM Accounts WHERE account_id = ?";
	            try (PreparedStatement ps2 = conn.prepareStatement(deleteAccountSQL)) {
	                ps2.setString(1, id);
	                ps2.executeUpdate();
	            }

	            conn.commit();

	            memberList = mem_getMemberList();
	            mem_tableView.setItems(memberList);
	            mem_clearMemberFields();
	            originalAccountID = null;

	        } catch (SQLException e) {
	            e.printStackTrace();
	            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete member.");
	        }
	    }
	}

	private void searchMembers(String name) {
	    memberList.clear();
	    
	    String sql = "SELECT account_id, full_name, email, phone, address, rank FROM Members WHERE full_name LIKE ?";
	    
	    try (Connection conn = connectDB.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {
	         
	        stmt.setString(1, "%" + name + "%"); // LIKE với wildcard
	        
	        ResultSet rs = stmt.executeQuery();
	        
	        while (rs.next()) {
	            String id = rs.getString("account_id");
	            String fullName = rs.getString("full_name");
	            String email = rs.getString("email");
	            String phone = rs.getString("phone");
	            String address = rs.getString("address");
	            String rank = rs.getString("rank");
	            
	            memberList.add(new Member(id, fullName, email, phone, address, rank, "Active"));
	        }
	        
	    } catch (SQLException e) {
	        e.printStackTrace();
	        showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to search members.");
	    }
	}

	// Orders
	private void addButtonToTable() {
	    Callback<TableColumn<Invoice, Void>, TableCell<Invoice, Void>> cellFactory = new Callback<>() {
	        @Override
	        public TableCell<Invoice, Void> call(final TableColumn<Invoice, Void> param) {
	            return new TableCell<>() {
	                private final Button btn = new Button("View Details");

	                {
	                    btn.setStyle("-fx-background-color: transparent;" +
	                                 "-fx-text-fill: #0066cc;" +
	                                 "-fx-underline: true;");
	                    btn.setCursor(Cursor.HAND);

	                    btn.setOnAction((ActionEvent event) -> {
	                        Invoice invoice = getTableView().getItems().get(getIndex());
	                        showInvoiceDetails(invoice);
	                    });
	                }

	                @Override
	                public void updateItem(Void item, boolean empty) {
	                    super.updateItem(item, empty);
	                    if (empty) {
	                        setGraphic(null);
	                    } else {
	                        setGraphic(btn);
	                    }
	                }
	            };
	        }
	    };

	    od_col_viewDetails.setCellFactory(cellFactory);
	}
	
	public ObservableList<Invoice> loadInvoices() {
	    ObservableList<Invoice> invoiceList = FXCollections.observableArrayList();

	    String sql = "SELECT invoice_id, member_id, employee_id, date_created, total_price, discount_applied FROM Invoices";

	    try (Connection conn = connectDB.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql);
	         ResultSet rs = stmt.executeQuery()) {

	        while (rs.next()) {
	            String invoiceId = rs.getString("invoice_id");
	            LocalDate dateCreated = rs.getDate("date_created").toLocalDate();
	            String memberId = rs.getString("member_id");
	            String employeeId = rs.getString("employee_id");
	            double totalPrice = rs.getDouble("total_price");
	            int discount = rs.getInt("discount_applied");

	            Invoice invoice = new Invoice(invoiceId, dateCreated, memberId, employeeId, totalPrice, discount);
	            invoiceList.add(invoice);
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return invoiceList;
	}
	
	private void showInvoiceDetails(Invoice invoice) {
	    try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml/make_receipt.fxml"));
	        Parent root = loader.load();

	        make_order_controller controller = loader.getController();
	        controller.loadReceipt(
	            invoice.getInvoiceId(),
	            invoice.getMemberId()
	        );

	        Stage stage = new Stage();
	        stage.initStyle(StageStyle.UNDECORATED);
	        stage.setScene(new Scene(root));
	        stage.show();
	    } catch (IOException | SQLException e) {
	        e.printStackTrace();
	        Alert alert = new Alert(Alert.AlertType.ERROR);
	        alert.setTitle("Error");
	        alert.setHeaderText("Failed to load receipt details");
	        alert.setContentText("An error occurred while opening the receipt view.");
	        alert.showAndWait();
	    }
	}

	

	@FXML
	private void mr_handleNewBtn(ActionEvent event) {
	    try {
	        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/fxml/make_receipt.fxml"));
	        Parent root = fxmlLoader.load();
	        
	        make_order_controller receiptController = fxmlLoader.getController();
	        receiptController.setAdminController(this);
	        
	        Stage stage = new Stage();
	        stage.initStyle(StageStyle.UNDECORATED);
	        stage.setScene(new Scene(root));
	        stage.initModality(Modality.APPLICATION_MODAL);
	        stage.showAndWait();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}

	public void refreshInvoiceTable() {
        invoiceList.clear();

        String sql = "SELECT invoice_id, date_created, member_id, employee_id, total_price, discount_applied FROM Invoices";

        try (Connection conn = connectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String invoiceId = rs.getString("invoice_id");
                LocalDate dateCreated = rs.getDate("date_created").toLocalDate();
                String memberId = rs.getString("member_id");
                String employeeId = rs.getString("employee_id");
                double totalPrice = rs.getDouble("total_price");
                int discountApplied = rs.getInt("discount_applied");

                Invoice invoice = new Invoice(invoiceId, dateCreated, memberId, employeeId, totalPrice, discountApplied);
                invoiceList.add(invoice);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
	
	@FXML
	private void handleDeleteInvoice() {
	    Invoice selectedInvoice = od_tableView.getSelectionModel().getSelectedItem();

	    if (selectedInvoice == null) {
	        Alert alert = new Alert(Alert.AlertType.WARNING);
	        alert.setTitle("No Selection");
	        alert.setHeaderText(null);
	        alert.setContentText("Please select an invoice to delete.");
	        alert.showAndWait();
	        return;
	    }

	    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
	    confirm.setTitle("Confirm Deletion");
	    confirm.setHeaderText("Are you sure you want to delete this invoice?");

	    Optional<ButtonType> result = confirm.showAndWait();
	    if (result.isPresent() && result.get() == ButtonType.OK) {
	        try (Connection conn = connectDB.getConnection()) {
	            conn.setAutoCommit(false); // bật transaction

	            String invoiceId = selectedInvoice.getInvoiceId();

	            // 1. Lấy danh sách sách và quantity từ hóa đơn
	            String getDetailsQuery = "SELECT book_id, quantity FROM Invoice_Details WHERE invoice_id = ?";
	            PreparedStatement getDetailsStmt = conn.prepareStatement(getDetailsQuery);
	            getDetailsStmt.setString(1, invoiceId);
	            ResultSet rs = getDetailsStmt.executeQuery();

	            Map<String, Integer> bookReturns = new HashMap<>();
	            while (rs.next()) {
	                String bookId = rs.getString("book_id");
	                int quantity = rs.getInt("quantity");
	                bookReturns.put(bookId, quantity);
	            }

	            // 2. Cập nhật lại quantity trong bảng Books
	            String updateBookQuery = "UPDATE Books SET quantity = quantity + ? WHERE book_id = ?";
	            PreparedStatement updateBookStmt = conn.prepareStatement(updateBookQuery);
	            for (Map.Entry<String, Integer> entry : bookReturns.entrySet()) {
	                updateBookStmt.setInt(1, entry.getValue());
	                updateBookStmt.setString(2, entry.getKey());
	                updateBookStmt.addBatch();
	            }
	            updateBookStmt.executeBatch();

	            // 3. Xóa Invoice_Details
	            String deleteDetailsQuery = "DELETE FROM Invoice_Details WHERE invoice_id = ?";
	            PreparedStatement deleteDetailsStmt = conn.prepareStatement(deleteDetailsQuery);
	            deleteDetailsStmt.setString(1, invoiceId);
	            deleteDetailsStmt.executeUpdate();

	            // 4. Xóa hóa đơn trong Invoices
	            String deleteInvoiceQuery = "DELETE FROM Invoices WHERE invoice_id = ?";
	            PreparedStatement deleteInvoiceStmt = conn.prepareStatement(deleteInvoiceQuery);
	            deleteInvoiceStmt.setString(1, invoiceId);
	            int rowsDeleted = deleteInvoiceStmt.executeUpdate();

	            if (rowsDeleted > 0) {
	                conn.commit();
	                invoiceList.remove(selectedInvoice);
	            } else {
	                conn.rollback();
	            }

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
	}


	// Statistics
	public void st_updateAvailableBooks() {
		String query = "SELECT SUM(quantity) AS total_quantity FROM Books";
		
		try (Connection conn = connectDB.getConnection();
	             PreparedStatement stmt = conn.prepareStatement(query);
	             ResultSet rs = stmt.executeQuery()) {

	            if (rs.next()) {
	                int total = rs.getInt("total_quantity");
	                st_availableBooks.setText(String.valueOf(total));
	            } else {
	            	st_availableBooks.setText("0");
	            }

	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	}
	
	public void st_updateTotalMembers() {
	    String query = "SELECT COUNT(*) AS total_members FROM Accounts WHERE role = 'member'";

	    try (Connection conn = connectDB.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(query);
	         ResultSet rs = stmt.executeQuery()) {

	        if (rs.next()) {
	            int total = rs.getInt("total_members");
	            st_totalMembers.setText(String.valueOf(total));
	        } else {
	            st_totalMembers.setText("0");
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public void st_updateTotalIncomes() {
	    String query = "SELECT SUM(total_price * (100 - discount_applied) / 100) AS total_income_after_discount FROM Invoices";

	    try (Connection conn = connectDB.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(query);
	         ResultSet rs = stmt.executeQuery()) {

	        if (rs.next()) {
	            double total = rs.getDouble("total_income_after_discount");
	            st_totalIncomes.setText("$" + String.format("%.2f", total));
	        } else {
	            st_totalIncomes.setText("$0.00");
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	        st_totalIncomes.setText("$0.00");
	    }
	}

	
	public void loadIncomeBarChart() {
	    String query = """
	        SELECT YEAR(date_created) AS year, MONTH(date_created) AS month, SUM(total_price) AS total_income
	        FROM Invoices
	        GROUP BY YEAR(date_created), MONTH(date_created)
	        ORDER BY YEAR(date_created), MONTH(date_created)
	        """;

	    XYChart.Series<String, Number> series = new XYChart.Series<>();

	    try (Connection conn = connectDB.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(query);
	         ResultSet rs = stmt.executeQuery()) {

	        while (rs.next()) {
	            int year = rs.getInt("year");
	            int month = rs.getInt("month");
	            double income = rs.getDouble("total_income");

	            String label = formatYearMonthLabel(year, month);
	            series.getData().add(new XYChart.Data<>(label, income));
	        }
	        st_incomeChart.getData().clear();
	        st_incomeChart.getData().add(series);

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	private String formatYearMonthLabel(int year, int month) {
	    return year + "-" + String.format("%02d", month);
	}
	
	public void loadMemberRankBarChart() {
	    String sql = "SELECT rank, COUNT(*) AS total FROM Members GROUP BY rank";

	    try (Connection conn = connectDB.getConnection();
	         PreparedStatement pst = conn.prepareStatement(sql);
	         ResultSet resultSet = pst.executeQuery()) {  // Thiếu executeQuery

	        // Clear old data
	        st_rankBarChart.getData().clear();

	        // Create new series
	        XYChart.Series<String, Number> series = new XYChart.Series<>();
	        series.setName("Thành viên theo hạng");

	        while (resultSet.next()) {
	            String rank = resultSet.getString("rank");
	            int count = resultSet.getInt("total");

	            series.getData().add(new XYChart.Data<>(rank, count));
	        }

	        st_rankBarChart.getData().add(series);

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	
	@FXML
	public void signout() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Confirmation Message");
		alert.setHeaderText(null);
		alert.setContentText("Are you sure you want to sign out?");
		Optional<ButtonType> option = alert.showAndWait();
		if (option.isPresent() && option.get().equals(ButtonType.OK)) {
			try {
				Parent root = FXMLLoader.load(getClass().getResource("/views/fxml/login.fxml"));
				Stage primaryStage = new Stage();
				Scene scene = new Scene(root);

				primaryStage.initStyle(StageStyle.TRANSPARENT);
				primaryStage.setScene(scene);
				primaryStage.show();

				Stage currentStage = (Stage) signOutBtn.getScene().getWindow();
				currentStage.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@FXML
	public void switchForm(ActionEvent event) {

		if (event.getSource() == manageBooksBtn) {
			manageBooksForm.setVisible(true);
			ordersForm.setVisible(false);
			membersForm.setVisible(false);
			staticsForm.setVisible(false);
			
			loadBooks();
		} else if (event.getSource() == recordsBtn) {
			manageBooksForm.setVisible(false);
			ordersForm.setVisible(true);
			membersForm.setVisible(false);
			staticsForm.setVisible(false);
			
			refreshInvoiceTable();
			
		} else if (event.getSource() == membersBtn) {
			manageBooksForm.setVisible(false);
			ordersForm.setVisible(false);
			membersForm.setVisible(true);
			staticsForm.setVisible(false);

		} else if (event.getSource() == staticsBtn) {
			manageBooksForm.setVisible(false);
			ordersForm.setVisible(false);
			membersForm.setVisible(false);
			staticsForm.setVisible(true);
			
			st_updateAvailableBooks();
			st_updateTotalMembers();
			st_updateTotalIncomes();
			loadIncomeBarChart();
			loadMemberRankBarChart();
		}

	}

	@FXML
	public void close() {
		Stage stage = (Stage) exitBtn.getScene().getWindow();
		stage.close();
	}

	@FXML
	public void minimize(ActionEvent event) {
		Stage stage = (Stage) minimizeBtn.getScene().getWindow();
		stage.setIconified(true);
	}

	private double x = 0;
	private double y = 0;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		main_form.setOnMousePressed(event -> {
			x = event.getSceneX();
			y = event.getSceneY();
		});

		main_form.setOnMouseDragged(event -> {
			Stage stage = (Stage) main_form.getScene().getWindow();
			stage.setX(event.getScreenX() - x);
			stage.setY(event.getScreenY() - y);
		});
		
		// Manage_Books
		mb_col_bookID.setCellValueFactory(new PropertyValueFactory<>("bookID"));
		mb_col_title.setCellValueFactory(new PropertyValueFactory<>("title"));
		mb_col_author.setCellValueFactory(new PropertyValueFactory<>("author"));
		mb_col_genre.setCellValueFactory(new PropertyValueFactory<>("genre"));
		mb_col_quantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
		mb_col_price.setCellValueFactory(new PropertyValueFactory<>("price"));

		mb_genre.setItems(genreOptions);
		mb_genreFilter.setItems(genreOptions);

		bookList = mb_getBookListFromDB();
		mb_tableView.setItems(bookList);

		mb_genreFilter.setOnAction(e -> mb_applyFilters());
		mb_search.textProperty().addListener((observable, oldValue, newValue) -> mb_applyFilters());

		mb_resetBtn.setOnAction(e -> {
			mb_genreFilter.getSelectionModel().clearSelection();
			mb_search.clear();
			mb_tableView.setItems(bookList);
		});

		mb_tableView.setOnMouseClicked(event -> {
			if (event.getClickCount() == 1) {
				mb_selectBook();
			}
		});
	
		// Members
		mem_col_ID.setCellValueFactory(new PropertyValueFactory<>("accountId"));
	    mem_col_fullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
	    mem_col_email.setCellValueFactory(new PropertyValueFactory<>("email"));
	    mem_col_phone.setCellValueFactory(new PropertyValueFactory<>("phone"));
	    mem_col_address.setCellValueFactory(new PropertyValueFactory<>("address"));
	    mem_col_rank.setCellValueFactory(new PropertyValueFactory<>("rank"));
	    mem_col_totalSpending.setCellValueFactory(new PropertyValueFactory<>("totalSpending"));
	    mem_col_status.setCellValueFactory(new PropertyValueFactory<>("status"));
	    mem_rank.setItems(rankOptions);
	    memberList = mem_getMemberList();
	    mem_tableView.setItems(memberList);

	    mem_tableView.setOnMouseClicked(event -> {
	        if (event.getClickCount() == 1) {
	            mem_selectMember();
	        }
	    });
	    
	    mem_search.setOnKeyReleased(event -> {
	        String searchText = mem_search.getText().trim();
	        searchMembers(searchText);
	    });
	    
	    // Orders
	    od_col_ID.setCellValueFactory(cellData -> cellData.getValue().invoiceIdProperty());
	    od_col_dateCreated.setCellValueFactory(cellData -> cellData.getValue().dateCreatedProperty());
	    od_col_member.setCellValueFactory(cellData -> cellData.getValue().memberIdProperty());
	    od_col_employee.setCellValueFactory(cellData -> cellData.getValue().employeeIdProperty());
	    od_col_totalPrice.setCellValueFactory(cellData -> cellData.getValue().totalPriceProperty().asObject());
	    od_col_discount.setCellValueFactory(cellData -> cellData.getValue().discountAppliedProperty().asObject());
	    od_col_finalPrice.setCellValueFactory(cellData -> cellData.getValue().finalPriceProperty().asObject());
	    
	    od_col_finalPrice.setCellFactory(tc -> new TableCell<Invoice, Double>() {
	        @Override
	        protected void updateItem(Double price, boolean empty) {
	            super.updateItem(price, empty);
	            if (empty || price == null) {
	                setText(null);
	            } else {
	                setText(String.format("%.2f", price));
	            }
	        }
	    });
	    
	    invoiceList = loadInvoices();
	    
	    FilteredList<Invoice> filteredList = new FilteredList<>(invoiceList, p -> true);
	    od_tableView.setItems(filteredList);
	    
	    od_search.textProperty().addListener((observable, oldValue, newValue) -> {
	        filteredList.setPredicate(invoice -> {
	            if (newValue == null || newValue.isEmpty()) {
	                return true;
	            }

	            String lowerCaseFilter = newValue.toLowerCase();

	            return invoice.getInvoiceId().toLowerCase().contains(lowerCaseFilter)
	                || invoice.getMemberId().toLowerCase().contains(lowerCaseFilter)
	                || invoice.getEmployeeId().toLowerCase().contains(lowerCaseFilter);
	        });
	    });
	    
	    addButtonToTable();
	}
	
}
