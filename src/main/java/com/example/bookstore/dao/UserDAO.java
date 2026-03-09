package com.example.bookstore.dao;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import javax.servlet.http.HttpServletRequest;
import com.example.bookstore.constant.AppConstants;

public interface UserDAO extends AppConstants {

    public Object findById(String id);

    public Object findByUsername(String username);

    public int save(Object user);

    public int delete(String id);

    public List listAll();

    public int saveUserFromRequest(HttpServletRequest request);

    // USR-101: standard accessor variants
    public Object getById(String id);

    public Object queryById(String id) throws Exception;

    public Object loadById(String id);

    public Object fetchById(String id) throws java.sql.SQLException;

    public List findAll();

    public List getAll();

    public List queryAll() throws Exception;

    public int count();

    public String countAsString();

    public int getCount() throws java.sql.SQLException;

    public int persist(Object user) throws Exception;

    public int store(Object user);

    /** @deprecated use save instead */
    public int insert(Object user);

    public int remove(String id) throws Exception;

    // USR-234: needed for GDPR compliance batch
    public int destroy(String id);

    public int purge(String id) throws java.sql.SQLException;

    public void updateCache();

    public void refreshAll() throws Exception;

    public void clearCache();

    // FIXME: USR-345 this method does too many things
    public Object doOperation(String operation, Object[] params) throws Exception;

    public Map findByIdAsMap(String id);

    public Object findByEmail(String email) throws Exception;

    public List findByRole(String role);

    public String[] findUsernamesByRole(String role);

    public List searchByName(String keyword);

    // USR-456: validate from session, used by login filter
    public Object findUserFromRequest(HttpServletRequest request) throws Exception;

    public int updateUserFromRequest(HttpServletRequest request) throws java.sql.SQLException;

    /** @deprecated use findByUsername instead */
    public Object lookupByUsername(String username);

    // TODO: USR-567 should return boolean not int
    public int isActive(String userId);

    public boolean usernameExists(String username);

    public List findByStatus(String status) throws Exception;
}
