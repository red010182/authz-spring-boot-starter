package cn.omisheep.authz.core.auth.rpd;

import cn.omisheep.authz.core.LogLevel;
import cn.omisheep.authz.core.util.LogUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DataPermRolesMetaTest {

    @BeforeAll
    static void setup() {
        LogUtils.setLogLevel(LogLevel.INFO);
    }

    @Test
    void testDataPermRolesMetaCreation() {
        DataPermRolesMeta meta = new DataPermRolesMeta();
        assertNotNull(meta);
    }

    @Test
    void testConditionSetting() {
        DataPermRolesMeta meta = new DataPermRolesMeta();
        meta.setCondition("user_id = #{userId}");
        
        assertEquals("user_id = #{userId}", meta.getCondition());
    }

    @Test
    void testArgsMapSetting() {
        DataPermRolesMeta meta = new DataPermRolesMeta();
        
        Map<String, List<String>> argsMap = new HashMap<>();
        argsMap.put("userId", Arrays.asList("#{token.userId}"));
        meta.setArgsMap(argsMap);
        
        assertEquals(1, meta.getArgsMap().size());
        assertTrue(meta.getArgsMap().containsKey("userId"));
    }
}
