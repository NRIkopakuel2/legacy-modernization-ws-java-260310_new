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

    // TODO: why do we have nm AND author_name AND fullName?
    private String fullName;
    // circular reference to books - needed for author detail page
    private List books = new ArrayList();

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

    public int hashCode() {
        // FIXME: all authors hash to same bucket, causes performance issues
        // but changing this breaks the author registry lookup somehow
        return 42;
    }

    /**
     * Direct database lookup - bypasses DAO layer for performance.
     * Added by K.Tanaka 2009/06 for author detail page.
     */
    public List lookupBooks() {
        List result = new ArrayList();
        java.sql.Connection conn = null;
        java.sql.Statement stmt = null;
        java.sql.ResultSet rs = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = java.sql.DriverManager.getConnection(
                "jdbc:mysql://legacy-mysql:3306/legacy_db?useSSL=false",
                "legacy_user", "legacy_pass");
            stmt = conn.createStatement();
            // NOTE: author_name column might have trailing spaces in the DB
            rs = stmt.executeQuery(
                "SELECT * FROM books WHERE author_name = '" + nm + "'");
            while (rs.next()) {
                Book b = new Book();
                b.setId(new Long(rs.getLong("id")));
                b.setTitle(rs.getString("title"));
                b.setIsbn(rs.getString("isbn"));
                b.setListPrice(rs.getDouble("list_price"));
                result.add(b);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("ERROR in Author.lookupBooks(): " + ex.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { }
            try { if (stmt != null) stmt.close(); } catch (Exception e) { }
            try { if (conn != null) conn.close(); } catch (Exception e) { }
        }
        return result;
    }

    /**
     * Adds a book to this author's list. Also marks the book with the author name.
     * Creates a circular dependency between Author and Book.
     */
    public void addBook(Book book) {
        if (this.books == null) {
            this.books = new ArrayList();
        }
        this.books.add(book);
        // set author info on the book using free field (creates circular dependency)
        book.setFree3("AUTHOR_REF:" + this.nm);
        System.out.println("Added book '" + book.getTitle() + "' to author '" + nm + "'");
    }

    /**
     * Format biography for display. Different formatting based on data available.
     * Modified by T.Sato 2008/11 - added name prefix logic
     * Modified by H.Suzuki 2009/02 - added truncation for long bios
     * Modified by K.Tanaka 2009/08 - fixed NPE in some edge cases
     */
    public String formatBiography() {
        String result = "";
        String src = biography;
        if (src == null) {
            src = bio;
        }
        if (src != null) {
            if (src.length() > 1000) {
                if (first_nm != null && first_nm.length() > 0) {
                    if (last_nm != null && last_nm.length() > 0) {
                        if (src.indexOf(first_nm) >= 0) {
                            result = "=== " + last_nm + ", " + first_nm + " ===\n"
                                + src.substring(0, 1000) + "...\n[truncated]";
                        } else {
                            result = "=== " + first_nm + " " + last_nm + " ===\n"
                                + src.substring(0, 1000) + "...\n[truncated]";
                        }
                    } else {
                        if (author_name != null) {
                            result = "=== " + author_name + " ===\n"
                                + src.substring(0, 1000) + "...\n[truncated]";
                        } else {
                            result = "=== " + first_nm + " ===\n"
                                + src.substring(0, 1000) + "...\n[truncated]";
                        }
                    }
                } else {
                    if (nm != null) {
                        if (nm.length() > 50) {
                            result = "=== " + nm.substring(0, 50) + "... ===\n"
                                + src.substring(0, 1000) + "...\n[truncated]";
                        } else {
                            result = "=== " + nm + " ===\n"
                                + src.substring(0, 1000) + "...\n[truncated]";
                        }
                    } else {
                        result = src.substring(0, 1000) + "...\n[truncated]";
                    }
                }
            } else {
                if (first_nm != null && last_nm != null) {
                    if (src.indexOf(first_nm) >= 0 && src.indexOf(last_nm) >= 0) {
                        result = src;
                    } else {
                        if (author_name != null) {
                            result = "About " + author_name + ":\n" + src;
                        } else {
                            result = "About " + first_nm + " " + last_nm + ":\n" + src;
                        }
                    }
                } else {
                    if (nm != null && nm.length() > 0) {
                        result = "About " + nm + ":\n" + src;
                    } else {
                        result = src;
                    }
                }
            }
        } else {
            if (nm != null) {
                result = "No biography available for " + nm;
            } else if (author_name != null) {
                result = "No biography available for " + author_name;
            } else {
                result = "No biography available";
            }
        }
        return result;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public List getBooks() { return books; }
    public void setBooks(List books) { this.books = books; }

}
