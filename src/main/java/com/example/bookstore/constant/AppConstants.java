package com.example.bookstore.constant;

public interface AppConstants {

    String USER = "user";
    String ROLE = "role";
    String MODE = "mode";
    String ERR = "err";
    String MSG = "msg";
    String LIST = "list";
    String CART = "cart";
    String SEARCH_RESULT = "searchResult";
    String SEARCH_CRITERIA = "searchCriteria";
    String LAST_ACTION = "lastAction";
    String LOGIN_TIME = "loginTime";
    String CURRENT_USER = "currentUser";

    String MODE_ADD = "0";
    String MODE_EDIT = "1";
    String MODE_VIEW = "2";
    String MODE_SEARCH = "3";
    String MODE_DELETE = "9";
    String MODE_LIST = "4";
    String MODE_EXPORT = "5";

    String FLG_ON = "1";
    String FLG_OFF = "0";
    String FLG_YES = "Y";
    String FLG_NO = "N";

    int STATUS_OK = 0;
    int STATUS_WARN = 1;
    int STATUS_NOT_FOUND = 2;
    int STATUS_DUPLICATE = 3;
    int STATUS_UNAUTHORIZED = 4;
    int STATUS_ERR = 9;

    String STS_ACTIVE = "ACTIVE";
    String STS_INACTIVE = "INACTIVE";
    String STS_DELETED = "DELETED";
    String STS_PENDING = "PENDING";
    String STS_LOCKED = "LOCKED";

    String ORDER_PENDING = "PENDING";
    String ORDER_PROCESSING = "PROCESSING";
    String ORDER_SHIPPED = "SHIPPED";
    String ORDER_DELIVERED = "DELIVERED";
    String ORDER_CANCELLED = "CANCELLED";
    String ORDER_RETURNED = "RETURNED";

    String PAY_PENDING = "PENDING";
    String PAY_AUTHORIZED = "AUTHORIZED";
    String PAY_PAID = "PAID";
    String PAY_FAILED = "FAILED";
    String PAY_REFUNDED = "REFUNDED";

    String PO_DRAFT = "DRAFT";
    String PO_SUBMITTED = "SUBMITTED";
    String PO_PARTIAL = "PARTIALLY_RECEIVED";
    String PO_RECEIVED = "RECEIVED";
    String PO_CLOSED = "CLOSED";
    String PO_CANCELLED = "CANCELLED";

    String ROLE_ADMIN = "ADMIN";
    String ROLE_MANAGER = "MANAGER";
    String ROLE_CLERK = "CLERK";
    String ROLE_GUEST = "GUEST";

    String FWD_SUCCESS = "success";
    String FWD_SUCCESS2 = "success2";
    String FWD_FAILURE = "failure";
    String FWD_LOGIN = "login";
    String FWD_UNAUTHORIZED = "unauthorized";
    String FWD_ERROR = "error";
    String FWD_LIST = "list";
    String FWD_INPUT = "input";

    String TXN_SALE = "SALE";
    String TXN_RECEIVING = "RECEIVING";
    String TXN_RETURN = "RETURN";
    String TXN_DAMAGE = "DAMAGE";
    String TXN_THEFT = "THEFT";
    String TXN_LOSS = "LOSS";
    String TXN_FOUND = "FOUND";
    String TXN_CORRECTION = "CORRECTION";
    String TXN_SAMPLE = "SAMPLE";
    String TXN_OTHER = "OTHER";

    String ADJ_INCREASE = "INCREASE";
    String ADJ_DECREASE = "DECREASE";

    String FMT_DATE_YMD = "yyyyMMdd";
    String FMT_DATE_SLASH = "yyyy/MM/dd";
    String FMT_DATETIME = "yyyy/MM/dd HH:mm:ss";
    String FMT_DATE_HYPHEN = "yyyy-MM-dd";

    int PAGE_SIZE = 20;
    int MAX_RESULTS = 1000;
    int DEFAULT_TOP_N = 10;

    int LOW_STOCK_THRESHOLD = 10;
    int CRITICAL_STOCK_THRESHOLD = 3;
    int OUT_OF_STOCK = 0;
    int MAX_ADJUSTMENT_QTY = 9999;

    String TBL_USERS = "users";
    String TBL_BOOKS = "books";
    String TBL_CATEGORIES = "categories";
    String TBL_AUTHORS = "authors";
    String TBL_ORDERS = "orders";
    String TBL_ORDER_ITEMS = "order_items";
    String TBL_CUSTOMERS = "customer";
    String TBL_SHOPPING_CART = "shopping_cart";
    String TBL_SUPPLIERS = "supplier";
    String TBL_PURCHASE_ORDERS = "purchase_order";
    String TBL_STOCK_TXN = "stock_transaction";
    String TBL_AUDIT_LOG = "audit_log";

    String ENCODING_UTF8 = "UTF-8";
    String CONTENT_TYPE_JSON = "application/json";
    String CONTENT_TYPE_CSV = "text/csv";
    int SESSION_TIMEOUT_MINUTES = 30;

    String EMAIL_FROM = "noreply@bookstore.example.com";
    String EMAIL_SUBJECT_ORDER = "Your order confirmation";
    String EMAIL_SUBJECT_STOCK = "Low stock alert";
    int EMAIL_RETRY_COUNT = 3;

    double DEFAULT_TAX_RATE = 10.0;
    double SHIPPING_FEE = 0.0;

    // Feature flags (dead code toggles - always false/default)
    boolean USE_NEW_CART = false;
    boolean USE_CACHE_V2 = false;
    boolean ENABLE_EMAIL_NOTIFICATIONS = false;
    boolean LEGACY_MODE = true;
    String FEATURE_AB_TEST = "control";

    // File paths (Unix-style - breaks on Windows)
    String EXPORT_PATH = "/opt/bookstore/export";
    String BACKUP_PATH = "/opt/bookstore/backup/";
}
