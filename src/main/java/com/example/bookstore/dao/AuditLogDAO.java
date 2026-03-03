package com.example.bookstore.dao;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import com.example.bookstore.constant.AppConstants;

public interface AuditLogDAO extends AppConstants {
    public int save(Object auditLog);
    public List findByFilters(String startDate, String endDate, String actionType,
                              String userId, String entityType, String searchText, String page);
    public String countByFilters(String startDate, String endDate, String actionType,
                                 String userId, String entityType, String searchText);
}
