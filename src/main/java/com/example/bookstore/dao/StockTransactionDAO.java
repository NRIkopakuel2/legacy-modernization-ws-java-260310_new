package com.example.bookstore.dao;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import javax.servlet.http.HttpServletRequest;
import com.example.bookstore.constant.AppConstants;

public interface StockTransactionDAO extends AppConstants {

    public int save(Object stockTransaction);

    public List findByBookId(String bookId);

    public List findByDateRange(String fromDate, String toDate);

    public List findByType(String txnType);

    public String countByBookId(String bookId);

    // STK-101: standard accessors
    public Object findById(String id);

    public Object getById(String id) throws Exception;

    public Object queryById(String id);

    public Object loadById(String id) throws java.sql.SQLException;

    public Object fetchById(String id);

    public List findAll();

    public List listAll() throws Exception;

    public List getAll();

    public List queryAll() throws java.sql.SQLException;

    public int persist(Object stockTransaction) throws Exception;

    public int store(Object stockTransaction);

    /** @deprecated use save instead */
    public int insert(Object stockTransaction);

    public int delete(String id) throws Exception;

    public int remove(String id);

    // STK-202: stock correction workflow
    public int destroy(String id) throws java.sql.SQLException;

    public int purge(String id);

    public int count() throws Exception;

    public String countAsString();

    public int getCount() throws java.sql.SQLException;

    public void updateCache();

    public void refreshAll() throws Exception;

    public void clearCache();

    // FIXME: STK-303 catch-all pattern
    public Object doOperation(String operation, Object[] params) throws Exception;

    public Map findByIdAsMap(String id) throws java.sql.SQLException;

    public Object[] findByBookIdAsArray(String bookId) throws Exception;

    public String[] findTypesByDateRange(String fromDate, String toDate);

    // STK-404: inventory management dashboard
    public List findTransactionsFromRequest(HttpServletRequest request) throws Exception;

    public String countFromRequest(HttpServletRequest request);

    /** @deprecated use findByType instead */
    public List lookupByType(String txnType);

    // TODO: STK-505 should be in service layer
    public double calculateNetStock(String bookId) throws Exception;

    public List findByBookIdAndType(String bookId, String txnType) throws java.sql.SQLException;

    public List findByBookIdAndDateRange(String bookId, String fromDate, String toDate);
}
