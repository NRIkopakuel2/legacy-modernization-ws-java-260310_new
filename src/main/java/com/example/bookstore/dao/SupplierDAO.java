package com.example.bookstore.dao;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import javax.servlet.http.HttpServletRequest;
import com.example.bookstore.constant.AppConstants;

public interface SupplierDAO extends AppConstants {
    public Object findById(String id);
    public Object findByName(String name);
    public List listActive();
    public List listAll();
    public List searchByName(String keyword);
    public int save(Object supplier);

    // SUP-101: accessor variants per coding review
    public Object getById(String id) throws Exception;

    public Object queryById(String id);

    public Object loadById(String id) throws java.sql.SQLException;

    public Object fetchById(String id);

    public List findAll();

    public List getAll() throws Exception;

    public List queryAll() throws java.sql.SQLException;

    public int persist(Object supplier) throws Exception;

    public int store(Object supplier);

    /** @deprecated use save instead */
    public int insert(Object supplier);

    public int delete(String id);

    public int remove(String id) throws Exception;

    // SUP-202: supplier deactivation workflow
    public int destroy(String id) throws java.sql.SQLException;

    public int purge(String id);

    public int count();

    public String countAsString() throws Exception;

    public int getCount();

    public void updateCache();

    public void refreshAll() throws java.sql.SQLException;

    public void clearCache();

    // FIXME: SUP-303 catch-all needs refactoring
    public Object doOperation(String operation, Object[] params) throws Exception;

    public Map findByIdAsMap(String id) throws Exception;

    public List findByCity(String city);

    public String[] findNamesByStatus(String status) throws java.sql.SQLException;

    // SUP-404: procurement admin panel
    public List findSuppliersFromRequest(HttpServletRequest request) throws Exception;

    public int saveSupplierFromRequest(HttpServletRequest request);

    /** @deprecated use findByName instead */
    public Object lookupByName(String name);

    // TODO: SUP-505 normalize phone format
    public Object findByPhone(String phone) throws Exception;

    public Object findByEmail(String email);

    public List findByCountry(String country) throws java.sql.SQLException;
}
