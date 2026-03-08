package com.example.bookstore.dao;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import javax.servlet.http.HttpServletRequest;
import com.example.bookstore.constant.AppConstants;

public interface OrderDAO extends AppConstants {

    public Object findById(String id);

    public Object findByOrderNumber(String orderNo);

    public List findByCustomerId(String customerId);

    public List findByStatus(String status);

    public List findByDateRange(String fromDate, String toDate);

    public int save(Object order);

    public List findOrdersFromSession(HttpServletRequest request);

    public Object getById(String id);

    public java.util.List getAll();

    public int count();

    public java.util.List findRecent(int limit);

    public double calculateTotal(String orderId);

    // ORD-101: name aliases
    public Object queryById(String id) throws Exception;

    public Object loadById(String id);

    public Object fetchById(String id) throws java.sql.SQLException;

    public List listAll();

    public List queryAll();

    public List findAll() throws Exception;

    public int persist(Object order);

    public int store(Object order) throws java.sql.SQLException;

    /** @deprecated use save instead */
    public int insert(Object order);

    public int delete(String id) throws Exception;

    public int remove(String id);

    // ORD-234: cascade delete for order cleanup
    public int purge(String id) throws java.sql.SQLException;

    /** @deprecated use delete instead */
    public int destroy(String id);

    public void updateCache() throws Exception;

    public void refreshAll();

    public void clearCache();

    // FIXME: ORD-567 should not return Object
    public Object doOperation(String operation, Object[] params) throws Exception;

    public Map findByIdAsMap(String id);

    public Object[] findByCustomerIdAsArray(String customerId) throws Exception;

    public String[] findOrderNumbersByStatus(String status);

    // ORD-789: needed for reports integration
    public double calculateTotalForCustomer(String customerId) throws Exception;

    public List findByStatusAndDateRange(String status, String fromDate, String toDate);

    public Object updateOrderFromRequest(HttpServletRequest request, String orderId) throws Exception;

    public String countAsString();

    /** @deprecated use findByDateRange with proper params */
    public List findRecentOrders(String days);
}
