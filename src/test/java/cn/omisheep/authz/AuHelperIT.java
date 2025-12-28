package cn.omisheep.authz;

import cn.omisheep.authz.core.AuthzContext;
import cn.omisheep.authz.core.ThreadWebEnvironmentException;
import cn.omisheep.authz.core.auth.ipf.Blacklist;
import cn.omisheep.authz.core.auth.ipf.HttpMeta;
import cn.omisheep.authz.core.oauth.ClientDetails;
import cn.omisheep.authz.core.tk.IssueToken;
import cn.omisheep.commons.util.web.ua.OS;
import cn.omisheep.commons.util.web.ua.Platform;
import cn.omisheep.commons.util.web.ua.UserAgent;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 整合測試 for AuHelper
 * 使用 Mockito 模擬靜態方法
 */
class AuHelperIT {

    @Test
    void testLogin() {
        try (MockedStatic<cn.omisheep.authz.core.helper.AuthzGranterHelper> granterHelperMock = 
                Mockito.mockStatic(cn.omisheep.authz.core.helper.AuthzGranterHelper.class)) {
            IssueToken mockToken = mock(IssueToken.class);
            when(mockToken.getAccessToken()).thenReturn("access-token");
            when(mockToken.getRefreshToken()).thenReturn("refresh-token");
            
            granterHelperMock.when(() -> 
                cn.omisheep.authz.core.helper.AuthzGranterHelper.grant(any(), any(), any())
            ).thenReturn(mockToken);
            
            IssueToken result = AuHelper.login("user123");
            
            assertNotNull(result);
            assertEquals("access-token", result.getAccessToken());
            assertEquals("refresh-token", result.getRefreshToken());
            granterHelperMock.verify(() -> 
                cn.omisheep.authz.core.helper.AuthzGranterHelper.grant(eq("user123"), isNull(), isNull())
            );
        }
    }

    @Test
    void testIsLogin() {
        try (MockedStatic<cn.omisheep.authz.core.helper.AuthzStateHelper> stateHelperMock = 
                Mockito.mockStatic(cn.omisheep.authz.core.helper.AuthzStateHelper.class)) {
            stateHelperMock.when(() -> 
                cn.omisheep.authz.core.helper.AuthzStateHelper.isLogin()
            ).thenReturn(true);
            
            boolean result = AuHelper.isLogin();
            
            assertTrue(result);
            stateHelperMock.verify(() -> 
                cn.omisheep.authz.core.helper.AuthzStateHelper.isLogin()
            );
        }
    }

    @Test
    void testHasRole() {
        try (MockedStatic<cn.omisheep.authz.core.helper.AuthzStateHelper> stateHelperMock = 
                Mockito.mockStatic(cn.omisheep.authz.core.helper.AuthzStateHelper.class)) {
            stateHelperMock.when(() -> 
                cn.omisheep.authz.core.helper.AuthzStateHelper.hasRoles(anyList())
            ).thenReturn(true);
            
            boolean result = AuHelper.hasRole("admin", "user");
            
            assertTrue(result);
            stateHelperMock.verify(() -> 
                cn.omisheep.authz.core.helper.AuthzStateHelper.hasRoles(argThat(list -> 
                    list.contains("admin") && list.contains("user")
                ))
            );
        }
    }

    @Test
    void testGetHttpMeta() throws ThreadWebEnvironmentException {
        try (MockedStatic<AuthzContext> authzContextMock = Mockito.mockStatic(AuthzContext.class)) {
            HttpMeta mockHttpMeta = mock(HttpMeta.class);
            authzContextMock.when(AuthzContext::getCurrentHttpMeta).thenReturn(mockHttpMeta);
            
            HttpMeta result = AuHelper.getHttpMeta();
            
            assertSame(mockHttpMeta, result);
            authzContextMock.verify(AuthzContext::getCurrentHttpMeta);
        }
    }

    @Test
    void testDenyIP() {
        try (MockedStatic<Blacklist.IP> blacklistMock = Mockito.mockStatic(Blacklist.IP.class)) {
            AuHelper.denyIP("192.168.1.1", 3600000L);
            
            blacklistMock.verify(() -> 
                Blacklist.IP.update(eq("192.168.1.1"), eq(3600000L))
            );
        }
    }

    @Test
    void testRSAGetPublicKey() {
        try (MockedStatic<cn.omisheep.authz.core.codec.AuthzRSAManager> rsaManagerMock = 
                Mockito.mockStatic(cn.omisheep.authz.core.codec.AuthzRSAManager.class)) {
            rsaManagerMock.when(() -> 
                cn.omisheep.authz.core.codec.AuthzRSAManager.getPublicKeyString()
            ).thenReturn("public-key");
            
            String result = AuHelper.RSA.getRSAPublicKey();
            
            assertEquals("public-key", result);
            rsaManagerMock.verify(() -> 
                cn.omisheep.authz.core.codec.AuthzRSAManager.getPublicKeyString()
            );
        }
    }

    @Test
    void testCheckUserIsActive() {
        try (MockedStatic<cn.omisheep.authz.core.helper.AuthzDeviceHelper> deviceHelperMock = 
                Mockito.mockStatic(cn.omisheep.authz.core.helper.AuthzDeviceHelper.class)) {
            deviceHelperMock.when(() -> 
                cn.omisheep.authz.core.helper.AuthzDeviceHelper.checkUserIsActive(any(), anyLong())
            ).thenReturn(true);
            
            boolean result = AuHelper.checkUserIsActive("user123", 60000L);
            
            assertTrue(result);
            deviceHelperMock.verify(() -> 
                cn.omisheep.authz.core.helper.AuthzDeviceHelper.checkUserIsActive(eq("user123"), eq(60000L))
            );
        }
    }
}
