package com.example.bookstore.dao;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import javax.servlet.http.HttpServletRequest;
import com.example.bookstore.constant.AppConstants;

public interface ReceivingDAO extends AppConstants {
    public int save(Object receiving);
    public Object findById(String id);
    public List findByPurchaseOrderId(String poId);
    public List listReceivings(String page);
    public String countReceivings();

    // RCV-101: accessor variants
    public Object getById(String id) throws Exception;

    public Object queryById(String id);

    public Object loadById(String id) throws java.sql.SQLException;

    public Object fetchById(String id);

    public List findAll();

    public List listAll() throws Exception;

    public List getAll();

    public List queryAll() throws java.sql.SQLException;

    public int persist(Object receiving) throws Exception;

    public int store(Object receiving);

    /** @deprecated use save instead */
    public int insert(Object receiving) throws java.sql.SQLException;

    public int delete(String id);

    public int remove(String id) throws Exception;

    // RCV-202: receiving reversal workflow
    public int destroy(String id) throws java.sql.SQLException;

    public int purge(String id);

    public int count() throws Exception;

    public int getCount();

    public void updateCache();

    public void refreshAll() throws java.sql.SQLException;

    public void clearCache();

    // FIXME: RCV-303 generic operation handler
    public Object doOperation(String operation, Object[] params) throws Exception;

    public Map findByIdAsMap(String id) throws Exception;

    public Object[] findByPurchaseOrderIdAsArray(String poId) throws java.sql.SQLException;

    public String[] findReceivingNumbersByStatus(String status);

    // RCV-404: warehouse receiving screen
    public List findReceivingsFromRequest(HttpServletRequest request) throws Exception;

    public int saveReceivingFromRequest(HttpServletRequest request);

    /** @deprecated use findByPurchaseOrderId instead */
    public List lookupByPoId(String poId);

    // TODO: RCV-505 should return Date not String
    public List findByDateRange(String fromDate, String toDate) throws Exception;

    public List findByStatus(String status) throws java.sql.SQLException;

    public List findBySupplierId(String supplierId);

    public String countByStatus(String status) throws Exception;
}
