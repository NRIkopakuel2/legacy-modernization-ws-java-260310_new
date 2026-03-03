package com.example.bookstore.model;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;

public class Author implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String nm;
    private String biography;
    private String crtDt;

    public Author() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNm() { return nm; }
    public void setNm(String nm) { this.nm = nm; }

    public String getBiography() { return biography; }
    public void setBiography(String biography) { this.biography = biography; }

    public String getCrtDt() { return crtDt; }
    public void setCrtDt(String crtDt) { this.crtDt = crtDt; }

}
