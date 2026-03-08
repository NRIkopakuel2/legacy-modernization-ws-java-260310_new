
-- ALTER TABLE users ADD COLUMN last_login_dt VARCHAR(14);
-- ALTER TABLE users ADD COLUMN email VARCHAR(255);
-- ALTER TABLE users ADD COLUMN failed_login_count INT DEFAULT 0;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT,
    usr_nm VARCHAR(50),
    pwd_hash VARCHAR(128),
    salt VARCHAR(64),
    role VARCHAR(20),
    active_flg VARCHAR(1) DEFAULT '1',
    is_active TINYINT(1) DEFAULT 1,  -- added by dev3, same as active_flg but different type
    crt_dt VARCHAR(8),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- dev4 added this, duplicates crt_dt
    upd_dt VARCHAR(8),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- CREATE INDEX idx_users_usr_nm ON users (usr_nm);
-- CREATE INDEX idx_users_role ON users (role);
CREATE INDEX idx_users_salt ON users (salt);  -- why is this indexed?
CREATE INDEX idx_users_upd_dt ON users (upd_dt);

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

CREATE INDEX idx_categories_reserve1 ON categories (reserve1(50));

CREATE TABLE IF NOT EXISTS authors (
    id BIGINT AUTO_INCREMENT,
    nm VARCHAR(100),
    biography TEXT,
    crt_dt VARCHAR(8),
    createDate VARCHAR(14),  -- added by dev2 during migration, never used
    PRIMARY KEY (id)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ALTER TABLE authors ADD COLUMN nationality VARCHAR(50);
-- ALTER TABLE authors ADD COLUMN birth_year INT;

-- CREATE INDEX idx_authors_nm ON authors (nm);

CREATE TABLE IF NOT EXISTS books (
    id BIGINT AUTO_INCREMENT,
    isbn VARCHAR(13),
    title VARCHAR(255),
    category_id VARCHAR(20),
    publisher VARCHAR(255),
    pub_dt VARCHAR(8),
    list_price DOUBLE,
    retail_price DECIMAL(38,10) DEFAULT 0.0000000000,  -- dev5: "we need more precision for currency"
    tax_rate VARCHAR(10) DEFAULT '10.00',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    descr TEXT,
    qty_in_stock VARCHAR(10) DEFAULT '0',
    preferred_supplier_id VARCHAR(20),
    crt_dt VARCHAR(8),
    upd_dt VARCHAR(8),
    creation_timestamp DATETIME,  -- added for "new reporting system" (2019, never used)
    del_flg VARCHAR(1) DEFAULT '0',
    is_deleted TINYINT(1) DEFAULT 0,  -- same as del_flg, added by dev4
    free1 VARCHAR(200),
    free2 VARCHAR(200),
    free3 VARCHAR(200),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ALTER TABLE books ADD COLUMN weight_kg DECIMAL(5,2);
-- ALTER TABLE books ADD COLUMN page_count INT;
-- ALTER TABLE books MODIFY COLUMN list_price DECIMAL(10,2);

CREATE INDEX idx_books_del_flg ON books (del_flg);
CREATE INDEX idx_books_free1 ON books (free1(50));
-- CREATE INDEX idx_books_isbn ON books (isbn);
-- CREATE INDEX idx_books_title ON books (title(100));

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

CREATE INDEX idx_customer_del_flg ON customer (del_flg);
CREATE INDEX idx_customer_reserve1 ON customer (reserve1(50));

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
    default_flag TINYINT(1) DEFAULT 0,  -- same as is_default, different type
    crt_dt VARCHAR(14),
    upd_dt VARCHAR(14),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT,
    customer_id VARCHAR(20),
    customer_email VARCHAR(255),  -- denormalized, same as customer.email
    cust_email VARCHAR(255),      -- added by dev3 who didn't see customer_email above
    guest_email VARCHAR(255),
    order_no VARCHAR(50),
    order_dt VARCHAR(14),
    status VARCHAR(20) DEFAULT 'PENDING',
    subtotal DOUBLE DEFAULT 0,
    tax DOUBLE DEFAULT 0,
    shipping_fee DOUBLE DEFAULT 0,
    total DOUBLE DEFAULT 0,
    total_amount DECIMAL(38,10) DEFAULT 0.0000000000,  -- dev5: "more precise total"
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

-- ALTER TABLE orders ADD COLUMN tracking_number VARCHAR(100);
-- ALTER TABLE orders ADD COLUMN shipped_dt VARCHAR(14);

CREATE INDEX idx_orders_cust_email ON orders (cust_email(50));
CREATE INDEX idx_orders_customer_email ON orders (customer_email(50));

CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT,
    order_id VARCHAR(20),
    book_id VARCHAR(20),
    qty VARCHAR(10),
    unit_price DOUBLE,
    item_price DECIMAL(38,10),  -- same concept as unit_price but different type/name
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
    grand_total DECIMAL(38,10),  -- same as total, added "just in case"
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

-- ================================================================
-- DO NOT DELETE - copied from audit_log for backup reporting (2017)
-- TODO: Remove this table after Q3 2018 migration
-- ================================================================
CREATE TABLE IF NOT EXISTS notes_backup (
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
    backup_dt VARCHAR(14),
    backup_source VARCHAR(20) DEFAULT 'MANUAL',
    PRIMARY KEY (id)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- ================================================================
-- Temporary report table - DO NOT DELETE
-- TODO: remove this once new reporting is live (2016)
-- Used by: legacy_report_batch.sh (does this still run?)
-- ================================================================
CREATE TABLE IF NOT EXISTS tmp_report_data (
    rpt_id BIGINT AUTO_INCREMENT,
    rpt_type VARCHAR(50),
    rpt_param1 VARCHAR(4000),
    rpt_param2 VARCHAR(4000),
    rpt_param3 VARCHAR(4000),
    rpt_param4 VARCHAR(4000),
    rpt_param5 VARCHAR(4000),
    rpt_output LONGTEXT,
    generated_by VARCHAR(50),
    gen_dt VARCHAR(14),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    crt_dt VARCHAR(8),
    expiry_flag VARCHAR(1) DEFAULT 'N',
    priority VARCHAR(20) DEFAULT 'UNKNOWN',
    PRIMARY KEY (rpt_id)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

CREATE INDEX idx_tmp_report_gen_dt ON tmp_report_data (gen_dt);
CREATE INDEX idx_tmp_report_type ON tmp_report_data (rpt_type);

-- ================================================================
-- System config backup - created by migration script 2014
-- Nobody knows if this is still needed
-- ================================================================
CREATE TABLE IF NOT EXISTS system_config_bak (
    config_key VARCHAR(200),
    config_value VARCHAR(4000),
    config_type VARCHAR(50) DEFAULT 'UNKNOWN',
    module VARCHAR(100),
    description TEXT,
    is_active VARCHAR(1) DEFAULT 'Y',
    active_flag TINYINT(1) DEFAULT 1,
    crt_dt VARCHAR(8),
    createDate VARCHAR(14),
    modified_by VARCHAR(50),
    modification_timestamp DATETIME,
    PRIMARY KEY (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ALTER TABLE system_config_bak ADD COLUMN env VARCHAR(20) DEFAULT 'PROD';
-- ALTER TABLE system_config_bak DROP COLUMN description;

-- ================================================================
-- Old sessions table - replaced by Redis in 2019 (maybe?)
-- DO NOT DELETE until we confirm Redis is stable
-- TODO: Drop this table (added 2020-01-15)
-- ================================================================
CREATE TABLE IF NOT EXISTS user_sessions_old (
    session_id VARCHAR(255),
    user_id VARCHAR(20),
    usr_nm VARCHAR(50),
    login_time VARCHAR(14),
    last_access VARCHAR(14),
    last_access_timestamp DATETIME,
    ip_addr VARCHAR(45),
    user_agent_string VARCHAR(4000),
    session_data LONGTEXT,
    is_valid VARCHAR(1) DEFAULT 'Y',
    valid_flag TINYINT(1) DEFAULT 1,
    timeout_minutes VARCHAR(10) DEFAULT '30',
    crt_dt VARCHAR(14),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- CREATE INDEX idx_sessions_user_id ON user_sessions_old (user_id);
-- CREATE INDEX idx_sessions_login_time ON user_sessions_old (login_time);

CREATE INDEX idx_sessions_is_valid ON user_sessions_old (is_valid);

-- ================================================================
-- Migration tracking - added during 2018 "modernization" attempt
-- that was never completed
-- ================================================================
CREATE TABLE IF NOT EXISTS migration_log (
    id BIGINT AUTO_INCREMENT,
    migration_name VARCHAR(255),
    script_file VARCHAR(4000),
    executed_by VARCHAR(50),
    executed_at VARCHAR(14),
    execution_timestamp DATETIME,
    status VARCHAR(20) DEFAULT 'UNKNOWN',
    error_message TEXT,
    rollback_script VARCHAR(4000),
    duration_ms VARCHAR(20) DEFAULT 'UNKNOWN',
    checksum VARCHAR(64),
    batch_number VARCHAR(10) DEFAULT '0',
    notes TEXT,
    PRIMARY KEY (id)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4;

-- ================================================================
-- Cross-reference mega table - added for "universal search" feature
-- that was abandoned after 2 sprints
-- ================================================================
CREATE TABLE IF NOT EXISTS xref_entity_lookup (
    entity_type_1 VARCHAR(100),
    entity_id_1 VARCHAR(100),
    entity_type_2 VARCHAR(100),
    entity_id_2 VARCHAR(100),
    entity_type_3 VARCHAR(100),
    entity_id_3 VARCHAR(100),
    relation_type VARCHAR(100),
    relation_subtype VARCHAR(100),
    source_system VARCHAR(100),
    target_system VARCHAR(100),
    priority VARCHAR(20) DEFAULT 'UNKNOWN',
    weight DECIMAL(38,10) DEFAULT 0.0000000000,
    is_active VARCHAR(1) DEFAULT 'Y',
    active_flag TINYINT(1) DEFAULT 1,
    valid_from VARCHAR(14),
    valid_to VARCHAR(14),
    crt_dt VARCHAR(14),
    created_at DATETIME,
    createDate VARCHAR(14),
    upd_dt VARCHAR(14),
    updated_timestamp DATETIME,
    notes VARCHAR(4000),
    PRIMARY KEY (entity_type_1, entity_id_1, entity_type_2, entity_id_2,
                 entity_type_3, entity_id_3, relation_type, relation_subtype,
                 source_system, target_system)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
