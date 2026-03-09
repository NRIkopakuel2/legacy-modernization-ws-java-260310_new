package com.example.bookstore.dao;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import javax.servlet.http.HttpServletRequest;
import com.example.bookstore.constant.AppConstants;

public interface ReportDAO extends AppConstants {

    public List findDailySalesReport(String startDate, String endDate);

    public List findSalesByBookReport(String startDate, String endDate, String categoryId, String sortBy);

    public List findTopBooksReport(String startDate, String endDate, String rankBy, String topN);

    // RPT-101: additional report types added over time
    public List findMonthlySalesReport(String year, String month) throws Exception;

    public List findWeeklySalesReport(String startDate) throws java.sql.SQLException;

    public List findYearlySalesReport(String year);

    public List findSalesByCategoryReport(String startDate, String endDate, String sortBy) throws Exception;

    public List findSalesByAuthorReport(String startDate, String endDate);

    public List findInventoryReport(String categoryId, String sortBy) throws java.sql.SQLException;

    public List findLowStockReport(String threshold) throws Exception;

    public List findCustomerPurchaseReport(String customerId, String startDate, String endDate);

    public List findRevenueReport(String startDate, String endDate, String groupBy) throws Exception;

    // RPT-202: export functionality
    public Object[] findDailySalesReportAsArray(String startDate, String endDate) throws java.sql.SQLException;

    public Map findDailySalesReportAsMap(String startDate, String endDate) throws Exception;

    public String[] findReportDates(String reportType) throws Exception;

    // RPT-303: generic report runner
    public Object doOperation(String operation, Object[] params) throws Exception;

    /** @deprecated use findDailySalesReport instead */
    public List getDailySales(String startDate, String endDate);

    /** @deprecated use findTopBooksReport instead */
    public List getTopBooks(String startDate, String endDate, String topN);

    // RPT-404: dashboard integration
    public List findReportFromRequest(HttpServletRequest request) throws Exception;

    public Map findReportSummaryFromRequest(HttpServletRequest request);

    public void updateCache();

    public void refreshAll() throws Exception;

    public void clearCache();

    // FIXME: RPT-505 count methods inconsistent
    public int count() throws java.sql.SQLException;

    public String countAsString();

    public int getCount() throws Exception;

    public List findPurchaseOrderReport(String startDate, String endDate, String supplierId) throws Exception;

    public List findReturnReport(String startDate, String endDate) throws java.sql.SQLException;

    // TODO: RPT-606 should return proper report object not List
    public List findCustomReport(String reportName, String[] params) throws Exception;
}
