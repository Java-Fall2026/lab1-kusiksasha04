package ua.restaurant.api;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LoggingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        long start = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } finally {
            long millis = (System.nanoTime() - start) / 1_000_000;
            String query = request.getQueryString();
            String target = request.getRequestURI() + (query == null ? "" : "?" + query);
            int status = response.getStatus();
            if (status >= 500) {
                log.error("{} {} -> {} ({} ms)", request.getMethod(), target, status, millis);
            } else if (status >= 400) {
                log.warn("{} {} -> {} ({} ms)", request.getMethod(), target, status, millis);
            } else {
                log.info("{} {} -> {} ({} ms)", request.getMethod(), target, status, millis);
            }
        }
    }
}
