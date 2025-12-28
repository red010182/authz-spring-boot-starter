package cn.omisheep.authz.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionClassesTest {

    @Test
    void testNotLoginException() {
        NotLoginException exception = new NotLoginException();
        
        assertEquals(ExceptionStatus.REQUIRE_LOGIN, exception.getExceptionStatus());
        assertEquals(ExceptionStatus.REQUIRE_LOGIN.getMessage(), exception.getMessage());
        assertNull(exception.getCause());
        assertTrue(exception instanceof AuthzException);
    }

    @Test
    void testPermissionException() {
        PermissionException exception = new PermissionException();
        
        assertEquals(ExceptionStatus.PERM_EXCEPTION, exception.getExceptionStatus());
        assertEquals(ExceptionStatus.PERM_EXCEPTION.getMessage(), exception.getMessage());
        assertNull(exception.getCause());
        assertTrue(exception instanceof AuthzException);
    }

    @Test
    void testTokenException() {
        TokenException exception = new TokenException();
        
        assertEquals(ExceptionStatus.TOKEN_EXCEPTION, exception.getExceptionStatus());
        assertEquals(ExceptionStatus.TOKEN_EXCEPTION.getMessage(), exception.getMessage());
        assertNull(exception.getCause());
        assertTrue(exception instanceof AuthzException);
    }

    @Test
    void testRefreshTokenExpiredException() {
        RefreshTokenExpiredException exception = new RefreshTokenExpiredException();
        
        assertEquals(ExceptionStatus.REFRESH_TOKEN_EXPIRED_EXCEPTION, exception.getExceptionStatus());
        assertEquals(ExceptionStatus.REFRESH_TOKEN_EXPIRED_EXCEPTION.getMessage(), exception.getMessage());
        assertNull(exception.getCause());
        assertTrue(exception instanceof AuthzException);
    }

    @Test
    void testThreadWebEnvironmentException() {
        ThreadWebEnvironmentException exception = new ThreadWebEnvironmentException();
        
        assertEquals(ExceptionStatus.WEB_ENVIRONMENT, exception.getExceptionStatus());
        assertEquals(ExceptionStatus.WEB_ENVIRONMENT.getMessage(), exception.getMessage());
        assertNull(exception.getCause());
        assertTrue(exception instanceof AuthzException);
    }

    @Test
    void testExceptionStatusEnum() {
        // Test a few key exception statuses
        assertEquals("Require login", ExceptionStatus.REQUIRE_LOGIN.getMessage());
        assertEquals("Insufficient permissions", ExceptionStatus.PERM_EXCEPTION.getMessage());
        assertEquals("AccessToken overdue", ExceptionStatus.ACCESS_TOKEN_OVERDUE.getMessage());
        assertEquals("RefreshToken expired", ExceptionStatus.REFRESH_TOKEN_EXPIRED_EXCEPTION.getMessage());
        assertEquals("Token exception", ExceptionStatus.TOKEN_EXCEPTION.getMessage());
        assertEquals("The current thread is in a non Web Environment", ExceptionStatus.WEB_ENVIRONMENT.getMessage());
        assertEquals("unknown", ExceptionStatus.UNKNOWN.getMessage());
        
        // Test all values exist
        ExceptionStatus[] values = ExceptionStatus.values();
        assertTrue(values.length > 0);
        
        // Test valueOf
        assertEquals(ExceptionStatus.REQUIRE_LOGIN, ExceptionStatus.valueOf("REQUIRE_LOGIN"));
        assertEquals(ExceptionStatus.PERM_EXCEPTION, ExceptionStatus.valueOf("PERM_EXCEPTION"));
        assertEquals(ExceptionStatus.ACCESS_TOKEN_OVERDUE, ExceptionStatus.valueOf("ACCESS_TOKEN_OVERDUE"));
    }

    @Test
    void testLogLevelEnum() {
        // Test LogLevel values
        LogLevel[] levels = LogLevel.values();
        assertEquals(5, levels.length);
        
        assertEquals(LogLevel.DEBUG, LogLevel.valueOf("DEBUG"));
        assertEquals(LogLevel.INFO, LogLevel.valueOf("INFO"));
        assertEquals(LogLevel.WARN, LogLevel.valueOf("WARN"));
        assertEquals(LogLevel.ERROR, LogLevel.valueOf("ERROR"));
        assertEquals(LogLevel.OFF, LogLevel.valueOf("OFF"));
        
        // Test ordinal positions
        assertEquals(0, LogLevel.DEBUG.ordinal());
        assertEquals(1, LogLevel.INFO.ordinal());
        assertEquals(2, LogLevel.WARN.ordinal());
        assertEquals(3, LogLevel.ERROR.ordinal());
        assertEquals(4, LogLevel.OFF.ordinal());
    }
}
