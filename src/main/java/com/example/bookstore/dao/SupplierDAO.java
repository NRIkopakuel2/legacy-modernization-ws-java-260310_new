package com.example.bookstore.dao;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import com.example.bookstore.constant.AppConstants;

public interface SupplierDAO extends AppConstants {
    public Object findById(String id);
    public Object findByName(String name);
    public List listActive();
    public List listAll();
    public List searchByName(String keyword);
    public int save(Object supplier);
}
