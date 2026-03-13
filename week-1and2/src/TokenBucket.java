import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

class TokenBucket {
    private int maxTokens;
    private double refillRate;
    private double tokens;
    private long lastRefillTime;

    public TokenBucket(int maxTokens, double refillRate) {
        this.maxTokens = maxTokens;
        this.refillRate = refillRate;
        this.tokens = maxTokens;
        this.lastRefillTime = System.currentTimeMillis();
    }

    public synchronized Result allowRequest() {
        refill();
        if (tokens >= 1) {
            tokens -= 1;
            return new Result(true, (int) tokens, getRetryAfter());
        } else {
            return new Result(false, 0, getRetryAfter());
        }
    }

    private void refill() {
        long now = System.currentTimeMillis();
        double seconds = (now - lastRefillTime) / 1000.0;
        double refill = seconds * refillRate;
        if (refill > 0) {
            tokens = Math.min(maxTokens, tokens + refill);
            lastRefillTime = now;
        }
    }

    private long getRetryAfter() {
        if (tokens >= 1) return 0;
        double needed = 1 - tokens;
        return (long) Math.ceil(needed / refillRate);
    }

    public synchronized Status getStatus() {
        refill();
        int used = maxTokens - (int) tokens;
        long reset = System.currentTimeMillis() / 1000 + (long)((maxTokens - tokens) / refillRate);
        return new Status(used, maxTokens, reset);
    }
}

class Result {
    boolean allowed;
    int remaining;
    long retryAfter;

    Result(boolean allowed, int remaining, long retryAfter) {
        this.allowed = allowed;
        this.remaining = remaining;
        this.retryAfter = retryAfter;
    }
}

class Status {
    int used;
    int limit;
    long reset;

    Status(int used, int limit, long reset) {
        this.used = used;
        this.limit = limit;
        this.reset = reset;
    }
}

class RateLimiter {
    private ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private static final int LIMIT = 1000;
    private static final double REFILL_RATE = 1000.0 / 3600.0;

    public Result checkRateLimit(String clientId) {
        TokenBucket bucket = buckets.computeIfAbsent(clientId,
                k -> new TokenBucket(LIMIT, REFILL_RATE));
        return bucket.allowRequest();
    }

    public Status getRateLimitStatus(String clientId) {
        TokenBucket bucket = buckets.computeIfAbsent(clientId,
                k -> new TokenBucket(LIMIT, REFILL_RATE));
        return bucket.getStatus();
    }
}

 class Main {
    public static void main(String[] args) {

        RateLimiter limiter = new RateLimiter();

        for (int i = 0; i < 1002; i++) {
            Result r = limiter.checkRateLimit("abc123");

            if (r.allowed) {
                System.out.println("Allowed (" + r.remaining + " requests remaining)");
            } else {
                System.out.println("Denied (0 requests remaining, retry after " + r.retryAfter + "s)");
            }
        }

        Status status = limiter.getRateLimitStatus("abc123");
        System.out.println("{used: " + status.used + ", limit: " + status.limit + ", reset: " + status.reset + "}");
    }
}