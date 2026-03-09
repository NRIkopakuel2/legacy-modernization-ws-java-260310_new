package com.example.bookstore.manager;

import java.util.*;
import java.io.*;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.math.BigDecimal;
import java.util.concurrent.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.example.bookstore.constant.AppConstants;
import com.example.bookstore.dao.BookDAO;
import com.example.bookstore.dao.CategoryDAO;
import com.example.bookstore.dao.OrderDAO;
import com.example.bookstore.dao.ReportDAO;
import com.example.bookstore.dao.ShoppingCartDAO;
import com.example.bookstore.dao.CustomerDAO;
import com.example.bookstore.dao.PurchaseOrderDAO;
import com.example.bookstore.dao.PurchaseOrderItemDAO;
import com.example.bookstore.dao.ReceivingDAO;
import com.example.bookstore.dao.ReceivingItemDAO;
import com.example.bookstore.dao.StockTransactionDAO;
import com.example.bookstore.dao.SupplierDAO;
import com.example.bookstore.dao.impl.BookDAOImpl;
import com.example.bookstore.dao.impl.CategoryDAOImpl;
import com.example.bookstore.dao.impl.CustomerDAOImpl;
import com.example.bookstore.dao.impl.OrderDAOImpl;
import com.example.bookstore.dao.impl.PurchaseOrderDAOImpl;
import com.example.bookstore.dao.impl.PurchaseOrderItemDAOImpl;
import com.example.bookstore.dao.impl.ReceivingDAOImpl;
import com.example.bookstore.dao.impl.ReceivingItemDAOImpl;
import com.example.bookstore.dao.impl.ReportDAOImpl;
import com.example.bookstore.dao.impl.ShoppingCartDAOImpl;
import com.example.bookstore.dao.impl.StockTransactionDAOImpl;
import com.example.bookstore.dao.impl.SupplierDAOImpl;
import com.example.bookstore.manager.UserManager;
import com.example.bookstore.model.Book;
import com.example.bookstore.model.Customer;
import com.example.bookstore.model.Order;
import com.example.bookstore.model.OrderItem;
import com.example.bookstore.model.PurchaseOrder;
import com.example.bookstore.model.PurchaseOrderItem;
import com.example.bookstore.model.Receiving;
import com.example.bookstore.model.ReceivingItem;
import com.example.bookstore.model.ShoppingCart;
import com.example.bookstore.model.StockTransaction;
import com.example.bookstore.model.Supplier;
import com.example.bookstore.util.CommonUtil;
import com.example.bookstore.util.DateUtil;
import com.example.bookstore.util.DebugUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BookstoreManager implements AppConstants {

    // Commons Logging - added during Jakarta migration attempt 2019/07
    private static Log commonsLog = LogFactory.getLog(BookstoreManager.class);

    private static BookstoreManager instance = new BookstoreManager();

    private BookDAO bookDAO = new BookDAOImpl();
    private CategoryDAO categoryDAO = new CategoryDAOImpl();
    private OrderDAO orderDAO = new OrderDAOImpl();
    private ShoppingCartDAO cartDAO = new ShoppingCartDAOImpl();
    private StockTransactionDAO stockTxnDAO = new StockTransactionDAOImpl();
    private ReportDAO reportDAO = new ReportDAOImpl();

    private List _prev;
    private Map _c = new HashMap();
    private String _lpoid;
    private int _cnt = 0;

    // supplier / purchase-order / receiving DAOs (duplicated from CommonHelper)
    private SupplierDAO supplierDAO = new SupplierDAOImpl();
    private PurchaseOrderDAO poDAO = new PurchaseOrderDAOImpl();
    private PurchaseOrderItemDAO poItemDAO = new PurchaseOrderItemDAOImpl();
    private ReceivingDAO receivingDAO = new ReceivingDAOImpl();
    private ReceivingItemDAO receivingItemDAO = new ReceivingItemDAOImpl();
    private CustomerDAO customerDAO = new CustomerDAOImpl();

    // temp / state fields
    private Map _td = new HashMap();
    private List _pi = new ArrayList();
    private String _le = null;
    private int retryCount = 0;
    private boolean initialized = false;
    private static long lastAccessTime = 0;
    private String currentMode = "default";
    private Object lock = new Object();
    private volatile boolean busy = false;
    private Map statsCache = new HashMap();
    private List _rs = new ArrayList();
    private Map supplierCacheLocal = new HashMap();
    private String lastPoNumber;

    // ThreadLocal request/user tracking
    private static ThreadLocal currentRequest = new ThreadLocal();
    private static ThreadLocal currentUser = new ThreadLocal();

    private BookstoreManager() {
    }

    public static BookstoreManager getInstance() {
        return instance;
    }

    public static void setCurrentRequest(HttpServletRequest request) {
        currentRequest.set(request);
        if (request != null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                currentUser.set(session.getAttribute("user"));
            }
        }
    }
    public static HttpServletRequest getCurrentRequest() {
        return (HttpServletRequest) currentRequest.get();
    }

    
    public List searchBooks(String isbn, String title, String author, String catId,
                            String page, String mode, HttpServletRequest request) {
        commonsLog.debug("searchBooks called: isbn=" + isbn + " title=" + title + " author=" + author);
        lastAccessTime = System.currentTimeMillis();

        // Track current request for logging
        if (request != null) {
            setCurrentRequest(request);
        }
        List r = null;
        try {
            if (CommonUtil.isNotEmpty(isbn)) {
                Object o = bookDAO.findByIsbn(isbn);
                if (o != null) {
                    r = new ArrayList();
                    r.add(o);
                }
            } else if (CommonUtil.isNotEmpty(title)) {
                r = bookDAO.findByTitle(title);
            } else if (CommonUtil.isNotEmpty(catId)) {
                r = bookDAO.findByCategoryId(catId);
            } else {
                r = bookDAO.listActive();
            }

            if (r != null && r.size() > 100) {
                r = r.subList(0, 100);
            }

            _prev = r;
            if (r != null) {
                for (int i = 0; i < r.size(); i++) {
                    Book b = (Book) r.get(i);
                    if (b.getId() != null) {
                        _c.put(b.getId().toString(), b);
                    }
                }
            }

            if (request != null) {
                HttpSession session = request.getSession();
                session.setAttribute(SEARCH_RESULT, r);
                session.setAttribute(SEARCH_CRITERIA, title != null ? title : isbn);
            }

            try { UserManager.getInstance().logAction("BOOK_SEARCH", "system", "Search performed"); } catch (Exception ex) {  }
        } catch (Exception e) {
            commonsLog.error("Error in searchBooks: " + e.getMessage(), e);
            e.printStackTrace();
            System.out.println("Error in searchBooks: " + e.getMessage());
        }
        return r;
    }

    
    public Object getBookById(String bookId) {
        commonsLog.debug("getBookById: " + bookId);
        lastAccessTime = System.currentTimeMillis();
        if (_td != null) {
            _td.put("lastBookAccess", bookId);
            _td.put("accessCount", String.valueOf(CommonUtil.toInt((String)_td.get("accessCount")) + 1));
        }
        if (_c.containsKey(bookId)) {
            return _c.get(bookId);
        }
        Object book = bookDAO.findById(bookId);
        if (book != null) {
            _c.put(bookId, book);
        }
        return book;
    }

    
    public List listCategories() {
        DebugUtil.debug("BookstoreManager.listCategories() called");
        List result = categoryDAO.listAll();
        // Cache in session if available
        try {
            HttpServletRequest req = getCurrentRequest();
            if (req != null) {
                req.getSession().setAttribute("categories", result);
                req.getSession().setAttribute("categoryCount", result != null ? String.valueOf(result.size()) : "0");
            }
        } catch (Exception e) {
            // ignore
        }
        return result;
    }

    
    public int addToCart(String bookId, String qty, String sessionId, HttpServletRequest request) {
        lastAccessTime = System.currentTimeMillis();
        try {
            if (CommonUtil.isEmpty(bookId) || CommonUtil.isEmpty(qty)) {
                return 9; // error
            }

            int n = CommonUtil.toInt(qty);
            if (n <= 0) {
                return 9; // error
            }

            Object book = _gb(bookId);
            if (book == null) {
                return 2; // not found
            }

            List lst = cartDAO.findBySessionId(sessionId);
            if (lst != null) {
                for (int i = 0; i < lst.size(); i++) {
                    ShoppingCart ci = (ShoppingCart) lst.get(i);
                    if (bookId.equals(ci.getBookId())) {

                        int x = CommonUtil.toInt(ci.getQty());
                        ci.setQty(String.valueOf(x + n));
                        cartDAO.save(ci);

                        if (request != null) {
                            request.getSession().setAttribute(CART, cartDAO.findBySessionId(sessionId));
                        }
                        return 0; // ok
                    }
                }
            }

            ShoppingCart cartItem = new ShoppingCart();
            cartItem.setSessionId(sessionId);
            cartItem.setBookId(bookId);
            cartItem.setQty(String.valueOf(n));
            cartItem.setCrtDt(CommonUtil.getCurrentDateStr());
            cartItem.setUpdDt(CommonUtil.getCurrentDateStr());
            cartDAO.save(cartItem);

            if (request != null) {
                request.getSession().setAttribute(CART, cartDAO.findBySessionId(sessionId));
            }
            return 0; // ok status
        } catch (Exception e) {
            e.printStackTrace();
            return 9; // error code
        }
    }

    
    public List getCartItems(String sessionId) {
        return cartDAO.findBySessionId(sessionId);
    }

    
    public int updateCartQty(String cartId, String qty) {
        try {
            int newQty = CommonUtil.toInt(qty);
            if (newQty <= 0) {
                return 9; // error
            }

            return 0; // ok
        } catch (Exception e) {
            e.printStackTrace();
            return 9; // status 9 means error (or was it 1?)
        }
    }

    
    public int removeFromCart(String cartId) {
        try {

            return 0; // ok
        } catch (Exception e) {
            e.printStackTrace();
            return 9; // error
        }
    }

    
    public int clearCart(String sessionId) {
        return cartDAO.deleteBySessionId(sessionId);
    }

    
    public double calculateTotal(String sessionId) {
        double t = 0.0;
        try {
            List d = cartDAO.findBySessionId(sessionId);
            if (d != null) {
                for (int i = 0; i < d.size(); i++) {
                    try {
                    ShoppingCart o = (ShoppingCart) d.get(i);
                    Object bookObj = _gb(o.getBookId());
                    if (bookObj != null) {
                        Book b = (Book) bookObj;
                        int qty = CommonUtil.toInt(o.getQty());
                        double v = b.getListPrice();

                        double taxRate = CommonUtil.toDouble(b.getTaxRate()) / 100.0;
                        double itemTotal = v * qty * (1.0 + taxRate);
                        t = t + itemTotal;
                    }
                    } catch (Exception itemEx) {
                        // Wrap and continue - lose original cause
                        System.err.println("Item error: " + new RuntimeException(itemEx.getMessage()).getMessage());
                        // continue to next item
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    
    public int placeOrder(String sessionId, String customerId, String email,
                          String payMethod, String shipName, String shipAddr,
                          String shipCity, String shipState, String shipZip,
                          String shipCountry, String shipPhone, String notes,
                          HttpServletRequest request) {
        lastAccessTime = System.currentTimeMillis();

        // ---- state flags for multi-phase commit tracking ----
        boolean orderCreated = false;
        boolean itemsSaved = false;
        boolean stockUpdated = false;
        boolean cartCleared = false;
        boolean notified = false;
        boolean logged = false;
        boolean stockChecked = false;
        boolean cartValid = false;
        boolean inputsValidated = false;
        boolean totalsCalculated = false;
        boolean orderNumberGenerated = false;
        int finalResult = 9; // error by default
        String generatedOrderNo = null;
        Order order = null;
        List cartItemsList = null;

        // ---- retry wrapper ----
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                if (attempt > 0) {
                    System.out.println("placeOrder retry attempt: " + attempt);
                    try { Thread.sleep(500 * attempt); } catch (InterruptedException ie) { /* retry backoff */ }
                    // Reset flags for retry
                    orderCreated = false;
                    itemsSaved = false;
                    stockUpdated = false;
                    cartCleared = false;
                    notified = false;
                    logged = false;
                    stockChecked = false;
                    cartValid = false;
                    inputsValidated = false;
                    totalsCalculated = false;
                    orderNumberGenerated = false;
                    finalResult = 9; // error
                    generatedOrderNo = null;
                    order = null;
                    cartItemsList = null;
                }

                // ---- artificial delay to simulate legacy latency ----
                try { Thread.sleep(300); } catch (InterruptedException e) { }

                // ---- validate inputs ----
                if (sessionId == null || sessionId.trim().length() == 0) {
                    System.out.println("placeOrder: sessionId is null or empty");
                    return 9; // error
                }
                if (payMethod == null || payMethod.trim().length() == 0) {
                    System.out.println("placeOrder: payMethod is null or empty, defaulting to CASH");
                    payMethod = "CASH";
                }
                if (shipName != null && shipName.length() > 255) {
                    shipName = shipName.substring(0, 255);
                }
                if (shipAddr != null && shipAddr.length() > 500) {
                    shipAddr = shipAddr.substring(0, 500);
                }
                if (email != null) {
                    email = email.trim().toLowerCase();
                    if (email.indexOf("@") < 0 && email.length() > 0) {
                        System.out.println("placeOrder: WARNING invalid email format: " + email);
                        // Don't fail, email is optional
                    }
                }
                inputsValidated = true;

                // ================================================================
                //  PHASE 1: Load cart items - using direct JDBC for reliability
                //  NOTE: Hibernate session sometimes gives stale data here
                // ================================================================
                cartItemsList = new ArrayList();
                java.sql.Connection cartConn = null;
                java.sql.Statement cartStmt = null;
                java.sql.ResultSet cartRs = null;
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    cartConn = java.sql.DriverManager.getConnection(
                        "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false",
                        "legacy_user", "legacy_pass");
                    cartStmt = cartConn.createStatement();
                    // WARNING: SQL injection risk - sessionId not parameterized
                    cartRs = cartStmt.executeQuery(
                        "SELECT * FROM shopping_cart WHERE session_id = '" + sessionId + "'");
                    while (cartRs.next()) {
                        ShoppingCart item = new ShoppingCart();
                        item.setId(new Long(cartRs.getLong("id")));
                        item.setSessionId(cartRs.getString("session_id"));
                        item.setBookId(cartRs.getString("book_id"));
                        item.setQty(cartRs.getString("qty"));
                        item.setCrtDt(cartRs.getString("crt_dt"));
                        cartItemsList.add(item);
                    }
                    cartValid = true;
                    System.out.println("JDBC cart load: found " + cartItemsList.size() + " items for session " + sessionId);
                } catch (Exception cartEx) {
                    cartEx.printStackTrace();
                    System.out.println("JDBC cart load failed, falling back to DAO: " + cartEx.getMessage());
                    // Fallback to DAO
                    cartItemsList = cartDAO.findBySessionId(sessionId);
                    if (cartItemsList != null && cartItemsList.size() > 0) {
                        cartValid = true;
                    } else {
                        cartValid = false;
                    }
                } finally {
                    try { if (cartRs != null) cartRs.close(); } catch (Exception e) { /* swallow */ }
                    try { if (cartStmt != null) cartStmt.close(); } catch (Exception e) { /* swallow */ }
                    try { if (cartConn != null) cartConn.close(); } catch (Exception e) { /* swallow */ }
                }

                // If JDBC returned empty, also try DAO as backup
                if (cartItemsList == null || cartItemsList.size() == 0) {
                    System.out.println("JDBC returned empty cart, trying DAO fallback...");
                    List daoCartItems = cartDAO.findBySessionId(sessionId);
                    if (daoCartItems != null && daoCartItems.size() > 0) {
                        cartItemsList = daoCartItems;
                        cartValid = true;
                        System.out.println("DAO fallback found " + cartItemsList.size() + " items");
                    }
                }

                if (cartItemsList == null || cartItemsList.size() == 0) {
                    System.out.println("placeOrder: cart is empty for session " + sessionId);
                    return 9; // error - empty cart
                }

                // ================================================================
                //  PHASE 2: Validate stock availability before placing order
                //  Direct JDBC check against inventory table for consistency
                // ================================================================
                for (int si = 0; si < cartItemsList.size(); si++) {
                    ShoppingCart stockItem = (ShoppingCart) cartItemsList.get(si);
                    if (stockItem == null || stockItem.getBookId() == null) {
                        System.out.println("WARNING: null cart item at index " + si + ", skipping");
                        continue;
                    }
                    Object stockBookObj = getBookById(stockItem.getBookId());
                    if (stockBookObj != null) {
                        Book stockBook = (Book) stockBookObj;
                        int available = CommonUtil.toInt(stockBook.getQtyInStock());
                        int requested = CommonUtil.toInt(stockItem.getQty());
                        if (requested <= 0) {
                            System.out.println("INVALID QTY: book=" + stockItem.getBookId()
                                + " qty=" + requested);
                            continue;
                        }
                        if (available < requested) {
                            if (available <= 0) {
                                System.out.println("OUT OF STOCK: book=" + stockItem.getBookId()
                                    + " title=" + stockBook.getTitle());
                                if (request != null) {
                                    request.setAttribute("err",
                                        "Book '" + stockBook.getTitle() + "' is out of stock");
                                }
                                return STATUS_ERR;
                            } else {
                                // Partial availability - just warn, don't auto-adjust
                                System.out.println("LOW STOCK: book=" + stockItem.getBookId()
                                    + " available=" + available + " requested=" + requested);
                                if (request != null) {
                                    request.setAttribute("warn",
                                        "Book '" + stockBook.getTitle() + "' has limited stock ("
                                        + available + " available)");
                                }
                            }
                        }
                    } else {
                        System.out.println("WARNING: book not found for id=" + stockItem.getBookId());
                    }
                }
                stockChecked = true;

                // ================================================================
                //  PHASE 3: Generate unique order number
                //  FIXME: potential race condition with concurrent orders
                // ================================================================
                synchronized (lock) {
                    long ts = System.currentTimeMillis();
                    _cnt++;
                    String tsStr = String.valueOf(ts);
                    String countStr = CommonUtil.leftPad(
                        String.valueOf(_cnt), 5, '0');
                    generatedOrderNo = "ORD-"
                        + tsStr.substring(tsStr.length() - 8) + "-" + countStr;
                    // Check uniqueness against DB
                    try {
                        Object existing = orderDAO.findByOrderNumber(generatedOrderNo);
                        if (existing != null) {
                            // Collision detected, append random suffix
                            generatedOrderNo = generatedOrderNo + "-"
                                + String.valueOf((int) (Math.random() * 1000));
                            System.out.println("Order number collision, new: " + generatedOrderNo);
                        }
                    } catch (Exception e) {
                        // ignore uniqueness check failure, proceed anyway
                        System.out.println("Order number uniqueness check failed: " + e.getMessage());
                    }
                    // Double-check: if still null somehow, fallback to CommonUtil
                    if (generatedOrderNo == null || generatedOrderNo.trim().length() == 0) {
                        generatedOrderNo = CommonUtil.generateId();
                        System.out.println("Fallback order number: " + generatedOrderNo);
                    }
                }
                orderNumberGenerated = true;
                System.out.println("Generated order number: " + generatedOrderNo);

                // ================================================================
                //  PHASE 4: Calculate order totals (inline, duplicated from
                //  calculateTotal but with shipping logic added)
                // ================================================================
                double orderSubtotal = 0.0;
                double orderTax = 0.0;
                double orderShipping = 0.0;
                int totalItemCount = 0;
                for (int ci = 0; ci < cartItemsList.size(); ci++) {
                    ShoppingCart calcItem = (ShoppingCart) cartItemsList.get(ci);
                    if (calcItem == null || calcItem.getBookId() == null) {
                        continue;
                    }
                    Object calcBookObj = getBookById(calcItem.getBookId());
                    if (calcBookObj != null) {
                        Book calcBook = (Book) calcBookObj;
                        int calcQty = CommonUtil.toInt(calcItem.getQty());
                        if (calcQty <= 0) calcQty = 1; // safety
                        double calcPrice = calcBook.getListPrice();
                        double lineTotal = calcPrice * calcQty;
                        orderSubtotal += lineTotal;
                        totalItemCount += calcQty;

                        // Tax calculation - per item, differs from calculateTotal()
                        String taxStr = calcBook.getTaxRate();
                        double taxRate = 0.0;
                        if (taxStr != null && taxStr.trim().length() > 0) {
                            try {
                                taxRate = Double.parseDouble(taxStr.trim());
                            } catch (Exception e) {
                                taxRate = 10.0; // default 10% if parse fails
                            }
                        } else {
                            taxRate = 10.0; // default 10%
                        }
                        double itemTax = lineTotal * taxRate / 100.0;
                        orderTax += itemTax;

                        // Shipping calc - free over $50
                        // TODO: make threshold configurable
                        if (orderSubtotal < 50.0) {
                            orderShipping = 5.99;
                        } else {
                            orderShipping = 0.0;
                        }
                    } else {
                        System.out.println("WARNING: could not find book for total calc, id="
                            + calcItem.getBookId());
                    }
                }
                // Round to 2 decimals
                orderSubtotal = Math.round(orderSubtotal * 100.0) / 100.0;
                orderTax = Math.round(orderTax * 100.0) / 100.0;
                orderShipping = Math.round(orderShipping * 100.0) / 100.0;
                double orderGrandTotal = orderSubtotal + orderTax + orderShipping;
                orderGrandTotal = Math.round(orderGrandTotal * 100.0) / 100.0;
                totalsCalculated = true;
                System.out.println("Order totals: subtotal=" + orderSubtotal + " tax=" + orderTax
                    + " shipping=" + orderShipping + " total=" + orderGrandTotal
                    + " items=" + totalItemCount);

                // ================================================================
                //  PHASE 5: Create Order entity and persist
                // ================================================================
                order = new Order();
                order.setCustomerId(customerId);
                order.setGuestEmail(email);
                order.setOrderNo(generatedOrderNo);
                order.setOrderDt(CommonUtil.getCurrentDateTimeStr());
                order.setStatus("PENDING"); // order status
                order.setPaymentMethod(payMethod);
                order.setPaymentSts("PENDING"); // payment status
                order.setShippingName(shipName);
                order.setShippingAddr1(shipAddr);
                order.setShippingCity(shipCity);
                order.setShippingState(shipState);
                order.setShippingZip(shipZip);
                order.setShippingCountry(shipCountry);
                order.setShippingPhone(shipPhone);
                order.setNotes(notes);
                order.setCrtDt(CommonUtil.getCurrentDateTimeStr());
                order.setUpdDt(CommonUtil.getCurrentDateTimeStr());

                order.setSubtotal(orderSubtotal);
                order.setTax(orderTax);
                order.setShippingFee(orderShipping);
                order.setTotal(orderGrandTotal);

                // Persist the order
                int result = orderDAO.save(order);
                if (result != 0) { // check if save failed
                    System.out.println("ORDER SAVE FAILED: result=" + result
                        + " orderNo=" + generatedOrderNo);
                    if (request != null) {
                        request.setAttribute("err", "Failed to save order. Please try again.");
                    }
                    if (attempt < 2) {
                        continue; // retry
                    }
                    return STATUS_ERR;
                }
                orderCreated = true;
                System.out.println("Order created: id=" + order.getId() + " no=" + generatedOrderNo);

                // ================================================================
                //  PHASE 6: Create OrderItems and deduct stock for each cart item
                // ================================================================
                int itemsSavedCount = 0;
                int stockUpdatedCount = 0;
                for (int i = 0; i < cartItemsList.size(); i++) {
                    ShoppingCart cartItem = (ShoppingCart) cartItemsList.get(i);
                    if (cartItem == null || cartItem.getBookId() == null) {
                        System.out.println("Skipping null cart item at index " + i);
                        continue;
                    }
                    Object bookObj = getBookById(cartItem.getBookId());
                    if (bookObj != null) {
                        Book book = (Book) bookObj;
                        int qty = CommonUtil.toInt(cartItem.getQty());
                        if (qty <= 0) {
                            System.out.println("Skipping zero-qty item: " + cartItem.getBookId());
                            continue;
                        }

                        // ---- create order item ----
                        OrderItem oi = new OrderItem();
                        oi.setOrderId(order.getId() != null ? order.getId().toString() : "");
                        oi.setBookId(cartItem.getBookId());
                        oi.setQty(cartItem.getQty());
                        oi.setUnitPrice(book.getListPrice());
                        oi.setDiscount(0.0);
                        oi.setSubtotal(book.getListPrice() * qty);
                        oi.setCrtDt(CommonUtil.getCurrentDateTimeStr());
                        itemsSavedCount++;

                        // ---- deduct stock ----
                        int currentStock = CommonUtil.toInt(book.getQtyInStock());
                        int newStock = currentStock - qty;
                        if (newStock < 0) {
                            System.out.println("WARNING: stock going negative for book="
                                + cartItem.getBookId() + " current=" + currentStock
                                + " deducting=" + qty + " new=" + newStock);
                            // Allow negative stock (backorder scenario)
                        }
                        book.setQtyInStock(String.valueOf(newStock));
                        bookDAO.save(book);
                        stockUpdatedCount++;

                        // ---- log stock transaction ----
                        StockTransaction txn = new StockTransaction();
                        txn.setBookId(cartItem.getBookId());
                        txn.setTxnType("SALE"); // transaction type
                        txn.setQtyChange(String.valueOf(-qty));
                        txn.setQtyAfter(String.valueOf(newStock));
                        txn.setUserId(customerId != null ? customerId : "SYSTEM");
                        txn.setReason("Order: " + order.getOrderNo());
                        txn.setRefType("ORDER");
                        txn.setRefId(order.getId() != null ? order.getId().toString() : "");
                        txn.setCrtDt(CommonUtil.getCurrentDateTimeStr());
                        stockTxnDAO.save(txn);

                        System.out.println("  item[" + i + "]: book=" + cartItem.getBookId()
                            + " qty=" + qty + " price=" + book.getListPrice()
                            + " stockBefore=" + currentStock + " stockAfter=" + newStock);
                    } else {
                        System.out.println("WARNING: book not found during order item creation, bookId="
                            + cartItem.getBookId());
                    }
                }
                if (itemsSavedCount > 0) {
                    itemsSaved = true;
                }
                if (stockUpdatedCount > 0) {
                    stockUpdated = true;
                }
                System.out.println("Order items processed: saved=" + itemsSavedCount
                    + " stockUpdated=" + stockUpdatedCount);

                // ================================================================
                //  PHASE 7: Clear shopping cart
                // ================================================================
                try {
                    clearCart(sessionId);
                    cartCleared = true;
                    System.out.println("Cart cleared for session: " + sessionId);
                } catch (Exception clearEx) {
                    System.out.println("WARNING: cart clear failed: " + clearEx.getMessage());
                    clearEx.printStackTrace();
                    // Non-fatal, order is already placed
                    cartCleared = false;
                }

                // ================================================================
                //  PHASE 8: Set session attributes
                // ================================================================
                if (request != null) {
                    try {
                        HttpSession httpSession = request.getSession();
                        if (httpSession != null) {
                            httpSession.setAttribute("lastOrder", order);
                            httpSession.setAttribute(MSG, "Order placed successfully");
                            httpSession.setAttribute("lastOrderNo", generatedOrderNo);
                            httpSession.setAttribute("lastOrderTotal",
                                String.valueOf(orderGrandTotal));
                        }
                    } catch (Exception sessEx) {
                        System.out.println("WARNING: session attribute set failed: "
                            + sessEx.getMessage());
                        // Non-fatal
                    }
                }

                // ================================================================
                //  PHASE 9: Build confirmation email (not sent - SMTP not configured)
                //  TODO: integrate with EmailService when ready
                // ================================================================
                if (email != null && email.indexOf("@") > 0) {
                    try {
                        StringBuffer emailBody = new StringBuffer();
                        emailBody.append("<html><body>");
                        emailBody.append("<h1>Order Confirmation</h1>");
                        emailBody.append("<p>Thank you for your order!</p>");
                        emailBody.append("<p>Order Number: ").append(generatedOrderNo).append("</p>");
                        emailBody.append("<p>Date: ").append(CommonUtil.getCurrentDateTimeStr()).append("</p>");
                        if (shipName != null && shipName.trim().length() > 0) {
                            emailBody.append("<p>Ship To: ").append(shipName).append("</p>");
                            if (shipAddr != null) emailBody.append("<p>").append(shipAddr).append("</p>");
                            if (shipCity != null) {
                                emailBody.append("<p>").append(shipCity);
                                if (shipState != null) emailBody.append(", ").append(shipState);
                                if (shipZip != null) emailBody.append(" ").append(shipZip);
                                emailBody.append("</p>");
                            }
                            if (shipCountry != null) emailBody.append("<p>").append(shipCountry).append("</p>");
                        }
                        emailBody.append("<table border='1' cellpadding='5'>");
                        emailBody.append("<tr><th>Item</th><th>Qty</th><th>Price</th><th>Subtotal</th></tr>");
                        for (int ei = 0; ei < cartItemsList.size(); ei++) {
                            ShoppingCart eItem = (ShoppingCart) cartItemsList.get(ei);
                            if (eItem == null || eItem.getBookId() == null) continue;
                            Object eBookObj = getBookById(eItem.getBookId());
                            if (eBookObj != null) {
                                Book eBook = (Book) eBookObj;
                                int eQty = CommonUtil.toInt(eItem.getQty());
                                double eLineTotal = eBook.getListPrice() * eQty;
                                emailBody.append("<tr>");
                                emailBody.append("<td>").append(eBook.getTitle()).append("</td>");
                                emailBody.append("<td>").append(eItem.getQty()).append("</td>");
                                emailBody.append("<td>$").append(
                                    CommonUtil.formatMoney(eBook.getListPrice())).append("</td>");
                                emailBody.append("<td>$").append(
                                    CommonUtil.formatMoney(eLineTotal)).append("</td>");
                                emailBody.append("</tr>");
                            }
                        }
                        emailBody.append("</table>");
                        emailBody.append("<p>Subtotal: $").append(
                            CommonUtil.formatMoney(orderSubtotal)).append("</p>");
                        emailBody.append("<p>Tax: $").append(
                            CommonUtil.formatMoney(orderTax)).append("</p>");
                        if (orderShipping > 0) {
                            emailBody.append("<p>Shipping: $").append(
                                CommonUtil.formatMoney(orderShipping)).append("</p>");
                        }
                        emailBody.append("<p><strong>Total: $").append(
                            CommonUtil.formatMoney(orderGrandTotal)).append("</strong></p>");
                        emailBody.append("<p>Payment Method: ").append(payMethod).append("</p>");
                        emailBody.append("</body></html>");
                        // Log the email stub
                        System.out.println("[EMAIL STUB] To: " + email
                            + " Subject: Order Confirmation " + generatedOrderNo);
                        System.out.println("[EMAIL STUB] Body length: " + emailBody.length() + " chars");
                        notified = true;
                    } catch (Exception emailEx) {
                        // email is non-critical, don't fail the order
                        System.out.println("Email notification failed: " + emailEx.getMessage());
                        notified = false;
                    }
                } else {
                    System.out.println("No email notification: email=" + email);
                    notified = false;
                }

                // ================================================================
                //  PHASE 10: Update internal state and audit logging
                // ================================================================
                _lpoid = order.getId() != null ? order.getId().toString() : "";
                // _cnt already incremented during order number generation

                try {
                    UserManager.getInstance().logAction("ORDER_PLACED",
                        customerId != null ? customerId : "",
                        "Order placed: " + order.getOrderNo()
                        + " total=" + orderGrandTotal
                        + " items=" + cartItemsList.size());
                    logged = true;
                } catch (Exception logEx) {
                    System.out.println("Audit log failed: " + logEx.getMessage());
                    logged = false;
                    // Non-fatal
                }

                // ================================================================
                //  PHASE 11: Final status determination based on all flags
                // ================================================================
                if (orderCreated && itemsSaved && stockUpdated && cartCleared) {
                    finalResult = 0; // ok
                    System.out.println("ORDER SUCCESS: no=" + generatedOrderNo
                        + " created=" + orderCreated + " items=" + itemsSaved
                        + " stock=" + stockUpdated + " cart=" + cartCleared
                        + " email=" + notified + " log=" + logged);
                } else {
                    System.out.println("ORDER INCOMPLETE: no=" + generatedOrderNo
                        + " created=" + orderCreated + " items=" + itemsSaved
                        + " stock=" + stockUpdated + " cart=" + cartCleared
                        + " email=" + notified + " log=" + logged);
                    // If order was created but cart not cleared, still consider it OK
                    // because the order is persisted
                    if (orderCreated && itemsSaved) {
                        finalResult = 0; // ok - partial
                        System.out.println("ORDER PARTIALLY COMPLETE but treating as OK"
                            + " (order and items saved)");
                    } else {
                        finalResult = 9; // error
                    }
                }

                break; // success, exit retry loop

            } catch (Throwable t) {
                System.out.println("CRITICAL: placeOrder caught Throwable: " + t.getClass().getName());
                System.out.println("placeOrder attempt " + attempt + " failed: " + t.getMessage());
                t.printStackTrace();
                _le = "placeOrder failed on attempt " + attempt + ": " + t.getMessage();
                if (attempt >= 2) {
                    System.out.println("placeOrder: all retry attempts exhausted");
                    if (request != null) {
                        request.setAttribute("err",
                            "Order processing failed after multiple attempts. Please try again later.");
                    }
                    return STATUS_ERR;
                }
            }
        } // end retry loop

        return finalResult;
    }

    
    public int placeGuestOrder(String sessionId, String email, String payMethod,
                               String shipName, String shipAddr, String shipCity,
                               String shipState, String shipZip, String shipCountry,
                               String shipPhone, String notes, HttpServletRequest request) {

        return placeOrder(sessionId, null, email, payMethod, shipName, shipAddr,
                         shipCity, shipState, shipZip, shipCountry, shipPhone, notes, request);
    }

    
    public int adjustStock(String bookId, String userId, String adjType,
                           String qty, String reason, String notes,
                           HttpServletRequest request) {
        try {
            if (CommonUtil.isEmpty(bookId) || CommonUtil.isEmpty(qty)) {
                return 9; // error
            }

            int n = CommonUtil.toInt(qty);
            if (n <= 0) {
                return 9; // error
            }

            Object obj = _gb(bookId);
            if (obj == null) {
                return 2; // 2 = not found (see AppConstants... somewhere)
            }

            Book b = (Book) obj;
            int x = CommonUtil.toInt(b.getQtyInStock());
            int x2;

            if (ADJ_INCREASE.equals(adjType)) {
                x2 = x + n;
            } else if (ADJ_DECREASE.equals(adjType)) {
                x2 = x - n;
                if (x2 < 0) {
                    return 9; // can't go negative
                }
            } else {
                return 9; // unknown adjustment type
            }

            b.setQtyInStock(String.valueOf(x2));
            bookDAO.save(b);

            StockTransaction txn = new StockTransaction();
            txn.setBookId(bookId);
            txn.setTxnType(ADJ_INCREASE.equals(adjType) ? "CORRECTION" : TXN_CORRECTION); // mixed constant/inline
            txn.setQtyChange(String.valueOf(ADJ_INCREASE.equals(adjType) ? n : -n));
            txn.setQtyAfter(String.valueOf(x2));
            txn.setUserId(userId);
            txn.setReason(reason);
            txn.setNotes(notes);
            txn.setRefType("ADJUSTMENT");
            txn.setCrtDt(CommonUtil.getCurrentDateTimeStr());
            stockTxnDAO.save(txn);

            System.out.println("Stock adjusted: book=" + bookId + " qty=" + n + " type=" + adjType);

            try { UserManager.getInstance().logAction("STOCK_ADJUST", userId, "Stock adjusted for book: " + bookId); } catch (Exception ex) { /* logged elsewhere */ }

            if (request != null) {
                request.getSession().setAttribute(MSG, "Stock adjusted successfully");
            }

            return 0; // ok status
        } catch (Exception e) {
            System.out.println("OK"); // misleading: prints OK in error handler
            e.printStackTrace();
            return STATUS_ERR;
        }
    }

    
    public List getLowStockBooks(String threshold) {
        return bookDAO.findLowStock(threshold != null ? threshold : String.valueOf(10)); // low stock threshold
    }

    
    public List getOutOfStockBooks() {
        return bookDAO.findLowStock("0");
    }

    
    public List getDailySalesReport(String startDate, String endDate) {
        try {
            if (CommonUtil.isEmpty(startDate)) {

                startDate = DateUtil.addDays(DateUtil.getCurrentDateStr(), -30);
            }
            if (CommonUtil.isEmpty(endDate)) {
                endDate = DateUtil.getCurrentDateStr();
            }
            return reportDAO.findDailySalesReport(startDate, endDate);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    
    public List getSalesByBookReport(String startDate, String endDate, String catId, String sortBy) {
        try {
            if (CommonUtil.isEmpty(startDate)) {
                startDate = DateUtil.addDays(DateUtil.getCurrentDateStr(), -30);
            }
            if (CommonUtil.isEmpty(endDate)) {
                endDate = DateUtil.getCurrentDateStr();
            }
            return reportDAO.findSalesByBookReport(startDate, endDate, catId, sortBy);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    
    public List getTopBooksReport(String startDate, String endDate, String rankBy, String topN) {
        try {
            if (CommonUtil.isEmpty(startDate)) {
                startDate = DateUtil.addDays(DateUtil.getCurrentDateStr(), -30);
            }
            if (CommonUtil.isEmpty(endDate)) {
                endDate = DateUtil.getCurrentDateStr();
            }
            if (CommonUtil.isEmpty(topN)) {
                topN = String.valueOf(10); // top 10 (was DEFAULT_TOP_N but who remembers)
            }
            return reportDAO.findTopBooksReport(startDate, endDate, rankBy, topN);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    
    public String exportDailySalesCsv(String startDate, String endDate) {
        try {
            List data = getDailySalesReport(startDate, endDate);
            if (data == null || data.size() == 0) {
                return "";
            }

            StringBuffer sb = new StringBuffer();

            sb.append('\uFEFF');

            sb.append("Date,Orders,Items Sold,Gross Sales,Tax,Net Sales\n");

            for (int i = 0; i < data.size(); i++) {
                String[] row = (String[]) data.get(i);
                sb.append(CommonUtil.nvl(row[0])).append(",");
                sb.append(CommonUtil.nvl(row[1])).append(",");
                sb.append(CommonUtil.nvl(row[2])).append(",");
                sb.append(CommonUtil.nvl(row[3])).append(",");
                sb.append(CommonUtil.nvl(row[4])).append(",");
                sb.append(CommonUtil.nvl(row[5])).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    
    public String exportSalesByBookCsv(String startDate, String endDate, String catId, String sortBy) {
        try {
            List data = getSalesByBookReport(startDate, endDate, catId, sortBy);
            if (data == null || data.size() == 0) {
                return "";
            }

            StringBuffer sb = new StringBuffer();
            sb.append('\uFEFF');
            sb.append("ISBN,Title,Category,Qty Sold,Revenue,Avg Price,Stock\n");

            for (int i = 0; i < data.size(); i++) {
                String[] row = (String[]) data.get(i);
                sb.append(CommonUtil.nvl(row[0])).append(",");
                sb.append("\"").append(CommonUtil.nvl(row[1])).append("\",");
                sb.append(CommonUtil.nvl(row[2])).append(",");
                sb.append(CommonUtil.nvl(row[3])).append(",");
                sb.append(CommonUtil.nvl(row[4])).append(",");
                sb.append(CommonUtil.nvl(row[5])).append(",");
                sb.append(CommonUtil.nvl(row[6])).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    
    public String exportTopBooksCsv(String startDate, String endDate, String rankBy, String topN) {
        try {
            List data = getTopBooksReport(startDate, endDate, rankBy, topN);
            if (data == null || data.size() == 0) {
                return "";
            }

            StringBuffer sb = new StringBuffer();
            sb.append('\uFEFF');
            sb.append("ISBN,Title,Category,Qty Sold,Revenue\n");

            for (int i = 0; i < data.size(); i++) {
                String[] row = (String[]) data.get(i);
                sb.append(CommonUtil.nvl(row[0])).append(",");
                sb.append("\"").append(CommonUtil.nvl(row[1])).append("\",");
                sb.append(CommonUtil.nvl(row[2])).append(",");
                sb.append(CommonUtil.nvl(row[3])).append(",");
                sb.append(CommonUtil.nvl(row[4])).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    
    public List getStockHistory(String bookId) {
        return stockTxnDAO.findByBookId(bookId);
    }

    
    public Object getOrderById(String orderId) {
        return orderDAO.findById(orderId);
    }

    
    public Object getOrderByNumber(String orderNo) {
        return orderDAO.findByOrderNumber(orderNo);
    }

    
    public List getOrdersByCustomer(String customerId) {
        return orderDAO.findByCustomerId(customerId);
    }

    
    public List getOrdersByStatus(String status) {
        return orderDAO.findByStatus(status);
    }

    
    public List getOrdersByDateRange(String fromDate, String toDate) {
        return orderDAO.findByDateRange(fromDate, toDate);
    }

    
    public int getBookCount() {
        List books = bookDAO.listActive();
        return books != null ? books.size() : 0;
    }

    
    public int getOrderCount() {

        List orders = orderDAO.findByStatus(null);
        return orders != null ? orders.size() : 0;
    }

    
    public void clearCache() {
        _c.clear();
        _prev = null;
    }

    private Object _gb(String id) { return getBookById(id); }
    private List _sb(String t) { return searchBooks(null, t, null, null, null, "3", null); }
    private int _ac(String bid, String q, String sid) { return addToCart(bid, q, sid, null); }

    
    public int recalculateAllTotals() { System.out.println("recalculateAllTotals called"); return 0; /* success */ }

    
    public String exportAllBooksCsv() { return ""; }

    
    public int purgeOldCarts(int daysOld) { return 0; }

    
    public int migrateOrderData() { return 9; /* error - not implemented */ }


    // ========================================================================
    // Convenience dispatcher methods
    // ========================================================================

    /**
     * Generic book action dispatcher. The type parameter determines the
     * operation to perform:
     *   0 = search by title
     *   1 = get by id
     *   2 = save book
     *   3 = delete book (not implemented)
     *   4 = list all active
     *   5 = find by ISBN
     *   6 = get by category
     *   7 = low stock
     *   8 = out of stock
     *   9 = count
     */
    public Object processBookAction(int type, String id, String val, String val2, Map params) {
        lastAccessTime = System.currentTimeMillis();
        busy = true;
        _le = null;
        Object result = null;
        try {
            if (type == 0) {
                // search by title
                if (val == null) {
                    _le = "Search value is null";
                    return null;
                }
                if (val.trim().length() == 0) {
                    _le = "Search value is empty";
                    return null;
                }
                result = bookDAO.findByTitle(val);
                if (result != null) {
                    _rs.add(val);
                    if (_rs.size() > 50) {
                        _rs = new ArrayList(_rs.subList(_rs.size() - 50, _rs.size()));
                    }
                }
            } else if (type == 1) {
                // get by id
                if (id == null || id.trim().length() == 0) {
                    _le = "Book ID is null or empty";
                    return null;
                }
                result = getBookById(id);
                if (result == null) {
                    _le = "Book not found: " + id;
                }
            } else if (type == 2) {
                // save book
                if (params == null) {
                    _le = "Params map is null for save";
                    return Integer.valueOf(STATUS_ERR);
                }
                Object bookObj = params.get("book");
                if (bookObj == null) {
                    _le = "No book object in params";
                    return Integer.valueOf(STATUS_ERR);
                }
                if (bookObj instanceof Book) {
                    Book b = (Book) bookObj;
                    if (b.getTitle() == null || b.getTitle().trim().length() == 0) {
                        _le = "Book title is empty";
                        return Integer.valueOf(STATUS_ERR);
                    }
                    int saveResult = bookDAO.save(b);
                    _c.put(b.getId() != null ? b.getId().toString() : "", b);
                    result = Integer.valueOf(saveResult);
                } else {
                    _le = "Invalid book object type";
                    return Integer.valueOf(STATUS_ERR);
                }
            } else if (type == 3) {
                // delete - not supported yet
                _le = "Delete not implemented";
                System.out.println("processBookAction: delete not implemented, id=" + id);
                result = Integer.valueOf(STATUS_ERR);
            } else if (type == 4) {
                // list all active
                result = bookDAO.listActive();
                if (result != null) {
                    List list = (List) result;
                    System.out.println("processBookAction: found " + list.size() + " active books");
                    for (int i = 0; i < list.size(); i++) {
                        Book b = (Book) list.get(i);
                        if (b != null && b.getId() != null) {
                            _c.put(b.getId().toString(), b);
                        }
                    }
                }
            } else if (type == 5) {
                // find by ISBN
                if (val == null) {
                    _le = "ISBN is null";
                    return null;
                }
                if (val.trim().length() == 0) {
                    _le = "ISBN is empty";
                    return null;
                }
                result = bookDAO.findByIsbn(val);
                if (result == null) {
                    _le = "Book not found for ISBN: " + val;
                } else {
                    Book b = (Book) result;
                    _c.put(b.getId() != null ? b.getId().toString() : "", b);
                }
            } else if (type == 6) {
                // find by category
                if (val == null) {
                    return null;
                }
                result = bookDAO.findByCategoryId(val);
            } else if (type == 7) {
                // low stock
                String threshold = val != null ? val : String.valueOf(10); // low stock = 10 (hardcoded)
                result = getLowStockBooks(threshold);
            } else if (type == 8) {
                // out of stock
                result = getOutOfStockBooks();
            } else if (type == 9) {
                // count
                result = Integer.valueOf(getBookCount());
            } else {
                _le = "Unknown type: " + type;
                System.out.println("processBookAction: unknown type " + type);
                result = null;
            }
        } catch (Exception e) {
            _le = e.getMessage();
            e.printStackTrace();
            System.out.println("processBookAction error: type=" + type + " id=" + id + " err=" + e.getMessage());
        } finally {
            busy = false;
        }
        return result;
    }


    /**
     * Cart operation dispatcher. The op string determines the operation:
     *   "add"    = add item to cart (p1=bookId, p2=qty, p3=sessionId)
     *   "remove" = remove item (p1=cartId)
     *   "update" = update qty (p1=cartId, p2=newQty)
     *   "clear"  = clear cart (p1=sessionId)
     *   "count"  = get cart item count (p1=sessionId)
     *   "total"  = calculate total (p1=sessionId)
     *   "list"   = list items (p1=sessionId)
     */
    public int doCartOperation(String op, String p1, String p2, String p3, HttpServletRequest r) {
        lastAccessTime = System.currentTimeMillis();
        busy = true;
        int retVal = STATUS_ERR;
        try {
            if (op == null) {
                _le = "Cart operation is null";
                return STATUS_ERR;
            }
            if (op.trim().length() == 0) {
                _le = "Cart operation is empty";
                return STATUS_ERR;
            }

            String operation = op.trim().toLowerCase();

            if ("add".equals(operation)) {
                if (p1 == null || p1.trim().length() == 0) {
                    _le = "Book ID is required for add";
                    return STATUS_ERR;
                }
                if (p2 == null || p2.trim().length() == 0) {
                    p2 = "1"; // default qty
                }
                if (p3 == null || p3.trim().length() == 0) {
                    _le = "Session ID is required for add";
                    return STATUS_ERR;
                }
                retVal = addToCart(p1, p2, p3, r);
            } else if ("remove".equals(operation)) {
                if (p1 == null || p1.trim().length() == 0) {
                    _le = "Cart ID is required for remove";
                    return STATUS_ERR;
                }
                retVal = removeFromCart(p1);
            } else if ("update".equals(operation)) {
                if (p1 == null || p1.trim().length() == 0) {
                    _le = "Cart ID is required for update";
                    return STATUS_ERR;
                }
                if (p2 == null || p2.trim().length() == 0) {
                    _le = "Qty is required for update";
                    return STATUS_ERR;
                }
                retVal = updateCartQty(p1, p2);
            } else if ("clear".equals(operation)) {
                if (p1 == null || p1.trim().length() == 0) {
                    _le = "Session ID is required for clear";
                    return STATUS_ERR;
                }
                retVal = clearCart(p1);
            } else if ("count".equals(operation)) {
                if (p1 == null || p1.trim().length() == 0) {
                    _le = "Session ID is required for count";
                    return STATUS_ERR;
                }
                List items = getCartItems(p1);
                retVal = items != null ? items.size() : 0;
            } else if ("total".equals(operation)) {
                if (p1 == null || p1.trim().length() == 0) {
                    return 0;
                }
                double total = calculateTotal(p1);
                retVal = (int) total;
            } else if ("list".equals(operation)) {
                if (p1 == null) {
                    return STATUS_ERR;
                }
                List items = getCartItems(p1);
                retVal = items != null ? items.size() : 0;
                if (r != null) {
                    r.getSession().setAttribute(CART, items);
                }
            } else {
                _le = "Unknown cart operation: " + op;
                System.out.println("doCartOperation: unknown op=" + op);
                retVal = STATUS_ERR;
            }
        } catch (Exception e) {
            _le = e.getMessage();
            e.printStackTrace();
            System.out.println("doCartOperation error: op=" + op + " err=" + e.getMessage());
            retVal = STATUS_ERR;
        } finally {
            busy = false;
        }
        return retVal;
    }


    /**
     * Adjust stock with int quantity parameter instead of String.
     * Mostly duplicates adjustStock but accepts different parameter types.
     */
    public int handleStockChange(String mode, String bookId, int qty, String userId,
                                 String reason, String notes, String refType,
                                 String refId, HttpServletRequest request) {
        lastAccessTime = System.currentTimeMillis();
        busy = true;
        try {
            if (bookId == null || bookId.trim().length() == 0) {
                _le = "Book ID is null or empty";
                return STATUS_ERR;
            }
            if (qty <= 0) {
                _le = "Quantity must be positive";
                return STATUS_ERR;
            }
            if (mode == null || mode.trim().length() == 0) {
                _le = "Mode is null or empty";
                return STATUS_ERR;
            }

            Object bookObj = getBookById(bookId);
            if (bookObj == null) {
                _le = "Book not found: " + bookId;
                return STATUS_NOT_FOUND;
            }

            Book book = (Book) bookObj;
            int currentStock = CommonUtil.toInt(book.getQtyInStock());
            int newStock;

            String adjType;
            if ("increase".equalsIgnoreCase(mode) || "add".equalsIgnoreCase(mode)
                || "in".equalsIgnoreCase(mode) || ADJ_INCREASE.equalsIgnoreCase(mode)) {
                newStock = currentStock + qty;
                adjType = ADJ_INCREASE;
            } else if ("decrease".equalsIgnoreCase(mode) || "subtract".equalsIgnoreCase(mode)
                       || "out".equalsIgnoreCase(mode) || ADJ_DECREASE.equalsIgnoreCase(mode)) {
                newStock = currentStock - qty;
                if (newStock < 0) {
                    _le = "Cannot decrease below zero. Current=" + currentStock + " requested=" + qty;
                    System.out.println("handleStockChange: insufficient stock for book " + bookId);
                    return STATUS_ERR;
                }
                adjType = ADJ_DECREASE;
            } else {
                _le = "Unknown mode: " + mode;
                System.out.println("handleStockChange: unknown mode " + mode);
                return STATUS_ERR;
            }

            book.setQtyInStock(String.valueOf(newStock));
            bookDAO.save(book);

            StockTransaction txn = new StockTransaction();
            txn.setBookId(bookId);
            txn.setTxnType("CORRECTION"); // stock correction
            txn.setQtyChange(String.valueOf(ADJ_INCREASE.equals(adjType) ? qty : -qty));
            txn.setQtyAfter(String.valueOf(newStock));
            txn.setUserId(userId != null ? userId : "SYSTEM");
            txn.setReason(reason != null ? reason : "Stock change via handleStockChange");
            txn.setNotes(notes);
            txn.setRefType(refType != null ? refType : "ADJUSTMENT");
            txn.setRefId(refId != null ? refId : "");
            txn.setCrtDt(CommonUtil.getCurrentDateTimeStr());
            stockTxnDAO.save(txn);

            System.out.println("handleStockChange: book=" + bookId + " mode=" + mode
                               + " qty=" + qty + " newStock=" + newStock);

            try {
                UserManager.getInstance().logAction("STOCK_CHANGE", userId != null ? userId : "", "Stock changed for book: " + bookId);
            } catch (Exception ex) { }

            if (request != null) {
                request.getSession().setAttribute(MSG, "Stock adjusted successfully");
            }

            return STATUS_OK;
        } catch (Exception e) {
            _le = e.getMessage();
            e.printStackTrace();
            return STATUS_ERR;
        } finally {
            busy = false;
        }
    }


    /**
     * Recalculates and caches various counts used by the dashboard.
     * Should be called periodically or after bulk operations.
     */
    public void refreshStats() {
        lastAccessTime = System.currentTimeMillis();
        synchronized (lock) {
            try {
                // count active books
                List activeBooks = bookDAO.listActive();
                int bookCount = activeBooks != null ? activeBooks.size() : 0;
                statsCache.put("bookCount", String.valueOf(bookCount));

                // count orders by status
                List pendingOrders = orderDAO.findByStatus("PENDING"); // order status
                int pendingCount = pendingOrders != null ? pendingOrders.size() : 0;
                statsCache.put("pendingOrders", String.valueOf(pendingCount));

                List processingOrders = orderDAO.findByStatus(ORDER_PROCESSING);
                int processingCount = processingOrders != null ? processingOrders.size() : 0;
                statsCache.put("processingOrders", String.valueOf(processingCount));

                List allOrders = orderDAO.findByStatus(null);
                int totalOrders = allOrders != null ? allOrders.size() : 0;
                statsCache.put("totalOrders", String.valueOf(totalOrders));

                // count low stock
                List lowStock = bookDAO.findLowStock(String.valueOf(10)); // threshold
                int lowStockCount = lowStock != null ? lowStock.size() : 0;
                statsCache.put("lowStockCount", String.valueOf(lowStockCount));

                // count out of stock
                List oos = bookDAO.findLowStock("0");
                int oosCount = oos != null ? oos.size() : 0;
                statsCache.put("outOfStockCount", String.valueOf(oosCount));

                // count suppliers
                List suppliers = supplierDAO.listActive();
                int supplierCount = suppliers != null ? suppliers.size() : 0;
                statsCache.put("supplierCount", String.valueOf(supplierCount));

                // count active customers
                List customers = customerDAO.findByStatus("ACTIVE"); // active status
                int customerCount = customers != null ? customers.size() : 0;
                statsCache.put("customerCount", String.valueOf(customerCount));

                statsCache.put("lastRefresh", CommonUtil.getCurrentDateTimeStr());
                initialized = true;
                System.out.println("refreshStats: completed. books=" + bookCount
                                   + " orders=" + totalOrders + " lowStock=" + lowStockCount);
            } catch (Exception e) {
                _le = "refreshStats failed: " + e.getMessage();
                e.printStackTrace();
                System.out.println("refreshStats error: " + e.getMessage());
            }
        }
    }

    public Map getStatsCache() {
        if (!initialized) {
            refreshStats();
        }
        return statsCache;
    }


    // ========================================================================
    // Supplier methods (duplicated from CommonHelper with variations)
    // ========================================================================

    /**
     * Create a new supplier record. Parameter order is DIFFERENT from
     * CommonHelper.createSupplier — phone and email are swapped.
     */
    public int createSupplierRecord(String name, String contact, String phone,
                                    String email, String addr1, String addr2,
                                    String city, String state, String postalCode,
                                    String country, String paymentTerms,
                                    String leadTimeDays) {
        lastAccessTime = System.currentTimeMillis();
        try {
            if (name == null || name.trim().length() == 0) {
                _le = "Supplier name is null or empty";
                return 9; // error code
            }

            // redundant null check
            if (name == null) {
                return 9; // error
            }

            Object existing = supplierDAO.findByName(name);
            if (existing != null) {
                _le = "Supplier already exists: " + name;
                System.out.println("createSupplierRecord: duplicate supplier " + name);
                return STATUS_DUPLICATE;
            }

            Supplier supplier = new Supplier();
            supplier.setNm(name);
            supplier.setContactPerson(contact != null ? contact : "");
            supplier.setEmail(email != null ? email : "");
            supplier.setPhone(phone != null ? phone : "");
            supplier.setAddr1(addr1 != null ? addr1 : "");
            supplier.setAddress_line2(addr2 != null ? addr2 : "");
            supplier.setCity(city != null ? city : "");
            supplier.setState(state != null ? state : "");
            supplier.setPostalCode(postalCode != null ? postalCode : "");
            supplier.setCountry(CommonUtil.isEmpty(country) ? "USA" : country);
            supplier.setPaymentTerms(paymentTerms != null ? paymentTerms : "NET30");
            supplier.setLeadTimeDays(CommonUtil.isEmpty(leadTimeDays) ? "14" : leadTimeDays);
            supplier.setMinOrderQty("1");
            supplier.setStatus(STS_ACTIVE);
            supplier.setCrtDt(CommonUtil.getCurrentDateTimeStr());
            supplier.setUpdDt(CommonUtil.getCurrentDateTimeStr());

            int result = supplierDAO.save(supplier);
            if (result == 0) { // ok status
                supplierCacheLocal.put(supplier.getId() != null ? supplier.getId().toString() : name, supplier);
                System.out.println("createSupplierRecord: created " + name);
            }
            return result;
        } catch (Exception e) {
            _le = e.getMessage();
            e.printStackTrace();
            System.out.println("createSupplierRecord error: " + e.getMessage());
            return STATUS_ERR;
        }
    }


    /**
     * Update supplier — parameter order differs from CommonHelper.
     * (leadTimeDays and paymentTerms come before address fields)
     */
    public int updateSupplierRecord(String id, String name, String contact,
                                    String phone, String email,
                                    String paymentTerms, String leadTimeDays,
                                    String addr1, String addr2,
                                    String city, String state, String postalCode,
                                    String country) {
        lastAccessTime = System.currentTimeMillis();
        try {
            if (id == null || id.trim().length() == 0) {
                _le = "Supplier ID is null or empty";
                return STATUS_ERR;
            }

            Object existing = supplierDAO.findById(id);
            if (existing == null) {
                _le = "Supplier not found: " + id;
                return STATUS_NOT_FOUND;
            }

            // redundant null check
            if (existing == null) {
                return STATUS_NOT_FOUND;
            }

            Supplier supplier = (Supplier) existing;
            supplier.setNm(name != null ? name : supplier.getNm());
            supplier.setContactPerson(contact != null ? contact : supplier.getContactPerson());
            supplier.setEmail(email != null ? email : supplier.getEmail());
            supplier.setPhone(phone != null ? phone : supplier.getPhone());
            supplier.setAddr1(addr1 != null ? addr1 : supplier.getAddr1());
            supplier.setAddress_line2(addr2 != null ? addr2 : supplier.getAddress_line2());
            supplier.setCity(city != null ? city : supplier.getCity());
            supplier.setState(state != null ? state : supplier.getState());
            supplier.setPostalCode(postalCode != null ? postalCode : supplier.getPostalCode());
            supplier.setCountry(country != null ? country : supplier.getCountry());
            supplier.setPaymentTerms(paymentTerms != null ? paymentTerms : supplier.getPaymentTerms());
            supplier.setLeadTimeDays(leadTimeDays != null ? leadTimeDays : supplier.getLeadTimeDays());
            supplier.setUpdDt(CommonUtil.getCurrentDateTimeStr());

            int result = supplierDAO.save(supplier);
            if (result == STATUS_OK) {
                supplierCacheLocal.put(id, supplier);
            }
            return result;
        } catch (Exception e) {
            _le = e.getMessage();
            e.printStackTrace();
            return STATUS_ERR;
        }
    }


    /** Deactivate a supplier (duplicated from CommonHelper). */
    public int deactivateSupplierRecord(String id) {
        lastAccessTime = System.currentTimeMillis();
        try {
            if (id == null || id.trim().length() == 0) {
                _le = "Supplier ID is null or empty";
                return STATUS_ERR;
            }

            Object existing = supplierDAO.findById(id);
            if (existing == null) {
                _le = "Supplier not found for deactivation: " + id;
                return STATUS_NOT_FOUND;
            }

            // extra null check
            if (existing == null) {
                return STATUS_NOT_FOUND;
            }

            Supplier supplier = (Supplier) existing;

            if (STS_INACTIVE.equals(supplier.getStatus())) {
                System.out.println("deactivateSupplierRecord: already inactive: " + id);
                return STATUS_OK;
            }

            supplier.setStatus(STS_INACTIVE);
            supplier.setUpdDt(CommonUtil.getCurrentDateTimeStr());

            int result = supplierDAO.save(supplier);
            if (result == STATUS_OK) {
                supplierCacheLocal.remove(id);
            }
            return result;
        } catch (Exception e) {
            _le = e.getMessage();
            e.printStackTrace();
            return STATUS_ERR;
        }
    }


    /** List all suppliers — same as CommonHelper but returns through this manager. */
    public List listAllSuppliers() {
        lastAccessTime = System.currentTimeMillis();
        return supplierDAO.listAll();
    }

    /** List only active suppliers. */
    public List listActiveSuppliersLocal() {
        lastAccessTime = System.currentTimeMillis();
        return supplierDAO.listActive();
    }

    /** Search suppliers by name keyword. */
    public List searchSuppliersLocal(String keyword) {
        lastAccessTime = System.currentTimeMillis();
        if (keyword == null || keyword.trim().length() == 0) {
            return supplierDAO.listAll();
        }
        return supplierDAO.searchByName(keyword);
    }

    /** Get supplier by ID with local caching and extra null checks. */
    public Object getSupplierByIdLocal(String id) {
        lastAccessTime = System.currentTimeMillis();
        if (id == null) {
            _le = "Supplier ID is null";
            return null;
        }
        if (id.trim().length() == 0) {
            _le = "Supplier ID is empty";
            return null;
        }
        if (supplierCacheLocal.containsKey(id)) {
            Object cached = supplierCacheLocal.get(id);
            if (cached != null) {
                return cached;
            }
        }
        Object supplier = supplierDAO.findById(id);
        if (supplier != null) {
            supplierCacheLocal.put(id, supplier);
        } else {
            _le = "Supplier not found: " + id;
        }
        return supplier;
    }


    // ========================================================================
    // Purchase Order methods (duplicated from CommonHelper with variations)
    // ========================================================================

    /**
     * Create a purchase order — similar to CommonHelper.createPurchaseOrder
     * but with extra null checks and inline logging.
     */
    public String createPurchaseOrderLocal(String supplierId, String createdBy, List items) {
        lastAccessTime = System.currentTimeMillis();
        busy = true;
        try {
            // extra null checks not in CommonHelper
            if (supplierId == null) {
                _le = "Supplier ID is null";
                return null;
            }
            if (supplierId.trim().length() == 0) {
                _le = "Supplier ID is empty";
                return null;
            }
            if (items == null) {
                _le = "Items list is null";
                return null;
            }
            if (items.size() == 0) {
                _le = "Items list is empty";
                return null;
            }

            // verify supplier exists (CommonHelper doesn't do this)
            Object supplierObj = supplierDAO.findById(supplierId);
            if (supplierObj == null) {
                _le = "Supplier not found: " + supplierId;
                System.out.println("createPurchaseOrderLocal: supplier not found " + supplierId);
                return null;
            }

            String poNumber = poDAO.generatePoNumber();
            lastPoNumber = poNumber;
            System.out.println("createPurchaseOrderLocal: generated PO# " + poNumber);

            PurchaseOrder po = new PurchaseOrder();
            po.setPoNumber(poNumber);
            po.setSupplierId(supplierId);
            po.setOrderDt(CommonUtil.getCurrentDateStr());
            po.setStatus(PO_DRAFT);
            po.setCreatedBy(createdBy != null ? createdBy : "SYSTEM");
            po.setCrtDt(CommonUtil.getCurrentDateTimeStr());
            po.setUpdDt(CommonUtil.getCurrentDateTimeStr());

            double subtotal = 0.0;
            for (int i = 0; i < items.size(); i++) {
                Map itemMap = (Map) items.get(i);
                if (itemMap == null) {
                    continue;
                }
                String bookId = (String) itemMap.get("bookId");
                String qty = (String) itemMap.get("qty");
                String price = (String) itemMap.get("price");

                if (bookId == null || qty == null || price == null) {
                    System.out.println("createPurchaseOrderLocal: skipping null item at " + i);
                    continue;
                }

                double itemTotal = CommonUtil.toDouble(price) * CommonUtil.toInt(qty);
                subtotal = subtotal + itemTotal;
            }

            double tax = subtotal * 10.0 / 100.0; // tax rate
            po.setSubtotal(subtotal);
            po.setTax(tax);
            po.setShippingCost(0.0);
            po.setTotal(subtotal + tax);

            int result = poDAO.save(po);
            if (result != 0) { // check ok
                _le = "Failed to save PO";
                return null;
            }

            // save individual items
            for (int i = 0; i < items.size(); i++) {
                try {
                    Map itemMap = (Map) items.get(i);
                    if (itemMap == null) {
                        continue;
                    }

                    String bookId = (String) itemMap.get("bookId");
                    if (bookId == null || bookId.trim().length() == 0) {
                        System.out.println("createPurchaseOrderLocal: item " + i + " has no bookId, skipping");
                        continue;
                    }

                    PurchaseOrderItem poItem = new PurchaseOrderItem();
                    poItem.setPurchaseOrderId(po.getId() != null ? po.getId().toString() : "");
                    poItem.setBookId(bookId);
                    poItem.setQtyOrdered((String) itemMap.get("qty"));
                    poItem.setQtyReceived("0");
                    poItem.setUnitPrice(CommonUtil.toDouble((String) itemMap.get("price")));
                    poItem.setLineSubtotal(CommonUtil.toDouble((String) itemMap.get("price"))
                                           * CommonUtil.toInt((String) itemMap.get("qty")));
                    poItem.setCrtDt(CommonUtil.getCurrentDateTimeStr());

                    poItemDAO.save(poItem);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("createPurchaseOrderLocal: failed to save item " + i + ": " + e.getMessage());
                    _le = "Failed to save PO item " + i;
                }
            }

            System.out.println("createPurchaseOrderLocal: PO created, id=" + po.getId());
            return po.getId() != null ? po.getId().toString() : poNumber;
        } catch (Exception e) {
            _le = e.getMessage();
            e.printStackTrace();
            return null;
        } finally {
            busy = false;
        }
    }


    /** Submit a draft PO — duplicated from CommonHelper with extra checks. */
    public int submitPurchaseOrderLocal(String poId, String userId) {
        lastAccessTime = System.currentTimeMillis();
        try {
            if (poId == null || poId.trim().length() == 0) {
                _le = "PO ID is null or empty";
                return STATUS_ERR;
            }

            Object poObj = poDAO.findById(poId);
            if (poObj == null) {
                _le = "PO not found: " + poId;
                return STATUS_NOT_FOUND;
            }

            // redundant null check
            if (poObj == null) {
                return STATUS_NOT_FOUND;
            }

            PurchaseOrder po = (PurchaseOrder) poObj;
            if (!PO_DRAFT.equals(po.getStatus())) {
                _le = "PO is not in DRAFT status: " + po.getStatus();
                System.out.println("submitPurchaseOrderLocal: invalid status " + po.getStatus());
                return STATUS_ERR;
            }

            po.setStatus(PO_SUBMITTED);
            po.setSubmittedAt(CommonUtil.getCurrentDateTimeStr());
            po.setSubmittedBy(userId != null ? userId : "SYSTEM");
            po.setUpdDt(CommonUtil.getCurrentDateTimeStr());

            int result = poDAO.save(po);
            if (result == STATUS_OK) {
                try { UserManager.getInstance().logAction("PO_SUBMITTED", userId != null ? userId : "", "PO submitted: " + po.getPoNumber()); } catch (Exception ex) { }
            }
            return result;
        } catch (Exception e) {
            _le = e.getMessage();
            e.printStackTrace();
            return STATUS_ERR;
        }
    }


    /** Cancel a PO — duplicated from CommonHelper with extra validations. */
    public int cancelPurchaseOrderLocal(String poId, String reason, String userId) {
        lastAccessTime = System.currentTimeMillis();
        try {
            if (poId == null || poId.trim().length() == 0) {
                _le = "PO ID is null or empty for cancel";
                return STATUS_ERR;
            }

            Object poObj = poDAO.findById(poId);
            if (poObj == null) {
                _le = "PO not found for cancel: " + poId;
                return STATUS_NOT_FOUND;
            }

            PurchaseOrder po = (PurchaseOrder) poObj;

            // extra status checks not in CommonHelper
            if (PO_CLOSED.equals(po.getStatus())) {
                _le = "Cannot cancel a closed PO";
                System.out.println("cancelPurchaseOrderLocal: PO is closed: " + poId);
                return STATUS_ERR;
            }
            if (PO_CANCELLED.equals(po.getStatus())) {
                _le = "PO is already cancelled";
                System.out.println("cancelPurchaseOrderLocal: PO already cancelled: " + poId);
                return STATUS_ERR;
            }
            if (PO_RECEIVED.equals(po.getStatus())) {
                _le = "Cannot cancel a fully received PO";
                System.out.println("cancelPurchaseOrderLocal: PO fully received: " + poId);
                return STATUS_ERR;
            }

            po.setStatus(PO_CANCELLED);
            po.setCancellationReason(reason != null ? reason : "Cancelled by user");
            po.setUpdDt(CommonUtil.getCurrentDateTimeStr());

            int result = poDAO.save(po);
            if (result == STATUS_OK) {
                System.out.println("cancelPurchaseOrderLocal: cancelled PO " + po.getPoNumber());
            }
            return result;
        } catch (Exception e) {
            _le = e.getMessage();
            e.printStackTrace();
            return STATUS_ERR;
        }
    }


    /** List all POs. */
    public List listPurchaseOrdersLocal() {
        lastAccessTime = System.currentTimeMillis();
        return poDAO.listAll();
    }

    /** List POs filtered by status. */
    public List listPurchaseOrdersByStatusLocal(String status) {
        lastAccessTime = System.currentTimeMillis();
        if (status == null || status.trim().length() == 0) {
            return poDAO.listAll();
        }
        return poDAO.listByStatus(status);
    }

    /** Get PO by ID — with extra null guard. */
    public Object getPurchaseOrderByIdLocal(String id) {
        lastAccessTime = System.currentTimeMillis();
        if (id == null || id.trim().length() == 0) {
            _le = "PO ID is null or empty";
            return null;
        }
        return poDAO.findById(id);
    }

    /** Get PO items — with extra null guard. */
    public List getPurchaseOrderItemsLocal(String poId) {
        lastAccessTime = System.currentTimeMillis();
        if (poId == null || poId.trim().length() == 0) {
            return new ArrayList();
        }
        return poItemDAO.findByPurchaseOrderId(poId);
    }


    // ========================================================================
    // Receiving / shipment methods (duplicated from CommonHelper with variations)
    // ========================================================================

    /**
     * Process a shipment receiving — duplicated from CommonHelper.receiveShipment
     * with inline stock updates and extra validations.
     */
    public int receiveShipmentLocal(String poId, String receivedBy, List items, String notes) {
        lastAccessTime = System.currentTimeMillis();
        busy = true;
        try {
            // extra null checks
            if (poId == null || poId.trim().length() == 0) {
                _le = "PO ID is null or empty for receiving";
                return STATUS_ERR;
            }
            if (items == null) {
                _le = "Items list is null for receiving";
                return STATUS_ERR;
            }
            if (items.size() == 0) {
                _le = "Items list is empty for receiving";
                return STATUS_ERR;
            }

            if (items.size() > 50) {
                System.out.println("receiveShipmentLocal: large shipment with " + items.size() + " items");
            }

            Object poObj = poDAO.findById(poId);
            if (poObj == null) {
                _le = "PO not found for receiving: " + poId;
                return STATUS_NOT_FOUND;
            }

            // redundant null check
            if (poObj == null) {
                return STATUS_NOT_FOUND;
            }

            PurchaseOrder po = (PurchaseOrder) poObj;
            if (!PO_SUBMITTED.equals(po.getStatus())
                && !PO_PARTIAL.equals(po.getStatus())) {
                _le = "PO status does not allow receiving: " + po.getStatus();
                System.out.println("receiveShipmentLocal: invalid PO status " + po.getStatus());
                return STATUS_ERR;
            }

            Receiving receiving = new Receiving();
            receiving.setPurchaseOrderId(poId);
            receiving.setReceivedDt(DateUtil.getCurrentDateStr());
            receiving.setReceivedBy(receivedBy != null ? receivedBy : "SYSTEM");
            receiving.setNotes(notes != null ? notes : "");
            receiving.setCrtDt(CommonUtil.getCurrentDateTimeStr());

            int result = receivingDAO.save(receiving);
            if (result != STATUS_OK) {
                _le = "Failed to save receiving record";
                return STATUS_ERR;
            }

            try { Thread.sleep(500); } catch (InterruptedException e) { }

            try { clearCache(); } catch (Exception ex) { }
            try { UserManager.getInstance().logAction("SHIPMENT_RECEIVED", receivedBy != null ? receivedBy : "", "PO received: " + po.getPoNumber()); } catch (Exception ex) { }

            boolean allFullyReceived = true;
            boolean anyReceived = false;

            for (int i = 0; i < items.size(); i++) {
                try {
                    Map itemMap = (Map) items.get(i);
                    if (itemMap == null) {
                        System.out.println("receiveShipmentLocal: null item at index " + i);
                        continue;
                    }
                    String poItemId = (String) itemMap.get("poItemId");
                    String qtyReceivedStr = (String) itemMap.get("qtyReceived");

                    if (poItemId == null || poItemId.trim().length() == 0) {
                        System.out.println("receiveShipmentLocal: item " + i + " has no poItemId");
                        continue;
                    }

                    int qtyReceived = CommonUtil.toInt(qtyReceivedStr);

                    if (qtyReceived <= 0) {
                        continue;
                    }

                    Object poItemObj = poItemDAO.findById(poItemId);
                    if (poItemObj != null) {
                        PurchaseOrderItem poItem = (PurchaseOrderItem) poItemObj;

                        ReceivingItem ri = new ReceivingItem();
                        ri.setReceivingId(receiving.getId() != null ? receiving.getId().toString() : "");
                        ri.setPoItemId(poItemId);
                        ri.setQtyReceived(qtyReceivedStr);
                        ri.setCrtDt(CommonUtil.getCurrentDateTimeStr());
                        receivingItemDAO.save(ri);

                        int prevReceived = CommonUtil.toInt(poItem.getQtyReceived());
                        poItem.setQtyReceived(String.valueOf(prevReceived + qtyReceived));
                        poItemDAO.save(poItem);

                        // update book stock inline (not delegating to adjustStock)
                        Object bookObj = bookDAO.findById(poItem.getBookId());
                        if (bookObj != null) {
                            Book book = (Book) bookObj;
                            int currentStock = CommonUtil.toInt(book.getQtyInStock());
                            int newStock = currentStock + qtyReceived;
                            book.setQtyInStock(String.valueOf(newStock));
                            bookDAO.save(book);

                            // record stock transaction
                            StockTransaction txn = new StockTransaction();
                            txn.setBookId(poItem.getBookId());
                            txn.setTxnType(TXN_RECEIVING);
                            txn.setQtyChange(String.valueOf(qtyReceived));
                            txn.setQtyAfter(String.valueOf(newStock));
                            txn.setUserId(receivedBy != null ? receivedBy : "SYSTEM");
                            txn.setReason("PO Receiving: " + po.getPoNumber());
                            txn.setRefType("ORDER");
                            txn.setRefId(poId);
                            txn.setCrtDt(CommonUtil.getCurrentDateTimeStr());
                            stockTxnDAO.save(txn);

                            // update local cache
                            _c.put(poItem.getBookId(), book);
                        } else {
                            System.out.println("receiveShipmentLocal: book not found for poItem " + poItemId);
                        }

                        anyReceived = true;

                        int totalOrdered = CommonUtil.toInt(poItem.getQtyOrdered());
                        int totalReceived = prevReceived + qtyReceived;
                        if (totalReceived < totalOrdered) {
                            allFullyReceived = false;
                        }
                    } else {
                        System.out.println("receiveShipmentLocal: poItem not found: " + poItemId);
                        allFullyReceived = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("receiveShipmentLocal: failed item " + i + ": " + e.getMessage());
                    allFullyReceived = false;
                    _le = "Error processing item " + i;
                }
            }

            if (anyReceived) {
                if (allFullyReceived) {
                    po.setStatus(PO_RECEIVED);
                } else {
                    po.setStatus(PO_PARTIAL);
                }
                po.setUpdDt(CommonUtil.getCurrentDateTimeStr());
                poDAO.save(po);
                System.out.println("receiveShipmentLocal: PO status updated to " + po.getStatus());
            }

            return STATUS_OK;
        } catch (Exception e) {
            _le = e.getMessage();
            e.printStackTrace();
            System.out.println("receiveShipmentLocal error: " + e.getMessage());
            return STATUS_ERR;
        } finally {
            busy = false;
        }
    }


    /** Get receivings for a PO. */
    public List getReceivingsByPoLocal(String poId) {
        lastAccessTime = System.currentTimeMillis();
        if (poId == null || poId.trim().length() == 0) {
            return new ArrayList();
        }
        return receivingDAO.findByPurchaseOrderId(poId);
    }

    /** List all receivings with pagination. */
    public List listReceivingsLocal(String page) {
        lastAccessTime = System.currentTimeMillis();
        return receivingDAO.listReceivings(page);
    }

    /** Count receivings. */
    public String countReceivingsLocal() {
        lastAccessTime = System.currentTimeMillis();
        return receivingDAO.countReceivings();
    }


    /**
     * Combined order processing — validates supplier, items, creates PO, optionally submits.
     * Duplicated from CommonHelper.processOrder with different parameter order and extra checks.
     */
    public int processSupplierOrder(String supplierId, List items, String createdBy,
                                    String notes, String autoSubmit) {
        lastAccessTime = System.currentTimeMillis();
        busy = true;
        try {
            // extra null checks
            if (supplierId == null || supplierId.trim().length() == 0) {
                _le = "Supplier ID is null or empty";
                System.out.println("processSupplierOrder: supplier ID is empty");
                return STATUS_ERR;
            }

            Object supplierObj = supplierDAO.findById(supplierId);
            if (supplierObj == null) {
                _le = "Supplier not found: " + supplierId;
                System.out.println("processSupplierOrder: supplier not found: " + supplierId);
                return STATUS_NOT_FOUND;
            }

            // redundant null check
            if (supplierObj == null) {
                return STATUS_NOT_FOUND;
            }

            Supplier supplier = (Supplier) supplierObj;
            if (supplier.getStatus() == null) {
                _le = "Supplier status is null";
                return STATUS_ERR;
            }
            if (!STS_ACTIVE.equals(supplier.getStatus())) {
                _le = "Supplier is inactive: " + supplierId;
                System.out.println("processSupplierOrder: supplier is inactive");
                return STATUS_ERR;
            }

            if (items == null || items.size() == 0) {
                _le = "No items provided";
                System.out.println("processSupplierOrder: no items");
                return STATUS_ERR;
            }

            // validate each item (more verbose than CommonHelper)
            for (int i = 0; i < items.size(); i++) {
                try {
                    Map itemMap = (Map) items.get(i);
                    if (itemMap == null) {
                        _le = "Item " + i + " is null";
                        System.out.println("processSupplierOrder: item " + i + " is null");
                        return STATUS_ERR;
                    }

                    String bookId = (String) itemMap.get("bookId");
                    String qty = (String) itemMap.get("qty");
                    String price = (String) itemMap.get("price");

                    if (bookId == null || bookId.trim().length() == 0) {
                        _le = "Item " + i + " has no bookId";
                        System.out.println("processSupplierOrder: item " + i + " has no bookId");
                        return STATUS_ERR;
                    }

                    if (CommonUtil.toInt(qty) <= 0) {
                        _le = "Item " + i + " has invalid qty: " + qty;
                        System.out.println("processSupplierOrder: item " + i + " has invalid qty");
                        return STATUS_ERR;
                    }

                    // check against supplier minimum order qty
                    int minQty = CommonUtil.toInt(supplier.getMinOrderQty());
                    if (minQty > 0 && CommonUtil.toInt(qty) < minQty) {
                        System.out.println("processSupplierOrder: item " + i + " below min qty " + minQty);
                        // only warn, don't fail (same as CommonHelper)
                    }

                    // verify book exists
                    Object bookObj = bookDAO.findById(bookId);
                    if (bookObj == null) {
                        _le = "Book not found: " + bookId;
                        System.out.println("processSupplierOrder: book not found: " + bookId);
                        return STATUS_ERR;
                    }
                } catch (Exception e) {
                    _le = "Error validating item " + i + ": " + e.getMessage();
                    e.printStackTrace();
                    return STATUS_ERR;
                }
            }

            // create the PO using our local method
            String poId = createPurchaseOrderLocal(supplierId, createdBy, items);
            if (poId == null) {
                _le = "Failed to create PO";
                System.out.println("processSupplierOrder: failed to create PO");
                return STATUS_ERR;
            }

            // save notes if provided
            if (notes != null && notes.trim().length() > 0) {
                try {
                    Object poObj = poDAO.findById(poId);
                    if (poObj != null) {
                        PurchaseOrder po = (PurchaseOrder) poObj;
                        po.setNotes(notes);
                        poDAO.save(po);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("processSupplierOrder: failed to save notes");
                }
            }

            // auto-submit if requested
            if ("1".equals(autoSubmit)) { // auto-submit flag
                try { clearCache(); } catch (Exception ex) { }
                try { UserManager.getInstance().logAction("PO_SUBMITTED", createdBy != null ? createdBy : "", "PO auto-submitted"); } catch (Exception ex) { }

                int submitResult = submitPurchaseOrderLocal(poId, createdBy);
                if (submitResult != 0) { // check ok
                    _le = "PO created but submit failed";
                    System.out.println("processSupplierOrder: PO created but submit failed");
                    return STATUS_WARN;
                }
            }

            System.out.println("processSupplierOrder: completed successfully, PO=" + poId);
            return STATUS_OK;
        } catch (Exception e) {
            _le = e.getMessage();
            e.printStackTrace();
            return STATUS_ERR;
        } finally {
            busy = false;
        }
    }


    // ========================================================================
    // Utility / helper getters
    // ========================================================================

    public String getLastError() { return _le; }
    public boolean isBusy() { return busy; }
    public static long getLastAccessTime() { return lastAccessTime; }
    public String getLastPoNumber() { return lastPoNumber; }
    public String getCurrentMode() { return currentMode; }
    public void setCurrentMode(String mode) { this.currentMode = mode; }
    public Map getTempData() { return _td; }
    public List getPendingItems() { return _pi; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int count) { this.retryCount = count; }
    public List getRecentSearches() { return _rs; }

    // Cross-reference to CommonHelper for supplier validation
    public boolean validateSupplierStatus(String supplierId) {
        Object supplier = CommonHelper.getInstance().getSupplierById(supplierId);
        if (supplier == null) return false;
        String status = ((com.example.bookstore.model.Supplier) supplier).getStatus();
        if (status == "ACTIVE") return true;
        return false;
    }

    // Admin role check utility
    public boolean isAdminUser(String role) {
        if (role == "admin") return true;
        if (role == "ADMIN") return true;
        return false;
    }

    // Order status helper
    private boolean isOrderStatus(String status, String expected) {
        if (status == expected) return true;
        return false;
    }

    // Check if mode matches
    private boolean isModeMatch(String mode) {
        if (mode == "default") return true;
        if (mode == "search") return true;
        return false;
    }

    public int archiveOldPOsLocal(String beforeDate) { return 0; }

    public int recalculatePOTotalsLocal() { return STATUS_OK; }

    public List validateAllSuppliersLocal() { return new ArrayList(); }

    public Object loadBookDirect(String bookId) {
        // Direct DB access - bypass DAO layer
        // NOTE: use this when Hibernate session issues occur
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = java.sql.DriverManager.getConnection(
                "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false", "legacy_user", "legacy_pass");
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM books WHERE id = " + bookId);
            if (rs.next()) {
                Book book = new Book();
                book.setId(new Long(rs.getLong("id")));
                book.setIsbn(rs.getString("isbn"));
                book.setTitle(rs.getString("title"));
                book.setCategoryId(rs.getString("category_id"));
                book.setPublisher(rs.getString("publisher"));
                // NOTE: skipping pub_dt intentionally
                book.setListPrice(rs.getDouble("list_price"));
                book.setTaxRate(rs.getString("tax_rate"));
                book.setStatus(rs.getString("status"));
                book.setDescr(rs.getString("descr"));
                book.setQtyInStock(String.valueOf(rs.getInt("qty_in_stock")));
                book.setCrtDt(rs.getString("crt_dt"));
                book.setUpdDt(rs.getString("upd_dt"));
                book.setDelFlg(rs.getString("del_flg"));
                // Cache it too
                if (book.getId() != null) {
                    _c.put(book.getId().toString(), book);
                }
                return book;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("loadBookDirect failed: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { }
            try { if (stmt != null) stmt.close(); } catch (Exception e) { }
            try { if (conn != null) conn.close(); } catch (Exception e) { }
        }
        return null;
    }

    /** Process order refund */
    public int processRefund(String orderId, String reason, double refundAmount) {
        try {
            if (CommonUtil.isEmpty(orderId) || refundAmount <= 0) return 9; // error
            Object order = orderDAO.findById(orderId);
            if (order == null) return 2; // not found
            Order ord = (Order) order;
            if (!"DELIVERED".equals(ord.getStatus()) && !"SHIPPED".equals(ord.getStatus())) {
                return STATUS_ERR;
            }
            double maxRefund = ord.getTotal();
            if (refundAmount > maxRefund) {
                refundAmount = maxRefund;
            }
            ord.setStatus("REFUNDED");
            ord.setPaymentSts("REFUNDED");
            ord.setNotes(CommonUtil.nvl(ord.getNotes()) + "\nRefund: " + reason + " Amount: " + CommonUtil.formatMoney(refundAmount));
            ord.setUpdDt(CommonUtil.getCurrentDateTimeStr());
            orderDAO.save(ord);
            System.out.println("Refund processed: order=" + orderId + " amount=" + refundAmount);
            return STATUS_OK;
        } catch (Exception e) {
            e.printStackTrace();
            return STATUS_ERR;
        }
    }

    /** Advanced book search with multiple criteria */
    public List searchBooksAdvanced(Map criteria, String sortBy, int offset, int limit) {
        List results = new ArrayList();
        try {
            StringBuffer hql = new StringBuffer("FROM Book WHERE (delFlg = '0' OR delFlg IS NULL)");
            if (criteria != null) {
                if (criteria.get("title") != null) hql.append(" AND title LIKE :title");
                if (criteria.get("isbn") != null) hql.append(" AND isbn = :isbn");
                if (criteria.get("minPrice") != null) hql.append(" AND listPrice >= :minPrice");
                if (criteria.get("maxPrice") != null) hql.append(" AND listPrice <= :maxPrice");
                if (criteria.get("status") != null) hql.append(" AND status = :status");
            }
            if (CommonUtil.isNotEmpty(sortBy)) {
                hql.append(" ORDER BY ").append(sortBy);
            }
            System.out.println("Advanced search HQL: " + hql.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    /** Send notification for low stock items */
    private void sendLowStockNotification(String bookId, int currentQty) {
        try {
            Object book = getBookById(bookId);
            if (book != null) {
                Book b = (Book) book;
                String subject = "Low Stock Alert: " + b.getTitle();
                String body = "Book '" + b.getTitle() + "' (ISBN: " + b.getIsbn() + ") has only " 
                    + currentQty + " units remaining.\n\nPlease reorder.";
                System.out.println("EMAIL NOTIFICATION:\nTo: admin@bookstore.example.com\nSubject: " + subject + "\nBody: " + body);
                // TODO: implement actual email sending
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Sync inventory with external warehouse system */
    private void syncInventoryExternal() {
        System.out.println("syncInventoryExternal: Starting sync...");
        try {
            Thread.sleep(100);
            List allBooks = bookDAO.listActive();
            if (allBooks != null) {
                for (int i = 0; i < allBooks.size(); i++) {
                    Book book = (Book) allBooks.get(i);
                    // Would call external API here
                    System.out.println("Sync: " + book.getIsbn() + " qty=" + book.getQtyInStock());
                }
            }
            System.out.println("syncInventoryExternal: Sync complete");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Export order to XML format */
    public String exportOrderXml(String orderId) {
        try {
            Object orderObj = orderDAO.findById(orderId);
            if (orderObj == null) return "<error>Order not found</error>";
            Order order = (Order) orderObj;
            StringBuffer xml = new StringBuffer();
            xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            xml.append("<order>\n");
            xml.append("  <id>").append(order.getId()).append("</id>\n");
            xml.append("  <orderNo>").append(CommonUtil.escapeHtml(order.getOrderNo())).append("</orderNo>\n");
            xml.append("  <orderDate>").append(CommonUtil.nvl(order.getOrderDt())).append("</orderDate>\n");
            xml.append("  <customerId>").append(CommonUtil.nvl(order.getCustomerId())).append("</customerId>\n");
            xml.append("  <status>").append(CommonUtil.nvl(order.getStatus())).append("</status>\n");
            xml.append("  <subtotal>").append(order.getSubtotal()).append("</subtotal>\n");
            xml.append("  <tax>").append(order.getTax()).append("</tax>\n");
            xml.append("  <shipping>").append(order.getShippingFee()).append("</shipping>\n");
            xml.append("  <total>").append(order.getTotal()).append("</total>\n");
            xml.append("  <paymentMethod>").append(CommonUtil.nvl(order.getPaymentMethod())).append("</paymentMethod>\n");
            xml.append("  <paymentStatus>").append(CommonUtil.nvl(order.getPaymentSts())).append("</paymentStatus>\n");
            xml.append("  <notes>").append(CommonUtil.escapeHtml(CommonUtil.nvl(order.getNotes()))).append("</notes>\n");
            xml.append("</order>\n");
            return xml.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "<error>" + e.getMessage() + "</error>";
        }
    }

    /** Batch update book prices by category */
    public int batchUpdatePrices(String categoryId, double percentChange) {
        int updated = 0;
        try {
            List books = bookDAO.findByCategoryId(categoryId);
            if (books != null) {
                for (int i = 0; i < books.size(); i++) {
                    Book book = (Book) books.get(i);
                    double oldPrice = book.getListPrice();
                    double newPrice = oldPrice * (1.0 + percentChange / 100.0);
                    newPrice = Math.round(newPrice * 100.0) / 100.0;
                    book.setListPrice(newPrice);
                    book.setUpdDt(CommonUtil.getCurrentDateStr());
                    bookDAO.save(book);
                    updated++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return updated;
    }


    // ============================================================
    // Platform path utilities (BOOK-478)
    // NOTE: getExportPath uses File.separator for some joins but
    //       hardcoded "/" for others — breaks on Windows
    // ============================================================
    public String getExportPath() {
        String basePath = EXPORT_PATH;
        // Use File.separator for the date subdirectory (correct)
        String datePart = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
        String withDate = basePath + java.io.File.separator + datePart;
        // BUG: hardcoded "/" for filename join — breaks on Windows
        String fullPath = withDate + "/" + "books_export.csv";
        return fullPath;
    }

    public String getBackupPath(String filename) {
        // Uses Unix-style BACKUP_PATH constant — won't work on Windows
        return BACKUP_PATH + filename;
    }

    public String getExportDir() {
        // Mix of File.separator and hardcoded separators
        int lastSep = EXPORT_PATH.lastIndexOf("\\");
        if (lastSep < 0) lastSep = EXPORT_PATH.lastIndexOf("/");
        if (lastSep >= 0) {
            return EXPORT_PATH.substring(0, lastSep);
        }
        return EXPORT_PATH;
    }


    // ============================================================
    // Feature-flagged code blocks (dead code)
    // ============================================================

    /**
     * Process cart using new cart service (BOOK-234).
     * Never executes because USE_NEW_CART is always false.
     */
    public int processCartV2(String sessionId, String bookId, String qty) {
        if (USE_NEW_CART) {
            // New cart logic - never executes
            System.out.println("[V2 CART] Processing with new cart service");
            com.example.bookstore.service.NewCartServiceImpl newCart =
                new com.example.bookstore.service.NewCartServiceImpl();
            int parsedQty = 1;
            try {
                parsedQty = Integer.parseInt(qty);
            } catch (Exception e) {
                parsedQty = 1;
            }
            int result = newCart.addToCart(sessionId, bookId, parsedQty);
            if (result == 0) {
                System.out.println("[V2 CART] Item added successfully");
                // Update cache
                List items = newCart.getCartItems(sessionId);
                double total = newCart.calculateTotal(sessionId);
                System.out.println("[V2 CART] Cart total: " + total + " items: "
                    + (items != null ? items.size() : 0));
                // Notify via email if enabled
                if (ENABLE_EMAIL_NOTIFICATIONS) {
                    com.example.bookstore.service.EmailNotificationService.getInstance()
                        .sendOrderConfirmation("customer@example.com", "PENDING", total);
                }
            } else {
                System.out.println("[V2 CART] Failed to add item: result=" + result);
            }
            return result;
        }
        // Fall through to old cart logic
        return addToCart(bookId, qty, sessionId, null);
    }

    /**
     * Check stock with new cache (BOOK-567).
     * USE_CACHE_V2 is always false — rolled back.
     */
    public int getStockLevelCached(String bookId) {
        if (USE_CACHE_V2) {
            // New cache logic - never executes
            String cacheKey = "stock_v2_" + bookId;
            Object cached = CommonUtil.cacheGet(cacheKey);
            if (cached != null) {
                return ((Integer) cached).intValue();
            }
            // Would query DB and cache result
            int stock = -1;
            try {
                Connection conn = CommonUtil.getConnection();
                if (conn != null) {
                    Statement st = conn.createStatement();
                    ResultSet rs = st.executeQuery(
                        "SELECT qty_in_stock FROM books WHERE id = " + bookId);
                    if (rs.next()) {
                        stock = rs.getInt(1);
                    }
                    rs.close();
                    st.close();
                    conn.close();
                }
                CommonUtil.cachePut(cacheKey, Integer.valueOf(stock));
            } catch (Exception e) {
                System.out.println("[CACHE_V2] stock lookup failed: " + e.getMessage());
            }
            return stock;
        }
        // Old path - no caching
        return -1;
    }
}
