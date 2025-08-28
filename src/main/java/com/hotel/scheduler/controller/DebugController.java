package com.hotel.scheduler.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Temporary debug controller to help identify client IP issues.
 * Remove this in production.
 */
@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @GetMapping("/ip-info")
    public Map<String, String> getIpInfo(HttpServletRequest request) {
        Map<String, String> ipInfo = new HashMap<>();
        
        ipInfo.put("remoteAddr", request.getRemoteAddr());
        ipInfo.put("cf-connecting-ip", request.getHeader("CF-Connecting-IP"));
        ipInfo.put("x-forwarded-for", request.getHeader("X-Forwarded-For"));
        ipInfo.put("x-real-ip", request.getHeader("X-Real-IP"));
        ipInfo.put("x-originating-ip", request.getHeader("X-Originating-IP"));
        ipInfo.put("x-client-ip", request.getHeader("X-Client-IP"));
        ipInfo.put("proxy-client-ip", request.getHeader("Proxy-Client-IP"));
        ipInfo.put("wl-proxy-client-ip", request.getHeader("WL-Proxy-Client-IP"));
        
        // Add all headers for debugging
        Map<String, String> allHeaders = new HashMap<>();
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> 
            allHeaders.put(headerName.toLowerCase(), request.getHeader(headerName))
        );
        
        ipInfo.put("all-headers", allHeaders.toString());
        
        return ipInfo;
    }
}
