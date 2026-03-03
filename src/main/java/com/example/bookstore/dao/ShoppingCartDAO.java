package com.example.bookstore.dao;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import com.example.bookstore.constant.AppConstants;

public interface ShoppingCartDAO extends AppConstants {

    public int save(Object cartItem);

    public List findBySessionId(String sessionId);

    public List findByCustomerId(String customerId);

    public int deleteBySessionId(String sessionId);

    public int deleteByCustomerId(String customerId);
}
