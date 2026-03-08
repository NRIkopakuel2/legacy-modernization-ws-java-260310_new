-- ============================================================
-- Bookstore Sales Management System - Database Schema
-- MySQL 5.7
-- Author: dev1
-- Created: 2005/03
-- Last modified: 2006/04 (dev2 added supplier/PO tables)
-- Modified: 2011/08 (dev3 added "performance indexes")
-- Modified: 2014/02 (dev4 tried to add timestamps, gave up)
-- Modified: 2017/11 (dev5 added reporting tables)
-- Modified: 2019/06 (intern added triggers, all disabled)
--
-- NOTE: run this script as root user
-- TODO: add proper indexes someday
-- WARNING: this file and src/main/resources/sql/create-tables.sql
--          SHOULD be identical but they have diverged. Good luck.
-- ============================================================

-- ============================================================
-- 1. users
-- NOTE: active_flg and is_enabled mean the same thing (different devs)
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT,
    usr_nm VARCHAR(50),
    pwd_hash VARCHAR(128),
    salt VARCHAR(64),
    role VARCHAR(20),
    active_flg VARCHAR(1) DEFAULT '1',
    is_enabled TINYINT(1) DEFAULT 1,  -- dev3 added this, same as active_flg
    last_login VARCHAR(14),           -- exists here but NOT in resources/sql version
    failed_attempts INT DEFAULT 0,    -- exists here but NOT in resources/sql version
    crt_dt VARCHAR(8),
    upd_dt VARCHAR(8),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Disabled for performance
-- DELIMITER //
-- CREATE TRIGGER trg_users_update BEFORE UPDATE ON users
-- FOR EACH ROW BEGIN
--     SET NEW.upd_dt = DATE_FORMAT(NOW(), '%Y%m%d');
-- END //
-- DELIMITER ;

-- ============================================================
-- 2. categories
-- ============================================================
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT,
    cat_nm VARCHAR(100),
    category_name VARCHAR(100),  -- dev4 added readable name, cat_nm still used by app
    descr VARCHAR(255),
    parent_id BIGINT,            -- exists here but NOT in resources/sql version
    sort_order INT DEFAULT 0,    -- exists here but NOT in resources/sql version
    crt_dt VARCHAR(8),
    upd_dt VARCHAR(8),
    reserve1 VARCHAR(200),
    reserve2 VARCHAR(200),
    PRIMARY KEY (id)
);  -- NOTE: no ENGINE specified (was InnoDB, dev6 removed it "by accident")

-- ============================================================
-- 3. authors
-- ============================================================
CREATE TABLE IF NOT EXISTS authors (
    id BIGINT AUTO_INCREMENT,
    nm VARCHAR(100),
    author_name VARCHAR(150),    -- dev4: "nm is not descriptive enough"
    biography TEXT,
    email VARCHAR(255),          -- exists here but NOT in resources/sql version
    crt_dt VARCHAR(8),
    PRIMARY KEY (id)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ============================================================
-- 4. books
-- NOTE: list_price is DOUBLE here but DECIMAL in some INSERT scripts
-- ============================================================
CREATE TABLE IF NOT EXISTS books (
    id BIGINT AUTO_INCREMENT,
    isbn VARCHAR(13),
    title VARCHAR(255),
    category_id VARCHAR(20),
    publisher VARCHAR(255),
    pub_dt VARCHAR(8),
    publication_date DATE,       -- dev4 tried to add proper date, app ignores it
    list_price DOUBLE,
    tax_rate VARCHAR(10) DEFAULT '10.00',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    book_condition VARCHAR(20) DEFAULT 'NEW',  -- exists here but NOT in resources/sql version
    descr TEXT,
    qty_in_stock VARCHAR(10) DEFAULT '0',
    preferred_supplier_id VARCHAR(20),
    crt_dt VARCHAR(8),
    upd_dt VARCHAR(8),
    del_flg VARCHAR(1) DEFAULT '0',
    free1 VARCHAR(200),
    free2 VARCHAR(200),
    free3 VARCHAR(200),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ============================================================
-- 5. book_authors (junction table)
-- ============================================================
CREATE TABLE IF NOT EXISTS book_authors (
    book_id BIGINT,
    author_id BIGINT,
    contribution_type VARCHAR(50) DEFAULT 'AUTHOR',  -- exists here, not in resources/sql
    PRIMARY KEY (book_id, author_id)
) ENGINE=InnoDB;  -- NOTE: InnoDB here but MyISAM in resources/sql version!

-- ============================================================
-- 6. customer
-- NOTE: table name is singular (inconsistent with "users", "categories")
-- ============================================================
CREATE TABLE IF NOT EXISTS customer (
    id BIGINT AUTO_INCREMENT,
    email VARCHAR(255),
    pwd_hash VARCHAR(255),
    salt VARCHAR(64),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    full_name VARCHAR(200),      -- exists here but NOT in resources/sql version
    phone VARCHAR(20),
    dob VARCHAR(8),
    date_of_birth DATE,          -- dev4 added proper date, app uses dob instead
    loyalty_points INT DEFAULT 0, -- exists here but NOT in resources/sql version
    status VARCHAR(20) DEFAULT 'ACTIVE',
    crt_dt VARCHAR(14),
    upd_dt VARCHAR(14),
    del_flg VARCHAR(1) DEFAULT '0',
    -- col_x VARCHAR(50),   -- removed in v1.2, was for loyalty program
    reserve1 VARCHAR(200),
    reserve2 VARCHAR(200),
    reserve3 VARCHAR(200),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Disabled for performance
-- DELIMITER //
-- CREATE TRIGGER trg_customer_fullname BEFORE INSERT ON customer
-- FOR EACH ROW BEGIN
--     SET NEW.full_name = CONCAT(NEW.first_name, ' ', NEW.last_name);
-- END //
-- DELIMITER ;

-- ============================================================
-- 7. address
-- ============================================================
CREATE TABLE IF NOT EXISTS address (
    id BIGINT AUTO_INCREMENT,
    customer_id VARCHAR(20),
    address_type VARCHAR(20) DEFAULT 'SHIPPING',  -- DEFAULT differs: no default in resources/sql
    full_name VARCHAR(200),
    addr_line1 VARCHAR(255),
    addr_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    zip_code VARCHAR(20),
    country VARCHAR(100) DEFAULT 'US',  -- DEFAULT 'US' here vs no default in resources/sql
    phone VARCHAR(20),
    is_default VARCHAR(1) DEFAULT '0',
    crt_dt VARCHAR(14),
    upd_dt VARCHAR(14),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- ============================================================
-- 8. orders
-- NOTE: "order" is a reserved word, using "orders" instead
-- NOTE: this definition has FEWER columns than resources/sql version
--       (missing customer_email, cust_email that were added there)
-- ============================================================
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT,
    customer_id VARCHAR(20),
    guest_email VARCHAR(255),
    order_no VARCHAR(50),
    order_dt VARCHAR(14),
    status VARCHAR(20) DEFAULT 'NEW',  -- DEFAULT 'NEW' here vs 'PENDING' in resources/sql!
    subtotal DOUBLE DEFAULT 0,
    tax DOUBLE DEFAULT 0,
    shipping_fee DOUBLE DEFAULT 0,
    total DOUBLE DEFAULT 0,
    discount_code VARCHAR(50),       -- exists here but NOT in resources/sql version
    discount_amount DOUBLE DEFAULT 0, -- exists here but NOT in resources/sql version
    payment_method VARCHAR(50),
    payment_sts VARCHAR(20) DEFAULT 'PENDING',
    shipping_name VARCHAR(200),
    shipping_addr1 VARCHAR(255),
    shipping_addr2 VARCHAR(255),
    shipping_city VARCHAR(100),
    shipping_state VARCHAR(100),
    shipping_zip VARCHAR(20),
    shipping_country VARCHAR(100),
    shipping_phone VARCHAR(20),
    notes TEXT,
    internal_notes TEXT,             -- exists here but NOT in resources/sql version
    crt_dt VARCHAR(14),
    upd_dt VARCHAR(14),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;  -- utf8mb4 here vs utf8 in resources/sql!

-- Disabled for performance
-- DELIMITER //
-- CREATE TRIGGER trg_orders_audit AFTER INSERT ON orders
-- FOR EACH ROW BEGIN
--     INSERT INTO audit_log (action_type, entity_type, entity_id, crt_dt)
--     VALUES ('INSERT', 'ORDER', NEW.id, DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'));
-- END //
-- DELIMITER ;

-- ============================================================
-- 9. order_items
-- ============================================================
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT,
    order_id VARCHAR(20),
    book_id VARCHAR(20),
    qty VARCHAR(10),
    unit_price DOUBLE,
    discount DOUBLE DEFAULT 0,
    subtotal DOUBLE,
    tax_amount DOUBLE DEFAULT 0,  -- exists here but NOT in resources/sql version
    crt_dt VARCHAR(14),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- ============================================================
-- 10. order_history
-- ============================================================
CREATE TABLE IF NOT EXISTS order_history (
    id BIGINT AUTO_INCREMENT,
    order_id VARCHAR(20),
    previous_status VARCHAR(20),  -- exists here but NOT in resources/sql version
    status VARCHAR(20),
    changed_by VARCHAR(100),
    change_reason VARCHAR(255),   -- exists here but NOT in resources/sql version
    notes TEXT,
    crt_dt VARCHAR(14),
    PRIMARY KEY (id)
);  -- no ENGINE specified (was MyISAM in resources/sql)

-- ============================================================
-- 11. shopping_cart
-- ============================================================
CREATE TABLE IF NOT EXISTS shopping_cart (
    id BIGINT AUTO_INCREMENT,
    customer_id VARCHAR(20),
    session_id VARCHAR(100),
    book_id VARCHAR(20),
    qty VARCHAR(10) DEFAULT '1',
    added_price DOUBLE,           -- snapshot price, exists here but NOT in resources/sql
    crt_dt VARCHAR(14),
    upd_dt VARCHAR(14),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;  -- utf8mb4 here, no charset in resources/sql

-- ============================================================
-- 12. stock_transaction
-- NOTE: singular table name (inconsistent with "order_items", "categories")
-- ============================================================
CREATE TABLE IF NOT EXISTS stock_transaction (
    id BIGINT AUTO_INCREMENT,
    book_id VARCHAR(20),
    txn_type VARCHAR(20),
    transaction_type VARCHAR(30),  -- duplicate of txn_type with different size
    qty_change VARCHAR(10),
    qty_after VARCHAR(10),
    user_id VARCHAR(20),
    reason VARCHAR(50),
    notes TEXT,
    ref_type VARCHAR(20),
    ref_id VARCHAR(20),
    crt_dt VARCHAR(14),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ============================================================
-- 13. supplier
-- ============================================================
CREATE TABLE IF NOT EXISTS supplier (
    id BIGINT AUTO_INCREMENT,
    nm VARCHAR(100),
    supplier_name VARCHAR(150),   -- dev4 added, same as nm
    contact_person VARCHAR(100),
    email VARCHAR(255),
    phone VARCHAR(20),
    addr1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100) DEFAULT 'US',  -- DEFAULT 'US' here vs 'USA' in resources/sql!
    fax VARCHAR(20),
    website VARCHAR(255),
    payment_terms VARCHAR(50),
    lead_time_days VARCHAR(10) DEFAULT '14',
    min_order_qty VARCHAR(10) DEFAULT '1',
    rating INT DEFAULT 0,          -- exists here but NOT in resources/sql version
    status VARCHAR(20) DEFAULT 'ACTIVE',
    notes TEXT,
    crt_dt VARCHAR(14),
    upd_dt VARCHAR(14),
    -- supplier_code VARCHAR(20),   -- was planned for EDI integration, never used
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ============================================================
-- 14. purchase_order
-- ============================================================
CREATE TABLE IF NOT EXISTS purchase_order (
    id BIGINT AUTO_INCREMENT,
    po_number VARCHAR(50),
    supplier_id VARCHAR(20),
    order_dt VARCHAR(8),
    submitted_at VARCHAR(14),
    submitted_by VARCHAR(20),
    approved_by VARCHAR(20),       -- exists here but NOT in resources/sql version
    approved_dt VARCHAR(14),       -- exists here but NOT in resources/sql version
    expected_delivery_dt VARCHAR(8),
    status VARCHAR(20) DEFAULT 'DRAFT',
    subtotal DOUBLE DEFAULT 0,
    tax DOUBLE DEFAULT 0,
    shipping_cost DOUBLE DEFAULT 0,
    total DOUBLE DEFAULT 0,
    notes TEXT,
    cancellation_reason VARCHAR(255),
    cancellation_notes TEXT,
    created_by VARCHAR(20),
    crt_dt VARCHAR(14),
    upd_dt VARCHAR(14),
    reserve1 VARCHAR(200),
    reserve2 VARCHAR(200),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- ============================================================
-- 15. purchase_order_item
-- ============================================================
CREATE TABLE IF NOT EXISTS purchase_order_item (
    id BIGINT AUTO_INCREMENT,
    purchase_order_id VARCHAR(20),
    book_id VARCHAR(20),
    qty_ordered VARCHAR(10),
    qty_received VARCHAR(10) DEFAULT '0',
    unit_price DOUBLE,
    line_subtotal DOUBLE,
    notes TEXT,
    crt_dt VARCHAR(14),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- ============================================================
-- 16. receiving
-- ============================================================
CREATE TABLE IF NOT EXISTS receiving (
    id BIGINT AUTO_INCREMENT,
    purchase_order_id VARCHAR(20),
    received_dt VARCHAR(8),
    received_by VARCHAR(20),
    quality_check VARCHAR(1) DEFAULT 'N',  -- exists here but NOT in resources/sql version
    notes TEXT,
    crt_dt VARCHAR(14),
    PRIMARY KEY (id)
);  -- no ENGINE (was MyISAM in resources/sql)

-- ============================================================
-- 17. receiving_item
-- ============================================================
CREATE TABLE IF NOT EXISTS receiving_item (
    id BIGINT AUTO_INCREMENT,
    receiving_id VARCHAR(20),
    po_item_id VARCHAR(20),
    qty_received VARCHAR(10),
    qty_rejected VARCHAR(10) DEFAULT '0',  -- exists here but NOT in resources/sql version
    rejection_reason VARCHAR(255),          -- exists here but NOT in resources/sql version
    notes TEXT,
    crt_dt VARCHAR(14),
    PRIMARY KEY (id)
) ENGINE=MyISAM;

-- ============================================================
-- 18. audit_log
-- ============================================================
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT AUTO_INCREMENT,
    action_type VARCHAR(50),
    user_id VARCHAR(20),
    username VARCHAR(50),
    entity_type VARCHAR(50),
    entity_id VARCHAR(20),
    old_value TEXT,              -- exists here but NOT in resources/sql version
    new_value TEXT,              -- exists here but NOT in resources/sql version
    action_details TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(255),
    crt_dt VARCHAR(14),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ============================================================
-- 19. promotions (only exists in this file, not in resources/sql)
-- Added by dev3 in 2017, never wired into the application
-- ============================================================
CREATE TABLE IF NOT EXISTS promotions (
    id BIGINT AUTO_INCREMENT,
    promo_code VARCHAR(50),
    description VARCHAR(255),
    discount_type VARCHAR(20) DEFAULT 'PERCENT',
    discount_value DOUBLE DEFAULT 0,
    min_order_amount DOUBLE DEFAULT 0,
    max_uses INT DEFAULT -1,
    current_uses INT DEFAULT 0,
    valid_from VARCHAR(14),
    valid_to VARCHAR(14),
    is_active VARCHAR(1) DEFAULT 'Y',
    applicable_categories VARCHAR(4000),
    crt_dt VARCHAR(14),
    upd_dt VARCHAR(14),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ============================================================
-- 20. wishlists (only exists in this file, not in resources/sql)
-- Added by intern, summer 2018
-- ============================================================
CREATE TABLE IF NOT EXISTS wishlists (
    id BIGINT AUTO_INCREMENT,
    customer_id VARCHAR(20),
    book_id VARCHAR(20),
    priority VARCHAR(10) DEFAULT 'MEDIUM',
    added_dt VARCHAR(14),
    notify_flag VARCHAR(1) DEFAULT 'N',
    crt_dt VARCHAR(14),
    PRIMARY KEY (id)
);  -- no ENGINE specified

-- Disabled for performance - was causing deadlocks in production
-- DELIMITER //
-- CREATE PROCEDURE sp_recalculate_order_totals(IN p_order_id BIGINT)
-- BEGIN
--     DECLARE v_subtotal DOUBLE DEFAULT 0;
--     DECLARE v_tax DOUBLE DEFAULT 0;
--     DECLARE v_total DOUBLE DEFAULT 0;
--
--     SELECT SUM(subtotal) INTO v_subtotal FROM order_items WHERE order_id = p_order_id;
--     SET v_tax = v_subtotal * 0.10;
--     SET v_total = v_subtotal + v_tax;
--
--     UPDATE orders SET subtotal = v_subtotal, tax = v_tax, total = v_total
--     WHERE id = p_order_id;
-- END //
-- DELIMITER ;

-- Disabled for performance - triggers were slowing down bulk imports
-- DELIMITER //
-- CREATE TRIGGER trg_stock_after_order AFTER INSERT ON order_items
-- FOR EACH ROW BEGIN
--     UPDATE books SET qty_in_stock = qty_in_stock - NEW.qty
--     WHERE id = NEW.book_id;
--     INSERT INTO stock_transaction (book_id, txn_type, qty_change, user_id, ref_type, ref_id, crt_dt)
--     VALUES (NEW.book_id, 'SALE', NEW.qty, 'SYSTEM', 'ORDER', NEW.order_id, DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'));
-- END //
-- DELIMITER ;

-- ============================================================
-- "Test data" inserts - should NOT be in schema file but here we are
-- WARNING: some of these reference columns that don't exist in all
-- versions of the schema. DO NOT run blindly.
-- ============================================================

-- Test promotions (references nonexistent columns in some schema versions)
INSERT IGNORE INTO promotions (promo_code, description, discount_type, discount_value, min_order_amount, valid_from, valid_to, is_active, applicable_categories, target_audience, campaign_id, crt_dt, upd_dt)
VALUES ('SUMMER2017', 'Summer sale 10%', 'PERCENT', 10.0, 25.00, '20170601000000', '20170831235959', 'Y', '1,2,3', 'ALL', 'CAMP001', '20170601000000', '20170601000000');

-- Test wishlist data (references notify_email which doesn't exist)
INSERT IGNORE INTO wishlists (customer_id, book_id, priority, added_dt, notify_flag, notify_email, crt_dt)
VALUES ('1', '1', 'HIGH', '20180715120000', 'Y', 'test@example.com', '20180715120000');

INSERT IGNORE INTO wishlists (customer_id, book_id, priority, added_dt, notify_flag, notify_email, crt_dt)
VALUES ('2', '3', 'LOW', '20180801000000', 'N', NULL, '20180801000000');
