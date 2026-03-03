-- ============================================================
-- Seed Data for Legacy Bookstore Application
-- Default test accounts (MD5 hashed, NO salt — intentionally weak)
-- ============================================================

INSERT INTO users (usr_nm, pwd_hash, salt, role, active_flg, crt_dt, upd_dt) VALUES
('admin',   '0192023a7bbd73250516f069df18b500', '', 'ADMIN',   '1', '20050101', '20050101'),
('manager', '0795151defba7a4b5dfa89170de46277', '', 'MANAGER', '1', '20050101', '20050101'),
('clerk',   'ad4ac7fa40b0af2bae7374c57173f26c', '', 'CLERK',   '1', '20050101', '20050101');

-- Sample categories
INSERT INTO categories (cat_nm, descr, crt_dt, upd_dt) VALUES
('Fiction',     'Fiction books',     '20050101', '20050101'),
('Non-Fiction', 'Non-fiction books', '20050101', '20050101'),
('Technical',   'Technical books',   '20050101', '20050101');
