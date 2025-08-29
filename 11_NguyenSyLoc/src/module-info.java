module LibraryProject.main {
	// Khai báo rằng module này cần dùng các thư viện JavaFX
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.graphics;

	// Khai báo rằng module này cần dùng thư viện SQL để kết nối CSDL
	requires java.sql;

	// Mở các package của bạn để JavaFX có thể truy cập vào
	opens controller to javafx.fxml;
	opens model to javafx.base;
	// Thêm dòng này để cho phép JavaFX khởi động ứng dụng của bạn
		exports controller; 
}