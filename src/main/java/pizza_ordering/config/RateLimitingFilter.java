package pizza_ordering.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int LIMIT = 60;
    private static final long WINDOW_SECONDS = 60;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String key = request.getRemoteAddr() + ":" + request.getRequestURI();
        Bucket bucket = buckets.computeIfAbsent(key, unused -> new Bucket());
        long now = Instant.now().getEpochSecond();

        synchronized (bucket) {
            if (now - bucket.windowStart >= WINDOW_SECONDS) {
                bucket.windowStart = now;
                bucket.requestCount = 0;
            }

            bucket.requestCount++;
            if (bucket.requestCount > LIMIT) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Rate limit exceeded. Try again in a minute.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private static final class Bucket {
        private long windowStart = Instant.now().getEpochSecond();
        private int requestCount;
    }
}
