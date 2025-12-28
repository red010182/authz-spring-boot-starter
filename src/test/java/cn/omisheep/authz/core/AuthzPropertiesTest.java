package cn.omisheep.authz.core;

import cn.omisheep.authz.core.auth.deviced.DeviceCountInfo;
import cn.omisheep.authz.core.codec.RSADecryptor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AuthzPropertiesTest {

    @Test
    void testDefaultValues() {
        AuthzProperties properties = new AuthzProperties();
        
        assertEquals("defaultApp", properties.getApp());
        assertTrue(properties.isBanner());
        assertEquals(LogLevel.WARN, properties.getLog());
        assertEquals(RSADecryptor.class, properties.getDefaultDecryptor());
        assertNull(properties.getOrm());
        
        // Test nested configs are not null
        assertNotNull(properties.getToken());
        assertNotNull(properties.getUser());
        assertNotNull(properties.getCache());
        assertNotNull(properties.getRsa());
        assertNotNull(properties.getGlobalIpRange());
        assertNotNull(properties.getDashboard());
        assertNotNull(properties.getResponse());
        assertNotNull(properties.getSys());
        assertNotNull(properties.getOauth());
    }

    @Test
    void testTokenConfigDefaults() {
        AuthzProperties.TokenConfig tokenConfig = new AuthzProperties.TokenConfig();
        
        assertNull(tokenConfig.getKey());
        assertEquals(8, tokenConfig.getIdBits());
        assertEquals("atkn", tokenConfig.getCookieName());
        assertEquals("authorization", tokenConfig.getHeaderName());
        assertEquals("Bearer", tokenConfig.getHeaderPrefix());
        assertEquals("7d", tokenConfig.getAccessTime());
        assertEquals("30d", tokenConfig.getRefreshTime());
        assertTrue(tokenConfig.isLogoutBeforeLogin());
        assertFalse(tokenConfig.isBindIp());
    }

    @Test
    void testUserConfigDefaults() {
        AuthzProperties.UserConfig userConfig = new AuthzProperties.UserConfig();
        
        assertEquals(-1, userConfig.getMaximumTotalDevice());
        assertEquals(-1, userConfig.getMaximumTotalSameTypeDevice());
        assertNotNull(userConfig.getTypesTotal());
        assertTrue(userConfig.getTypesTotal().isEmpty());
    }

    @Test
    void testUserConfigGetMaximumTotalDevice() {
        AuthzProperties.UserConfig userConfig = new AuthzProperties.UserConfig();
        
        // Test default
        assertEquals(-1, userConfig.getMaximumTotalDevice());
        
        // Test 0 becomes 1
        userConfig.setMaximumTotalDevice(0);
        assertEquals(1, userConfig.getMaximumTotalDevice());
        
        // Test negative stays negative
        userConfig.setMaximumTotalDevice(-5);
        assertEquals(-1, userConfig.getMaximumTotalDevice());
        
        // Test positive stays positive
        userConfig.setMaximumTotalDevice(10);
        assertEquals(10, userConfig.getMaximumTotalDevice());
    }

    @Test
    void testUserConfigGetMaximumTotalSameTypeDevice() {
        AuthzProperties.UserConfig userConfig = new AuthzProperties.UserConfig();
        
        // Test default
        assertEquals(-1, userConfig.getMaximumTotalSameTypeDevice());
        
        // Test 0 becomes 1
        userConfig.setMaximumTotalSameTypeDevice(0);
        assertEquals(1, userConfig.getMaximumTotalSameTypeDevice());
        
        // Test negative stays negative
        userConfig.setMaximumTotalSameTypeDevice(-5);
        assertEquals(-1, userConfig.getMaximumTotalSameTypeDevice());
        
        // Test positive stays positive
        userConfig.setMaximumTotalSameTypeDevice(5);
        assertEquals(5, userConfig.getMaximumTotalSameTypeDevice());
    }

    @Test
    void testUserConfigTypesTotal() {
        AuthzProperties.UserConfig userConfig = new AuthzProperties.UserConfig();
        
        List<DeviceCountInfo> typesTotal = new ArrayList<>();
        DeviceCountInfo info1 = new DeviceCountInfo();
        info1.setTypes(Set.of("web", "mobile"));
        info1.setTotal(5);
        
        DeviceCountInfo info2 = new DeviceCountInfo();
        info2.setTypes(Set.of("tablet"));
        info2.setTotal(3);
        
        typesTotal.add(info1);
        typesTotal.add(info2);
        
        userConfig.setTypesTotal(typesTotal);
        
        assertEquals(2, userConfig.getTypesTotal().size());
        assertEquals(Set.of("web", "mobile"), userConfig.getTypesTotal().get(0).getTypes());
        assertEquals(5, userConfig.getTypesTotal().get(0).getTotal());
        assertEquals(Set.of("tablet"), userConfig.getTypesTotal().get(1).getTypes());
        assertEquals(3, userConfig.getTypesTotal().get(1).getTotal());
    }

    @Test
    void testIpRangeConfigDefaults() {
        AuthzProperties.IpRangeConfig ipRangeConfig = new AuthzProperties.IpRangeConfig();
        
        assertNull(ipRangeConfig.getAllow());
        assertNull(ipRangeConfig.getDeny());
        assertTrue(ipRangeConfig.isSupportNative());
    }

    @Test
    void testCacheConfigDefaults() {
        AuthzProperties.CacheConfig cacheConfig = new AuthzProperties.CacheConfig();
        
        assertFalse(cacheConfig.isEnableRedis());
        assertFalse(cacheConfig.isEnableRedisActuator());
        assertEquals(10000, cacheConfig.getRedisScanCount());
        assertNull(cacheConfig.getCacheMaximumSize());
        assertEquals("10m", cacheConfig.getExpireAfterCreateTime());
        assertEquals("10m", cacheConfig.getExpireAfterUpdateTime());
        assertEquals("10m", cacheConfig.getExpireAfterReadTime());
    }

    @Test
    void testRSAConfigDefaults() {
        AuthzProperties.RSAConfig rsaConfig = new AuthzProperties.RSAConfig();
        
        assertTrue(rsaConfig.isAuto());
        assertEquals("7d", rsaConfig.getRsaKeyRefreshWithPeriod());
        assertNull(rsaConfig.getCustomPublicKey());
        assertNull(rsaConfig.getCustomPrivateKey());
    }

    @Test
    void testDashboardConfigDefaults() {
        AuthzProperties.DashboardConfig dashboardConfig = new AuthzProperties.DashboardConfig();
        
        assertFalse(dashboardConfig.isEnabled());
        assertNotNull(dashboardConfig.getUsers());
        assertTrue(dashboardConfig.getUsers().isEmpty());
        assertEquals("authz", dashboardConfig.getUsername());
        assertEquals("authz", dashboardConfig.getPassword());
        assertEquals(1, dashboardConfig.getPermissions().length);
        assertEquals(AuthzProperties.DashboardConfig.DashboardPermission.ALL, dashboardConfig.getPermissions()[0]);
        assertEquals("10m", dashboardConfig.getUnresponsiveExpirationTime());
        assertNull(dashboardConfig.getAllow());
        assertNull(dashboardConfig.getDeny());
    }

    @Test
    void testDashboardUserClass() {
        AuthzProperties.DashboardConfig.User user = new AuthzProperties.DashboardConfig.User();
        
        assertNull(user.getUsername());
        assertNull(user.getPassword());
        assertNull(user.getIp());
        assertNull(user.getPermissions());
        
        user.setUsername("testUser");
        user.setPassword("testPass");
        user.setIp("127.0.0.1");
        user.setPermissions(new AuthzProperties.DashboardConfig.DashboardPermission[]{
            AuthzProperties.DashboardConfig.DashboardPermission.API,
            AuthzProperties.DashboardConfig.DashboardPermission.DOCS
        });
        
        assertEquals("testUser", user.getUsername());
        assertEquals("testPass", user.getPassword());
        assertEquals("127.0.0.1", user.getIp());
        assertNotNull(user.getPermissions());
        assertEquals(2, user.getPermissions().length);
        assertEquals(AuthzProperties.DashboardConfig.DashboardPermission.API, user.getPermissions()[0]);
        assertEquals(AuthzProperties.DashboardConfig.DashboardPermission.DOCS, user.getPermissions()[1]);
    }

    @Test
    void testDashboardPermissionEnum() {
        AuthzProperties.DashboardConfig.DashboardPermission[] values = 
            AuthzProperties.DashboardConfig.DashboardPermission.values();
        
        assertEquals(11, values.length);
        assertEquals(AuthzProperties.DashboardConfig.DashboardPermission.API, 
            AuthzProperties.DashboardConfig.DashboardPermission.valueOf("API"));
        assertEquals(AuthzProperties.DashboardConfig.DashboardPermission.PARAMETER, 
            AuthzProperties.DashboardConfig.DashboardPermission.valueOf("PARAMETER"));
        assertEquals(AuthzProperties.DashboardConfig.DashboardPermission.ALL, 
            AuthzProperties.DashboardConfig.DashboardPermission.valueOf("ALL"));
    }

    @Test
    void testResponseConfigDefaults() {
        AuthzProperties.ResponseConfig responseConfig = new AuthzProperties.ResponseConfig();
        
        assertFalse(responseConfig.isAlwaysOk());
    }

    @Test
    void testOtherConfigDefaults() {
        AuthzProperties.OtherConfig otherConfig = new AuthzProperties.OtherConfig();
        
        assertNull(otherConfig.getGcPeriod());
        assertFalse(otherConfig.isMd5check());
    }

    @Test
    void testOpenAuthConfigDefaults() {
        AuthzProperties.OpenAuthConfig openAuthConfig = new AuthzProperties.OpenAuthConfig();
        
        assertEquals("10m", openAuthConfig.getAuthorizationCodeTime());
        assertEquals("basic", openAuthConfig.getDefaultBasicScope());
        assertEquals(" ", openAuthConfig.getScopeSeparator());
        assertEquals(24, openAuthConfig.getClientIdLength());
        assertEquals(30, openAuthConfig.getClientSecretLength());
        assertEquals(AuthzProperties.OpenAuthConfig.AuthorizationCodeAlgorithm.SHA1, 
            openAuthConfig.getAlgorithm());
    }

    @Test
    void testOpenAuthAuthorizationCodeAlgorithmEnum() {
        AuthzProperties.OpenAuthConfig.AuthorizationCodeAlgorithm sha256 = 
            AuthzProperties.OpenAuthConfig.AuthorizationCodeAlgorithm.SHA_256;
        AuthzProperties.OpenAuthConfig.AuthorizationCodeAlgorithm sha1 = 
            AuthzProperties.OpenAuthConfig.AuthorizationCodeAlgorithm.SHA1;
        AuthzProperties.OpenAuthConfig.AuthorizationCodeAlgorithm md5 = 
            AuthzProperties.OpenAuthConfig.AuthorizationCodeAlgorithm.MD5;
        
        assertEquals("SHA-256", sha256.getValue());
        assertEquals("SHA1", sha1.getValue());
        assertEquals("MD5", md5.getValue());
        
        AuthzProperties.OpenAuthConfig.AuthorizationCodeAlgorithm[] values = 
            AuthzProperties.OpenAuthConfig.AuthorizationCodeAlgorithm.values();
        assertEquals(3, values.length);
    }

    @Test
    void testORMEnum() {
        AuthzProperties.ORM[] values = AuthzProperties.ORM.values();
        
        assertEquals(1, values.length);
        assertEquals(AuthzProperties.ORM.MYBATIS, AuthzProperties.ORM.valueOf("MYBATIS"));
    }

    @Test
    void testSettersAndGetters() {
        AuthzProperties properties = new AuthzProperties();
        
        // Test basic properties
        properties.setApp("testApp");
        properties.setBanner(false);
        properties.setLog(LogLevel.DEBUG);
        properties.setDefaultDecryptor(RSADecryptor.class);
        properties.setOrm(AuthzProperties.ORM.MYBATIS);
        
        assertEquals("testApp", properties.getApp());
        assertFalse(properties.isBanner());
        assertEquals(LogLevel.DEBUG, properties.getLog());
        assertEquals(RSADecryptor.class, properties.getDefaultDecryptor());
        assertEquals(AuthzProperties.ORM.MYBATIS, properties.getOrm());
        
        // Test nested configs
        AuthzProperties.TokenConfig tokenConfig = new AuthzProperties.TokenConfig();
        tokenConfig.setKey("testKey");
        tokenConfig.setIdBits(16);
        properties.setToken(tokenConfig);
        
        assertEquals("testKey", properties.getToken().getKey());
        assertEquals(16, properties.getToken().getIdBits());
    }
}
