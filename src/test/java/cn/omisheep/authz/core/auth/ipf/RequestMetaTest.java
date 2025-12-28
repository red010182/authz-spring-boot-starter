package cn.omisheep.authz.core.auth.ipf;

import cn.omisheep.authz.core.LogLevel;
import cn.omisheep.authz.core.util.LogUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestMetaTest {

    @BeforeAll
    static void setup() {
        LogUtils.setLogLevel(LogLevel.INFO);
    }

    @Test
    void testRequestMetaCreation() {
        long now = System.currentTimeMillis();
        RequestMeta meta = new RequestMeta(now, "192.168.1.1", null);
        
        assertNotNull(meta);
        assertEquals("192.168.1.1", meta.getIp());
        assertNull(meta.getUserId());
        assertFalse(meta.isBan());
    }

    @Test
    void testRequestMetaWithUserId() {
        long now = System.currentTimeMillis();
        RequestMeta meta = new RequestMeta(now, null, "user123");
        
        assertNotNull(meta);
        assertNull(meta.getIp());
        assertEquals("user123", meta.getUserId());
    }

    @Test
    void testRequestMethod() {
        long now = System.currentTimeMillis();
        RequestMeta meta = new RequestMeta(now, "192.168.1.1", null);
        
        // Request within limits should return true
        boolean result = meta.request(now + 1000, 10, 60000, 100);
        assertTrue(result);
    }

    @Test
    void testEnableRelive() {
        long now = System.currentTimeMillis();
        RequestMeta meta = new RequestMeta(now, "192.168.1.1", null);
        
        // Without being banned, reliveTime is 0, so should be relive-able
        assertTrue(meta.enableRelive(now));
    }

    @Test
    void testSinceLastTime() {
        long now = System.currentTimeMillis();
        RequestMeta meta = new RequestMeta(now, "192.168.1.1", null);
        meta.setLastRequestTime(now);
        
        String sinceLastTime = meta.sinceLastTime();
        assertNotNull(sinceLastTime);
    }

    @Test
    void testGetLastRequestTime() {
        long now = System.currentTimeMillis();
        RequestMeta meta = new RequestMeta(now, "192.168.1.1", null);
        
        assertNotNull(meta.getLastRequestTime());
    }

    @Test
    void testGetRequestTimeList() {
        long now = System.currentTimeMillis();
        RequestMeta meta = new RequestMeta(now, "192.168.1.1", null);
        
        assertNotNull(meta.getRequestTimeList());
        assertFalse(meta.getRequestTimeList().isEmpty());
    }

    @Test
    void testEqualsAndHashCode() {
        long now = System.currentTimeMillis();
        RequestMeta meta1 = new RequestMeta(now, "192.168.1.1", null);
        RequestMeta meta2 = new RequestMeta(now, "192.168.1.1", null);
        RequestMeta meta3 = new RequestMeta(now, "192.168.1.2", null);
        
        // equals and hashCode
        assertNotNull(meta1.hashCode());
        assertFalse(meta1.equals(meta3));
    }
}
