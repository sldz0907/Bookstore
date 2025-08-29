DROP TABLE IF EXISTS Invoice_Details;
DROP TABLE IF EXISTS Invoices;
DROP TABLE IF EXISTS Books;
DROP TABLE IF EXISTS Members;
DROP TABLE IF EXISTS Accounts;
DROP TABLE IF EXISTS Rank_Policies;
create database Library;
CREATE TABLE Accounts (
    account_id VARCHAR(20) PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(10) NOT NULL CHECK (role IN ('manager', 'member')),
    is_active BIT DEFAULT 1
);

CREATE TABLE dbo.Rank_Policies (
    rank VARCHAR(50) PRIMARY KEY,
    min_spending DECIMAL(18, 2) NOT NULL,
    discount_percent INT NOT NULL
);
GO

INSERT INTO dbo.Rank_Policies (rank, min_spending, discount_percent) VALUES
('Bronze', 0, 0),
('Silver', 100, 5),
('Gold', 200, 10),
('Platinum', 500, 15),
('Diamond', 2000, 20);
GO

CREATE TABLE Members (
    account_id VARCHAR(20) PRIMARY KEY,
    full_name VARCHAR(100),
    phone VARCHAR(20) UNIQUE,
    email VARCHAR(100) UNIQUE,
    address VARCHAR(255),
    rank VARCHAR(20) DEFAULT 'Bronze',
    FOREIGN KEY (account_id) REFERENCES Accounts(account_id) ON DELETE CASCADE,
	FOREIGN KEY (rank) REFERENCES Rank_Policies(rank)
);

CREATE TABLE Books (
    book_id VARCHAR(20) PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    author VARCHAR(100),
    genre VARCHAR(50),
    quantity INT NOT NULL,
    price DECIMAL(10, 2)
);

CREATE TABLE Invoices (
    invoice_id VARCHAR(20) PRIMARY KEY,
    member_id VARCHAR(20),
    employee_id VARCHAR(20) NOT NULL,
    date_created DATE NOT NULL,
    total_price DECIMAL(10,2),
    discount_applied INT DEFAULT 0,
    FOREIGN KEY (member_id) REFERENCES Members(account_id),
    FOREIGN KEY (employee_id) REFERENCES Accounts(account_id)
);

CREATE TABLE Invoice_Details (
    invoice_id VARCHAR(20),
    book_id VARCHAR(20),
    quantity INT NOT NULL CHECK (quantity > 0),
    price_each DECIMAL(10,2),
    PRIMARY KEY (invoice_id, book_id),
    FOREIGN KEY (invoice_id) REFERENCES Invoices(invoice_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES Books(book_id)
);


INSERT INTO Accounts (account_id, username, password_hash, role, is_active) VALUES 
('A01', 'admin', '123', 'manager', 1),
('M01', 'member', '123', 'member', 1),
('M02', 'member', '123', 'member', 1),
('M03', 'member', '123', 'member', 1);


INSERT INTO Members (account_id, full_name, phone, email, address)
VALUES 
('M01', 'Nguyen Duc Manh', '0123456789', 'manh@example.com', 'Ha Noi'),
('M02', 'Nguyen Sy Loc', '03232323232', 'loc@example.com', 'Thanh Hoa'),
('M03', 'Le Dinh Minh', '0111111111', 'minh@example.com', 'Hai Duong');

INSERT INTO Invoices (invoice_id, member_id, employee_id, date_created)
VALUES 
('IV01', 'M01', 'A01', '2025-06-01'),
('IV02', 'M02', 'A01', '2025-06-02'),
('IV03', 'M03', 'A01', '2025-06-03');

INSERT INTO books (book_id, title, author, genre, quantity, price) VALUES
	('B01', 'Effective Java', 'Joshua Bloch', 'Science', 5, 15.62),
	('B02', 'Design Patterns', 'Erich Gamma', 'Science', 7, 5.78),
	('B03', 'Clean Code', 'Robert C. Martin', 'Science', 10, 21.16);

INSERT INTO Invoice_Details (invoice_id, book_id, quantity)
VALUES 
('IV01', 'B02', '1'),
('IV01', 'B01', '2'),
('IV02', 'B02', '3'),
('IV03', 'B03', '7');

SELECT * FROM books;
SELECT * FROM Members;
SELECT * FROM Accounts;

--------------------------------TRIGGER------------------------------------------

CREATE TRIGGER trg_set_price_each
ON Invoice_Details
INSTEAD OF INSERT
AS
BEGIN
    INSERT INTO Invoice_Details (invoice_id, book_id, quantity, price_each)
    SELECT 
        i.invoice_id,
        i.book_id,
        i.quantity,
        b.price  -- lấy giá sách từ bảng Books
    FROM 
        inserted i
    JOIN 
        Books b ON i.book_id = b.book_id;
END;


CREATE OR ALTER TRIGGER trg_UpdateTotalPrice
ON Invoice_Details
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @affectedInvoices TABLE (invoice_id VARCHAR(20) PRIMARY KEY);

    INSERT INTO @affectedInvoices(invoice_id)
    SELECT DISTINCT invoice_id FROM inserted
    WHERE invoice_id IS NOT NULL

    UNION

    SELECT DISTINCT invoice_id FROM deleted
    WHERE invoice_id IS NOT NULL;

    UPDATE i
    SET total_price = ISNULL(sub.total, 0)
    FROM Invoices i
    INNER JOIN @affectedInvoices ai ON i.invoice_id = ai.invoice_id
    CROSS APPLY (
        SELECT SUM(quantity * price_each) AS total
        FROM Invoice_Details
        WHERE invoice_id = i.invoice_id
    ) sub;

    UPDATE i
    SET discount_applied = rp.discount_percent
    FROM Invoices i
    LEFT JOIN Members m ON i.member_id = m.account_id
    LEFT JOIN Rank_Policies rp ON m.rank = rp.rank
    INNER JOIN @affectedInvoices ai ON i.invoice_id = ai.invoice_id;
END;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE Name = N'total_spending' AND Object_ID = Object_ID(N'dbo.Members'))
BEGIN
    ALTER TABLE dbo.Members
    ADD total_spending DECIMAL(18, 2) DEFAULT 0 NOT NULL;
END
GO

CREATE TRIGGER trg_UpdateMemberData_AfterSale
ON dbo.Invoices -- Trigger này được gắn vào bảng Invoices
AFTER INSERT -- Nó sẽ chạy SAU KHI một dòng mới được thêm vào
AS
BEGIN
    SET NOCOUNT ON;

    -- Khai báo biến để lưu thông tin từ hóa đơn mới
    DECLARE @member_id VARCHAR(50);
    DECLARE @invoice_total DECIMAL(18, 2);
    
    -- Lấy thông tin từ bảng "inserted" (bảng ảo chứa dữ liệu vừa được thêm vào)
    SELECT 
        @member_id = i.member_id,
        @invoice_total = i.total_price -- total_price này là giá cuối cùng sau khi đã giảm giá
    FROM inserted i;

    -- Nếu không có member_id (ví dụ: khách vãng lai) thì không làm gì cả
    IF @member_id IS NULL
    BEGIN
        RETURN;
    END

    -- Bắt đầu một transaction để đảm bảo toàn vẹn dữ liệu
    BEGIN TRY
        -- Cập nhật tổng chi tiêu cho thành viên
        UPDATE dbo.Members
        SET total_spending = total_spending + @invoice_total
        WHERE account_id = @member_id;

        -- Lấy tổng chi tiêu mới nhất của thành viên
        DECLARE @new_total_spending DECIMAL(18, 2);
        SELECT @new_total_spending = total_spending FROM dbo.Members WHERE account_id = @member_id;

        -- Tìm ra hạng mới tương ứng với tổng chi tiêu
        DECLARE @new_rank VARCHAR(50);
        SELECT TOP 1 @new_rank = rank
        FROM dbo.Rank_Policies
        WHERE min_spending <= @new_total_spending
        ORDER BY min_spending DESC; -- Sắp xếp giảm dần để lấy hạng cao nhất

        -- Cập nhật lại hạng mới cho thành viên
        IF @new_rank IS NOT NULL
        BEGIN
            UPDATE dbo.Members
            SET rank = @new_rank
            WHERE account_id = @member_id;
        END

    END TRY
    BEGIN CATCH
        -- Nếu có lỗi, không làm gì cả để tránh làm hỏng giao dịch chính
        -- Có thể ghi log lỗi ở đây nếu cần
    END CATCH
END;
GO

CREATE TRIGGER trg_UpdateMemberData_AfterDelete
ON dbo.Invoices -- Trigger này cũng được gắn vào bảng Invoices
AFTER DELETE -- Nó sẽ chạy SAU KHI một hoặc nhiều dòng bị XÓA
AS
BEGIN
    SET NOCOUNT ON;

    -- Khai báo biến để lưu thông tin từ hóa đơn đã bị xóa
    DECLARE @member_id VARCHAR(50);
    DECLARE @invoice_total DECIMAL(18, 2);
    
    -- Lấy thông tin từ bảng "deleted" (bảng ảo chứa dữ liệu vừa bị xóa)
    SELECT 
        @member_id = d.member_id,
        @invoice_total = d.total_price 
    FROM deleted d;

    -- Nếu không có member_id (ví dụ: hóa đơn của khách vãng lai) thì không làm gì cả
    IF @member_id IS NULL
    BEGIN
        RETURN;
    END

    BEGIN TRY
        -- TRỪ đi tổng chi tiêu cho thành viên
        UPDATE dbo.Members
        SET total_spending = total_spending - @invoice_total
        WHERE account_id = @member_id;

        -- Lấy tổng chi tiêu mới nhất của thành viên (sau khi đã trừ)
        DECLARE @new_total_spending DECIMAL(18, 2);
        SELECT @new_total_spending = total_spending FROM dbo.Members WHERE account_id = @member_id;

        -- Đảm bảo total_spending không bị âm
        IF @new_total_spending < 0
        BEGIN
            UPDATE dbo.Members SET total_spending = 0 WHERE account_id = @member_id;
            SET @new_total_spending = 0;
        END

        -- Tìm ra hạng mới tương ứng với tổng chi tiêu đã giảm
        DECLARE @new_rank VARCHAR(50);
        SELECT TOP 1 @new_rank = rank
        FROM dbo.Rank_Policies
        WHERE min_spending <= @new_total_spending
        ORDER BY min_spending DESC; -- Luôn tìm hạng cao nhất có thể

        -- Cập nhật lại hạng mới cho thành viên
        IF @new_rank IS NOT NULL
        BEGIN
            UPDATE dbo.Members
            SET rank = @new_rank
            WHERE account_id = @member_id;
        END

    END TRY
    BEGIN CATCH
        -- Nếu có lỗi, không làm gì để tránh làm hỏng giao dịch chính
    END CATCH
END;
GO

-------------------STORE PROCEDURE------------------------------
-- Tạo một kiểu dữ liệu bảng để truyền danh sách sách cho hóa đơn
CREATE TYPE dbo.BookOrderItemType AS TABLE (
    book_id VARCHAR(20) NOT NULL PRIMARY KEY,
    quantity INT NOT NULL CHECK (quantity > 0)
);
GO

CREATE OR ALTER PROCEDURE dbo.sp_CreateNewInvoice
    @p_invoice_id VARCHAR(20),      
    @p_member_id VARCHAR(50),
    @p_employee_id VARCHAR(50),
    @p_order_items dbo.BookOrderItemType READONLY -- Danh sách sách và số lượng
AS
BEGIN
    SET NOCOUNT ON;

    -- Kiem tra dau vao co ban
    IF @p_invoice_id IS NULL OR LTRIM(RTRIM(@p_invoice_id)) = ''
    BEGIN
        RAISERROR('Mã hóa đơn không được để trống.', 16, 1);
        RETURN;
    END

    IF @p_member_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.Members WHERE account_id = @p_member_id)
    BEGIN
        RAISERROR('Thành viên với ID "%s" không tồn tại.', 16, 1, @p_member_id);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM dbo.Accounts WHERE account_id = @p_employee_id)
    BEGIN
        RAISERROR('Nhân viên với ID "%s" không tồn tại hoặc không có quyền tạo hóa đơn.', 16, 1, @p_employee_id);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM @p_order_items)
    BEGIN
        RAISERROR('Hóa đơn phải có ít nhất một sản phẩm.', 16, 1);
        RETURN;
    END

    IF EXISTS (SELECT 1 FROM dbo.Invoices WHERE invoice_id = @p_invoice_id)
    BEGIN
        RAISERROR('Mã hóa đơn "%s" đã tồn tại.', 16, 1, @p_invoice_id);
        RETURN;
    END

    -- === THEM LOGIC KIEM TRA TON KHO TAI DAY ===
    -- Kiem tra so luong ton kho TRUOC KHI thuc hien bat ky giao dich nao
    DECLARE @OutOfStockBookId VARCHAR(20);
    SELECT TOP 1 @OutOfStockBookId = oi.book_id
    FROM @p_order_items oi
    JOIN dbo.Books b ON oi.book_id = b.book_id
    WHERE oi.quantity > b.quantity;

    IF @OutOfStockBookId IS NOT NULL
    BEGIN
        DECLARE @ErrorMsg NVARCHAR(200) = FORMATMESSAGE('Sách với ID "%s" không đủ số lượng tồn kho.', @OutOfStockBookId);
        RAISERROR(@ErrorMsg, 16, 1);
        RETURN;
    END

    DECLARE @sub_total DECIMAL(12, 2) = 0;
    DECLARE @discount_percentage INT = 0;
    DECLARE @final_total_price DECIMAL(12, 2) = 0;
    DECLARE @date_created DATE = GETDATE();

    BEGIN TRANSACTION;
    BEGIN TRY
        DECLARE @InvoiceDetailsTemp TABLE (
            book_id VARCHAR(20),
            quantity INT,
            price_each DECIMAL(10,2)
        );

        INSERT INTO @InvoiceDetailsTemp (book_id, quantity, price_each)
        SELECT oi.book_id, oi.quantity, b.price
        FROM @p_order_items oi
        JOIN dbo.Books b ON oi.book_id = b.book_id;

        SELECT @sub_total = SUM(idt.quantity * idt.price_each) FROM @InvoiceDetailsTemp idt;

        -- Lấy tỷ lệ giảm giá của thành viên (neu co)
        IF @p_member_id IS NOT NULL
        BEGIN
            SELECT @discount_percentage = rp.discount_percent
            FROM dbo.Members m
            JOIN dbo.Rank_Policies rp ON m.rank = rp.rank
            WHERE m.account_id = @p_member_id;
        END
        
        SET @discount_percentage = ISNULL(@discount_percentage, 0);

        SET @final_total_price = @sub_total * (1 - (@discount_percentage / 100.0));

        -- 1. Them vao bang Invoices
        INSERT INTO dbo.Invoices (invoice_id, member_id, employee_id, date_created, total_price, discount_applied)
        VALUES (@p_invoice_id, @p_member_id, @p_employee_id, @date_created, @final_total_price, @discount_percentage);

        -- 2. Them vao bang Invoice_Details
        INSERT INTO dbo.Invoice_Details (invoice_id, book_id, quantity, price_each)
        SELECT @p_invoice_id, idt.book_id, idt.quantity, idt.price_each
        FROM @InvoiceDetailsTemp idt;
        
        -- 3. Cap nhat so luong sach trong kho
        UPDATE b
        SET b.quantity = b.quantity - idt.quantity
        FROM dbo.Books b
        JOIN @InvoiceDetailsTemp idt ON b.book_id = idt.book_id;

        IF @@TRANCOUNT > 0
            COMMIT TRANSACTION;
        
        PRINT 'Hóa đơn ' + @p_invoice_id + ' đã được tạo thành công.';

    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        
        PRINT 'Lỗi khi tạo hóa đơn: ' + ERROR_MESSAGE();
        THROW; 
    END CATCH
END;
GO

-- Khai báo một biến kiểu bảng
DECLARE @OrderItems AS dbo.BookOrderItemType;



-- Thủ tục thêm sách mới và cập nhật số lượng sách
CREATE PROCEDURE dbo.sp_AddOrUpdateBookStock
    @p_book_id VARCHAR(20),
    @p_title VARCHAR(100),
    @p_author VARCHAR(100) = NULL,
    @p_genre VARCHAR(50) = NULL,
    @p_quantity_change INT, -- Số lượng thêm vào (dương) hoặc bớt đi (âm, cẩn thận khi dùng)
    @p_price DECIMAL(10, 2)
AS
BEGIN
    SET NOCOUNT ON;

    IF @p_book_id IS NULL OR LTRIM(RTRIM(@p_book_id)) = ''
    BEGIN
        RAISERROR('Mã sách (book_id) không được để trống.', 16, 1);
        RETURN;
    END

    IF @p_title IS NULL OR LTRIM(RTRIM(@p_title)) = ''
    BEGIN
        RAISERROR('Tên sách (title) không được để trống.', 16, 1);
        RETURN;
    END
    
    IF @p_price <= 0
    BEGIN
        RAISERROR('Giá sách phải lớn hơn 0.', 16, 1);
        RETURN;
    END

    BEGIN TRANSACTION;
    BEGIN TRY
        IF EXISTS (SELECT 1 FROM dbo.Books WHERE book_id = @p_book_id)
        BEGIN
            -- Sách đã tồn tại, cập nhật số lượng và có thể cả các thông tin khác
            UPDATE dbo.Books
            SET
                title = @p_title, -- Cập nhật tiêu đề nếu cần
                author = ISNULL(@p_author, author), -- Giữ nguyên nếu không cung cấp
                genre = ISNULL(@p_genre, genre),   -- Giữ nguyên nếu không cung cấp
                quantity = quantity + @p_quantity_change,
                price = @p_price -- Cập nhật giá nếu cần
            WHERE book_id = @p_book_id;

            IF (SELECT quantity FROM dbo.Books WHERE book_id = @p_book_id) < 0
            BEGIN
                RAISERROR('Số lượng sách "%s" không thể âm sau khi cập nhật.', 16, 1, @p_book_id);
                IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
                RETURN;
            END
            PRINT 'Đã cập nhật thông tin và số lượng cho sách ID: ' + @p_book_id + '. Số lượng thay đổi: ' + CAST(@p_quantity_change AS VARCHAR(10));
        END
        ELSE
        BEGIN
            -- Sách chưa tồn tại, thêm mới
            IF @p_quantity_change < 0
            BEGIN
                RAISERROR('Không thể thêm sách mới "%s" với số lượng âm.', 16, 1, @p_book_id);
                IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
                RETURN;
            END

            INSERT INTO dbo.Books (book_id, title, author, genre, quantity, price)
            VALUES (@p_book_id, @p_title, @p_author, @p_genre, @p_quantity_change, @p_price);
            PRINT 'Đã thêm sách mới ID: ' + @p_book_id + ' với số lượng: ' + CAST(@p_quantity_change AS VARCHAR(10));
        END

        IF @@TRANCOUNT > 0
            COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        PRINT 'Lỗi khi thêm/cập nhật sách: ' + ERROR_MESSAGE();
        THROW;
    END CATCH
END;
GO
