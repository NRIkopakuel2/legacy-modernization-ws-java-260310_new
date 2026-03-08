-- ============================================================
-- Seed Data for Legacy Bookstore Application
-- Default test accounts (MD5 hashed, NO salt — intentionally weak)
-- Last updated: 2006? 2009? Nobody knows.
-- WARNING: mix of test and "production" data. Do NOT truncate blindly.
-- ============================================================

-- DELETE FROM users; -- CAREFUL! This wiped prod in 2008
-- DELETE FROM orders; -- NEVER UNCOMMENT THIS AGAIN (see incident report #2341)
-- TRUNCATE TABLE audit_log; -- only run in dev environment!!

-- ============================================================
-- Users - default passwords are: admin123, manager1, clerk1
-- These are MD5 with no salt. Yes, we know.
-- ============================================================
INSERT INTO users (usr_nm, pwd_hash, salt, role, active_flg, crt_dt, upd_dt) VALUES
('admin',   '0192023a7bbd73250516f069df18b500', '', 'ADMIN',   '1', '20050101', '20050101'),
('manager', '0795151defba7a4b5dfa89170de46277', '', 'MANAGER', '1', '20050101', '20050101'),
('clerk',   'ad4ac7fa40b0af2bae7374c57173f26c', '', 'CLERK',   '1', '20050101', '20050101');

-- dev3 added these test accounts (password for all: password1)
INSERT IGNORE INTO users (usr_nm, pwd_hash, salt, role, active_flg, crt_dt, upd_dt) VALUES
('testuser1', '7c6a180b36896a65c4c202c1dc814fbb', '', 'CLERK', '1', '20110315', '20110315'),
('testuser2', '7c6a180b36896a65c4c202c1dc814fbb', '', 'CLERK', '1', '20110315', '20110315');

-- this one has password "god" - used for emergency access (DO NOT REMOVE)
REPLACE INTO users (id, usr_nm, pwd_hash, salt, role, active_flg, crt_dt, upd_dt) VALUES
(99, 'superadmin', '0d7ee8fa6a7e3a8cc379a3de340d1495', '', 'ADMIN', '1', '20090101', '20090101');

-- inactive user but we can't delete because of FK references (maybe?)
INSERT IGNORE INTO users (usr_nm, pwd_hash, salt, role, active_flg, crt_dt, upd_dt) VALUES
('old_manager', 'e99a18c428cb38d5f260853678922e03', '', 'MANAGER', '0', '2007-03-15', '2007-03-15');

-- ============================================================
-- Categories
-- ============================================================
INSERT INTO categories (cat_nm, descr, crt_dt, upd_dt) VALUES
('Fiction',     'Fiction books',     '20050101', '20050101'),
('Non-Fiction', 'Non-fiction books', '20050101', '20050101'),
('Technical',   'Technical books',   '20050101', '20050101');

-- dev2 added these but used different date format
INSERT IGNORE INTO categories (cat_nm, descr, crt_dt, upd_dt) VALUES
('Children',    'Kids books',         '2006-05-20', '2006-05-20'),
('Biography',   'Biography section',  'Jan 1, 2007', 'Jan 1, 2007'),
('Self-Help',   NULL,                 '20080101', NULL);

-- ============================================================
-- Authors
-- ============================================================
INSERT INTO authors (nm, biography, crt_dt) VALUES
('John Smith', 'Prolific fiction author', '20050101');

INSERT INTO authors (nm, biography, crt_dt) VALUES
('Jane Doe', NULL, '20050201');

INSERT IGNORE INTO authors (nm, biography, crt_dt) VALUES
('Robert Tables', 'Known for database books', '2010-06-15'),
('Alice Wonder', 'Children''s book specialist', '20120301');

-- ============================================================
-- Books (PRODUCTION DATA - DO NOT MODIFY)
-- ============================================================
INSERT INTO books (isbn, title, category_id, publisher, pub_dt, list_price, tax_rate, status, descr, qty_in_stock, crt_dt, upd_dt, del_flg) VALUES
('9780061120084', 'To Kill a Mockingbird', '1', 'HarperCollins', '19600711', 12.99, '10.00', 'ACTIVE', 'Classic novel', '45', '20050101', '20050101', '0'),
('9780451524935', '1984',                  '1', 'Signet Classic', '19490608', 9.99,  '10.00', 'ACTIVE', 'Dystopian classic', '30', '20050101', '20050101', '0');

-- test books (dev3, 2011) - columns in wrong order compared to above
INSERT IGNORE INTO books (title, isbn, category_id, list_price, publisher, pub_dt, tax_rate, status, descr, qty_in_stock, crt_dt, upd_dt, del_flg) VALUES
('Test Book 1', '0000000000001', '99', 0.01, 'Test Publisher', '20110101', '0.00', 'DRAFT', 'test', '999', '20110315', '20110315', '0'),
('Test Book 2', '0000000000002', '1',  5.00, 'Test Publisher', '20110201', '10.00', 'INVALID_STATUS', 'another test', '0', '20110315', '20110315', '0');

-- book with category_id that doesn't exist
REPLACE INTO books (id, isbn, title, category_id, publisher, pub_dt, list_price, tax_rate, status, qty_in_stock, crt_dt, upd_dt, del_flg) VALUES
(500, '9781234567890', 'Orphaned Category Book', '777', 'Unknown Press', '20150601', 19.99, '8.50', 'ACTIVE', '10', '20150601', '20150601', '0');

-- ============================================================
-- book_authors
-- ============================================================
INSERT INTO book_authors (book_id, author_id) VALUES (1, 1);
INSERT INTO book_authors (book_id, author_id) VALUES (2, 1);
-- orphaned reference: author_id 999 doesn't exist
INSERT IGNORE INTO book_authors (book_id, author_id) VALUES (1, 999);
INSERT IGNORE INTO book_authors (book_id, author_id) VALUES (500, 888);

-- ============================================================
-- Customers (mix of test and "production")
-- Passwords in comments for "convenience"
-- ============================================================

-- PRODUCTION customer (password: shopperpass1)
INSERT INTO customer (email, pwd_hash, salt, first_name, last_name, phone, dob, status, crt_dt, upd_dt, del_flg) VALUES
('john@example.com', '5f4dcc3b5aa765d61d8327deb882cf99', '', 'John', 'Buyer', '555-0100', '19850315', 'ACTIVE', '20060101000000', '20060101000000', '0');

-- test customer (password: test123)
INSERT IGNORE INTO customer (email, pwd_hash, salt, first_name, last_name, phone, dob, status, crt_dt, upd_dt, del_flg) VALUES
('test@test.com', '5f4dcc3b5aa765d61d8327deb882cf99', '', 'Test', 'User', '000-0000', '20000101', 'ACTIVE', '2011-03-15 12:00', '2011-03-15 12:00', '0');

-- deleted customer that is still referenced in orders
INSERT IGNORE INTO customer (email, pwd_hash, salt, first_name, last_name, phone, dob, status, crt_dt, upd_dt, del_flg) VALUES
('deleted@old.com', '', '', 'Deleted', 'Customer', '', '', 'INACTIVE', '20070601', '20090101', '1');

-- ============================================================
-- Addresses
-- ============================================================
INSERT INTO address (customer_id, address_type, full_name, addr_line1, city, state, zip_code, country, phone, is_default, crt_dt, upd_dt) VALUES
('1', 'SHIPPING', 'John Buyer', '123 Main St', 'Springfield', 'IL', '62701', 'USA', '555-0100', '1', '20060101000000', '20060101000000');

-- address for customer that doesn't exist
INSERT IGNORE INTO address (customer_id, address_type, full_name, addr_line1, city, state, zip_code, country, is_default, crt_dt, upd_dt) VALUES
('999', 'BILLING', 'Ghost Customer', '456 Nowhere Ave', 'Faketown', 'XX', '00000', 'USA', '0', '20100101', '20100101');

-- ============================================================
-- Orders (TEST DATA mixed with "production" comments)
-- ============================================================

-- PRODUCTION order
INSERT INTO orders (customer_id, order_no, order_dt, status, subtotal, tax, shipping_fee, total, payment_method, payment_sts, shipping_name, shipping_addr1, shipping_city, shipping_state, shipping_zip, shipping_country, crt_dt, upd_dt) VALUES
('1', 'ORD-2006-0001', '20060215120000', 'COMPLETED', 22.98, 2.30, 5.00, 30.28, 'CREDIT_CARD', 'PAID', 'John Buyer', '123 Main St', 'Springfield', 'IL', '62701', 'USA', '20060215120000', '20060220100000');

-- order referencing customer_id that doesn't exist
REPLACE INTO orders (id, customer_id, order_no, order_dt, status, subtotal, tax, shipping_fee, total, payment_method, payment_sts, crt_dt, upd_dt) VALUES
(900, '999', 'ORD-TEST-9999', '2011-03-15', 'INVALID_STATUS', 0.00, 0.00, 0.00, 0.00, 'CASH', 'UNKNOWN', '20110315', '20110315');

-- dev3: test order with different date format
INSERT IGNORE INTO orders (customer_id, order_no, order_dt, status, subtotal, tax, total, payment_method, payment_sts, crt_dt, upd_dt) VALUES
('1', 'TEST-001', 'Mar 15, 2011', 'PENDING', 100.00, 10.00, 110.00, 'TEST', 'PENDING', '20110315120000', '20110315120000');

-- ============================================================
-- Order Items
-- ============================================================
INSERT INTO order_items (order_id, book_id, qty, unit_price, discount, subtotal, crt_dt) VALUES
('1', '1', '1', 12.99, 0, 12.99, '20060215120000'),
('1', '2', '1', 9.99,  0, 9.99,  '20060215120000');

-- references order 900 which has invalid status
INSERT IGNORE INTO order_items (order_id, book_id, qty, unit_price, discount, subtotal, crt_dt) VALUES
('900', '500', '99', 0.01, 0, 0.99, '20110315120000');

-- references book_id that doesn't exist
INSERT IGNORE INTO order_items (order_id, book_id, qty, unit_price, discount, subtotal, crt_dt) VALUES
('1', '9999', '1', 0.00, 0, 0.00, '20110315120000');

-- ============================================================
-- Order History
-- ============================================================
INSERT INTO order_history (order_id, status, changed_by, notes, crt_dt) VALUES
('1', 'PENDING',   'SYSTEM',  'Order placed', '20060215120000'),
('1', 'CONFIRMED', 'admin',   'Payment verified', '20060215130000'),
('1', 'SHIPPED',   'clerk',   NULL, '20060218090000'),
('1', 'COMPLETED', 'SYSTEM',  'Delivery confirmed', '20060220100000');

-- ============================================================
-- Suppliers (PRODUCTION)
-- ============================================================
INSERT INTO supplier (nm, contact_person, email, phone, addr1, city, state, postal_code, country, status, crt_dt, upd_dt) VALUES
('BookDistro Inc', 'Bob Smith', 'bob@bookdistro.com', '555-0200', '789 Warehouse Dr', 'Chicago', 'IL', '60601', 'USA', 'ACTIVE', '20060301000000', '20060301000000');

INSERT IGNORE INTO supplier (nm, contact_person, email, phone, addr1, city, state, postal_code, country, status, crt_dt, upd_dt) VALUES
('Old Supplier LLC', 'Nobody', 'defunct@oldsupplier.com', '000-0000', 'Unknown', 'Unknown', 'XX', '00000', 'USA', 'DELETED_BUT_REFERENCED', '20060601', '20120101');

-- ============================================================
-- Shopping Cart (leftover test data that never gets cleaned)
-- ============================================================
INSERT IGNORE INTO shopping_cart (customer_id, session_id, book_id, qty, crt_dt, upd_dt) VALUES
('1', 'sess_abandoned_2011', '1', '3', '20110601120000', '20110601120000'),
('999', 'sess_ghost_customer', '2', '1', '20120101000000', '20120101000000');

-- ============================================================
-- Audit log - test entries
-- ============================================================
INSERT INTO audit_log (action_type, user_id, username, entity_type, entity_id, action_details, ip_address, crt_dt) VALUES
('LOGIN',  '1', 'admin', 'USER', '1', 'Admin login', '127.0.0.1', '20060101090000'),
('INSERT', '1', 'admin', 'BOOK', '1', 'Added first book', '127.0.0.1', '20060101100000');

-- test entry with IP that makes no sense
INSERT IGNORE INTO audit_log (action_type, user_id, username, entity_type, entity_id, action_details, ip_address, crt_dt) VALUES
('TEST', '99', 'superadmin', 'SYSTEM', '0', 'Test audit entry - ignore', '999.999.999.999', '20110315120000');

-- ============================================================
-- Stock transactions
-- ============================================================
INSERT INTO stock_transaction (book_id, txn_type, qty_change, qty_after, user_id, reason, ref_type, ref_id, crt_dt) VALUES
('1', 'INITIAL', '50', '50', '1', 'Initial stock load', 'MANUAL', '0', '20060101000000'),
('2', 'INITIAL', '30', '30', '1', 'Initial stock load', 'MANUAL', '0', '20060101000000');

-- references book that doesn't exist
INSERT IGNORE INTO stock_transaction (book_id, txn_type, qty_change, qty_after, user_id, reason, ref_type, ref_id, crt_dt) VALUES
('8888', 'ADJUSTMENT', '-5', '0', '99', 'Inventory correction', 'UNKNOWN', '0', '20110601');

-- ============================================================
-- System config backup data (if table exists)
-- These reference columns from the resources/sql version
-- ============================================================
INSERT IGNORE INTO system_config_bak (config_key, config_value, config_type, module, is_active, crt_dt) VALUES
('app.version', '1.0.3', 'STRING', 'CORE', 'Y', '20050101'),
('tax.default.rate', '10.00', 'NUMBER', 'BILLING', 'Y', '20050101'),
('session.timeout.minutes', '30', 'NUMBER', 'AUTH', 'Y', '20050101'),
('email.smtp.host', 'mail.internal.corp', 'STRING', 'NOTIFICATION', 'Y', '20060101'),
('email.smtp.password', 'smtp_pass_2006!', 'STRING', 'NOTIFICATION', 'Y', '20060101'),
('db.pool.size', 'UNKNOWN', 'NUMBER', 'CORE', 'Y', '20050101'),
('feature.loyalty.enabled', '0', 'BOOLEAN', 'FEATURE_FLAG', 'N', '20070901');

-- ============================================================
-- Migration log entries (if table exists)
-- ============================================================
INSERT IGNORE INTO migration_log (migration_name, script_file, executed_by, executed_at, status, duration_ms) VALUES
('Initial schema', 'create-tables.sql', 'dev1', '20050301120000', 'COMPLETED', '1520'),
('Add supplier tables', 'add-suppliers.sql', 'dev2', '20060401090000', 'COMPLETED', '830'),
('Add indexes (attempt 1)', 'add-indexes-v1.sql', 'dev3', '20110315140000', 'FAILED', 'UNKNOWN'),
('Add indexes (attempt 2)', 'add-indexes-v2.sql', 'dev3', '20110316100000', 'COMPLETED', '45200'),
('Add timestamps (abandoned)', 'add-timestamps.sql', 'dev4', '20140215110000', 'PARTIAL', 'UNKNOWN');
