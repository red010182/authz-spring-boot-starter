package cn.omisheep.authz.core.init;

import cn.omisheep.authz.core.LogLevel;
import cn.omisheep.authz.core.util.LogUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthzInitializerTest {

    @BeforeAll
    static void setupClass() {
        LogUtils.setLogLevel(LogLevel.INFO);
    }

    @Test
    void testDefaultGetOrder() {
        AuthzInitializer initializer = context -> { };
        
        assertEquals(0, initializer.getOrder());
    }

    @Test
    void testDefaultGetName() {
        AuthzInitializer initializer = new AuthzInitializer() {
            @Override
            public void initialize(ApplicationContext context) {
            }
        };
        
        // Default name is the class simple name (anonymous classes have specific names)
        assertNotNull(initializer.getName());
    }

    @Test
    void testCustomImplementation() {
        AuthzInitializer initializer = new AuthzInitializer() {
            @Override
            public void initialize(ApplicationContext context) {
                // Custom initialization
            }
            
            @Override
            public int getOrder() {
                return 100;
            }
            
            @Override
            public String getName() {
                return "CustomInitializer";
            }
        };
        
        assertEquals(100, initializer.getOrder());
        assertEquals("CustomInitializer", initializer.getName());
    }

    @Test
    void testInitializeIsCalled() {
        ApplicationContext mockContext = mock(ApplicationContext.class);
        boolean[] called = {false};
        
        AuthzInitializer initializer = context -> {
            called[0] = true;
            assertSame(mockContext, context);
        };
        
        initializer.initialize(mockContext);
        
        assertTrue(called[0]);
    }
}
