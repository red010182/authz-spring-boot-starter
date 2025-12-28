package cn.omisheep.authz.core.interceptor;

import cn.omisheep.authz.core.ExceptionStatus;
import cn.omisheep.authz.core.auth.ipf.HttpMeta;
import cn.omisheep.authz.core.slot.Error;
import cn.omisheep.authz.core.slot.Slot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.HandlerMethod;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static cn.omisheep.authz.core.config.Constants.HTTP_META;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthzSlotCoreInterceptorTest {

    private AuthzExceptionHandler mockAuthzExceptionHandler;
    private List<Slot> mockSlots;
    private AuthzSlotCoreInterceptor interceptor;
    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private HandlerMethod mockHandlerMethod;
    private HttpMeta mockHttpMeta;

    @BeforeEach
    void setUp() {
        mockAuthzExceptionHandler = mock(AuthzExceptionHandler.class);
        mockSlots = new ArrayList<>();
        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockHandlerMethod = mock(HandlerMethod.class);
        mockHttpMeta = mock(HttpMeta.class);
        
        // Mock HandlerMethod to return a non-null bean type
        when(mockHandlerMethod.getBeanType()).thenReturn((Class) String.class);
        
        interceptor = new AuthzSlotCoreInterceptor(mockAuthzExceptionHandler, mockSlots);
    }

    @Test
    void testPreHandleWhenHttpMetaIsNull() throws Exception {
        // Given
        when(mockRequest.getAttribute(HTTP_META)).thenReturn(null);
        
        // When
        boolean result = interceptor.preHandle(mockRequest, mockResponse, mockHandlerMethod);
        
        // Then
        assertTrue(result);
        verify(mockHttpMeta, never()).exportLog();
    }

    @Test
    void testPreHandleWhenHandlerIsNotHandlerMethod() throws Exception {
        // Given
        when(mockRequest.getAttribute(HTTP_META)).thenReturn(mockHttpMeta);
        Object nonHandlerMethod = new Object();
        
        // When
        boolean result = interceptor.preHandle(mockRequest, mockResponse, nonHandlerMethod);
        
        // Then
        assertTrue(result);
        verify(mockHttpMeta).exportLog();
    }

    @Test
    void testPreHandleWhenExceptionStatusListIsNotEmpty() throws Exception {
        // Given
        when(mockRequest.getAttribute(HTTP_META)).thenReturn(mockHttpMeta);
        LinkedList<ExceptionStatus> exceptionStatusList = new LinkedList<>();
        exceptionStatusList.add(ExceptionStatus.REQUIRE_LOGIN);
        when(mockHttpMeta.getExceptionStatusList()).thenReturn(exceptionStatusList);
        when(mockAuthzExceptionHandler.handle(any(), any(), any(), any(), any())).thenReturn(false);
        
        // When
        boolean result = interceptor.preHandle(mockRequest, mockResponse, mockHandlerMethod);
        
        // Then
        assertFalse(result);
        verify(mockHttpMeta).exportLog();
        verify(mockAuthzExceptionHandler).handle(eq(mockRequest), eq(mockResponse), eq(mockHttpMeta), 
                eq(ExceptionStatus.REQUIRE_LOGIN), any());
    }       

    @Test
    void testConstructorWithSlots() {
        // Given
        Slot slot1 = mock(Slot.class);
        Slot slot2 = mock(Slot.class);
        Collection<Slot> slots = List.of(slot2, slot1); // Unsorted
        
        // When
        AuthzSlotCoreInterceptor interceptor = new AuthzSlotCoreInterceptor(mockAuthzExceptionHandler, slots);
        
        // Then - constructor should not throw exception
        assertNotNull(interceptor);
    }

    @Test
    void testPreHandleWhenSlotCallsStop() throws Exception {
        // Given
        when(mockRequest.getAttribute(HTTP_META)).thenReturn(mockHttpMeta);
        when(mockHttpMeta.getExceptionStatusList()).thenReturn(new LinkedList<>());
        when(mockHttpMeta.getExceptionObjectList()).thenReturn(new LinkedList<>());
        
        Slot mockSlot = mock(Slot.class);
        mockSlots.add(mockSlot);
        
        // Make the slot call stop()
        doAnswer(invocation -> {
            Error error = invocation.getArgument(2);
            error.stop();
            return null;
        }).when(mockSlot).chain(any(), any(), any());
        
        // When
        boolean result = interceptor.preHandle(mockRequest, mockResponse, mockHandlerMethod);
        
        // Then
        assertTrue(result); // No errors reported, should return true
        verify(mockAuthzExceptionHandler, never()).handle(any(), any(), any(), any(), any());
    }

}
