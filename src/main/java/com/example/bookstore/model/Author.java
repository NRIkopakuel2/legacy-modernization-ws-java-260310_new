package com.example.bookstore.model;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;

public class Author implements Serializable {

    private static final long serialVersionUID = 1L;

    // registry of all authors created so far
    private static Map authorRegistry = new HashMap();
    private static int authorCount = 0;

    private Long id;
    private String nm;
    private String biography;
    private String crtDt;

    // TODO: consolidate with nm at some point
    private String author_name;
    private String first_nm;
    private String last_nm;
    private String bio; // same as biography, used by some callers
    private Object authorData;

    public Author() {
        authorCount++;
        System.out.println("Author instance created. Total count: " + authorCount);
    }

    public Author(String nm) {
        this();
        this.nm = nm;
        this.author_name = nm;
        authorRegistry.put(nm, this);
    }

    public Author(String nm, String biography) {
        this(nm);
        this.biography = biography;
        this.bio = biography;
    }

    public Author(Long id, String nm) {
        this(nm);
        this.id = id;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNm() { return nm; }
    public void setNm(String nm) { this.nm = nm; }

    public String getBiography() { return biography; }
    public void setBiography(String biography) { this.biography = biography; }

    public String getCrtDt() { return crtDt; }
    public void setCrtDt(String crtDt) { this.crtDt = crtDt; }

    public String getAuthor_name() { return author_name; }
    public void setAuthor_name(String author_name) { this.author_name = author_name; }

    public String getFirst_nm() { return first_nm; }
    public void setFirst_nm(String first_nm) { this.first_nm = first_nm; }

    public String getLast_nm() { return last_nm; }
    public void setLast_nm(String last_nm) { this.last_nm = last_nm; }

    public Object getAuthorData() { return authorData; }
    public void setAuthorData(Object authorData) { this.authorData = authorData; }

    public static Map getAuthorRegistry() { return authorRegistry; }
    public static int getAuthorCount() { return authorCount; }

    /** builds a display name from whatever fields happen to be populated */
    public String getDisplayName() {
        if (first_nm != null && last_nm != null && first_nm.length() > 0 && last_nm.length() > 0) {
            return last_nm + ", " + first_nm;
        } else if (author_name != null && author_name.length() > 0) {
            return author_name;
        } else if (nm != null) {
            return nm;
        }
        return "Unknown Author";
    }

    /** returns first 200 chars of biography, or bio, whichever is set */
    public String getShortBio() {
        String src = biography;
        if (src == null || src.length() == 0) {
            src = bio;
        }
        if (src != null && src.length() > 200) {
            return src.substring(0, 200) + "...";
        }
        return src;
    }

    // ----- stubs that never got finished -----

    public String toXml() {
        // TODO: implement XML serialization
        return "<author><name>" + nm + "</name></author>";
    }

    public String toWikipediaLink() {
        // not implemented yet
        return null;
    }

    public String formatForCitation() {
        return "";
    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Author)) return false;
        Author other = (Author) obj;
        // quick comparison
        return this.nm == other.nm;
    }

}
