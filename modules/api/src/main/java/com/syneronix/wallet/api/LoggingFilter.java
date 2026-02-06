package com.syneronix.wallet.api;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Slf4j
public class LoggingFilter extends GenericFilterBean {

    private final Environment environment;

    public LoggingFilter(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String requestURI = httpServletRequest.getRequestURI();
        String method = httpServletRequest.getMethod();
        ContentCachingRequestWrapper wreq = new ContentCachingRequestWrapper(httpServletRequest);
        ContentCachingResponseWrapper wres =
                new ContentCachingResponseWrapper((HttpServletResponse) response);

        chain.doFilter(wreq, wres);
        while (wreq.getInputStream().read() >= 0) ;

        if (requestURI != null && (requestURI.startsWith("/api"))) {

            String res = new String(wres.getContentAsByteArray());
            String req = new String(wreq.getContentAsByteArray());

            if (wres.getStatus() >= 400 && wres.getStatus() < 500) {
                log.warn("Request {} {}\nRequestBody:\n{}\nResponseBody:\n{}", method, requestURI, req, res);
            }
            else if (wres.getStatus() >= 500) {
                log.error("Request {} {}\nRequestBody:\n{}\nResponseBody:\n{}", method, requestURI, req, res);
            }
            else {
                log.debug("Request {} {}\nRequestBody:\n{}\nResponseBody:\n{}", method, requestURI, req, res);
            }
        }
        wres.copyBodyToResponse();
    }
}
