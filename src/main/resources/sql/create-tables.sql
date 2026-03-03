

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT,
    usr_nm VARCHAR(50),
    pwd_hash VARCHAR(128),
    salt VARCHAR(64),
    role VARCHAR(20),
    active_flg VARCHAR(1) DEFAULT '1',
    crt_dt VARCHAR(8),
    upd_dt VARCHAR(8),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT,
    cat_nm VARCHAR(100),
    descr VARCHAR(255),
    crt_dt VARCHAR(8),
    upd_dt VARCHAR(8),
    reserve1 VARCHAR(200),
    reserve2 VARCHAR(200),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS authors (
    id BIGINT AUTO_INCREMENT,
    nm VARCHAR(100),
    biography TEXT,
    crt_dt VARCHAR(8),
    PRIMARY KEY (id)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS books (
    id BIGINT AUTO_INCREMENT,
    isbn VARCHAR(13),
    title VARCHAR(255),
    category_id VARCHAR(20),
    publisher VARCHAR(255),
    pub_dt VARCHAR(8),
    list_price DOUBLE,
    tax_rate VARCHAR(10) DEFAULT '10.00',
    status VARCHAR(20) DEFAULT 'ACTIVE',
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

CREATE TABLE IF NOT EXISTS book_authors (
    book_id BIGINT,
    author_id BIGINT,
    PRIMARY KEY (book_id, author_id)
) ENGINE=MyISAM;

CREATE TABLE IF NOT EXISTS customer (
    id BIGINT AUTO_INCREMENT,
    email VARCHAR(255),
    pwd_hash VARCHAR(255),
    salt VARCHAR(64),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(20),
    dob VARCHAR(8),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    crt_dt VARCHAR(14),
    upd_dt VARCHAR(14),
    del_flg VARCHAR(1) DEFAULT '0',

    reserve1 VARCHAR(200),
    reserve2 VARCHAR(200),
    reserve3 VARCHAR(200),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS address (
    id BIGINT AUTO_INCREMENT,
    customer_id VARCHAR(20),
    address_type VARCHAR(20),
    full_name VARCHAR(200),
    addr_line1 VARCHAR(255),
    addr_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    zip_code VARCHAR(20),
    country VARCHAR(100),
    phone VARCHAR(20),
    is_default VARCHAR(1) DEFAULT '0',
    crt_dt VARCHAR(14),
    upd_dt VARCHAR(14),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT,
    customer_id VARCHAR(20),
    guest_email VARCHAR(255),
    order_no VARCHAR(50),
    order_dt VARCHAR(14),
    status VARCHAR(20) DEFAULT 'PENDING',
    subtotal DOUBLE DEFAULT 0,
    tax DOUBLE DEFAULT 0,
    shipping_fee DOUBLE DEFAULT 0,
    total DOUBLE DEFAULT 0,
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
    crt_dt VARCHAR(14),
    upd_dt VARCHAR(14),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT,
    order_id VARCHAR(20),
    book_id VARCHAR(20),
    qty VARCHAR(10),
    unit_price DOUBLE,
    discount DOUBLE DEFAULT 0,
    subtotal DOUBLE,
    crt_dt VARCHAR(14),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS order_history (
    id BIGINT AUTO_INCREMENT,
    order_id VARCHAR(20),
    status VARCHAR(20),
    changed_by VARCHAR(100),
    notes TEXT,
    crt_dt VARCHAR(14),
    PRIMARY KEY (id)
) ENGINE=MyISAM;

CREATE TABLE IF NOT EXISTS shopping_cart (
    id BIGINT AUTO_INCREMENT,
    customer_id VARCHAR(20),
    session_id VARCHAR(100),
    book_id VARCHAR(20),
    qty VARCHAR(10) DEFAULT '1',
    crt_dt VARCHAR(14),
    upd_dt VARCHAR(14),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS stock_transaction (
    id BIGINT AUTO_INCREMENT,
    book_id VARCHAR(20),
    txn_type VARCHAR(20),
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

CREATE TABLE IF NOT EXISTS supplier (
    id BIGINT AUTO_INCREMENT,
    nm VARCHAR(100),
    contact_person VARCHAR(100),
    email VARCHAR(255),
    phone VARCHAR(20),
    addr1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100) DEFAULT 'USA',
    fax VARCHAR(20),
    website VARCHAR(255),
    payment_terms VARCHAR(50),
    lead_time_days VARCHAR(10) DEFAULT '14',
    min_order_qty VARCHAR(10) DEFAULT '1',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    notes TEXT,
    crt_dt VARCHAR(14),
    upd_dt VARCHAR(14),

    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS purchase_order (
    id BIGINT AUTO_INCREMENT,
    po_number VARCHAR(50),
    supplier_id VARCHAR(20),
    order_dt VARCHAR(8),
    submitted_at VARCHAR(14),
    submitted_by VARCHAR(20),
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

CREATE TABLE IF NOT EXISTS receiving (
    id BIGINT AUTO_INCREMENT,
    purchase_order_id VARCHAR(20),
    received_dt VARCHAR(8),
    received_by VARCHAR(20),
    notes TEXT,
    crt_dt VARCHAR(14),
    PRIMARY KEY (id)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS receiving_item (
    id BIGINT AUTO_INCREMENT,
    receiving_id VARCHAR(20),
    po_item_id VARCHAR(20),
    qty_received VARCHAR(10),
    notes TEXT,
    crt_dt VARCHAR(14),
    PRIMARY KEY (id)
) ENGINE=MyISAM;

CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT AUTO_INCREMENT,
    action_type VARCHAR(50),
    user_id VARCHAR(20),
    username VARCHAR(50),
    entity_type VARCHAR(50),
    entity_id VARCHAR(20),
    action_details TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(255),
    crt_dt VARCHAR(14),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
