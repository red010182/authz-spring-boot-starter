package cn.omisheep.authz.core.oauth;

import cn.omisheep.authz.core.LogLevel;
import cn.omisheep.authz.core.tk.GrantType;
import cn.omisheep.authz.core.util.LogUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OpenAuthDictTest {

    @BeforeAll
    static void setup() {
        LogUtils.setLogLevel(LogLevel.INFO);
    }

    @Test
    void testOAuthInfo() {
        OpenAuthDict.OAuthInfo info = new OpenAuthDict.OAuthInfo();
        
        // Initially should be non (empty)
        assertTrue(info.non());
        
        // Add scope
        info.getScope().add("read");
        info.getScope().add("write");
        assertFalse(info.non());
        assertEquals(2, info.getScope().size());
        
        // Add type (using actual enum values)
        info.getType().add(GrantType.PASSWORD);
        assertEquals(1, info.getType().size());
    }

    @Test
    void testGetSrc() {
        Map<String, Map<String, OpenAuthDict.OAuthInfo>> src = OpenAuthDict.getSrc();
        assertNotNull(src);
        // Should be unmodifiable
        assertThrows(UnsupportedOperationException.class, () -> src.put("test", null));
    }

    @Test
    void testTarget() {
        // Without initialization, should return false
        Set<String> scope = new HashSet<>();
        scope.add("read");
        
        boolean result = OpenAuthDict.target("/nonexistent", "GET", GrantType.PASSWORD, scope);
        assertFalse(result);
    }

    @Test
    void testOAuthInfoChaining() {
        OpenAuthDict.OAuthInfo info = new OpenAuthDict.OAuthInfo();
        
        Set<String> scope = new HashSet<>();
        scope.add("admin");
        
        Set<GrantType> types = new HashSet<>();
        types.add(GrantType.CLIENT_CREDENTIALS);
        
        info.setScope(scope).setType(types);
        
        assertEquals(scope, info.getScope());
        assertEquals(types, info.getType());
    }

    @Test
    void testGrantTypeFactory() {
        assertEquals(GrantType.PASSWORD, GrantType.grantType("password"));
        assertEquals(GrantType.AUTHORIZATION_CODE, GrantType.grantType("authorization_code"));
        assertEquals(GrantType.CLIENT_CREDENTIALS, GrantType.grantType("client_credentials"));
        assertNull(GrantType.grantType("unknown"));
    }
}
