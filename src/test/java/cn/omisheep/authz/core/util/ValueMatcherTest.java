package cn.omisheep.authz.core.util;

import cn.omisheep.authz.core.LogLevel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static cn.omisheep.authz.core.util.ValueMatcher.ValueType.*;
import static org.junit.jupiter.api.Assertions.*;

class ValueMatcherTest {

    @BeforeAll
    static void setup() {
        LogUtils.setLogLevel(LogLevel.INFO);
    }

    @Test
    void testValueTypeEnum() {
        assertEquals("range", RANGE.getValue());
        assertEquals("equals", EQUALS.getValue());
        assertEquals("other", OTHER.getValue());
        
        assertTrue(OTHER.isOther());
        assertFalse(OTHER.notOther());
        assertFalse(RANGE.isOther());
        assertTrue(RANGE.notOther());
    }

    @Test
    void testCheckTypeByName() {
        assertEquals(EQUALS, ValueMatcher.checkTypeByName("java.lang.String"));
        assertEquals(RANGE, ValueMatcher.checkTypeByName("java.lang.Integer"));
        assertEquals(RANGE, ValueMatcher.checkTypeByName("int"));
        assertEquals(RANGE, ValueMatcher.checkTypeByName("long"));
        assertEquals(RANGE, ValueMatcher.checkTypeByName("double"));
        assertEquals(RANGE, ValueMatcher.checkTypeByName("float"));
        assertEquals(EQUALS, ValueMatcher.checkTypeByName("boolean"));
        assertEquals(EQUALS, ValueMatcher.checkTypeByName("char"));
        assertEquals(OTHER, ValueMatcher.checkTypeByName("unknown.type"));
    }

    @Test
    void testCheckType() {
        assertEquals(EQUALS, ValueMatcher.checkType("string"));
        assertEquals(RANGE, ValueMatcher.checkType(123));
        assertEquals(RANGE, ValueMatcher.checkType(123L));
        assertEquals(RANGE, ValueMatcher.checkType(1.5));
        assertEquals(RANGE, ValueMatcher.checkType(1.5f));
        assertEquals(EQUALS, ValueMatcher.checkType(true));
        assertEquals(EQUALS, ValueMatcher.checkType('c'));
        assertEquals(OTHER, ValueMatcher.checkType(new Object()));
    }

    @Test
    void testCheckTypeByClass() {
        assertEquals(EQUALS, ValueMatcher.checkTypeByClass(String.class));
        assertEquals(RANGE, ValueMatcher.checkTypeByClass(Integer.class));
        assertEquals(RANGE, ValueMatcher.checkTypeByClass(Long.class));
        assertEquals(OTHER, ValueMatcher.checkTypeByClass(Object.class));
    }

    @Test
    void testGetType() {
        assertEquals(String.class, ValueMatcher.getType("java.lang.String"));
        assertEquals(Integer.class, ValueMatcher.getType("java.lang.Integer"));
        assertEquals(Integer.class, ValueMatcher.getType("int"));
        assertNull(ValueMatcher.getType("nonexistent.class"));
    }

    @Test
    void testMatchWithWildcard() {
        assertTrue(ValueMatcher.match("*", "anyValue", "java.lang.String", EQUALS));
    }

    @Test
    void testMatchWithNullResource() {
        assertFalse(ValueMatcher.match((String) null, "value", "java.lang.String", EQUALS));
    }

    @Test
    void testMatchWithSet() {
        Set<String> resources = new HashSet<>();
        resources.add("*");
        
        // Wildcard in set should match anything
        assertTrue(ValueMatcher.match(resources, "anything", "java.lang.String", EQUALS));
    }

    @Test
    void testMatchInvalidRange() {
        // Invalid range (too many dashes) should return false
        assertFalse(ValueMatcher.match("1-50-100", "50", "int", RANGE));
    }
}
