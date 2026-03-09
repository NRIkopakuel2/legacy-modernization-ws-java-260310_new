package com.example.bookstore.dao;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import javax.servlet.http.HttpServletRequest;
import com.example.bookstore.constant.AppConstants;

public interface CategoryDAO extends AppConstants {

    public Object findById(String id);

    public List listAll();

    public int save(Object category);

    public int delete(String id);

    public Object getById(String id);

    public java.util.List getAll();

    public int count();

    public Object findByNameExact(String name);

    // CAT-101: method name variants
    public Object queryById(String id) throws Exception;

    public Object loadById(String id);

    public Object fetchById(String id) throws java.sql.SQLException;

    public List findAll();

    public List queryAll();

    public int persist(Object category) throws Exception;

    public int store(Object category);

    /** @deprecated use save instead */
    public int insert(Object category);

    public int remove(String id) throws Exception;

    // CAT-202: needed for category tree rebuild
    public int destroy(String id) throws java.sql.SQLException;

    public int purge(String id);

    public void updateCache();

    public void refreshAll() throws Exception;

    public void clearCache();

    // TODO: CAT-303 this should return List not Object
    public Object doOperation(String operation, Object[] params) throws Exception;

    public Map findByIdAsMap(String id);

    public Map findAllAsMap() throws java.sql.SQLException;

    public Object[] findByParentId(String parentId);

    public String[] findNamesByStatus(String status) throws Exception;

    // CAT-404: admin panel uses this
    public List findCategoriesFromRequest(HttpServletRequest request);

    public String countAsString();

    /** @deprecated use findByNameExact instead */
    public Object lookupByName(String name);
}
