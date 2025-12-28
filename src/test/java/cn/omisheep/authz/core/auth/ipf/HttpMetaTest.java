package cn.omisheep.authz.core.auth.ipf;

import cn.omisheep.authz.core.AuthzContext;
import cn.omisheep.authz.core.auth.PermLibrary;
import cn.omisheep.authz.core.tk.AccessToken;
import cn.omisheep.authz.core.util.LogUtils;
import cn.omisheep.authz.core.LogLevel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.context.ApplicationContext;

import java.util.HashSet;
import java.util.Set;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HttpMetaTest {

    static PermLibrary permLibrary;

    @BeforeAll
    static void setup() {
        LogUtils.setLogLevel(LogLevel.INFO);
        ApplicationContext ctx = mock(ApplicationContext.class);
        permLibrary = mock(PermLibrary.class);
        when(ctx.getBean(PermLibrary.class)).thenReturn(permLibrary);
        AuthzContext.init(ctx);
    }

    @Test
    void testHttpMetaBasic() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        request.addHeader("User-Agent", "Mozilla/5.0");
        
        HttpMeta meta = new HttpMeta(request, "/api/test", "/api/test");
        assertEquals("GET", meta.getMethod());
        assertEquals("/api/test", meta.getUri());
        assertNotNull(meta.getUserAgent());
    }

    @Test
    void testGetBody() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/test");
        request.setContent("hello".getBytes(StandardCharsets.UTF_8));
        
        HttpMeta meta = new HttpMeta(request, "/api/test", "/api/test");
        String body = meta.getBody();
        assertEquals("hello", body);
        
        // Calling again should return cached body
        assertEquals("hello", meta.getBody());
    }
    
    @Test
    void testGetRolesAndPermissions() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        HttpMeta meta = new HttpMeta(request, "/api", "/api");
        
        // AccessToken(id, token, tokenId, expiresIn, expiresAt, grantType, clientId, scope, userId, deviceType, deviceId)
        AccessToken token = new AccessToken(
            "id", "token", "tokenId", 1000L, System.currentTimeMillis() + 1000, 
            null, "client", "scope", "user1", "web", "device1"
        );
        meta.setToken(token);
        
        Set<String> roles = new HashSet<>();
        roles.add("admin");
        when(permLibrary.getRolesByUserId("user1")).thenReturn(roles);
        when(permLibrary.getPermissionsByRole("admin")).thenReturn(Set.of("read"));
        
        Set<String> userRoles = meta.getRoles();
        assertTrue(userRoles.contains("admin"));
        
        Set<String> perms = meta.getPermissions();
        assertTrue(perms.contains("read"));
    }
}
