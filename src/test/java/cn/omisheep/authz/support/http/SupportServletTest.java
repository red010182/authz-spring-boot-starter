package cn.omisheep.authz.support.http;

import cn.omisheep.authz.core.AuthzContext;
import cn.omisheep.authz.core.AuthzProperties;
import cn.omisheep.authz.core.auth.PermLibrary;
import cn.omisheep.authz.core.auth.deviced.UserDevicesDict;
import cn.omisheep.authz.core.cache.Cache;
import cn.omisheep.authz.core.oauth.OpenAuthLibrary;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SupportServletTest {

    private SupportServlet servlet;
    
    @Mock
    private Cache cache;
    
    @BeforeAll
    static void initContext() {
        ApplicationContext ctx = mock(ApplicationContext.class);
        AuthzProperties properties = new AuthzProperties();
        AuthzProperties.TokenConfig tokenConfig = new AuthzProperties.TokenConfig();
        tokenConfig.setKey("12345678901234567890123456789012");
        properties.setToken(tokenConfig);
        
        when(ctx.getBean(AuthzProperties.class)).thenReturn(properties);
        when(ctx.getBean(UserDevicesDict.class)).thenReturn(mock(UserDevicesDict.class));
        when(ctx.getBean("authzCache", Cache.class)).thenReturn(mock(Cache.class));
        when(ctx.getBean(PermLibrary.class)).thenReturn(mock(PermLibrary.class));
        when(ctx.getBean(OpenAuthLibrary.class)).thenReturn(mock(OpenAuthLibrary.class));

        AuthzContext.init(ctx);
    }
    
    @BeforeEach
    void setUp() {
        AuthzProperties.DashboardConfig config = new AuthzProperties.DashboardConfig();
        config.setUsername("admin");
        config.setPassword("admin");
        // default allow/deny is empty
        
        servlet = new SupportServlet(config, cache);
    }
    
    @Test
    void testService_RootRedirect() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setContextPath("");
        request.setServletPath("/authz");
        request.setRequestURI("/authz/"); 
        // path calculation in servlet: 
        // uri = contextPath + servletPath
        // path = requestURI.substring(contextPath.length() + servletPath.length())
        // if requestURI is "/authz/", path is "/"
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        servlet.service(request, response);
        
        assertEquals(302, response.getStatus()); // redirect
        assertTrue(response.getRedirectedUrl().contains("authz.html"));
    }
}
