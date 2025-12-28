package cn.omisheep.authz.core.auth.ipf;

import cn.omisheep.authz.core.AuthzContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.context.ApplicationContext;
import cn.omisheep.authz.core.AuthzProperties;
import cn.omisheep.authz.core.auth.deviced.UserDevicesDict;
import cn.omisheep.authz.core.cache.Cache;
import cn.omisheep.authz.core.auth.PermLibrary;
import cn.omisheep.authz.core.oauth.OpenAuthLibrary;

@ExtendWith(MockitoExtension.class)
class AuthzHttpFilterTest {

    private AuthzHttpFilter filter;

    @Mock
    private FilterChain filterChain;

    @BeforeAll
    static void initContext() {
        ApplicationContext ctx = mock(ApplicationContext.class);
        AuthzProperties properties = new AuthzProperties();
        AuthzProperties.TokenConfig tokenConfig = new AuthzProperties.TokenConfig();
        tokenConfig.setKey("12345678901234567890123456789012");
        tokenConfig.setAccessTime("1h");
        tokenConfig.setRefreshTime("7d");
        properties.setToken(tokenConfig);
        
        when(ctx.getBean(AuthzProperties.class)).thenReturn(properties);
        
        // Mock other dependencies of BaseHelper to avoid NPEs if used
        when(ctx.getBean(UserDevicesDict.class)).thenReturn(mock(UserDevicesDict.class));
        when(ctx.getBean("authzCache", Cache.class)).thenReturn(mock(Cache.class));
        when(ctx.getBean(PermLibrary.class)).thenReturn(mock(PermLibrary.class));
        when(ctx.getBean(OpenAuthLibrary.class)).thenReturn(mock(OpenAuthLibrary.class));
        
        AuthzContext.init(ctx);
    }

    @BeforeEach
    void setUp() {
        filter = new AuthzHttpFilter(false); // Not dashboard mode
    }

    @Test
    void testDoFilterInternal_NormalRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/test");
        request.setServletPath("/api/test");
        
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(any(), eq(response));
        
        // Verify HttpUtils context was set (indirectly)
        // Accessing AuthzContext.currentHttpMeta might require real request context or thread local setup
        // But the filter sets it.
    }

    @Test
    void testDoFilterInternal_IgnoreSuffix() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/assets/style.css");
        request.setServletPath("/assets/style.css");
        
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        // Verify it continues chain
        verify(filterChain).doFilter(any(), eq(response));
    }
}
