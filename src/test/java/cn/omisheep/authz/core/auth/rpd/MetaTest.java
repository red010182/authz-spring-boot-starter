package cn.omisheep.authz.core.auth.rpd;

import cn.omisheep.authz.core.LogLevel;
import cn.omisheep.authz.core.util.LogUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MetaTest {

    @BeforeAll
    static void setup() {
        LogUtils.setLogLevel(LogLevel.INFO);
    }

    @Test
    void testMetaCreation() {
        Meta meta = new Meta();
        assertNotNull(meta);
        assertTrue(meta.non()); // Empty meta is non
    }

    @Test
    void testMetaWithRequire() {
        Meta meta = new Meta();
        
        Set<Set<String>> require = new HashSet<>();
        Set<String> roleSet = new HashSet<>();
        roleSet.add("ADMIN");
        require.add(roleSet);
        
        meta.require = require;
        
        assertFalse(meta.non());
        assertEquals(1, meta.require.size());
    }

    @Test
    void testMetaWithExclude() {
        Meta meta = new Meta();
        
        Set<Set<String>> exclude = new HashSet<>();
        Set<String> roleSet = new HashSet<>();
        roleSet.add("BANNED");
        exclude.add(roleSet);
        
        meta.exclude = exclude;
        
        assertFalse(meta.non());
        assertEquals(1, meta.exclude.size());
    }

    @Test
    void testMetaToString() {
        Meta meta = new Meta();
        
        Set<Set<String>> require = new HashSet<>();
        Set<String> roleSet = new HashSet<>();
        roleSet.add("USER");
        require.add(roleSet);
        meta.require = require;
        
        String result = meta.toString();
        assertNotNull(result);
    }
}
