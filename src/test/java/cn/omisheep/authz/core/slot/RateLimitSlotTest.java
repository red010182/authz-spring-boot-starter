package cn.omisheep.authz.core.slot;

import cn.omisheep.authz.core.AuthzProperties;
import cn.omisheep.authz.core.LogLevel;
import cn.omisheep.authz.core.auth.ipf.HttpMeta;
import cn.omisheep.authz.core.tk.AccessToken;
import cn.omisheep.authz.core.util.LogUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.HandlerMethod;

import java.util.Date;

import static org.mockito.Mockito.*;

class RateLimitSlotTest {

    private RateLimitSlot rateLimitSlot;
    private AuthzProperties properties;
    private HttpMeta httpMeta;
    private HandlerMethod handler;
    private Error error;

    @BeforeAll
    static void staticSetup() {
        LogUtils.setLogLevel(LogLevel.INFO);
    }

    @BeforeEach
    void setup() {
        properties = mock(AuthzProperties.class);
        AuthzProperties.CacheConfig cacheConfig = mock(AuthzProperties.CacheConfig.class);
        when(properties.getCache()).thenReturn(cacheConfig);
        when(cacheConfig.isEnableRedis()).thenReturn(false);
        
        rateLimitSlot = new RateLimitSlot(properties);
        
        httpMeta = mock(HttpMeta.class);
        handler = mock(HandlerMethod.class);
        error = mock(Error.class);
        
        when(httpMeta.getIp()).thenReturn("192.168.1.1");
        when(httpMeta.getNow()).thenReturn(new Date());
        when(httpMeta.getMethod()).thenReturn("GET");
        when(httpMeta.getServletPath()).thenReturn("/api/test");
        when(httpMeta.getApi()).thenReturn("/api/test");
    }

    @Test
    void testChainWithNoLimitMeta() {
        // When no limit metadata exists, should just log and return
        when(httpMeta.hasToken()).thenReturn(false);
        
        rateLimitSlot.chain(httpMeta, handler, error);
        
        // Verify error was never called (no rate limit violation)
        verify(error, never()).error(any());
    }

    @Test
    void testChainWithTokenButNoLimit() {
        when(httpMeta.hasToken()).thenReturn(true);
        AccessToken token = mock(AccessToken.class);
        when(token.getUserId()).thenReturn("user1");
        when(token.getDeviceType()).thenReturn("web");
        when(token.getDeviceId()).thenReturn("device1");
        when(token.getClientId()).thenReturn("client1");
        when(httpMeta.getToken()).thenReturn(token);
        
        rateLimitSlot.chain(httpMeta, handler, error);
        
        verify(error, never()).error(any());
    }
}
