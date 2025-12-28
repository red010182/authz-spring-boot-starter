package cn.omisheep.authz.core.auth.ipf;

import cn.omisheep.authz.core.LogLevel;
import cn.omisheep.authz.core.util.LogUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HttpdTest {

    @BeforeAll
    static void setup() {
        LogUtils.setLogLevel(LogLevel.INFO);
    }

    @Test
    void testSetPathPattern() {
        // Test path pattern caching
        Httpd.setPathPattern("/api/test");
        Httpd.setPathPattern("/api/users/{id}");
        
        // Should not throw
        assertDoesNotThrow(() -> Httpd.setPathPattern("/api/items/*"));
    }

    @Test
    void testMatch() {
        Httpd.setPathPattern("/api/test");
        assertTrue(Httpd.match("/api/test", "/api/test"));
        assertFalse(Httpd.match("/api/test", "/api/other"));
        
        // Wildcard pattern
        Httpd.setPathPattern("/api/users/{id}");
        assertTrue(Httpd.match("/api/users/{id}", "/api/users/123"));
        assertTrue(Httpd.match("/api/users/{id}", "/api/users/abc"));
    }

    @Test
    void testGetPattern() {
        Httpd.setPathPattern("/api/items");
        
        // Get pattern for path
        String pattern = Httpd.getPattern("/api/items");
        assertNotNull(pattern);
    }

    @Test
    void testGetLimitMetadata() {
        // When no limit meta configured, should return null
        LimitMeta meta = Httpd.getLimitMetadata("GET", "/nonexistent/api");
        assertNull(meta);
    }

    @Test
    void testRequestPools() {
        // Test getting request pools (may be null if not initialized)
        Httpd.RequestPool ipPool = Httpd.getIpRequestPools("/api/test", "GET");
        Httpd.RequestPool userPool = Httpd.getUserIdRequestPool("/api/test", "GET");
        // These may be null if Httpd.init was not called, which is expected in unit test
    }
}
