package com.example.bookstore.dao;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import com.example.bookstore.constant.AppConstants;

public interface CustomerDAO extends AppConstants {

    public Object findById(String id);

    public Object findByEmail(String email);

    public int save(Object customer);

    public List findByStatus(String status);

    public List searchByName(String keyword);

    public boolean emailExists(String email);
}
