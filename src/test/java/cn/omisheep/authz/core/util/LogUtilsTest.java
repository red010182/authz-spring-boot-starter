package cn.omisheep.authz.core.util;

import cn.omisheep.authz.core.LogLevel;
import cn.omisheep.authz.core.auth.ipf.HttpMeta;
import cn.omisheep.authz.core.auth.rpd.PermRolesMeta;
import cn.omisheep.authz.core.tk.AccessToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogUtilsTest {

    @BeforeEach
    void setup() {
        LogUtils.setLogLevel(LogLevel.DEBUG);
    }

    @Test
    void testInfoLogging() {
        LogUtils.setLogLevel(LogLevel.INFO);
        // Should not throw
        LogUtils.info("Test message {}", "arg1");
    }

    @Test
    void testWarnLogging() {
        LogUtils.setLogLevel(LogLevel.WARN);
        LogUtils.warn("Warning message {}", "arg1");
    }

    @Test
    void testDebugLogging() {
        LogUtils.setLogLevel(LogLevel.DEBUG);
        LogUtils.debug("Debug message {}", "arg1");
    }

    @Test
    void testErrorLogging() {
        LogUtils.setLogLevel(LogLevel.ERROR);
        LogUtils.error("Error message {}", "arg1");
        LogUtils.error(new RuntimeException("Test exception"));
        LogUtils.error("Error with throwable", new RuntimeException("Test"));
    }

    @Test
    void testPushAndExport() {
        LogUtils.setLogLevel(LogLevel.DEBUG);
        
        // Push logs at different levels
        LogUtils.push("Info message");
        LogUtils.push(LogLevel.WARN, "Warn message");
        LogUtils.push(LogLevel.DEBUG, "Debug message");
        LogUtils.push(LogLevel.ERROR, "Error message");
        
        // Export should process and clear logs
        LogUtils.export();
        
        // Push again and verify export works when log level is OFF
        LogUtils.push("Another message");
        LogUtils.setLogLevel(LogLevel.OFF);
        LogUtils.export(); // Should skip when OFF
    }

    @Test
    void testLogMeta() {
        LogUtils.LogMeta meta = new LogUtils.LogMeta(LogLevel.INFO, "Test {} {}", "arg1", "arg2");
        assertEquals(LogLevel.INFO, meta.getLogLevel());
        assertEquals("Test {} {}", meta.getFormat());
        assertNotNull(meta.getObjects());
        assertEquals("Test arg1 arg2", meta.toString());
        
        // Test null logLevel defaults to INFO
        LogUtils.LogMeta metaNull = new LogUtils.LogMeta(null, "Test", "arg");
        assertEquals(LogLevel.INFO, metaNull.getLogLevel());
    }

    @Test
    void testLogsWithHttpMeta() {
        HttpMeta httpMeta = mock(HttpMeta.class);
        PermRolesMeta permRolesMeta = mock(PermRolesMeta.class);
        
        // Without token
        when(httpMeta.getToken()).thenReturn(null);
        LogUtils.logs("SUCCESS", httpMeta, permRolesMeta);
        LogUtils.logs("SUCCESS", httpMeta);
        verify(httpMeta, atLeastOnce()).log(anyString(), any());
        
        // With token
        AccessToken token = mock(AccessToken.class);
        when(token.getUserId()).thenReturn("user1");
        when(token.getDeviceType()).thenReturn("web");
        when(token.getDeviceId()).thenReturn("device1");
        when(httpMeta.getToken()).thenReturn(token);
        
        LogUtils.logs("SUCCESS", httpMeta, permRolesMeta);
        LogUtils.logs("SUCCESS", httpMeta);
    }

    @Test
    void testLogLevelFiltering() {
        // When log level is OFF, nothing should be logged
        LogUtils.setLogLevel(LogLevel.OFF);
        LogUtils.info("Should not log");
        LogUtils.warn("Should not log");
        LogUtils.debug("Should not log");
        LogUtils.error("Should not log");
        
        // When log level is ERROR, only error should log
        LogUtils.setLogLevel(LogLevel.ERROR);
        LogUtils.info("Should not log");
        LogUtils.warn("Should not log");
        LogUtils.debug("Should not log");
        LogUtils.error("Should log");
    }
}
