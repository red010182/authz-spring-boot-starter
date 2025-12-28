package cn.omisheep.authz.core.tk;

import cn.omisheep.authz.core.AuthzContext;
import cn.omisheep.authz.core.AuthzProperties;
import cn.omisheep.authz.core.auth.PermLibrary;
import cn.omisheep.authz.core.auth.deviced.UserDevicesDict;
import cn.omisheep.authz.core.cache.Cache;
import cn.omisheep.authz.core.oauth.OpenAuthLibrary;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenHelperTest {

    static AuthzProperties properties;

    @BeforeAll
    static void setup() {
        ApplicationContext ctx = mock(ApplicationContext.class);
        properties = new AuthzProperties();
        AuthzProperties.TokenConfig tokenConfig = new AuthzProperties.TokenConfig();
        // 32 chars specific for HS256 to ensure enough entropy for testing
        tokenConfig.setKey("12345678901234567890123456789012"); 
        tokenConfig.setIdBits(8);
        tokenConfig.setAccessTime("1h");
        tokenConfig.setRefreshTime("2h");
        tokenConfig.setCookieName("atkn");
        properties.setToken(tokenConfig);

        when(ctx.getBean(AuthzProperties.class)).thenReturn(properties);
        when(ctx.getBean(UserDevicesDict.class)).thenReturn(mock(UserDevicesDict.class));
        when(ctx.getBean("authzCache", Cache.class)).thenReturn(mock(Cache.class));
        when(ctx.getBean(PermLibrary.class)).thenReturn(mock(PermLibrary.class));
        when(ctx.getBean(OpenAuthLibrary.class)).thenReturn(mock(OpenAuthLibrary.class));

        AuthzContext.init(ctx);
    }

    @Test
    void testCreateTokenPair() {
        TokenPair tokenPair = TokenHelper.createTokenPair("user1", "web", "device1");
        assertNotNull(tokenPair);
        assertNotNull(tokenPair.getAccessToken());
        assertNotNull(tokenPair.getRefreshToken());
        assertEquals("user1", tokenPair.getAccessToken().getUserId());
    }

    @Test
    void testParseAccessToken() {
        TokenPair tokenPair = TokenHelper.createTokenPair("user1", "web", "device1");
        AccessToken accessToken = TokenHelper.parseAccessToken(tokenPair.getAccessToken().getToken());
        
        assertNotNull(accessToken);
        assertEquals("user1", accessToken.getUserId());
        assertEquals("web", accessToken.getDeviceType());
        assertEquals("device1", accessToken.getDeviceId());
    }

    @Test
    void testParseRefreshToken() {
        TokenPair tokenPair = TokenHelper.createTokenPair("user1", "web", "device1");
        RefreshToken refreshToken = TokenHelper.parseRefreshToken(tokenPair.getRefreshToken().getToken());
        
        assertNotNull(refreshToken);
        assertEquals("user1", refreshToken.getUserId());
    }
    
    @Test
    void testExpiration() {
        TokenPair tokenPair = TokenHelper.createTokenPair("user1", "web", "device1");
        long now = System.currentTimeMillis();
        // Access token should adhere to 1h
        long diff = tokenPair.getAccessToken().getExpiresAt() - now;
        assertTrue(diff > 3500000 && diff < 3700000, "Access token expiration should be approx 1h");
    }
}
