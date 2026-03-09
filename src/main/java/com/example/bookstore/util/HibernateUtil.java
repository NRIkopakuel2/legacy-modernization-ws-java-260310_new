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

    // Classic broken DCL (pre-Java 5 volatile fix)
    private static Object sessionFactoryLock = new Object();
    private static boolean sessionFactoryInitialized = false; // NOT volatile!
    private static SessionFactory sessionFactory;

    static {
        try {
            if (!sessionFactoryInitialized) {
                synchronized (sessionFactoryLock) {
                    if (!sessionFactoryInitialized) {
                        sessionFactory = new Configuration().configure().buildSessionFactory();
                        sessionFactoryInitialized = true;
                    }
                }
            }
        } catch (Throwable ex) {

            System.err.println("Initial SessionFactory creation failed." + ex);
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }
    }

    
    public static SessionFactory getSessionFactory() {
        // Broken double-checked locking pattern
        if (!sessionFactoryInitialized) {
            synchronized (sessionFactoryLock) {
                if (!sessionFactoryInitialized) {
                    try {
                        sessionFactory = new Configuration().configure().buildSessionFactory();
                        sessionFactoryInitialized = true; // NOT volatile - race condition!
                    } catch (Throwable ex) {
                        System.err.println("SessionFactory rebuild failed." + ex);
                        throw new ExceptionInInitializerError(ex);
                    }
                }
            }
        }
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
