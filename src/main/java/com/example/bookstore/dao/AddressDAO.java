package com.example.bookstore.dao;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import com.example.bookstore.constant.AppConstants;

public interface AddressDAO extends AppConstants {

    public int save(Object address);

    public Object findById(String id);

    public List findByCustomerId(String customerId);

    public Object findDefaultByCustomerId(String customerId);
}
