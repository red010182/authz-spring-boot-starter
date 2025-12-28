package cn.omisheep.authz.core.auth.rpd;

import cn.omisheep.authz.core.LogLevel;
import cn.omisheep.authz.core.util.LogUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PermRolesMetaTest {

    @BeforeAll
    static void setup() {
        LogUtils.setLogLevel(LogLevel.INFO);
    }

    @Test
    void testPermRolesMetaCreation() {
        PermRolesMeta meta = new PermRolesMeta();
        assertNotNull(meta);
        
        // New meta should be "non" (empty)
        assertTrue(meta.non());
    }

    @Test
    void testPermRolesMetaWithRoles() {
        PermRolesMeta meta = new PermRolesMeta();
        
        // PermRolesMeta uses Set<Set<String>> for roles
        Set<Set<String>> roles = new HashSet<>();
        Set<String> roleGroup = new HashSet<>();
        roleGroup.add("ADMIN");
        roleGroup.add("USER");
        roles.add(roleGroup);
        meta.setRequireRoles(roles);
        
        assertNotNull(meta.getRequireRoles());
        assertFalse(meta.non()); // Now has roles, so not "non"
    }

    @Test
    void testNonInterface() {
        PermRolesMeta meta = new PermRolesMeta();
        
        // Empty meta should be non
        assertTrue(meta.non());
        
        // With permissions, should not be non
        Set<Set<String>> perms = new HashSet<>();
        Set<String> permGroup = new HashSet<>();
        permGroup.add("read:users");
        perms.add(permGroup);
        meta.setRequirePermissions(perms);
        
        assertFalse(meta.non());
    }

    @Test
    void testClear() {
        PermRolesMeta meta = new PermRolesMeta();
        
        Set<Set<String>> roles = new HashSet<>();
        Set<String> roleGroup = new HashSet<>();
        roleGroup.add("ADMIN");
        roles.add(roleGroup);
        meta.setRequireRoles(roles);
        
        assertFalse(meta.non());
        
        meta.clear();
        assertTrue(meta.non());
    }
}
