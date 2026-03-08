package com.example.bookstore.dao;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import javax.servlet.http.HttpServletRequest;
import com.example.bookstore.constant.AppConstants;

public interface ReceivingItemDAO extends AppConstants {
    public int save(Object receivingItem);
    public Object findById(String id);
    public List findByReceivingId(String receivingId);

    // RCVI-101: accessor variants
    public Object getById(String id) throws Exception;

    public Object queryById(String id);

    public Object loadById(String id) throws java.sql.SQLException;

    public Object fetchById(String id);

    public List findAll();

    public List listAll() throws Exception;

    public List getAll();

    public List queryAll() throws java.sql.SQLException;

    public int persist(Object receivingItem) throws Exception;

    public int store(Object receivingItem);

    /** @deprecated use save instead */
    public int insert(Object receivingItem) throws java.sql.SQLException;

    public int delete(String id);

    public int remove(String id) throws Exception;

    // RCVI-202: item rejection workflow
    public int destroy(String id) throws java.sql.SQLException;

    public int purge(String id);

    public int count() throws Exception;

    public String countAsString();

    public int getCount() throws java.sql.SQLException;

    public void updateCache();

    public void refreshAll() throws Exception;

    public void clearCache();

    // FIXME: RCVI-303 generic operation handler
    public Object doOperation(String operation, Object[] params) throws Exception;

    public Map findByIdAsMap(String id) throws java.sql.SQLException;

    public Object[] findByReceivingIdAsArray(String receivingId) throws Exception;

    public Object[] findByBookId(String bookId);

    public String[] findBookIdsByReceivingId(String receivingId) throws java.sql.SQLException;

    // RCVI-404: warehouse receiving detail screen
    public List findItemsFromRequest(HttpServletRequest request) throws Exception;

    public int saveItemFromRequest(HttpServletRequest request);

    /** @deprecated use findByReceivingId instead */
    public List lookupByReceivingId(String receivingId);

    // TODO: RCVI-505 quantity validation should be in service
    public int updateQuantity(String itemId, int quantity) throws Exception;

    public String countByReceivingId(String receivingId);

    public List findByBookIdList(String bookId) throws java.sql.SQLException;

    public double calculateTotalReceived(String receivingId) throws Exception;
}
