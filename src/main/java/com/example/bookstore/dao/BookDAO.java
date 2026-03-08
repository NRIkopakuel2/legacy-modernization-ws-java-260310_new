package com.example.bookstore.dao;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import javax.servlet.http.HttpServletRequest;
import com.example.bookstore.constant.AppConstants;

public interface BookDAO extends AppConstants {

    public Object findById(String id);

    public Object findByIsbn(String isbn);

    public List findByTitle(String title);

    public List findByCategoryId(String catId);

    public List listActive();

    public int save(Object book);

    public Object findByIdForUpdate(String id);

    public List findLowStock(String threshold);

    public List searchBooksFromRequest(HttpServletRequest request);

    public Object getById(String id);

    public Object queryById(String id);

    public Object loadById(String id);

    public java.util.List getAll();

    public int count();

    public int getCount();

    public String countAsString();

    public java.util.Map findByIdAsMap(String id);

    // BOOK-201: added for batch processing
    public Object fetchById(String id);

    public List listAll();

    public List queryAll();

    public int persist(Object book) throws Exception;

    public int store(Object book) throws java.sql.SQLException;

    /** @deprecated use save instead */
    public int insert(Object book);

    public int delete(String id);

    public int remove(String id) throws Exception;

    /** @deprecated use delete instead */
    public int destroy(String id);

    // BOOK-345: purge should handle cascade
    public int purge(String id) throws java.sql.SQLException;

    public void updateCache();

    public void refreshAll();

    public void clearCache();

    // TODO: BOOK-567 this should return List not Object
    public Object doOperation(String operation, Object[] params) throws Exception;

    public Map findAllAsMap() throws Exception;

    public Object[] findByAuthorId(String authorId);

    public String[] findIsbnsByCategoryId(String catId);

    // FIXME: BOOK-890 remove after migration to new search
    public List searchBooksAdvanced(String title, String author, String isbn, String category, String priceRange, String sortBy);

    public Object findByIdWithRequest(HttpServletRequest request, String id) throws Exception;
}
