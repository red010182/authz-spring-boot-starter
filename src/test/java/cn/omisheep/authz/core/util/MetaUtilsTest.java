package cn.omisheep.authz.core.util;

import cn.omisheep.authz.annotation.Auth;
import cn.omisheep.authz.core.auth.rpd.PermRolesMeta;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MetaUtilsTest {

    @Test
    void testIsController() {
        assertTrue(MetaUtils.isController(new TestController()));
        assertTrue(MetaUtils.isController(new TestRestController())); // RestController includes Controller
        assertFalse(MetaUtils.isController(new NotController()));
    }

    @Test
    void testGetTypeName() {
        // MetaUtils.getTypeName handles proxies or inner classes potentially?
        // Logic: name.substring(0, name.indexOf("$")) if $ exists.
        TestController tc = new TestController();
        String typeName = MetaUtils.getTypeName(tc);
        // Inner class name is cn...MetaUtilsTest$TestController
        // Logic splits at $, so it returns cn...MetaUtilsTest?
        // Let's check logic:
        // int i = name.indexOf('$'); if (i != -1) return name.substring(0, name.indexOf("$"));
        // This logic seems to strip inner class name part?
        // If so, it returns the outer class name?
        
        // Wait, if it is CGLIB proxy, it has $$ in name.
        // If it is inner class, it has $ in name.
        // If the intention is to get the user class name from a proxy, this logic is crude.
        // But let's verify what it does.
        
        String actual = tc.getClass().getTypeName();
        // e.g. cn.omisheep.authz.core.util.MetaUtilsTest$TestController
        
        String result = MetaUtils.getTypeName(tc);
        // Should be cn.omisheep.authz.core.util.TestController because it is top-level now (no $)
        assertEquals(TestController.class.getName(), result);
    }

    @Test
    void testGeneratePermRolesMeta() {
        Auth auth = MetaUtils.getAnnotation(new AuthClass(), Auth.class);
        assertNotNull(auth);
        
        PermRolesMeta meta = MetaUtils.generatePermRolesMeta(auth);
        assertNotNull(meta);
        assertTrue(meta.getRequireRoles().stream().anyMatch(s -> s.contains("admin")));
        assertTrue(meta.getExcludePermissions().stream().anyMatch(s -> s.contains("read")));
    }
    
    @Test
    void testGeneratePermRolesMetaFromArray() {
        PermRolesMeta meta = MetaUtils.generatePermRolesMeta(
                new String[]{"admin"}, 
                new String[]{}, 
                new String[]{"write"}, 
                new String[]{}
        );
        assertNotNull(meta);
        assertTrue(meta.getRequireRoles().stream().anyMatch(s -> s.contains("admin")));
        assertTrue(meta.getRequirePermissions().stream().anyMatch(s -> s.contains("write")));
    }
}

@Controller
class TestController {
}

@RestController
class TestRestController {
}

class NotController {}

@cn.omisheep.authz.annotation.Auth(requireRoles = "admin", excludePermissions = "read")
class AuthClass {}
