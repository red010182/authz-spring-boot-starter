package cn.omisheep.authz.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthzExceptionTest {

    @Test
    void testAuthzExceptionWithExceptionStatus() {
        ExceptionStatus status = ExceptionStatus.PERM_EXCEPTION;
        AuthzException exception = new AuthzException(status);
        
        assertEquals(status, exception.getExceptionStatus());
        assertEquals(status.getMessage(), exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testAuthzExceptionWithCauseAndExceptionStatus() {
        ExceptionStatus status = ExceptionStatus.ACCESS_TOKEN_OVERDUE;
        Throwable cause = new RuntimeException("Root cause");
        AuthzException exception = new AuthzException(cause, status);
        
        assertEquals(status, exception.getExceptionStatus());
        assertEquals(status.getMessage(), exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    void testAuthzExceptionWithCauseOnly() {
        Throwable cause = new RuntimeException("Root cause");
        AuthzException exception = new AuthzException(cause);
        
        assertEquals(ExceptionStatus.UNKNOWN, exception.getExceptionStatus());
        assertEquals(cause.getMessage(), exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    void testOfFactoryMethod() {
        ExceptionStatus status = ExceptionStatus.REQUIRE_LOGIN;
        AuthzException exception = AuthzException.of(status);
        
        assertEquals(status, exception.getExceptionStatus());
        assertEquals(status.getMessage(), exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testExceptionInheritance() {
        AuthzException exception = new AuthzException(ExceptionStatus.UNKNOWN);
        
        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof Throwable);
    }
}
