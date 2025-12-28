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
import java.util.List;
import java.util.Map;

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
    
    public static class ComplexBean {
        public ComplexBean self() { return this; }
        public String getName() { return "complex"; }
        public int[] getInts() { return new int[]{1, 2, 3}; }
        public List<String> getList() { return java.util.Arrays.asList("x", "y"); }
        public Map<String, Object> getMap() { 
            HashMap<String, Object> map = new HashMap<>();
            map.put("key", "value");
            map.put("inner", new TestBean());
            return map;
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
        
        ComplexBean complexBean = new ComplexBean();
        when(ctx.getBean(ComplexBean.class)).thenReturn(complexBean);
        argsMap.put("complex", ArgsMeta.of(ComplexBean.class, "self")); // self return this
        
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


    @Test
    void testParseComplexPath() {
        // complex.map.key -> "value"
        Object val = ArgsParser.parse("#{complex.map.key}");
        assertEquals("value", val);
        
        // complex.list[0] -> "x"
        Object listVal = ArgsParser.parse("#{complex.list[0]}");
        assertEquals("x", listVal);
        
        // complex.ints -> list [1, 2, 3]
        Object intsVal = ArgsParser.parse("#{complex.ints}");
        assertTrue(intsVal instanceof java.util.List);
        assertEquals(3, ((java.util.List)intsVal).size());
        
        // complex.list[*] -> list ["x", "y"]
        Object wildcardVal = ArgsParser.parse("#{complex.list[*]}");
         assertTrue(wildcardVal instanceof java.util.List);
    }

    @Test
    void testParseCondition() {
        cn.omisheep.authz.core.auth.rpd.DataPermRolesMeta meta = new cn.omisheep.authz.core.auth.rpd.DataPermRolesMeta();
        meta.setCondition("user.id = #{testValue} AND role in #{testArray}");
        
        String result = ArgsParser.parse(meta);
        // "user.id = testValue AND role in ('a', 'b')"
        // check specific formatting
        assertNotNull(result);
        assertTrue(result.contains("testValue"));
        assertTrue(result.contains("a, b"));
    }
    @Test
    void testPrimitiveArrays() {
        // Test int[] which should be parsed to "( 1, 2 )" string format logic in parseArray
        // We can't access parseArray(int[]) directly.
        // But parse(String) calls convert -> parseAndToString -> parseArray.
        // We need a bean returning int[]
        // ComplexBean has getInts -> [1, 2, 3]
        
        Object res = ArgsParser.parse("#{complex.ints}");
        // "convert" returns List if it parses.
        // Wait, convert logic:
        // if trace ends with array, it might call parseAndToString internally if it's intermediate?
        // No, convert returns object.
        // But parse(String) calls:
        // Object convert = convert(...);
        // if (isArrayOrCollection(convert)) { String arrString = parseAndToString(convert); return Arrays.stream(...) }
        // So it converts array to string "( 1, 2, 3 )" then splits by comma.
        // So the result is List<String> ["1", "2", "3"].
        
        assertTrue(res instanceof java.util.List);
        java.util.List list = (java.util.List) res;
        assertEquals(3, list.size());
        assertEquals("1", list.get(0)); // string because it was parsed from string representation
        
        // We need coverage for other types (boolean[], char[], etc).
        // Testing them all requires more bean methods, but maybe we can mock generic object?
        // No, ArgsParser uses reflection or JSON.
        // But we can test `parseArray` directly via reflection? Or just `parseAndToString` via reflection.
        // Reflection is easier to cover all 8 primitive types without creating bean methods for all.
        
        try {
            java.lang.reflect.Method m = ArgsParser.class.getDeclaredMethod("parseAndToString", Object.class);
            m.setAccessible(true);
            
            assertEquals("( 1, 2 )", m.invoke(null, new int[]{1, 2}));
            assertEquals("( 1, 2 )", m.invoke(null, new long[]{1L, 2L}));
            assertEquals("( 1.0, 2.0 )", m.invoke(null, new float[]{1f, 2f}));
            assertEquals("( 1.0, 2.0 )", m.invoke(null, new double[]{1d, 2d}));
            assertEquals("( a, b )", m.invoke(null, new char[]{'a', 'b'}));
            assertEquals("( true, false )", m.invoke(null, new boolean[]{true, false}));
            assertEquals("( 1, 2 )", m.invoke(null, new short[]{1, 2}));
            // byte[] is not supported in ArgsParser source code I viewed? I saw int, long, float, double, char, boolean, short, string.
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
