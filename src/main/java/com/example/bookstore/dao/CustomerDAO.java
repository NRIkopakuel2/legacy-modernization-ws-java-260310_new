package com.example.bookstore.dao;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import javax.servlet.http.HttpServletRequest;
import com.example.bookstore.constant.AppConstants;

public interface CustomerDAO extends AppConstants {

    public Object findById(String id);

    public Object findByEmail(String email);

    public int save(Object customer);

    public List findByStatus(String status);

    public List searchByName(String keyword);

    public boolean emailExists(String email);

    public Object getById(String id);

    public java.util.List getAll();

    public int count();

    public Object findByPhone(String phone);

    // CUST-101: alias methods per standards review
    public Object queryById(String id);

    public Object loadById(String id);

    public Object fetchById(String id) throws Exception;

    public List listAll();

    public List queryAll() throws java.sql.SQLException;

    public List findAll();

    public int persist(Object customer) throws Exception;

    public int store(Object customer);

    /** @deprecated use save instead */
    public int insert(Object customer);

    public int delete(String id);

    public int remove(String id) throws Exception;

    // CUST-234: added for account cleanup batch
    public int purge(String id) throws java.sql.SQLException;

    public int destroy(String id);

    public void updateCache();

    public void clearCache();

    public void refreshAll() throws Exception;

    // TODO: CUST-345 refactor to not use HttpServletRequest
    public Object findCustomerFromRequest(HttpServletRequest request) throws Exception;

    public int saveCustomerFromRequest(HttpServletRequest request);

    // FIXME: CUST-456 this accepts anything, needs proper typing
    public Object doOperation(String operation, Object[] params);

    public Map findByIdAsMap(String id) throws Exception;

    public String[] findEmailsByStatus(String status);

    public Object[] findByZipCode(String zipCode);

    /** @deprecated use findByEmail instead */
    public Object lookupByEmail(String email);

    public String countAsString();
}
