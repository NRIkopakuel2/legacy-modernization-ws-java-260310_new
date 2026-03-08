package com.example.bookstore.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Simple request/response character encoding filter used by the legacy Struts app.
 */
public class CharacterEncodingFilter implements Filter {

    private String encoding = "UTF-8";
    private boolean forceEncoding;

    public void init(FilterConfig filterConfig) throws ServletException {
        String configuredEncoding = filterConfig.getInitParameter("encoding");
        if (configuredEncoding != null && configuredEncoding.trim().length() > 0) {
            encoding = configuredEncoding.trim();
        }

        String configuredForceEncoding = filterConfig.getInitParameter("forceEncoding");
        if (configuredForceEncoding != null) {
            forceEncoding = Boolean.valueOf(configuredForceEncoding).booleanValue();
        }
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (forceEncoding || request.getCharacterEncoding() == null) {
            request.setCharacterEncoding(encoding);
        }

        response.setCharacterEncoding(encoding);
        chain.doFilter(request, response);
    }

    public void destroy() {
        // no-op
    }
}
