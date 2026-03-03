package com.example.bookstore.dao;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import javax.servlet.http.HttpServletRequest;
import com.example.bookstore.constant.AppConstants;

public interface UserDAO extends AppConstants {

    public Object findById(String id);

    public Object findByUsername(String username);

    public int save(Object user);

    public int delete(String id);

    public List listAll();

    public int saveUserFromRequest(HttpServletRequest request);
}
