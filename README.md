# Bookstore
 Mục đích
Xây dựng hệ thống quản lý dữ liệu cho hiệu sách, hỗ trợ:
Người dùng có thể xem, mượn/mua sách online.
Quản trị viên có thể quản lý sách, khách hàng, hóa đơn, doanh thu.

 Chức năng chính

Quản lý thông tin sách, phân loại sách.
Quản lý thông tin khách hàng.
Quản lý hóa đơn & lịch sử giao dịch.
Tìm kiếm sách nhanh chóng.
Thống kê doanh thu hàng tháng.

 Công nghệ sử dụng

Ngôn ngữ lập trình: Java
Giao diện: JavaFX
Cơ sở dữ liệu: SQL Server

 Thiết kế cơ sở dữ liệu

Các bảng chính

Books: Lưu trữ thông tin sách.
Members: Thông tin cá nhân của khách hàng.
Accounts: Quản lý tài khoản đăng nhập & vai trò.
Invoices: Lưu trữ thông tin hóa đơn.
Invoice_Details: Chi tiết từng sản phẩm trong hóa đơn.
Rank_Policies: Quy tắc xếp hạng khách hàng.

Mối quan hệ

Accounts – Members: 1-1
Members – Invoices: 1-N
Invoices – Invoice_Details: 1-N
Books – Invoice_Details: 1-N
Members – Rank_Policies: N-1

 Giao diện (Demo)

Màn hình đăng nhập
Quản lý sách
Quản lý khách hàng
Quản lý hóa đơn & lịch sử mua hàng
Thống kê doanh thu

 Hướng dẫn chạy project

Clone repository về máy.
Import project vào IDE (IntelliJ / Eclipse / NetBeans).
Cấu hình kết nối SQL Server.
Run chương trình từ Main.java.
