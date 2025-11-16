package org.miniboot.app.util.errorvalidation;

import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * RateLimiter: Simple rate limiting implementation
 * Limits requests per IP address to prevent abuse
 * 
 * Algorithm: Sliding window with token bucket
 * - Each IP gets N tokens per time window
 * - Each request consumes 1 token
 * - Tokens refill after window expires
 */
public class RateLimiter {

    // Request limit per IP per time window
    private static final int MAX_REQUESTS_PER_WINDOW = 100;
    
    // Time window in milliseconds (1 minute)
    private static final long WINDOW_SIZE_MS = 60000;
    
    // Store request counts per IP
    private static final ConcurrentHashMap<String, RequestWindow> requestCounts = new ConcurrentHashMap<>();
    
    /**
     * Check if request should be rate limited
     * 
     * @param req HTTP request
     * @return HttpResponse with 429 if rate limited, null if allowed
     */
    public static HttpResponse checkRateLimit(HttpRequest req) {
        String clientIp = getClientIp(req);
        
        long currentTime = System.currentTimeMillis();
        
        // Get or create request window for this IP
        RequestWindow window = requestCounts.compute(clientIp, (ip, existingWindow) -> {
            if (existingWindow == null || currentTime - existingWindow.startTime >= WINDOW_SIZE_MS) {
                // Create new window
                return new RequestWindow(currentTime, 1);
            } else {
                // Increment existing window
                existingWindow.count++;
                return existingWindow;
            }
        });
        
        // Check if limit exceeded
        if (window.count > MAX_REQUESTS_PER_WINDOW) {
            long resetTime = window.startTime + WINDOW_SIZE_MS;
            long secondsUntilReset = (resetTime - currentTime) / 1000;
            
            return ValidationUtils.error(429, "TOO_MANY_REQUESTS",
                    "Rate limit exceeded. Maximum " + MAX_REQUESTS_PER_WINDOW + 
                    " requests per minute. Try again in " + secondsUntilReset + " seconds.");
        }
        
        return null; // Not rate limited
    }
    
    /**
     * Extract client IP from request
     * Checks X-Forwarded-For header first (for proxies), then falls back to remote address
     */
    private static String getClientIp(HttpRequest req) {
        // Check X-Forwarded-For header (for requests behind proxy/load balancer)
        String forwardedFor = req.headers.get("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return forwardedFor.split(",")[0].trim();
        }
        
        // Fall back to remote address
        String remoteAddr = req.headers.get("Remote-Addr");
        if (remoteAddr != null && !remoteAddr.isEmpty()) {
            return remoteAddr;
        }
        
        // Default fallback
        return "unknown";
    }
    
    /**
     * Reset rate limit for specific IP (useful for testing or admin override)
     */
    public static void resetRateLimit(String ip) {
        requestCounts.remove(ip);
    }
    
    /**
     * Clear all rate limit data (useful for testing)
     */
    public static void clearAllRateLimits() {
        requestCounts.clear();
    }
    
    /**
     * Request window for tracking requests per IP
     */
    private static class RequestWindow {
        long startTime;
        int count;
        
        RequestWindow(long startTime, int count) {
            this.startTime = startTime;
            this.count = count;
        }
    }
}
