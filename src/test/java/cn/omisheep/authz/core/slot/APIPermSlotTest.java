package cn.omisheep.authz.core.slot;

import cn.omisheep.authz.core.LogLevel;
import cn.omisheep.authz.core.auth.PermLibrary;
import cn.omisheep.authz.core.auth.ipf.HttpMeta;
import cn.omisheep.authz.core.util.LogUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.HandlerMethod;

import java.util.Date;

import static org.mockito.Mockito.*;

class APIPermSlotTest {

    private APIPermSlot apiPermSlot;
    private PermLibrary permLibrary;
    private HttpMeta httpMeta;
    private HandlerMethod handler;
    private Error error;

    @BeforeAll
    static void staticSetup() {
        LogUtils.setLogLevel(LogLevel.INFO);
    }

    @BeforeEach
    void setup() {
        permLibrary = mock(PermLibrary.class);
        apiPermSlot = new APIPermSlot(permLibrary);
        
        httpMeta = mock(HttpMeta.class);
        handler = mock(HandlerMethod.class);
        error = mock(Error.class);
        
        when(httpMeta.getMethod()).thenReturn("GET");
        when(httpMeta.getApi()).thenReturn("/api/test");
        when(httpMeta.getNow()).thenReturn(new Date());
    }

    @Test
    void testChainWithNoApiAuth() {
        when(httpMeta.isHasApiAuth()).thenReturn(false);
        
        apiPermSlot.chain(httpMeta, handler, error);
        
        verify(error, never()).error(any());
    }

    @Test
    void testChainWithApiAuth() {
        when(httpMeta.isHasApiAuth()).thenReturn(true);
        when(httpMeta.getController()).thenReturn("testController");
        
        apiPermSlot.chain(httpMeta, handler, error);
        
        // With no permissions configured, should pass
        verify(error, never()).error(any());
    }
}
