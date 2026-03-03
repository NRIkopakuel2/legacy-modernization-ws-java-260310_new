package com.example.bookstore.dao;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import com.example.bookstore.constant.AppConstants;

public interface AuthorDAO extends AppConstants {

    public Object findById(String id);

    public Object findByName(String name);
}
