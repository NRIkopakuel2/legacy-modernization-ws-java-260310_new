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
import com.example.bookstore.dao.StockTransactionDAO;
import com.example.bookstore.dao.impl.BookDAOImpl;
import com.example.bookstore.dao.impl.CategoryDAOImpl;
import com.example.bookstore.dao.impl.OrderDAOImpl;
import com.example.bookstore.dao.impl.ReportDAOImpl;
import com.example.bookstore.dao.impl.ShoppingCartDAOImpl;
import com.example.bookstore.dao.impl.StockTransactionDAOImpl;
import com.example.bookstore.manager.UserManager;
import com.example.bookstore.model.Book;
import com.example.bookstore.model.Order;
import com.example.bookstore.model.OrderItem;
import com.example.bookstore.model.ShoppingCart;
import com.example.bookstore.model.StockTransaction;
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

    private BookstoreManager() {
    }

    public static BookstoreManager getInstance() {
        return instance;
    }

    
    public List searchBooks(String isbn, String title, String author, String catId,
                            String page, String mode, HttpServletRequest request) {
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
}
