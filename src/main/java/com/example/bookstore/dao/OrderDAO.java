package com.example.bookstore.dao;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import javax.servlet.http.HttpServletRequest;
import com.example.bookstore.constant.AppConstants;

public interface OrderDAO extends AppConstants {

    public Object findById(String id);

    public Object findByOrderNumber(String orderNo);

    public List findByCustomerId(String customerId);

    public List findByStatus(String status);

    public List findByDateRange(String fromDate, String toDate);

    public int save(Object order);

    public List findOrdersFromSession(HttpServletRequest request);
}
