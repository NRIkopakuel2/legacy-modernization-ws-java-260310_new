package com.example.bookstore.model;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;

public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String catNm;
    private String descr;
    private String crtDt;
    private String updDt;
    private String reserve1;
    private String reserve2;

    public Category() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCatNm() { return catNm; }
    public void setCatNm(String catNm) { this.catNm = catNm; }

    public String getDescr() { return descr; }
    public void setDescr(String descr) { this.descr = descr; }

    public String getCrtDt() { return crtDt; }
    public void setCrtDt(String crtDt) { this.crtDt = crtDt; }

    public String getUpdDt() { return updDt; }
    public void setUpdDt(String updDt) { this.updDt = updDt; }

    public String getReserve1() { return reserve1; }
    public void setReserve1(String reserve1) { this.reserve1 = reserve1; }

    public String getReserve2() { return reserve2; }
    public void setReserve2(String reserve2) { this.reserve2 = reserve2; }

}
