package cn.omisheep.authz.core.auth.rpd;

import cn.omisheep.authz.core.LogLevel;
import cn.omisheep.authz.core.util.LogUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArgsMetaTest {

    @BeforeAll
    static void setup() {
        LogUtils.setLogLevel(LogLevel.INFO);
    }

    @Test
    void testArgsMetaOf() {
        // Use a method that definitely exists: String.toString()
        ArgsMeta meta = ArgsMeta.of(String.class, "toString");
        
        assertNotNull(meta);
        assertEquals(String.class, meta.getType());
        assertEquals("toString", meta.getMethod());
    }

    @Test
    void testArgsMetaWithNonExistentMethod() {
        // Should return null for non-existent methods
        ArgsMeta meta = ArgsMeta.of(String.class, "nonExistentMethod123");
        assertNull(meta);
    }

    @Test
    void testArgsMetaEqualsAndHashCode() {
        ArgsMeta meta1 = ArgsMeta.of(String.class, "toString");
        ArgsMeta meta2 = ArgsMeta.of(String.class, "toString");
        ArgsMeta meta3 = ArgsMeta.of(String.class, "length");
        
        assertNotNull(meta1);
        assertNotNull(meta2);
        assertNotNull(meta3);
        
        assertEquals(meta1, meta2);
        assertNotEquals(meta1, meta3);
        assertEquals(meta1.hashCode(), meta2.hashCode());
    }

    @Test
    void testArgsMetaGetters() {
        ArgsMeta meta = ArgsMeta.of(String.class, "substring", int.class);
        
        assertNotNull(meta);
        assertEquals("substring", meta.getMethod());
        assertNotNull(meta.getParameters());
        assertEquals(1, meta.getParameters().size());
        assertEquals(String.class, meta.getReturnType());
    }
}
