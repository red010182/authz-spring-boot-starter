package cn.omisheep.authz.core;

import cn.omisheep.authz.core.auth.ipf.HttpMeta;
import cn.omisheep.authz.core.config.Constants;
import cn.omisheep.authz.core.tk.AccessToken;
import cn.omisheep.authz.core.util.HttpUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthzContextTest {

    private ApplicationContext mockApplicationContext;
    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private HttpMeta mockHttpMeta;
    private AccessToken mockAccessToken;

    @BeforeEach
    void setUp() {
        mockApplicationContext = mock(ApplicationContext.class);
        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockHttpMeta = mock(HttpMeta.class);
        mockAccessToken = mock(AccessToken.class);

        // Clear thread locals
        AuthzContext.currentHttpMeta.remove();
        HttpUtils.currentRequest.remove();
        HttpUtils.currentResponse.remove();
    }

    @AfterEach
    void tearDown() {
        // Clear thread locals
        AuthzContext.currentHttpMeta.remove();
        HttpUtils.currentRequest.remove();
        HttpUtils.currentResponse.remove();
    }

    @Test
    void testConstructorThrowsException() {
        // Use reflection to test private constructor
        assertThrows(UnsupportedOperationException.class, () -> {
            try {
                java.lang.reflect.Constructor<AuthzContext> constructor = AuthzContext.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            } catch (java.lang.reflect.InvocationTargetException e) {
                throw e.getTargetException();
            }
        });
    }

    @Test
    void testInit() {
        AuthzContext.init(mockApplicationContext);
        
        assertNotNull(AuthzContext.getCtx());
    }

    @Test
    void testGetBean() {
        AuthzContext.init(mockApplicationContext);
        String expectedBean = "testBean";
        when(mockApplicationContext.getBean(String.class)).thenReturn(expectedBean);
        
        String result = AuthzContext.getBean(String.class);
        
        assertEquals(expectedBean, result);
        verify(mockApplicationContext).getBean(String.class);
    }

    @Test
    void testGetBeanByName() {
        AuthzContext.init(mockApplicationContext);
        String expectedBean = "testBean";
        when(mockApplicationContext.getBean("myBean", String.class)).thenReturn(expectedBean);
        
        String result = AuthzContext.getBean("myBean", String.class);
        
        assertEquals(expectedBean, result);
        verify(mockApplicationContext).getBean("myBean", String.class);
    }

    @Test
    void testGetBeansOfType() {
        AuthzContext.init(mockApplicationContext);
        Map<String, String> expectedBeans = new HashMap<>();
        expectedBeans.put("bean1", "value1");
        expectedBeans.put("bean2", "value2");
        when(mockApplicationContext.getBeansOfType(String.class)).thenReturn(expectedBeans);
        
        Map<String, String> result = AuthzContext.getBeansOfType(String.class);
        
        assertEquals(expectedBeans, result);
        verify(mockApplicationContext).getBeansOfType(String.class);
    }

    @Test
    void testGetCtx() {
        AuthzContext.init(mockApplicationContext);
        
        ApplicationContext result = AuthzContext.getCtx();
        
        assertSame(mockApplicationContext, result);
    }

    @Test
    void testGetCurrentHttpMetaFromThreadLocal() {
        AuthzContext.currentHttpMeta.set(mockHttpMeta);
        
        HttpMeta result = AuthzContext.getCurrentHttpMeta();
        
        assertSame(mockHttpMeta, result);
    }

    @Test
    void testGetCurrentHttpMetaFromRequestAttribute() {
        try (MockedStatic<HttpUtils> httpUtilsMock = Mockito.mockStatic(HttpUtils.class)) {
            httpUtilsMock.when(HttpUtils::getCurrentRequest).thenReturn(mockRequest);
            when(mockRequest.getAttribute(Constants.HTTP_META)).thenReturn(mockHttpMeta);
            
            HttpMeta result = AuthzContext.getCurrentHttpMeta();
            
            assertSame(mockHttpMeta, result);
            verify(mockRequest).getAttribute(Constants.HTTP_META);
        }
    }

    @Test
    void testGetCurrentHttpMetaThrowsThreadWebEnvironmentException() {
        try (MockedStatic<HttpUtils> httpUtilsMock = Mockito.mockStatic(HttpUtils.class)) {
            httpUtilsMock.when(HttpUtils::getCurrentRequest).thenReturn(mockRequest);
            when(mockRequest.getAttribute(Constants.HTTP_META)).thenReturn(null);
            
            assertThrows(ThreadWebEnvironmentException.class, AuthzContext::getCurrentHttpMeta);
        }
    }

    @Test
    void testGetCurrentHttpMetaWithException() {
        try (MockedStatic<HttpUtils> httpUtilsMock = Mockito.mockStatic(HttpUtils.class)) {
            httpUtilsMock.when(HttpUtils::getCurrentRequest).thenThrow(new RuntimeException("Test exception"));
            
            assertThrows(ThreadWebEnvironmentException.class, AuthzContext::getCurrentHttpMeta);
        }
    }

    @Test
    void testGetCurrentToken() {
        AuthzContext.currentHttpMeta.set(mockHttpMeta);
        when(mockHttpMeta.getToken()).thenReturn(mockAccessToken);
        
        AccessToken result = AuthzContext.getCurrentToken();
        
        assertSame(mockAccessToken, result);
    }

    @Test
    void testGetCurrentTokenThrowsNotLoginException() {
        AuthzContext.currentHttpMeta.set(mockHttpMeta);
        when(mockHttpMeta.getToken()).thenReturn(null);
        
        assertThrows(NotLoginException.class, AuthzContext::getCurrentToken);
    }

    @Test
    void testGetCurrentTokenWithException() {
        try (MockedStatic<HttpUtils> httpUtilsMock = Mockito.mockStatic(HttpUtils.class)) {
            httpUtilsMock.when(HttpUtils::getCurrentRequest).thenThrow(new RuntimeException("Test exception"));
            
            assertThrows(NotLoginException.class, AuthzContext::getCurrentToken);
        }
    }

    @Test
    void testCreateUserIdWithString() {
        // Mock the static fields in AuthzAppVersion
        cn.omisheep.authz.core.config.AuthzAppVersion.USER_ID_TYPE = String.class;
        
        Object result = AuthzContext.createUserId("123");
        
        assertEquals("123", result);
        assertTrue(result instanceof String);
    }

    @Test
    void testCreateUserIdWithNonString() {
        // Mock the static fields in AuthzAppVersion
        cn.omisheep.authz.core.config.AuthzAppVersion.USER_ID_TYPE = String.class;
        
        Object result = AuthzContext.createUserId(123);
        
        assertEquals("123", result);
        assertTrue(result instanceof String);
    }

    @Test
    void testCreateUserIdWithCustomType() throws Exception {
        // Mock the static fields in AuthzAppVersion
        cn.omisheep.authz.core.config.AuthzAppVersion.USER_ID_TYPE = Long.class;
        
        // Create a mock constructor for Long that takes a String
        java.lang.reflect.Constructor<Long> mockConstructor = mock(java.lang.reflect.Constructor.class);
        when(mockConstructor.newInstance("456")).thenReturn(456L);
        cn.omisheep.authz.core.config.AuthzAppVersion.USER_ID_CONSTRUCTOR = mockConstructor;
        
        Object result = AuthzContext.createUserId(456);
        
        assertEquals(456L, result);
        assertTrue(result instanceof Long);
        verify(mockConstructor).newInstance("456");
    }

    @Test
    void testCreateUserIdWithConstructorException() throws Exception {
        // Mock the static fields in AuthzAppVersion
        cn.omisheep.authz.core.config.AuthzAppVersion.USER_ID_TYPE = Long.class;
        
        // Create a mock constructor that throws an exception
        java.lang.reflect.Constructor<Long> mockConstructor = mock(java.lang.reflect.Constructor.class);
        when(mockConstructor.newInstance("789")).thenThrow(new InstantiationException("Test exception"));
        cn.omisheep.authz.core.config.AuthzAppVersion.USER_ID_CONSTRUCTOR = mockConstructor;
        
        Object result = AuthzContext.createUserId(789);
        
        // Should return the original input when constructor fails
        assertEquals(789, result);
        verify(mockConstructor).newInstance("789");
    }

    @Test
    void testCreateUserIdWithNullInput() {
        // Mock the static fields in AuthzAppVersion
        cn.omisheep.authz.core.config.AuthzAppVersion.USER_ID_TYPE = String.class;
        
        // When userId is null, it should return "null" string
        Object result = AuthzContext.createUserId(null);
        
        assertEquals("null", result);
        assertTrue(result instanceof String);
    }

    @Test
    void testGetMainClassPkg() {
        // Mock the static field in AuthzAppVersion
        cn.omisheep.authz.core.config.AuthzAppVersion.mainClass = AuthzContextTest.class;
        
        String result = AuthzContext.getMainClassPkg();
        
        assertEquals("cn.omisheep.authz.core", result);
    }

    @Test
    void testCurrentRequestSupplier() {
        HttpUtils.currentRequest.set(mockRequest);
        
        HttpServletRequest result = AuthzContext.currentRequest.get();
        
        assertSame(mockRequest, result);
    }

    @Test
    void testCurrentResponseSupplier() {
        HttpUtils.currentResponse.set(mockResponse);
        
        HttpServletResponse result = AuthzContext.currentResponse.get();
        
        assertSame(mockResponse, result);
    }
}
