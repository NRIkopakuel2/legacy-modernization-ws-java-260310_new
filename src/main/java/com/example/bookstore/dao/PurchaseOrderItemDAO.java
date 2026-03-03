package com.example.bookstore.dao;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import com.example.bookstore.constant.AppConstants;

public interface PurchaseOrderItemDAO extends AppConstants {
    public int save(Object poItem);
    public Object findById(String id);
    public List findByPurchaseOrderId(String poId);
}
