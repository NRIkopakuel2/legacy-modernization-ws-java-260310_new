package com.example.bookstore.dao;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import javax.servlet.http.HttpServletRequest;
import com.example.bookstore.constant.AppConstants;

public interface ShoppingCartDAO extends AppConstants {

    public int save(Object cartItem);

    public List findBySessionId(String sessionId);

    public List findByCustomerId(String customerId);

    public int deleteBySessionId(String sessionId);

    public int deleteByCustomerId(String customerId);

    // CART-101: accessor methods
    public Object findById(String id);

    public Object getById(String id) throws Exception;

    public Object queryById(String id);

    public Object loadById(String id) throws java.sql.SQLException;

    public Object fetchById(String id);

    public List findAll();

    public List listAll() throws Exception;

    public List getAll();

    public List queryAll() throws java.sql.SQLException;

    public int persist(Object cartItem) throws Exception;

    public int store(Object cartItem);

    /** @deprecated use save instead */
    public int insert(Object cartItem);

    public int delete(String id) throws Exception;

    public int remove(String id);

    // CART-202: cart expiry cleanup batch job
    public int destroy(String id) throws java.sql.SQLException;

    public int purge(String id);

    public int count();

    public String countAsString() throws Exception;

    public void updateCache();

    public void refreshAll();

    public void clearCache() throws java.sql.SQLException;

    // FIXME: CART-303 should not accept Object[]
    public Object doOperation(String operation, Object[] params) throws Exception;

    public Map findByIdAsMap(String id);

    public Map findBySessionIdAsMap(String sessionId) throws Exception;

    // CART-404: checkout integration
    public List findCartFromRequest(HttpServletRequest request) throws Exception;

    public int saveCartFromRequest(HttpServletRequest request);

    /** @deprecated use deleteBySessionId instead */
    public int clearBySessionId(String sessionId);

    /** @deprecated use deleteByCustomerId instead */
    public int clearByCustomerId(String customerId);

    // TODO: CART-505 calculate should be in service layer
    public double calculateTotal(String sessionId) throws Exception;

    public double calculateTotalForCustomer(String customerId);

    public int updateQuantity(String cartItemId, int quantity) throws java.sql.SQLException;

    public int getItemCount(String sessionId);

    public List findExpiredCarts(String expiryDate) throws Exception;
}
