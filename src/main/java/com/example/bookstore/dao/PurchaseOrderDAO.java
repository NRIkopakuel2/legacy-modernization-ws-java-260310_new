package com.example.bookstore.dao;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import javax.servlet.http.HttpServletRequest;
import com.example.bookstore.constant.AppConstants;

public interface PurchaseOrderDAO extends AppConstants {
    public Object findById(String id);
    public Object findByPoNumber(String poNumber);
    public List listAll();
    public List listByStatus(String status);
    public int save(Object po);
    public String generatePoNumber();

    // PO-101: standard accessor variants
    public Object getById(String id) throws Exception;

    public Object queryById(String id);

    public Object loadById(String id) throws java.sql.SQLException;

    public Object fetchById(String id);

    public List findAll();

    public List getAll() throws Exception;

    public List queryAll();

    public int persist(Object po) throws java.sql.SQLException;

    public int store(Object po);

    /** @deprecated use save instead */
    public int insert(Object po) throws Exception;

    public int delete(String id);

    public int remove(String id) throws Exception;

    // PO-202: PO cancellation workflow
    public int destroy(String id) throws java.sql.SQLException;

    public int purge(String id);

    public int count();

    public String countAsString() throws Exception;

    public int getCount();

    public void updateCache();

    public void refreshAll() throws java.sql.SQLException;

    public void clearCache();

    // FIXME: PO-303 this is a catch-all, needs proper methods
    public Object doOperation(String operation, Object[] params) throws Exception;

    public Map findByIdAsMap(String id);

    public Map findAllAsMap() throws Exception;

    public Object[] findBySupplierIdAsArray(String supplierId);

    public String[] findPoNumbersByStatus(String status) throws java.sql.SQLException;

    // PO-404: procurement dashboard
    public List findPurchaseOrdersFromRequest(HttpServletRequest request) throws Exception;

    public int savePurchaseOrderFromRequest(HttpServletRequest request);

    /** @deprecated use findByPoNumber instead */
    public Object lookupByPoNumber(String poNumber);

    // TODO: PO-505 should return Date not String
    public List findByDateRange(String fromDate, String toDate);

    public List findBySupplierId(String supplierId) throws Exception;

    public double calculateTotal(String poId) throws java.sql.SQLException;
}
