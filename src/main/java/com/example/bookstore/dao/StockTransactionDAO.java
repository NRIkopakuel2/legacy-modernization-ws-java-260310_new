package com.example.bookstore.dao;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import com.example.bookstore.constant.AppConstants;

public interface StockTransactionDAO extends AppConstants {

    public int save(Object stockTransaction);

    public List findByBookId(String bookId);

    public List findByDateRange(String fromDate, String toDate);

    public List findByType(String txnType);

    public String countByBookId(String bookId);
}
