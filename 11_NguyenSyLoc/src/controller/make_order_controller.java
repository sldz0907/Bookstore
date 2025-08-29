package controller;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import dao.connectDB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Spinner;
import javafx.beans.value.ChangeListener;
import model.Book;
import model.Invoice;

public class make_order_controller implements Initializable {
	
    @FXML
    private TableColumn<Book, String> mr_col_author;

    @FXML
    private TableColumn<Book, Integer> mr_col_available;

    @FXML
    private TableColumn<Book, String> mr_col_id;

    @FXML
    private TableColumn<Book, Double> mr_col_price;

    @FXML
    private TableColumn<Book, Integer> mr_col_selected;

    @FXML
    private TableColumn<Book, String> mr_col_title;

    @FXML
    private TableView<Book> mr_tableView;
    
	@FXML 
	private ComboBox<String> mr_id;
	
	@FXML 
	private TextField mr_memberName;
	
	@FXML
	private BorderPane mr_form;
	
	@FXML
	private Button mr_close;
	
	@FXML
    private Label mr_subtotal;
	
	@FXML
    private Label mr_discount;
	
	@FXML
    private Label mr_total;
	
	@FXML
	private Button mr_create;
	
	@FXML
	private Button mr_clear;
	
	@FXML
	private TextField mr_search;
	
	@FXML
	private VBox mr_vbox_books;
	
	private Map<String, String> memberMap = new HashMap<>();
	
	private admin_controller admin_controller; 

    public void setAdminController(admin_controller admin_controller) {
        this.admin_controller = admin_controller;
    }
	
	private Connection conn;
	private int discountPercent = 0; 
    private ObservableList<Book> bookList;
	
    public void setInvoice(Invoice invoice) {
        String memberId = invoice.memberIdProperty().get();

        mr_id.getItems().clear();
        mr_id.getItems().add(memberId);
        mr_id.setValue(memberId);

        updateMemberInfo(memberId);

        try (Connection conn = connectDB.getConnection()) {
            ObservableList<Book> orderedBooks = mr_getBooksFromInvoice(invoice.invoiceIdProperty().get());
            bookList = mr_getBookListFromDB();

            for (Book b : bookList) {
                for (Book ordered : orderedBooks) {
                    if (b.getBookID().equals(ordered.getBookID())) {
                        b.setSelectedQuantity(ordered.getSelectedQuantity()); // ✅ Cập nhật số lượng
                    }
                }
            }

            mr_tableView.setItems(bookList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateMemberInfo(String memberId) {
        if (memberId == null || memberId.isEmpty()) {
            mr_memberName.setText("");
            mr_discount.setText("0%");
            discountPercent = 0;
            return;
        }

        String sqlName = "SELECT full_name FROM Members WHERE account_id = ?";
        String sqlDiscount = "SELECT discount_percent FROM Rank_Policies WHERE rank = (SELECT rank FROM Members WHERE account_id = ?)";

        try (
            Connection conn = connectDB.getConnection();
            PreparedStatement psName = conn.prepareStatement(sqlName);
            PreparedStatement psDiscount = conn.prepareStatement(sqlDiscount);
        ) {
            // Query name
            psName.setString(1, memberId);
            try (ResultSet rsName = psName.executeQuery()) {
                if (rsName.next()) {
                    mr_memberName.setText(rsName.getString("full_name"));
                } else {
                    mr_memberName.setText("");
                }
            }

            // Query discount
            psDiscount.setString(1, memberId);
            try (ResultSet rsDiscount = psDiscount.executeQuery()) {
                if (rsDiscount.next()) {
                    discountPercent = rsDiscount.getInt("discount_percent");
                } else {
                    discountPercent = 0;
                }
            }
            mr_discount.setText("-" + discountPercent + "%");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
	
	private ObservableList<Book> mr_getBooksFromInvoice(String invoiceId) {
	    ObservableList<Book> books = FXCollections.observableArrayList();
	    String sql = "SELECT b.book_id, b.title, b.author, b.genre, id.quantity, id.price_each " +
	                 "FROM Books b " +
	                 "JOIN Invoice_Details id ON b.book_id = id.book_id " +
	                 "WHERE id.invoice_id = ?";
	    try (Connection conn = connectDB.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setString(1, invoiceId);
	        ResultSet rs = stmt.executeQuery();
	        while (rs.next()) {
	            String bookID = rs.getString("book_id");
	            String title = rs.getString("title");
	            String author = rs.getString("author");
	            String genre = rs.getString("genre");
	            int quantity = rs.getInt("quantity"); 
	            double price = rs.getDouble("price_each");

	            Book book = new Book(bookID, title, author, genre, quantity, price);
	            
	            books.add(book);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return books;
	}

	
	private ObservableList<Book> mr_getBookListFromDB() {
	    ObservableList<Book> list = FXCollections.observableArrayList();
	    String query = "SELECT book_id, title, author, quantity, price FROM books";

	    try (Connection conn = connectDB.getConnection();
	         Statement stmt = conn.createStatement();
	         ResultSet rs = stmt.executeQuery(query)) {

	        while (rs.next()) {
	            list.add(new Book(
	                rs.getString("book_id"),
	                rs.getString("title"),
	                rs.getString("author"),
	                rs.getInt("quantity"),
	                rs.getDouble("price")
	            ));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return list;
	}

	
	private void addSpinnerToBookTable() {
	    mr_col_selected.setCellValueFactory(cellData -> cellData.getValue().selectedQuantityProperty().asObject());

	    mr_col_selected.setCellFactory(column -> new TableCell<>() {
	        private final Spinner<Integer> spinner = new Spinner<>();
	        private final ChangeListener<Integer> listener = (obs, oldVal, newVal) -> {
	            Book book = getTableView().getItems().get(getIndex());
	            if (newVal != null) {
	                book.setSelectedQuantity(newVal);
	                updateReceiptView(bookList, discountPercent);
	            }
	        };

	        {
	            spinner.setEditable(true);
	            spinner.setPrefWidth(100);
	        }

	        @Override
	        protected void updateItem(Integer item, boolean empty) {
	            super.updateItem(item, empty);

	            if (empty || getIndex() >= getTableView().getItems().size()) {
	                setGraphic(null);
	            } else {
	                Book book = getTableView().getItems().get(getIndex());

	                spinner.valueProperty().removeListener(listener); // tránh lặp listener

	                SpinnerValueFactory<Integer> valueFactory =
	                    new SpinnerValueFactory.IntegerSpinnerValueFactory(
	                        0,
	                        book.getQuantity(),
	                        book.getSelectedQuantity()
	                    );

	                spinner.setValueFactory(valueFactory);
	                spinner.valueProperty().addListener(listener);

	                setGraphic(spinner);
	            }
	        }
	    });
	}

	
	public void loadReceipt(String invoiceId, String memberId) throws SQLException {
	    double subtotal = 0;
	    mr_vbox_books.getChildren().clear();
	    Map<String, Integer> invoiceBookQuantities = new HashMap<>();

	    // 1. Lấy danh sách sách trong hóa đơn
	    String query = """
	        SELECT b.book_id, b.title, idt.quantity, b.price
	        FROM Invoice_Details idt
	        JOIN Books b ON idt.book_id = b.book_id
	        WHERE idt.invoice_id = ?
	    """;
	    PreparedStatement stmt = conn.prepareStatement(query);
	    stmt.setString(1, invoiceId);
	    ResultSet rs = stmt.executeQuery();

	    while (rs.next()) {
	        String bookId = rs.getString("book_id");
	        String title = rs.getString("title");
	        int quantity = rs.getInt("quantity");
	        double price = rs.getDouble("price");

	        invoiceBookQuantities.put(bookId, quantity);
	        subtotal += quantity * price;

	        HBox line = new HBox(10);
	        Label titleLabel = new Label(title);
	        titleLabel.setStyle("-fx-font-size: 14px;");
	        Label priceLabel = new Label(quantity + " x $" + String.format("%.2f", price));
	        priceLabel.setStyle("-fx-font-size: 14px;");
	        Region spacer = new Region();
	        HBox.setHgrow(spacer, Priority.ALWAYS);
	        line.getChildren().addAll(titleLabel, spacer, priceLabel);
	        mr_vbox_books.getChildren().add(line);
	    }

	    // 2. Cập nhật selectedQuantity cho TableView
	    for (Book book : bookList) {
	        if (invoiceBookQuantities.containsKey(book.getBookID())) {
	            book.setSelectedQuantity(invoiceBookQuantities.get(book.getBookID()));
	        } else {
	            book.setSelectedQuantity(0);
	        }
	    }
	    mr_tableView.refresh();

	    // ✅ 3. Lấy discount_applied từ bảng Invoices (sửa chỗ này!)
	    int discountPercent = 0;
	    String discountQuery = "SELECT discount_applied FROM Invoices WHERE invoice_id = ?";
	    PreparedStatement dstmt = conn.prepareStatement(discountQuery);
	    dstmt.setString(1, invoiceId);
	    ResultSet drs = dstmt.executeQuery();

	    if (drs.next()) {
	        discountPercent = drs.getInt("discount_applied");
	    }

	    this.discountPercent = discountPercent;

	    // 4. Tính tổng tiền sau giảm giá
	    double total = subtotal * (1 - discountPercent / 100.0);
	    mr_subtotal.setText(String.format("$%.2f", subtotal));
	    mr_discount.setText("-" + discountPercent + "%");
	    mr_total.setText(String.format("$%.2f", total));

	    // 5. Hiển thị thông tin member
	    loadMemberInfo(memberId);
	}



	public void loadMemberInfo(String memberId) throws SQLException {
	    // Giả sử bạn đã có memberMap hoặc có thể query tên member từ DB

	    // Nếu chưa có memberMap, bạn có thể query như sau:
	    String sql = "SELECT full_name FROM Members WHERE account_id = ?";
	    PreparedStatement stmt = conn.prepareStatement(sql);
	    stmt.setString(1, memberId);
	    ResultSet rs = stmt.executeQuery();

	    String memberName = "";
	    if (rs.next()) {
	        memberName = rs.getString("full_name");
	    }

	    // Set comboBox nếu đã có items
	    if (!mr_id.getItems().contains(memberId)) {
	        mr_id.getItems().add(memberId);  // Nếu chưa có thì thêm vào
	    }
	    mr_id.setValue(memberId);

	    // Set textField
	    mr_memberName.setText(memberName);
	}

	
	private void updateReceiptView(ObservableList<Book> books, int discountPercent) {
	    mr_vbox_books.getChildren().clear();
	    double subtotal = 0;

	    for (Book book : books) {
	        int qty = book.getSelectedQuantity();
	        if (qty > 0) {
	            double price = book.getPrice();
	            double itemTotal = qty * price;
	            subtotal += itemTotal;

	            HBox line = new HBox(10);
	            Label titleLabel = new Label(book.getTitle());
	            Label priceLabel = new Label(qty + " x $" + String.format("%.2f", price));
	            Region spacer = new Region();
	            HBox.setHgrow(spacer, Priority.ALWAYS);

	            line.getChildren().addAll(titleLabel, spacer, priceLabel);
	            mr_vbox_books.getChildren().add(line);
	        }
	    }

	    double total = subtotal * (1 - discountPercent / 100.0);

	    mr_subtotal.setText(String.format("$%.2f", subtotal));
	    mr_discount.setText("-" + discountPercent + "%");
	    mr_total.setText(String.format("$%.2f", total));
	}
	
	private void loadMembersFromDatabase() {
        try {
            Connection conn = connectDB.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
            	    "SELECT m.account_id, m.full_name " +
            	    "FROM Members m JOIN Accounts a ON m.account_id = a.account_id " +
            	    "WHERE a.role = 'member' " +
            	    "ORDER BY m.account_id ASC"
            	);
                        
            while (rs.next()) {
                String id = rs.getString("account_id");
                String name = rs.getString("full_name");
                memberMap.put(id, name);
            }
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
	
	
	@FXML
	private void handleCreate(ActionEvent event) {
	    createInvoice();
	}
	
	public String getSelectedMemberId() {
	    Object selected = mr_id.getValue();
	    if (selected != null) {
	        return selected.toString();

	    }
	    return null; 
	}

	public void createInvoice() {
	    Connection conn = null;
	    PreparedStatement psInvoice = null;
	    PreparedStatement psInvoiceDetail = null;
	    PreparedStatement psUpdateBook = null;
	    PreparedStatement psGetMaxInvoiceId = null;
	    PreparedStatement psGetDiscount = null;

	    try {
	        conn = connectDB.getConnection();
	        conn.setAutoCommit(false);  // Bắt đầu transaction

	        // 1. Sinh invoice_id mới (dùng TOP 1 thay cho LIMIT 1)
	        String sqlGetMaxInvoiceId = "SELECT TOP 1 invoice_id FROM Invoices ORDER BY invoice_id DESC";
	        psGetMaxInvoiceId = conn.prepareStatement(sqlGetMaxInvoiceId);
	        ResultSet rsMax = psGetMaxInvoiceId.executeQuery();

	        String newInvoiceId;
	        if (rsMax.next()) {
	            String lastId = rsMax.getString("invoice_id");
	            int num = Integer.parseInt(lastId.substring(2)) + 1;
	            newInvoiceId = String.format("IV%02d", num);
	        } else {
	            newInvoiceId = "IV01";
	        }

	        // 2. Lấy member_id từ comboBox (giả sử bạn có phương thức getSelectedMemberId())
	        String memberId = getSelectedMemberId(); // hoặc null nếu khách lẻ

	        // 3. employee_id cố định
	        String employeeId = "A01";

	        // 4. Lấy sách và số lượng đã chọn (giả sử bạn có List<Book> bookList chứa các book có selectedQuantity)
	        List<Book> selectedBooks = bookList.stream()
	            .filter(book -> book.getSelectedQuantity() > 0)
	            .collect(Collectors.toList());

	        if (selectedBooks.isEmpty()) {
	            System.out.println("No books selected.");
	            return;
	        }

	        // 5. Lấy discount percent từ Rank_Policies theo rank của member
	        int discountPercent = 0;
	        if (memberId != null) {
	            String sqlGetDiscount = "SELECT discount_percent FROM Rank_Policies " +
	                "WHERE rank = (SELECT rank FROM Members WHERE account_id = ?)";
	            psGetDiscount = conn.prepareStatement(sqlGetDiscount);
	            psGetDiscount.setString(1, memberId);
	            ResultSet rsDiscount = psGetDiscount.executeQuery();
	            if (rsDiscount.next()) {
	                discountPercent = rsDiscount.getInt("discount_percent");
	            }
	            rsDiscount.close();
	            psGetDiscount.close();
	        }

	        // Tính tổng tiền trước và sau giảm giá
	        double totalPriceBeforeDiscount = 0;
	        for (Book b : selectedBooks) {
	            totalPriceBeforeDiscount += b.getPrice() * b.getSelectedQuantity();
	        }
	        double totalPriceAfterDiscount = totalPriceBeforeDiscount * (1 - discountPercent / 100.0);

	        String sqlInsertInvoice = "INSERT INTO Invoices(invoice_id, member_id, employee_id, date_created, total_price, discount_applied) " +
	            "VALUES (?, ?, ?, CAST(GETDATE() AS DATE), ?, ?)";
	        psInvoice = conn.prepareStatement(sqlInsertInvoice);
	        psInvoice.setString(1, newInvoiceId);
	        if (memberId != null) {
	            psInvoice.setString(2, memberId);
	        } else {
	            psInvoice.setNull(2, java.sql.Types.VARCHAR);
	        }
	        psInvoice.setString(3, employeeId);
	        psInvoice.setDouble(4, totalPriceAfterDiscount);
	        psInvoice.setInt(5, discountPercent);
	        psInvoice.executeUpdate();

	        String sqlInsertDetail = "INSERT INTO Invoice_Details(invoice_id, book_id, quantity, price_each) VALUES (?, ?, ?, ?)";
	        String sqlUpdateBookQty = "UPDATE Books SET quantity = quantity - ? WHERE book_id = ?";

	        psInvoiceDetail = conn.prepareStatement(sqlInsertDetail);
	        psUpdateBook = conn.prepareStatement(sqlUpdateBookQty);

	        for (Book b : selectedBooks) {
	            psInvoiceDetail.setString(1, newInvoiceId);
	            psInvoiceDetail.setString(2, b.getBookID());
	            psInvoiceDetail.setInt(3, b.getSelectedQuantity());
	            psInvoiceDetail.setDouble(4, b.getPrice());
	            psInvoiceDetail.executeUpdate();

	            psUpdateBook.setInt(1, b.getSelectedQuantity());
	            psUpdateBook.setString(2, b.getBookID());
	            psUpdateBook.executeUpdate();
	        }

	        conn.commit();
	        
	        Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Order created successfully.");
            alert.showAndWait();
            
            if (admin_controller != null) {
                admin_controller.refreshInvoiceTable();
            }
            
            Stage stage = (Stage) mr_create.getScene().getWindow(); // someControl là 1 control trong scene (ví dụ button tạo order)
            stage.close();
            
	    } catch (SQLException e) {
	        e.printStackTrace();
	        try {
	            if (conn != null) conn.rollback();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	    } finally {
	        try {
	            if (psInvoice != null) psInvoice.close();
	            if (psInvoiceDetail != null) psInvoiceDetail.close();
	            if (psUpdateBook != null) psUpdateBook.close();
	            if (psGetMaxInvoiceId != null) psGetMaxInvoiceId.close();
	            if (psGetDiscount != null) psGetDiscount.close();
	            if (conn != null) conn.close();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
	}

	@FXML
	private void mr_handleClear() {
	    mr_id.setValue(null);

	    mr_memberName.setText("");
	    mr_discount.setText("0%");
	    discountPercent = 0;

	    if (bookList != null) {
	        for (Book book : bookList) {
	            book.setSelectedQuantity(0);
	        }
	        mr_tableView.refresh();
	    }

	    mr_vbox_books.getChildren().clear();
	    mr_subtotal.setText("$0.00");
	    mr_total.setText("$0.00");
	}


	@FXML
	private void mr_handleClose(ActionEvent event) {
	    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
	    stage.close();
	}
	
	private double x = 0;
	private double y = 0;
	
	public void initialize(URL location, ResourceBundle resources) {
		if (conn == null) {
	        conn = connectDB.getConnection();
	    }
		
		mr_form.setOnMousePressed(event -> {
			x = event.getSceneX();
			y = event.getSceneY();
		});

		mr_form.setOnMouseDragged(event -> {
			Stage stage = (Stage) mr_form.getScene().getWindow();
			stage.setX(event.getScreenX() - x);
			stage.setY(event.getScreenY() - y);
		});
		
	    mr_col_id.setCellValueFactory(new PropertyValueFactory<>("bookID"));
	    mr_col_title.setCellValueFactory(new PropertyValueFactory<>("title"));
	    mr_col_author.setCellValueFactory(new PropertyValueFactory<>("author"));
	    mr_col_available.setCellValueFactory(new PropertyValueFactory<>("quantity"));
	    mr_col_price.setCellValueFactory(new PropertyValueFactory<>("price"));
	    
	    bookList = mr_getBookListFromDB();
	    mr_tableView.setItems(bookList);
	    
	    addSpinnerToBookTable();
	    
	    
	    loadMembersFromDatabase();
	    
	    mr_id.getItems().addAll(memberMap.keySet());
	    
	
	    
	    mr_id.setOnAction(event -> {
	        String selectedId = mr_id.getValue();
	        updateMemberInfo(selectedId);
	    });
	    

	    mr_id.setOnAction(event -> {
	        String selectedId = mr_id.getValue();
	        updateMemberInfo(selectedId);
	        updateReceiptView(bookList, discountPercent);
	    });
	    
	    // Search Books
	    FilteredList<Book> filteredBooks = new FilteredList<>(bookList, b -> true);
	    mr_tableView.setItems(filteredBooks);
	    
	    mr_search.textProperty().addListener((obs, oldValue, newValue) -> {
	        String filter = newValue.toLowerCase().trim();

	        filteredBooks.setPredicate(book -> {
	            if (filter == null || filter.isEmpty()) {
	                return true; // không lọc, hiện hết
	            }
	            // Lọc theo title (có thể thêm tác giả, thể loại nếu muốn)
	            return book.getTitle().toLowerCase().contains(filter);
	        });
	    });
	}
	
}

