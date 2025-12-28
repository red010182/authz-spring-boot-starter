package cn.omisheep.authz.core.resolver;

import cn.omisheep.authz.core.auth.ipf.HttpMeta;
import cn.omisheep.authz.core.tk.AccessToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

import jakarta.servlet.http.HttpServletRequest;

import static cn.omisheep.authz.core.config.Constants.HTTP_META;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuArgumentResolversTest {

    AuHttpMetaResolver httpMetaResolver;
    AuTokenOrHttpMetaResolver tokenOrHttpMetaResolver;
    NativeWebRequest webRequest;
    HttpServletRequest request;
    HttpMeta httpMeta;
    AccessToken accessToken;

    @BeforeEach
    void setup() {
        httpMetaResolver = new AuHttpMetaResolver();
        tokenOrHttpMetaResolver = new AuTokenOrHttpMetaResolver();
        
        webRequest = mock(NativeWebRequest.class);
        request = mock(HttpServletRequest.class);
        when(webRequest.getNativeRequest()).thenReturn(request);
        
        httpMeta = mock(HttpMeta.class);
        accessToken = mock(AccessToken.class);
        when(httpMeta.getToken()).thenReturn(accessToken);
    }

    @Test
    void testAuHttpMetaResolver() {
        MethodParameter param = mock(MethodParameter.class);
        
        // supportsParameter
        doReturn(HttpMeta.class).when(param).getParameterType();
        assertTrue(httpMetaResolver.supportsParameter(param));
        
        doReturn(String.class).when(param).getParameterType();
        assertFalse(httpMetaResolver.supportsParameter(param));
        
        // resolveArgument
        when(request.getAttribute(HTTP_META)).thenReturn(httpMeta);
        Object result = httpMetaResolver.resolveArgument(param, null, webRequest, null);
        assertEquals(httpMeta, result);
        
        when(request.getAttribute(HTTP_META)).thenReturn(null);
        result = httpMetaResolver.resolveArgument(param, null, webRequest, null);
        assertNull(result);
    }

    @Test
    void testAuTokenOrHttpMetaResolver() {
        MethodParameter param = mock(MethodParameter.class);
        
        // supportsParameter
        doReturn(HttpMeta.class).when(param).getParameterType();
        assertTrue(tokenOrHttpMetaResolver.supportsParameter(param));
        
        doReturn(AccessToken.class).when(param).getParameterType();
        assertTrue(tokenOrHttpMetaResolver.supportsParameter(param));
        
        doReturn(String.class).when(param).getParameterType();
        assertFalse(tokenOrHttpMetaResolver.supportsParameter(param));
        
        // resolveArgument - HttpMeta
        when(request.getAttribute(HTTP_META)).thenReturn(httpMeta);
        doReturn(HttpMeta.class).when(param).getParameterType();
        Object result = tokenOrHttpMetaResolver.resolveArgument(param, null, webRequest, null);
        assertEquals(httpMeta, result);
        
        // resolveArgument - AccessToken
        doReturn(AccessToken.class).when(param).getParameterType();
        result = tokenOrHttpMetaResolver.resolveArgument(param, null, webRequest, null);
        assertEquals(accessToken, result);
        
        // resolveArgument - null meta
        when(request.getAttribute(HTTP_META)).thenReturn(null);
        result = tokenOrHttpMetaResolver.resolveArgument(param, null, webRequest, null);
        assertNull(result);
    }
}
