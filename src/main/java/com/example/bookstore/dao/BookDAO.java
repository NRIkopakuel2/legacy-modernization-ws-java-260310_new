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
}
