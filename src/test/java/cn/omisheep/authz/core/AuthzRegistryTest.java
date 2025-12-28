package cn.omisheep.authz.core;

import cn.omisheep.authz.core.auth.PermLibrary;
import cn.omisheep.authz.core.cache.Cache;
import cn.omisheep.authz.core.oauth.OpenAuthLibrary;
import cn.omisheep.authz.core.util.LogUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthzRegistryTest {

    @BeforeAll
    static void setupClass() {
        LogUtils.setLogLevel(LogLevel.INFO);
    }

    private ApplicationContext mockContext;
    private AuthzProperties mockProperties;
    private Cache mockCache;
    private PermLibrary mockPermLibrary;
    private OpenAuthLibrary mockOpenAuthLibrary;

    @BeforeEach
    void setup() {
        mockContext = mock(ApplicationContext.class);
        mockProperties = mock(AuthzProperties.class);
        mockCache = mock(Cache.class);
        mockPermLibrary = mock(PermLibrary.class);
        mockOpenAuthLibrary = mock(OpenAuthLibrary.class);
    }

    @Test
    void testRegistryCreation() {
        AuthzRegistry registry = new AuthzRegistry(
                mockContext, mockProperties, mockCache, mockPermLibrary, mockOpenAuthLibrary
        );
        
        assertNotNull(registry);
        assertEquals(mockContext, registry.getApplicationContext());
        assertEquals(mockProperties, registry.getProperties());
        assertEquals(mockCache, registry.getCache());
        assertEquals(mockPermLibrary, registry.getPermLibrary());
        assertEquals(mockOpenAuthLibrary, registry.getOpenAuthLibrary());
    }

    @Test
    void testStaticAccess() {
        AuthzRegistry registry = new AuthzRegistry(
                mockContext, mockProperties, mockCache, mockPermLibrary, mockOpenAuthLibrary
        );
        
        assertTrue(AuthzRegistry.isInitialized());
        assertSame(registry, AuthzRegistry.getInstance());
        assertSame(mockProperties, AuthzRegistry.properties());
        assertSame(mockCache, AuthzRegistry.cache());
        assertSame(mockPermLibrary, AuthzRegistry.permLibrary());
        assertSame(mockOpenAuthLibrary, AuthzRegistry.openAuthLibrary());
    }

    @Test
    void testGetBean() {
        when(mockContext.getBean(String.class)).thenReturn("test");
        
        AuthzRegistry registry = new AuthzRegistry(
                mockContext, mockProperties, mockCache, null, null
        );
        
        String result = registry.getBean(String.class);
        assertEquals("test", result);
        verify(mockContext).getBean(String.class);
    }

    @Test
    void testGetBeanByName() {
        when(mockContext.getBean("myBean", String.class)).thenReturn("test");
        
        AuthzRegistry registry = new AuthzRegistry(
                mockContext, mockProperties, mockCache, null, null
        );
        
        String result = registry.getBean("myBean", String.class);
        assertEquals("test", result);
        verify(mockContext).getBean("myBean", String.class);
    }

    @Test
    void testNullableLibraries() {
        // PermLibrary and OpenAuthLibrary can be null
        AuthzRegistry registry = new AuthzRegistry(
                mockContext, mockProperties, mockCache, null, null
        );
        
        assertNull(registry.getPermLibrary());
        assertNull(registry.getOpenAuthLibrary());
        assertNull(AuthzRegistry.permLibrary());
        assertNull(AuthzRegistry.openAuthLibrary());
    }
}
