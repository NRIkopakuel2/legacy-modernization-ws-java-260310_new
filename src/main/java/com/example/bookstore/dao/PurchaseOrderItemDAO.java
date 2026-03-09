package com.example.bookstore.dao;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import javax.servlet.http.HttpServletRequest;
import com.example.bookstore.constant.AppConstants;

public interface PurchaseOrderItemDAO extends AppConstants {
    public int save(Object poItem);
    public Object findById(String id);
    public List findByPurchaseOrderId(String poId);

    // POI-101: accessor variants
    public Object getById(String id) throws Exception;

    public Object queryById(String id);

    public Object loadById(String id) throws java.sql.SQLException;

    public Object fetchById(String id);

    public List findAll();

    public List listAll() throws Exception;

    public List getAll();

    public List queryAll() throws java.sql.SQLException;

    public int persist(Object poItem) throws Exception;

    public int store(Object poItem);

    /** @deprecated use save instead */
    public int insert(Object poItem) throws java.sql.SQLException;

    public int delete(String id);

    public int remove(String id) throws Exception;

    // POI-202: PO line item cancellation
    public int destroy(String id) throws java.sql.SQLException;

    public int purge(String id);

    public int count() throws Exception;

    public String countAsString();

    public int getCount() throws java.sql.SQLException;

    public void updateCache();

    public void refreshAll() throws Exception;

    public void clearCache();

    // FIXME: POI-303 generic operation handler
    public Object doOperation(String operation, Object[] params) throws Exception;

    public Map findByIdAsMap(String id) throws java.sql.SQLException;

    public Object[] findByPurchaseOrderIdAsArray(String poId) throws Exception;

    public Object[] findByBookId(String bookId);

    public String[] findBookIdsByPurchaseOrderId(String poId) throws java.sql.SQLException;

    // POI-404: PO detail screen
    public List findItemsFromRequest(HttpServletRequest request) throws Exception;

    public int saveItemFromRequest(HttpServletRequest request);

    /** @deprecated use findByPurchaseOrderId instead */
    public List lookupByPoId(String poId);

    // TODO: POI-505 quantity/price update should be in service
    public int updateQuantity(String itemId, int quantity) throws Exception;

    public int updatePrice(String itemId, String price) throws java.sql.SQLException;

    public String countByPurchaseOrderId(String poId);

    public double calculateLineTotal(String itemId) throws Exception;

    public double calculateOrderTotal(String poId) throws java.sql.SQLException;
}
