package com.example.bookstore.dao;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import com.example.bookstore.constant.AppConstants;

public interface ReportDAO extends AppConstants {

    public List findDailySalesReport(String startDate, String endDate);

    public List findSalesByBookReport(String startDate, String endDate, String categoryId, String sortBy);

    public List findTopBooksReport(String startDate, String endDate, String rankBy, String topN);
}
