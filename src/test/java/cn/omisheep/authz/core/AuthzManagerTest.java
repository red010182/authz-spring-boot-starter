package cn.omisheep.authz.core;

import cn.omisheep.authz.core.auth.ipf.Blacklist;
import cn.omisheep.authz.core.auth.ipf.Httpd;
import cn.omisheep.authz.core.auth.rpd.PermissionDict;
import cn.omisheep.authz.core.cache.Cache;
import cn.omisheep.authz.core.cache.L2Cache;
import cn.omisheep.authz.core.config.AuthzAppVersion;
import cn.omisheep.authz.core.msg.AuthzModifier;
import cn.omisheep.authz.core.oauth.OpenAuthDict;
import cn.omisheep.web.entity.ResponseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthzManagerTest {

    private AuthzModifier authzModifier;
    private L2Cache mockCache;

    @BeforeEach
    void setUp() {
        authzModifier = mock(AuthzModifier.class);
        mockCache = mock(L2Cache.class);
    }

    // Helper method to set the cache field using reflection
    private void setCacheField(Cache cache) {
        try {
            Field cacheField = cn.omisheep.authz.core.helper.BaseHelper.class.getDeclaredField("cache");
            cacheField.setAccessible(true);
            cacheField.set(null, cache);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set cache field", e);
        }
    }

    @Test
    void testOpWithOpenAuthTarget() {
        when(authzModifier.getTarget()).thenReturn(AuthzModifier.Target.OPEN_AUTH);
        Object expectedResult = new Object();
        
        try (MockedStatic<OpenAuthDict> openAuthDictMock = Mockito.mockStatic(OpenAuthDict.class)) {
            openAuthDictMock.when(() -> OpenAuthDict.modify(any(AuthzModifier.class)))
                    .thenReturn(expectedResult);
            
            Object result = AuthzManager.op(authzModifier);
            
            assertEquals(expectedResult, result);
            openAuthDictMock.verify(() -> OpenAuthDict.modify(authzModifier));
        }
    }

    @Test
    void testOpWithRateTarget() {
        when(authzModifier.getTarget()).thenReturn(AuthzModifier.Target.RATE);
        Object expectedResult = new Object();
        
        try (MockedStatic<Httpd> httpdMock = Mockito.mockStatic(Httpd.class)) {
            httpdMock.when(() -> Httpd.modify(any(AuthzModifier.class)))
                    .thenReturn(expectedResult);
            
            Object result = AuthzManager.op(authzModifier);
            
            assertEquals(expectedResult, result);
            httpdMock.verify(() -> Httpd.modify(authzModifier));
        }
    }

    @Test
    void testOpWithBlacklistTarget() {
        when(authzModifier.getTarget()).thenReturn(AuthzModifier.Target.BLACKLIST);
        // Blacklist.modify returns ResponseResult, not Object
        ResponseResult<?> expectedResult = mock(ResponseResult.class);
        
        try (MockedStatic<Blacklist> blacklistMock = Mockito.mockStatic(Blacklist.class)) {
            blacklistMock.when(() -> Blacklist.modify(any(AuthzModifier.class)))
                    .thenReturn(expectedResult);
            
            Object result = AuthzManager.op(authzModifier);
            
            assertSame(expectedResult, result);
            blacklistMock.verify(() -> Blacklist.modify(authzModifier));
        }
    }

    @Test
    void testOpWithApiTarget() {
        when(authzModifier.getTarget()).thenReturn(AuthzModifier.Target.API);
        Object expectedResult = new Object();
        
        try (MockedStatic<PermissionDict> permissionDictMock = Mockito.mockStatic(PermissionDict.class)) {
            permissionDictMock.when(() -> PermissionDict.modify(any(AuthzModifier.class)))
                    .thenReturn(expectedResult);
            
            Object result = AuthzManager.op(authzModifier);
            
            assertEquals(expectedResult, result);
            permissionDictMock.verify(() -> PermissionDict.modify(authzModifier));
        }
    }

    @Test
    void testOpWithParameterTarget() {
        when(authzModifier.getTarget()).thenReturn(AuthzModifier.Target.PARAMETER);
        Object expectedResult = new Object();
        
        try (MockedStatic<PermissionDict> permissionDictMock = Mockito.mockStatic(PermissionDict.class)) {
            permissionDictMock.when(() -> PermissionDict.modify(any(AuthzModifier.class)))
                    .thenReturn(expectedResult);
            
            Object result = AuthzManager.op(authzModifier);
            
            assertEquals(expectedResult, result);
            permissionDictMock.verify(() -> PermissionDict.modify(authzModifier));
        }
    }

    @Test
    void testOpWithException() {
        when(authzModifier.getTarget()).thenReturn(AuthzModifier.Target.OPEN_AUTH);
        
        try (MockedStatic<OpenAuthDict> openAuthDictMock = Mockito.mockStatic(OpenAuthDict.class)) {
            openAuthDictMock.when(() -> OpenAuthDict.modify(any(AuthzModifier.class)))
                    .thenThrow(new RuntimeException("Test exception"));
            
            Object result = AuthzManager.op(authzModifier);
            
            // Compare ResponseResult content
            assertTrue(result instanceof ResponseResult);
            ResponseResult<?> actual = (ResponseResult<?>) result;
            assertEquals(AuthzResult.FAIL.getCode(), actual.getCode());
            assertEquals(AuthzResult.FAIL.getMessage(), actual.getMessage());
            assertNull(actual.getData());
        }
    }

    @Test
    void testModifyWithL2Cache() {
        when(authzModifier.getTarget()).thenReturn(AuthzModifier.Target.OPEN_AUTH);
        Object expectedResult = new Object();
        
        try (MockedStatic<OpenAuthDict> openAuthDictMock = Mockito.mockStatic(OpenAuthDict.class);
             MockedStatic<AuthzAppVersion> authzAppVersionMock = Mockito.mockStatic(AuthzAppVersion.class)) {
            
            openAuthDictMock.when(() -> OpenAuthDict.modify(any(AuthzModifier.class)))
                    .thenReturn(expectedResult);
            
            // Set cache using reflection
            setCacheField(mockCache);
            
            Object result = AuthzManager.modify(authzModifier);
            
            assertEquals(expectedResult, result);
            openAuthDictMock.verify(() -> OpenAuthDict.modify(authzModifier));
            authzAppVersionMock.verify(() -> AuthzAppVersion.send(authzModifier));
        }
    }

    @Test
    void testModifyWithoutL2Cache() {
        when(authzModifier.getTarget()).thenReturn(AuthzModifier.Target.OPEN_AUTH);
        Object expectedResult = new Object();
        
        try (MockedStatic<OpenAuthDict> openAuthDictMock = Mockito.mockStatic(OpenAuthDict.class);
             MockedStatic<AuthzAppVersion> authzAppVersionMock = Mockito.mockStatic(AuthzAppVersion.class)) {
            
            openAuthDictMock.when(() -> OpenAuthDict.modify(any(AuthzModifier.class)))
                    .thenReturn(expectedResult);
            
            // Mock the cache to NOT be L2Cache
            Cache nonL2Cache = mock(Cache.class);
            setCacheField(nonL2Cache);
            
            Object result = AuthzManager.modify(authzModifier);
            
            assertEquals(expectedResult, result);
            openAuthDictMock.verify(() -> OpenAuthDict.modify(authzModifier));
            authzAppVersionMock.verify(() -> AuthzAppVersion.send(authzModifier), never());
        }
    }

    @Test
    void testOperateWithNullResult() {
        when(authzModifier.getTarget()).thenReturn(AuthzModifier.Target.OPEN_AUTH);
        
        try (MockedStatic<OpenAuthDict> openAuthDictMock = Mockito.mockStatic(OpenAuthDict.class)) {
            openAuthDictMock.when(() -> OpenAuthDict.modify(any(AuthzModifier.class)))
                    .thenReturn(null);
            
            ResponseResult<?> result = AuthzManager.operate(authzModifier);
            
            assertNotNull(result);
            assertEquals(AuthzResult.SUCCESS.getCode(), result.getCode());
            assertEquals(AuthzResult.SUCCESS.getMessage(), result.getMessage());
            assertNull(result.getData());
        }
    }

    @Test
    void testOperateWithResponseResult() {
        when(authzModifier.getTarget()).thenReturn(AuthzModifier.Target.OPEN_AUTH);
        // Create a mock ResponseResult
        ResponseResult<String> expectedResult = mock(ResponseResult.class);
        
        try (MockedStatic<OpenAuthDict> openAuthDictMock = Mockito.mockStatic(OpenAuthDict.class)) {
            openAuthDictMock.when(() -> OpenAuthDict.modify(any(AuthzModifier.class)))
                    .thenReturn(expectedResult);
            
            ResponseResult<?> result = AuthzManager.operate(authzModifier);
            
            assertSame(expectedResult, result);
        }
    }

    @Test
    void testOperateWithAuthzResult() {
        when(authzModifier.getTarget()).thenReturn(AuthzModifier.Target.OPEN_AUTH);
        
        try (MockedStatic<OpenAuthDict> openAuthDictMock = Mockito.mockStatic(OpenAuthDict.class)) {
            openAuthDictMock.when(() -> OpenAuthDict.modify(any(AuthzModifier.class)))
                    .thenReturn(AuthzResult.SUCCESS);
            
            ResponseResult<?> result = AuthzManager.operate(authzModifier);
            
            assertEquals(AuthzResult.SUCCESS.getCode(), result.getCode());
            assertEquals(AuthzResult.SUCCESS.getMessage(), result.getMessage());
            assertNull(result.getData());
        }
    }

    @Test
    void testOperateWithOtherObject() {
        when(authzModifier.getTarget()).thenReturn(AuthzModifier.Target.OPEN_AUTH);
        Object expectedObject = "test result";
        
        try (MockedStatic<OpenAuthDict> openAuthDictMock = Mockito.mockStatic(OpenAuthDict.class)) {
            openAuthDictMock.when(() -> OpenAuthDict.modify(any(AuthzModifier.class)))
                    .thenReturn(expectedObject);
            
            ResponseResult<?> result = AuthzManager.operate(authzModifier);
            
            assertNotNull(result);
            assertEquals(AuthzResult.SUCCESS.getCode(), result.getCode());
            assertEquals(AuthzResult.SUCCESS.getMessage(), result.getMessage());
            assertEquals(expectedObject, result.getData());
        }
    }

    @Test
    void testConstructorThrowsException() {
        // Use reflection to test private constructor
        assertThrows(UnsupportedOperationException.class, () -> {
            try {
                java.lang.reflect.Constructor<AuthzManager> constructor = AuthzManager.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            } catch (java.lang.reflect.InvocationTargetException e) {
                throw e.getTargetException();
            }
        });
    }
}
