package cn.omisheep.authz.core.init;

import cn.omisheep.authz.core.LogLevel;
import cn.omisheep.authz.core.util.LogUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthzInitializerManagerTest {

    @BeforeAll
    static void setupClass() {
        LogUtils.setLogLevel(LogLevel.INFO);
    }

    private ApplicationContext mockContext;

    @BeforeEach
    void setup() {
        mockContext = mock(ApplicationContext.class);
    }

    @Test
    void testInitializeAllWithNoInitializers() {
        AuthzInitializerManager manager = new AuthzInitializerManager(mockContext, null);
        
        assertDoesNotThrow(() -> manager.initializeAll());
        assertTrue(manager.getInitializers().isEmpty());
    }

    @Test
    void testInitializeAllWithEmptyList() {
        AuthzInitializerManager manager = new AuthzInitializerManager(mockContext, new ArrayList<>());
        
        assertDoesNotThrow(() -> manager.initializeAll());
        assertTrue(manager.getInitializers().isEmpty());
    }

    @Test
    void testInitializeAllWithSingleInitializer() {
        AuthzInitializer mockInitializer = mock(AuthzInitializer.class);
        when(mockInitializer.getName()).thenReturn("TestInitializer");
        when(mockInitializer.getOrder()).thenReturn(0);
        
        List<AuthzInitializer> initializers = List.of(mockInitializer);
        AuthzInitializerManager manager = new AuthzInitializerManager(mockContext, initializers);
        
        manager.initializeAll();
        
        verify(mockInitializer).initialize(mockContext);
    }

    @Test
    void testInitializeAllRespectsOrder() {
        List<String> initOrder = new ArrayList<>();
        
        AuthzInitializer first = new AuthzInitializer() {
            @Override
            public void initialize(ApplicationContext context) {
                initOrder.add("first");
            }
            @Override
            public int getOrder() { return 1; }
            @Override
            public String getName() { return "First"; }
        };
        
        AuthzInitializer second = new AuthzInitializer() {
            @Override
            public void initialize(ApplicationContext context) {
                initOrder.add("second");
            }
            @Override
            public int getOrder() { return 2; }
            @Override
            public String getName() { return "Second"; }
        };
        
        AuthzInitializer third = new AuthzInitializer() {
            @Override
            public void initialize(ApplicationContext context) {
                initOrder.add("third");
            }
            @Override
            public int getOrder() { return 0; }
            @Override
            public String getName() { return "Third"; }
        };
        
        // Add in wrong order - manager should sort by getOrder()
        List<AuthzInitializer> initializers = List.of(second, first, third);
        AuthzInitializerManager manager = new AuthzInitializerManager(mockContext, initializers);
        
        manager.initializeAll();
        
        assertEquals(3, initOrder.size());
        assertEquals("third", initOrder.get(0));  // order 0
        assertEquals("first", initOrder.get(1));  // order 1
        assertEquals("second", initOrder.get(2)); // order 2
    }

    @Test
    void testInitializeAllStopsOnError() {
        AuthzInitializer failingInitializer = mock(AuthzInitializer.class);
        when(failingInitializer.getName()).thenReturn("FailingInitializer");
        when(failingInitializer.getOrder()).thenReturn(0);
        doThrow(new RuntimeException("Test error")).when(failingInitializer).initialize(any());
        
        List<AuthzInitializer> initializers = List.of(failingInitializer);
        AuthzInitializerManager manager = new AuthzInitializerManager(mockContext, initializers);
        
        assertThrows(RuntimeException.class, () -> manager.initializeAll());
    }
}
