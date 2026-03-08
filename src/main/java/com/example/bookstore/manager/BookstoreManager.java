package com.example.bookstore.manager;

import java.util.*;
import java.io.*;
import java.sql.Date;
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

public class BookstoreManager implements AppConstants {

    private static BookstoreManager instance = new BookstoreManager();

    private BookDAO bookDAO = new BookDAOImpl();
    private CategoryDAO categoryDAO = new CategoryDAOImpl();
    private OrderDAO orderDAO = new OrderDAOImpl();
    private ShoppingCartDAO cartDAO = new ShoppingCartDAOImpl();
    private StockTransactionDAO stockTxnDAO = new StockTransactionDAOImpl();
    private ReportDAO reportDAO = new ReportDAOImpl();

    private List lastSearchResults;
    private Map bookCache = new HashMap();
    private String lastProcessedOrderId;
    private int orderCount = 0;

    // supplier / purchase-order / receiving DAOs (duplicated from CommonHelper)
    private SupplierDAO supplierDAO = new SupplierDAOImpl();
    private PurchaseOrderDAO poDAO = new PurchaseOrderDAOImpl();
    private PurchaseOrderItemDAO poItemDAO = new PurchaseOrderItemDAOImpl();
    private ReceivingDAO receivingDAO = new ReceivingDAOImpl();
    private ReceivingItemDAO receivingItemDAO = new ReceivingItemDAOImpl();
    private CustomerDAO customerDAO = new CustomerDAOImpl();

    // temp / state fields
    private Map tempData = new HashMap();
    private List pendingItems = new ArrayList();
    private String lastError = null;
    private int retryCount = 0;
    private boolean initialized = false;
    private static long lastAccessTime = 0;
    private String currentMode = "default";
    private Object lock = new Object();
    private volatile boolean busy = false;
    private Map statsCache = new HashMap();
    private List recentSearches = new ArrayList();
    private Map supplierCacheLocal = new HashMap();
    private String lastPoNumber;

    private BookstoreManager() {
    }

    public static BookstoreManager getInstance() {
        return instance;
    }

    
    public List searchBooks(String isbn, String title, String author, String catId,
                            String page, String mode, HttpServletRequest request) {
        lastAccessTime = System.currentTimeMillis();
        List results = null;
        try {
            if (CommonUtil.isNotEmpty(isbn)) {
                Object book = bookDAO.findByIsbn(isbn);
                if (book != null) {
                    results = new ArrayList();
                    results.add(book);
                }
            } else if (CommonUtil.isNotEmpty(title)) {
                results = bookDAO.findByTitle(title);
            } else if (CommonUtil.isNotEmpty(catId)) {
                results = bookDAO.findByCategoryId(catId);
            } else {
                results = bookDAO.listActive();
            }

            if (results != null && results.size() > 100) {
                results = results.subList(0, 100);
            }

            lastSearchResults = results;
            if (results != null) {
                for (int i = 0; i < results.size(); i++) {
                    Book b = (Book) results.get(i);
                    if (b.getId() != null) {
                        bookCache.put(b.getId().toString(), b);
                    }
                }
            }

            if (request != null) {
                HttpSession session = request.getSession();
                session.setAttribute(SEARCH_RESULT, results);
                session.setAttribute(SEARCH_CRITERIA, title != null ? title : isbn);
            }

            try { UserManager.getInstance().logAction("BOOK_SEARCH", "system", "Search performed"); } catch (Exception ex) {  }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in searchBooks: " + e.getMessage());
        }
        return results;
    }

    
    public Object getBookById(String bookId) {
        lastAccessTime = System.currentTimeMillis();
        if (bookCache.containsKey(bookId)) {
            return bookCache.get(bookId);
        }
        Object book = bookDAO.findById(bookId);
        if (book != null) {
            bookCache.put(bookId, book);
        }
        return book;
    }

    
    public List listCategories() {
        return categoryDAO.listAll();
    }

    
    public int addToCart(String bookId, String qty, String sessionId, HttpServletRequest request) {
        lastAccessTime = System.currentTimeMillis();
        try {
            if (CommonUtil.isEmpty(bookId) || CommonUtil.isEmpty(qty)) {
                return STATUS_ERR;
            }

            int quantity = CommonUtil.toInt(qty);
            if (quantity <= 0) {
                return STATUS_ERR;
            }

            Object book = getBookById(bookId);
            if (book == null) {
                return STATUS_NOT_FOUND;
            }

            List cartItems = cartDAO.findBySessionId(sessionId);
            if (cartItems != null) {
                for (int i = 0; i < cartItems.size(); i++) {
                    ShoppingCart item = (ShoppingCart) cartItems.get(i);
                    if (bookId.equals(item.getBookId())) {

                        int existing = CommonUtil.toInt(item.getQty());
                        item.setQty(String.valueOf(existing + quantity));
                        cartDAO.save(item);

                        if (request != null) {
                            request.getSession().setAttribute(CART, cartDAO.findBySessionId(sessionId));
                        }
                        return STATUS_OK;
                    }
                }
            }

            ShoppingCart cartItem = new ShoppingCart();
            cartItem.setSessionId(sessionId);
            cartItem.setBookId(bookId);
            cartItem.setQty(String.valueOf(quantity));
            cartItem.setCrtDt(CommonUtil.getCurrentDateStr());
            cartItem.setUpdDt(CommonUtil.getCurrentDateStr());
            cartDAO.save(cartItem);

            if (request != null) {
                request.getSession().setAttribute(CART, cartDAO.findBySessionId(sessionId));
            }
            return STATUS_OK;
        } catch (Exception e) {
            e.printStackTrace();
            return STATUS_ERR;
        }
    }

    
    public List getCartItems(String sessionId) {
        return cartDAO.findBySessionId(sessionId);
    }

    
    public int updateCartQty(String cartId, String qty) {
        try {
            int newQty = CommonUtil.toInt(qty);
            if (newQty <= 0) {
                return STATUS_ERR;
            }

            return STATUS_OK;
        } catch (Exception e) {
            e.printStackTrace();
            return STATUS_ERR;
        }
    }

    
    public int removeFromCart(String cartId) {
        try {

            return STATUS_OK;
        } catch (Exception e) {
            e.printStackTrace();
            return STATUS_ERR;
        }
    }

    
    public int clearCart(String sessionId) {
        return cartDAO.deleteBySessionId(sessionId);
    }

    
    public double calculateTotal(String sessionId) {
        double total = 0.0;
        try {
            List cartItems = cartDAO.findBySessionId(sessionId);
            if (cartItems != null) {
                for (int i = 0; i < cartItems.size(); i++) {
                    ShoppingCart item = (ShoppingCart) cartItems.get(i);
                    Object bookObj = getBookById(item.getBookId());
                    if (bookObj != null) {
                        Book book = (Book) bookObj;
                        int qty = CommonUtil.toInt(item.getQty());
                        double price = book.getListPrice();

                        double taxRate = CommonUtil.toDouble(book.getTaxRate()) / 100.0;
                        double itemTotal = price * qty * (1.0 + taxRate);
                        total = total + itemTotal;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    
    public int placeOrder(String sessionId, String customerId, String email,
                          String payMethod, String shipName, String shipAddr,
                          String shipCity, String shipState, String shipZip,
                          String shipCountry, String shipPhone, String notes,
                          HttpServletRequest request) {
        lastAccessTime = System.currentTimeMillis();
        try {

            try { Thread.sleep(300); } catch (InterruptedException e) { }

            List cartItems = cartDAO.findBySessionId(sessionId);
            if (cartItems == null || cartItems.size() == 0) {
                return STATUS_ERR;
            }

            Order order = new Order();
            order.setCustomerId(customerId);
            order.setGuestEmail(email);
            order.setOrderNo(CommonUtil.generateId());
            order.setOrderDt(CommonUtil.getCurrentDateTimeStr());
            order.setStatus(ORDER_PENDING);
            order.setPaymentMethod(payMethod);
            order.setPaymentSts(PAY_PENDING);
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

            double subtotal = 0.0;
            double taxTotal = 0.0;
            for (int i = 0; i < cartItems.size(); i++) {
                ShoppingCart item = (ShoppingCart) cartItems.get(i);
                Object bookObj = getBookById(item.getBookId());
                if (bookObj != null) {
                    Book book = (Book) bookObj;
                    int qty = CommonUtil.toInt(item.getQty());
                    double price = book.getListPrice();
                    double itemSubtotal = price * qty;
                    subtotal = subtotal + itemSubtotal;

                    double taxRate = CommonUtil.toDouble(book.getTaxRate());
                    taxTotal = taxTotal + (itemSubtotal * taxRate / 100.0);
                }
            }

            order.setSubtotal(subtotal);
            order.setTax(taxTotal);
            order.setShippingFee(0.0);
            order.setTotal(subtotal + taxTotal);

            int result = orderDAO.save(order);
            if (result != STATUS_OK) {
                return STATUS_ERR;
            }

            for (int i = 0; i < cartItems.size(); i++) {
                ShoppingCart cartItem = (ShoppingCart) cartItems.get(i);
                Object bookObj = getBookById(cartItem.getBookId());
                if (bookObj != null) {
                    Book book = (Book) bookObj;
                    int qty = CommonUtil.toInt(cartItem.getQty());

                    OrderItem oi = new OrderItem();
                    oi.setOrderId(order.getId() != null ? order.getId().toString() : "");
                    oi.setBookId(cartItem.getBookId());
                    oi.setQty(cartItem.getQty());
                    oi.setUnitPrice(book.getListPrice());
                    oi.setDiscount(0.0);
                    oi.setSubtotal(book.getListPrice() * qty);
                    oi.setCrtDt(CommonUtil.getCurrentDateTimeStr());

                    int currentStock = CommonUtil.toInt(book.getQtyInStock());
                    int newStock = currentStock - qty;
                    book.setQtyInStock(String.valueOf(newStock));
                    bookDAO.save(book);

                    StockTransaction txn = new StockTransaction();
                    txn.setBookId(cartItem.getBookId());
                    txn.setTxnType(TXN_SALE);
                    txn.setQtyChange(String.valueOf(-qty));
                    txn.setQtyAfter(String.valueOf(newStock));
                    txn.setUserId(customerId != null ? customerId : "SYSTEM");
                    txn.setReason("Order: " + order.getOrderNo());
                    txn.setRefType("ORDER");
                    txn.setRefId(order.getId() != null ? order.getId().toString() : "");
                    txn.setCrtDt(CommonUtil.getCurrentDateTimeStr());
                    stockTxnDAO.save(txn);
                }
            }

            clearCart(sessionId);

            if (request != null) {
                request.getSession().setAttribute("lastOrder", order);
                request.getSession().setAttribute(MSG, "Order placed successfully");
            }

            lastProcessedOrderId = order.getId() != null ? order.getId().toString() : "";
            orderCount++;

            try { UserManager.getInstance().logAction("ORDER_PLACED", customerId != null ? customerId : "", "Order placed: " + order.getOrderNo()); } catch (Exception ex) {  }

            return STATUS_OK;
        } catch (Exception e) {
            e.printStackTrace();

            return STATUS_ERR;
        }
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
                return STATUS_ERR;
            }

            int quantity = CommonUtil.toInt(qty);
            if (quantity <= 0) {
                return STATUS_ERR;
            }

            Object bookObj = getBookById(bookId);
            if (bookObj == null) {
                return STATUS_NOT_FOUND;
            }

            Book book = (Book) bookObj;
            int currentStock = CommonUtil.toInt(book.getQtyInStock());
            int newStock;

            if (ADJ_INCREASE.equals(adjType)) {
                newStock = currentStock + quantity;
            } else if (ADJ_DECREASE.equals(adjType)) {
                newStock = currentStock - quantity;
                if (newStock < 0) {
                    return STATUS_ERR;
                }
            } else {
                return STATUS_ERR;
            }

            book.setQtyInStock(String.valueOf(newStock));
            bookDAO.save(book);

            StockTransaction txn = new StockTransaction();
            txn.setBookId(bookId);
            txn.setTxnType(ADJ_INCREASE.equals(adjType) ? TXN_CORRECTION : TXN_CORRECTION);
            txn.setQtyChange(String.valueOf(ADJ_INCREASE.equals(adjType) ? quantity : -quantity));
            txn.setQtyAfter(String.valueOf(newStock));
            txn.setUserId(userId);
            txn.setReason(reason);
            txn.setNotes(notes);
            txn.setRefType("ADJUSTMENT");
            txn.setCrtDt(CommonUtil.getCurrentDateTimeStr());
            stockTxnDAO.save(txn);

            System.out.println("Stock adjusted: book=" + bookId + " qty=" + quantity + " type=" + adjType);

            try { UserManager.getInstance().logAction("STOCK_ADJUST", userId, "Stock adjusted for book: " + bookId); } catch (Exception ex) {  }

            if (request != null) {
                request.getSession().setAttribute(MSG, "Stock adjusted successfully");
            }

            return STATUS_OK;
        } catch (Exception e) {
            e.printStackTrace();
            return STATUS_ERR;
        }
    }

    
    public List getLowStockBooks(String threshold) {
        return bookDAO.findLowStock(threshold != null ? threshold : String.valueOf(LOW_STOCK_THRESHOLD));
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
                topN = String.valueOf(DEFAULT_TOP_N);
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
        bookCache.clear();
        lastSearchResults = null;
    }

    
    public int recalculateAllTotals() { System.out.println("recalculateAllTotals called"); return STATUS_OK; }

    
    public String exportAllBooksCsv() { return ""; }

    
    public int purgeOldCarts(int daysOld) { return 0; }

    
    public int migrateOrderData() { return STATUS_ERR; }


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
        lastError = null;
        Object result = null;
        try {
            if (type == 0) {
                // search by title
                if (val == null) {
                    lastError = "Search value is null";
                    return null;
                }
                if (val.trim().length() == 0) {
                    lastError = "Search value is empty";
                    return null;
                }
                result = bookDAO.findByTitle(val);
                if (result != null) {
                    recentSearches.add(val);
                    if (recentSearches.size() > 50) {
                        recentSearches = new ArrayList(recentSearches.subList(recentSearches.size() - 50, recentSearches.size()));
                    }
                }
            } else if (type == 1) {
                // get by id
                if (id == null || id.trim().length() == 0) {
                    lastError = "Book ID is null or empty";
                    return null;
                }
                result = getBookById(id);
                if (result == null) {
                    lastError = "Book not found: " + id;
                }
            } else if (type == 2) {
                // save book
                if (params == null) {
                    lastError = "Params map is null for save";
                    return Integer.valueOf(STATUS_ERR);
                }
                Object bookObj = params.get("book");
                if (bookObj == null) {
                    lastError = "No book object in params";
                    return Integer.valueOf(STATUS_ERR);
                }
                if (bookObj instanceof Book) {
                    Book b = (Book) bookObj;
                    if (b.getTitle() == null || b.getTitle().trim().length() == 0) {
                        lastError = "Book title is empty";
                        return Integer.valueOf(STATUS_ERR);
                    }
                    int saveResult = bookDAO.save(b);
                    bookCache.put(b.getId() != null ? b.getId().toString() : "", b);
                    result = Integer.valueOf(saveResult);
                } else {
                    lastError = "Invalid book object type";
                    return Integer.valueOf(STATUS_ERR);
                }
            } else if (type == 3) {
                // delete - not supported yet
                lastError = "Delete not implemented";
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
                            bookCache.put(b.getId().toString(), b);
                        }
                    }
                }
            } else if (type == 5) {
                // find by ISBN
                if (val == null) {
                    lastError = "ISBN is null";
                    return null;
                }
                if (val.trim().length() == 0) {
                    lastError = "ISBN is empty";
                    return null;
                }
                result = bookDAO.findByIsbn(val);
                if (result == null) {
                    lastError = "Book not found for ISBN: " + val;
                } else {
                    Book b = (Book) result;
                    bookCache.put(b.getId() != null ? b.getId().toString() : "", b);
                }
            } else if (type == 6) {
                // find by category
                if (val == null) {
                    return null;
                }
                result = bookDAO.findByCategoryId(val);
            } else if (type == 7) {
                // low stock
                String threshold = val != null ? val : String.valueOf(LOW_STOCK_THRESHOLD);
                result = getLowStockBooks(threshold);
            } else if (type == 8) {
                // out of stock
                result = getOutOfStockBooks();
            } else if (type == 9) {
                // count
                result = Integer.valueOf(getBookCount());
            } else {
                lastError = "Unknown type: " + type;
                System.out.println("processBookAction: unknown type " + type);
                result = null;
            }
        } catch (Exception e) {
            lastError = e.getMessage();
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
                lastError = "Cart operation is null";
                return STATUS_ERR;
            }
            if (op.trim().length() == 0) {
                lastError = "Cart operation is empty";
                return STATUS_ERR;
            }

            String operation = op.trim().toLowerCase();

            if ("add".equals(operation)) {
                if (p1 == null || p1.trim().length() == 0) {
                    lastError = "Book ID is required for add";
                    return STATUS_ERR;
                }
                if (p2 == null || p2.trim().length() == 0) {
                    p2 = "1"; // default qty
                }
                if (p3 == null || p3.trim().length() == 0) {
                    lastError = "Session ID is required for add";
                    return STATUS_ERR;
                }
                retVal = addToCart(p1, p2, p3, r);
            } else if ("remove".equals(operation)) {
                if (p1 == null || p1.trim().length() == 0) {
                    lastError = "Cart ID is required for remove";
                    return STATUS_ERR;
                }
                retVal = removeFromCart(p1);
            } else if ("update".equals(operation)) {
                if (p1 == null || p1.trim().length() == 0) {
                    lastError = "Cart ID is required for update";
                    return STATUS_ERR;
                }
                if (p2 == null || p2.trim().length() == 0) {
                    lastError = "Qty is required for update";
                    return STATUS_ERR;
                }
                retVal = updateCartQty(p1, p2);
            } else if ("clear".equals(operation)) {
                if (p1 == null || p1.trim().length() == 0) {
                    lastError = "Session ID is required for clear";
                    return STATUS_ERR;
                }
                retVal = clearCart(p1);
            } else if ("count".equals(operation)) {
                if (p1 == null || p1.trim().length() == 0) {
                    lastError = "Session ID is required for count";
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
                lastError = "Unknown cart operation: " + op;
                System.out.println("doCartOperation: unknown op=" + op);
                retVal = STATUS_ERR;
            }
        } catch (Exception e) {
            lastError = e.getMessage();
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
                lastError = "Book ID is null or empty";
                return STATUS_ERR;
            }
            if (qty <= 0) {
                lastError = "Quantity must be positive";
                return STATUS_ERR;
            }
            if (mode == null || mode.trim().length() == 0) {
                lastError = "Mode is null or empty";
                return STATUS_ERR;
            }

            Object bookObj = getBookById(bookId);
            if (bookObj == null) {
                lastError = "Book not found: " + bookId;
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
                    lastError = "Cannot decrease below zero. Current=" + currentStock + " requested=" + qty;
                    System.out.println("handleStockChange: insufficient stock for book " + bookId);
                    return STATUS_ERR;
                }
                adjType = ADJ_DECREASE;
            } else {
                lastError = "Unknown mode: " + mode;
                System.out.println("handleStockChange: unknown mode " + mode);
                return STATUS_ERR;
            }

            book.setQtyInStock(String.valueOf(newStock));
            bookDAO.save(book);

            StockTransaction txn = new StockTransaction();
            txn.setBookId(bookId);
            txn.setTxnType(TXN_CORRECTION);
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
            lastError = e.getMessage();
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
                List pendingOrders = orderDAO.findByStatus(ORDER_PENDING);
                int pendingCount = pendingOrders != null ? pendingOrders.size() : 0;
                statsCache.put("pendingOrders", String.valueOf(pendingCount));

                List processingOrders = orderDAO.findByStatus(ORDER_PROCESSING);
                int processingCount = processingOrders != null ? processingOrders.size() : 0;
                statsCache.put("processingOrders", String.valueOf(processingCount));

                List allOrders = orderDAO.findByStatus(null);
                int totalOrders = allOrders != null ? allOrders.size() : 0;
                statsCache.put("totalOrders", String.valueOf(totalOrders));

                // count low stock
                List lowStock = bookDAO.findLowStock(String.valueOf(LOW_STOCK_THRESHOLD));
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
                List customers = customerDAO.findByStatus(STS_ACTIVE);
                int customerCount = customers != null ? customers.size() : 0;
                statsCache.put("customerCount", String.valueOf(customerCount));

                statsCache.put("lastRefresh", CommonUtil.getCurrentDateTimeStr());
                initialized = true;
                System.out.println("refreshStats: completed. books=" + bookCount
                                   + " orders=" + totalOrders + " lowStock=" + lowStockCount);
            } catch (Exception e) {
                lastError = "refreshStats failed: " + e.getMessage();
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
                lastError = "Supplier name is null or empty";
                return STATUS_ERR;
            }

            // redundant null check
            if (name == null) {
                return STATUS_ERR;
            }

            Object existing = supplierDAO.findByName(name);
            if (existing != null) {
                lastError = "Supplier already exists: " + name;
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
            if (result == STATUS_OK) {
                supplierCacheLocal.put(supplier.getId() != null ? supplier.getId().toString() : name, supplier);
                System.out.println("createSupplierRecord: created " + name);
            }
            return result;
        } catch (Exception e) {
            lastError = e.getMessage();
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
                lastError = "Supplier ID is null or empty";
                return STATUS_ERR;
            }

            Object existing = supplierDAO.findById(id);
            if (existing == null) {
                lastError = "Supplier not found: " + id;
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
            lastError = e.getMessage();
            e.printStackTrace();
            return STATUS_ERR;
        }
    }


    /** Deactivate a supplier (duplicated from CommonHelper). */
    public int deactivateSupplierRecord(String id) {
        lastAccessTime = System.currentTimeMillis();
        try {
            if (id == null || id.trim().length() == 0) {
                lastError = "Supplier ID is null or empty";
                return STATUS_ERR;
            }

            Object existing = supplierDAO.findById(id);
            if (existing == null) {
                lastError = "Supplier not found for deactivation: " + id;
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
            lastError = e.getMessage();
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
            lastError = "Supplier ID is null";
            return null;
        }
        if (id.trim().length() == 0) {
            lastError = "Supplier ID is empty";
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
            lastError = "Supplier not found: " + id;
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
                lastError = "Supplier ID is null";
                return null;
            }
            if (supplierId.trim().length() == 0) {
                lastError = "Supplier ID is empty";
                return null;
            }
            if (items == null) {
                lastError = "Items list is null";
                return null;
            }
            if (items.size() == 0) {
                lastError = "Items list is empty";
                return null;
            }

            // verify supplier exists (CommonHelper doesn't do this)
            Object supplierObj = supplierDAO.findById(supplierId);
            if (supplierObj == null) {
                lastError = "Supplier not found: " + supplierId;
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

            double tax = subtotal * DEFAULT_TAX_RATE / 100.0;
            po.setSubtotal(subtotal);
            po.setTax(tax);
            po.setShippingCost(0.0);
            po.setTotal(subtotal + tax);

            int result = poDAO.save(po);
            if (result != STATUS_OK) {
                lastError = "Failed to save PO";
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
                    lastError = "Failed to save PO item " + i;
                }
            }

            System.out.println("createPurchaseOrderLocal: PO created, id=" + po.getId());
            return po.getId() != null ? po.getId().toString() : poNumber;
        } catch (Exception e) {
            lastError = e.getMessage();
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
                lastError = "PO ID is null or empty";
                return STATUS_ERR;
            }

            Object poObj = poDAO.findById(poId);
            if (poObj == null) {
                lastError = "PO not found: " + poId;
                return STATUS_NOT_FOUND;
            }

            // redundant null check
            if (poObj == null) {
                return STATUS_NOT_FOUND;
            }

            PurchaseOrder po = (PurchaseOrder) poObj;
            if (!PO_DRAFT.equals(po.getStatus())) {
                lastError = "PO is not in DRAFT status: " + po.getStatus();
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
            lastError = e.getMessage();
            e.printStackTrace();
            return STATUS_ERR;
        }
    }


    /** Cancel a PO — duplicated from CommonHelper with extra validations. */
    public int cancelPurchaseOrderLocal(String poId, String reason, String userId) {
        lastAccessTime = System.currentTimeMillis();
        try {
            if (poId == null || poId.trim().length() == 0) {
                lastError = "PO ID is null or empty for cancel";
                return STATUS_ERR;
            }

            Object poObj = poDAO.findById(poId);
            if (poObj == null) {
                lastError = "PO not found for cancel: " + poId;
                return STATUS_NOT_FOUND;
            }

            PurchaseOrder po = (PurchaseOrder) poObj;

            // extra status checks not in CommonHelper
            if (PO_CLOSED.equals(po.getStatus())) {
                lastError = "Cannot cancel a closed PO";
                System.out.println("cancelPurchaseOrderLocal: PO is closed: " + poId);
                return STATUS_ERR;
            }
            if (PO_CANCELLED.equals(po.getStatus())) {
                lastError = "PO is already cancelled";
                System.out.println("cancelPurchaseOrderLocal: PO already cancelled: " + poId);
                return STATUS_ERR;
            }
            if (PO_RECEIVED.equals(po.getStatus())) {
                lastError = "Cannot cancel a fully received PO";
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
            lastError = e.getMessage();
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
            lastError = "PO ID is null or empty";
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
                lastError = "PO ID is null or empty for receiving";
                return STATUS_ERR;
            }
            if (items == null) {
                lastError = "Items list is null for receiving";
                return STATUS_ERR;
            }
            if (items.size() == 0) {
                lastError = "Items list is empty for receiving";
                return STATUS_ERR;
            }

            if (items.size() > 50) {
                System.out.println("receiveShipmentLocal: large shipment with " + items.size() + " items");
            }

            Object poObj = poDAO.findById(poId);
            if (poObj == null) {
                lastError = "PO not found for receiving: " + poId;
                return STATUS_NOT_FOUND;
            }

            // redundant null check
            if (poObj == null) {
                return STATUS_NOT_FOUND;
            }

            PurchaseOrder po = (PurchaseOrder) poObj;
            if (!PO_SUBMITTED.equals(po.getStatus())
                && !PO_PARTIAL.equals(po.getStatus())) {
                lastError = "PO status does not allow receiving: " + po.getStatus();
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
                lastError = "Failed to save receiving record";
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
                            bookCache.put(poItem.getBookId(), book);
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
                    lastError = "Error processing item " + i;
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
            lastError = e.getMessage();
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
                lastError = "Supplier ID is null or empty";
                System.out.println("processSupplierOrder: supplier ID is empty");
                return STATUS_ERR;
            }

            Object supplierObj = supplierDAO.findById(supplierId);
            if (supplierObj == null) {
                lastError = "Supplier not found: " + supplierId;
                System.out.println("processSupplierOrder: supplier not found: " + supplierId);
                return STATUS_NOT_FOUND;
            }

            // redundant null check
            if (supplierObj == null) {
                return STATUS_NOT_FOUND;
            }

            Supplier supplier = (Supplier) supplierObj;
            if (supplier.getStatus() == null) {
                lastError = "Supplier status is null";
                return STATUS_ERR;
            }
            if (!STS_ACTIVE.equals(supplier.getStatus())) {
                lastError = "Supplier is inactive: " + supplierId;
                System.out.println("processSupplierOrder: supplier is inactive");
                return STATUS_ERR;
            }

            if (items == null || items.size() == 0) {
                lastError = "No items provided";
                System.out.println("processSupplierOrder: no items");
                return STATUS_ERR;
            }

            // validate each item (more verbose than CommonHelper)
            for (int i = 0; i < items.size(); i++) {
                try {
                    Map itemMap = (Map) items.get(i);
                    if (itemMap == null) {
                        lastError = "Item " + i + " is null";
                        System.out.println("processSupplierOrder: item " + i + " is null");
                        return STATUS_ERR;
                    }

                    String bookId = (String) itemMap.get("bookId");
                    String qty = (String) itemMap.get("qty");
                    String price = (String) itemMap.get("price");

                    if (bookId == null || bookId.trim().length() == 0) {
                        lastError = "Item " + i + " has no bookId";
                        System.out.println("processSupplierOrder: item " + i + " has no bookId");
                        return STATUS_ERR;
                    }

                    if (CommonUtil.toInt(qty) <= 0) {
                        lastError = "Item " + i + " has invalid qty: " + qty;
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
                        lastError = "Book not found: " + bookId;
                        System.out.println("processSupplierOrder: book not found: " + bookId);
                        return STATUS_ERR;
                    }
                } catch (Exception e) {
                    lastError = "Error validating item " + i + ": " + e.getMessage();
                    e.printStackTrace();
                    return STATUS_ERR;
                }
            }

            // create the PO using our local method
            String poId = createPurchaseOrderLocal(supplierId, createdBy, items);
            if (poId == null) {
                lastError = "Failed to create PO";
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
            if (FLG_ON.equals(autoSubmit)) {
                try { clearCache(); } catch (Exception ex) { }
                try { UserManager.getInstance().logAction("PO_SUBMITTED", createdBy != null ? createdBy : "", "PO auto-submitted"); } catch (Exception ex) { }

                int submitResult = submitPurchaseOrderLocal(poId, createdBy);
                if (submitResult != STATUS_OK) {
                    lastError = "PO created but submit failed";
                    System.out.println("processSupplierOrder: PO created but submit failed");
                    return STATUS_WARN;
                }
            }

            System.out.println("processSupplierOrder: completed successfully, PO=" + poId);
            return STATUS_OK;
        } catch (Exception e) {
            lastError = e.getMessage();
            e.printStackTrace();
            return STATUS_ERR;
        } finally {
            busy = false;
        }
    }


    // ========================================================================
    // Utility / helper getters
    // ========================================================================

    public String getLastError() { return lastError; }
    public boolean isBusy() { return busy; }
    public static long getLastAccessTime() { return lastAccessTime; }
    public String getLastPoNumber() { return lastPoNumber; }
    public String getCurrentMode() { return currentMode; }
    public void setCurrentMode(String mode) { this.currentMode = mode; }
    public Map getTempData() { return tempData; }
    public List getPendingItems() { return pendingItems; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int count) { this.retryCount = count; }
    public List getRecentSearches() { return recentSearches; }

    public int archiveOldPOsLocal(String beforeDate) { return 0; }

    public int recalculatePOTotalsLocal() { return STATUS_OK; }

    public List validateAllSuppliersLocal() { return new ArrayList(); }
}
