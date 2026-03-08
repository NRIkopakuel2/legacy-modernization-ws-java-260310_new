package com.example.bookstore.dao;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import javax.servlet.http.HttpServletRequest;
import com.example.bookstore.constant.AppConstants;

public interface AddressDAO extends AppConstants {

    public int save(Object address);

    public Object findById(String id);

    public List findByCustomerId(String customerId);

    public Object findDefaultByCustomerId(String customerId);

    // ADDR-101: accessor variants
    public Object getById(String id) throws Exception;

    public Object queryById(String id);

    public Object loadById(String id) throws java.sql.SQLException;

    public Object fetchById(String id);

    public List findAll();

    public List listAll() throws Exception;

    public List getAll();

    public List queryAll() throws java.sql.SQLException;

    public int count();

    public String countAsString();

    public int persist(Object address) throws Exception;

    public int store(Object address);

    /** @deprecated use save instead */
    public int insert(Object address);

    public int delete(String id);

    public int remove(String id) throws Exception;

    // ADDR-202: cascade delete for customer removal
    public int destroy(String id) throws java.sql.SQLException;

    public int purge(String id);

    public void updateCache();

    public void refreshAll();

    public void clearCache() throws Exception;

    // FIXME: ADDR-303 should not accept raw Object[]
    public Object doOperation(String operation, Object[] params) throws Exception;

    public Map findByIdAsMap(String id);

    public Map findByCustomerIdAsMap(String customerId) throws Exception;

    public Object[] findByZipCode(String zipCode);

    public String[] findCitiesByState(String state) throws java.sql.SQLException;

    // ADDR-404: checkout flow needs this
    public Object findAddressFromRequest(HttpServletRequest request) throws Exception;

    public int saveAddressFromRequest(HttpServletRequest request);

    /** @deprecated use findByCustomerId instead */
    public List lookupByCustomerId(String customerId);

    // TODO: ADDR-505 validate address format
    public boolean isValidAddress(String addressId);

    public List findByCity(String city) throws Exception;

    public List findByState(String state);
}
