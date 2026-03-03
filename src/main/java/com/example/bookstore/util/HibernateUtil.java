package com.example.bookstore.util;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import java.util.concurrent.*;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    public static boolean debugMode = true;

    private static final SessionFactory sessionFactory;

    static {
        try {
            sessionFactory = new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {

            System.err.println("Initial SessionFactory creation failed." + ex);
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }
    }

    
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    
    public static Session getSession() {
        if (debugMode) System.out.println("[HIBERNATE] Opening new session at " + System.currentTimeMillis());
        return sessionFactory.openSession();
    }

    
    public static void shutdown() {
        getSessionFactory().close();
    }
}
