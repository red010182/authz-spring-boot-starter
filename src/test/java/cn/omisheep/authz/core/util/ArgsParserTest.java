package cn.omisheep.authz.core.util;

import cn.omisheep.authz.core.AuthzContext;
import cn.omisheep.authz.core.auth.rpd.ArgsMeta;
import cn.omisheep.authz.core.auth.rpd.PermissionDict;
import cn.omisheep.authz.core.LogLevel;
import cn.omisheep.authz.core.util.LogUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import cn.omisheep.authz.core.LogLevel;
import cn.omisheep.authz.core.util.LogUtils;

class ArgsParserTest {

    public static class TestBean {
        public String getValue() {
            return "testValue";
        }
        
        public String[] getArray() {
            return new String[]{"a", "b"};
        }
    }

    static ApplicationContext ctx;

    @BeforeAll
    static void setup() {
        LogUtils.setLogLevel(LogLevel.INFO); // Fix NPE
        ctx = mock(ApplicationContext.class);
        TestBean testBean = new TestBean();
        when(ctx.getBean(TestBean.class)).thenReturn(testBean);
        AuthzContext.init(ctx);

        HashMap<String, ArgsMeta> argsMap = new HashMap<>();
        argsMap.put("testValue", ArgsMeta.of(TestBean.class, "getValue"));
        argsMap.put("testArray", ArgsMeta.of(TestBean.class, "getArray"));
        
        PermissionDict.initArgs(new java.util.HashSet<>(), 
                                new HashMap<>(), 
                                new HashMap<>(), 
                                argsMap);
    }

    @Test
    void testParseSimpleArg() {
        Object result = ArgsParser.parse("#{testValue}");
        // Verify context interaction
        verify(ctx, org.mockito.Mockito.atLeastOnce()).getBean(TestBean.class);
        
        assertEquals("testValue", result);
    }
    
    @Test
    void testParseArray() {
        // ArgsParser logic for arrays:
        // if trace length is 1, calling convert with array object returns the array object.
        // But parse method:
        // Object convert = convert(trace, ArgsHandler.handle(trace[0]));
        // if (isArrayOrCollection(convert)) { ... parseAndToString ... }
        
        // Let's test what happens when we simply request the array
        Object result = ArgsParser.parse("#{testArray}");
        // It should return list because parse method parses array string to list
        assertNotNull(result);
        assertTrue(result instanceof java.util.List);
        assertEquals(2, ((java.util.List) result).size());
    }
}
