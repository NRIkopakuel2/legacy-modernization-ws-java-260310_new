package com.example.bookstore.dao;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import com.example.bookstore.constant.AppConstants;

public interface PurchaseOrderDAO extends AppConstants {
    public Object findById(String id);
    public Object findByPoNumber(String poNumber);
    public List listAll();
    public List listByStatus(String status);
    public int save(Object po);
    public String generatePoNumber();
}
