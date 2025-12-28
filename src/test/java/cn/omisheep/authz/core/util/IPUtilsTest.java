package cn.omisheep.authz.core.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IPUtilsTest {

    @Mock
    HttpServletRequest request;

    @Test
    void testGetIpFromRemoteAddr() {
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        assertEquals("192.168.1.1", IPUtils.getIp(request));
    }

    @Test
    void testGetIpFromHeader() {
        when(request.getHeader("x-forwarded-for")).thenReturn("10.0.0.1");
        assertEquals("10.0.0.1", IPUtils.getIp(request));
    }

    @Test
    void testGetIpMultiProxy() {
        when(request.getHeader("x-forwarded-for")).thenReturn("unknown, 10.0.0.1, 192.168.1.1");
        assertEquals("192.168.1.1", IPUtils.getIp(request));
    }
    
    @Test
    void testLocalIpv6() {
        when(request.getRemoteAddr()).thenReturn("0:0:0:0:0:0:0:1");
        assertEquals("127.0.0.1", IPUtils.getIp(request));
    }
}
