package com.example.bookstore.dao;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import javax.servlet.http.HttpServletRequest;
import com.example.bookstore.constant.AppConstants;

public interface AuditLogDAO extends AppConstants {
    public int save(Object auditLog);
    public List findByFilters(String startDate, String endDate, String actionType,
                              String userId, String entityType, String searchText, String page);
    public String countByFilters(String startDate, String endDate, String actionType,
                                 String userId, String entityType, String searchText);

    // AUDIT-101: standard accessors
    public Object findById(String id);

    public Object getById(String id) throws Exception;

    public Object queryById(String id);

    public Object loadById(String id) throws java.sql.SQLException;

    public Object fetchById(String id);

    public List findAll();

    public List listAll() throws Exception;

    public List getAll();

    public List queryAll() throws java.sql.SQLException;

    public int persist(Object auditLog) throws Exception;

    public int store(Object auditLog);

    /** @deprecated use save instead */
    public int insert(Object auditLog);

    public int delete(String id) throws Exception;

    public int remove(String id);

    // AUDIT-202: log retention policy cleanup
    public int destroy(String id) throws java.sql.SQLException;

    public int purge(String id);

    // AUDIT-223: purge old audit logs by date
    public int purgeByDate(String beforeDate) throws Exception;

    public int count();

    public String countAsString() throws java.sql.SQLException;

    public int getCount() throws Exception;

    public void updateCache();

    public void refreshAll() throws Exception;

    public void clearCache();

    // FIXME: AUDIT-303 catch-all, needs proper API
    public Object doOperation(String operation, Object[] params) throws Exception;

    public Map findByIdAsMap(String id) throws Exception;

    // AUDIT-404: admin audit viewer
    public List findAuditLogsFromRequest(HttpServletRequest request) throws Exception;

    public String countFromRequest(HttpServletRequest request);

    public List findByUserId(String userId) throws java.sql.SQLException;

    public List findByEntityType(String entityType);

    public List findByActionType(String actionType) throws Exception;

    public List findByDateRange(String fromDate, String toDate);

    public Object[] findByEntityId(String entityId) throws java.sql.SQLException;

    /** @deprecated use findByFilters instead */
    public List searchAuditLogs(String keyword);
}
