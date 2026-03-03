package com.example.bookstore.dao;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import com.example.bookstore.constant.AppConstants;

public interface ReceivingItemDAO extends AppConstants {
    public int save(Object receivingItem);
    public Object findById(String id);
    public List findByReceivingId(String receivingId);
}
