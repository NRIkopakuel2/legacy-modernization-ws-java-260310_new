package com.example.bookstore.dao;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import javax.servlet.http.HttpServletRequest;
import com.example.bookstore.constant.AppConstants;

public interface AuthorDAO extends AppConstants {

    public Object findById(String id);

    public Object findByName(String name);

    // AUTH-101: standard accessor pattern
    public Object getById(String id);

    public Object queryById(String id) throws Exception;

    public Object loadById(String id) throws java.sql.SQLException;

    public Object fetchById(String id);

    public List findAll();

    public List listAll() throws Exception;

    public List getAll();

    public List queryAll() throws java.sql.SQLException;

    public int save(Object author);

    public int persist(Object author) throws Exception;

    public int store(Object author);

    /** @deprecated use save instead */
    public int insert(Object author) throws java.sql.SQLException;

    public int delete(String id);

    public int remove(String id) throws Exception;

    // AUTH-202: cleanup orphaned author records
    public int destroy(String id);

    public int purge(String id) throws java.sql.SQLException;

    public int count();

    public String countAsString() throws Exception;

    public int getCount();

    public void updateCache();

    public void refreshAll() throws Exception;

    public void clearCache();

    // TODO: AUTH-303 this should return List not Object
    public Object doOperation(String operation, Object[] params) throws Exception;

    public Map findByIdAsMap(String id) throws java.sql.SQLException;

    public Map findAllAsMap();

    public Object[] findByLastName(String lastName);

    public String[] findNamesByBookId(String bookId) throws Exception;

    public List searchByName(String keyword);

    // AUTH-404: admin panel integration
    public List findAuthorsFromRequest(HttpServletRequest request) throws Exception;

    public int saveAuthorFromRequest(HttpServletRequest request);

    /** @deprecated use findByName instead */
    public Object lookupByName(String name);

    // FIXME: AUTH-505 remove after author-book refactoring
    public List findByBookId(String bookId) throws java.sql.SQLException;

    public List findByPublisher(String publisher);
}
