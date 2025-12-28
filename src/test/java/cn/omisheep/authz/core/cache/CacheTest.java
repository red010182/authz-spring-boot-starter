package cn.omisheep.authz.core.cache;

import cn.omisheep.authz.core.LogLevel;
import cn.omisheep.authz.core.util.LogUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CacheTest {

    @BeforeAll
    static void setup() {
        LogUtils.setLogLevel(LogLevel.INFO);
    }

    @Test
    void testCacheExpiry() {
        Cache.CacheExpiry expiry = new Cache.CacheExpiry();
        assertNotNull(expiry);
        
        // Test with time values
        Cache.CacheExpiry expiryWithValues = new Cache.CacheExpiry(1000L, 2000L, 3000L);
        assertNotNull(expiryWithValues);
    }

    @Test
    void testCacheItem() {
        String value = "testValue";
        
        // CacheItem(long ttl, E value) - ttl in seconds
        Cache.CacheItem<String> item = new Cache.CacheItem<>(60, value);  // 60 seconds TTL
        
        assertEquals(value, item.getValue());
        assertTrue(item.ttl() > 0);  // Should have remaining TTL
        
        // Infinite TTL
        Cache.CacheItem<String> infiniteItem = new Cache.CacheItem<>(value);
        assertEquals(Cache.INFINITE, infiniteItem.ttl());
    }

    @Test
    void testCacheItemExpireAfterNanos() {
        String value = "test";
        Cache.CacheItem<String> item = new Cache.CacheItem<>(value);
        
        long nanos = item.expireAfterNanos(1000000000L);
        assertEquals(1000000000L, nanos); // infinite TTL returns expireNanos
    }
}
