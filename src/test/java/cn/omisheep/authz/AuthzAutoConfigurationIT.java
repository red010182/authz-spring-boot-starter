package cn.omisheep.authz;

import cn.omisheep.authz.core.AuthzProperties;
import cn.omisheep.authz.core.LogLevel;
import cn.omisheep.authz.core.auth.PermLibrary;
import cn.omisheep.authz.core.auth.deviced.UserDevicesDict;
import cn.omisheep.authz.core.cache.Cache;
import cn.omisheep.authz.core.codec.DecryptHandler;
import cn.omisheep.authz.core.codec.RSADecryptor;
import cn.omisheep.authz.core.config.AuCoreInitialization;
import cn.omisheep.authz.core.interceptor.AuthzExceptionHandler;
import cn.omisheep.authz.core.interceptor.AuthzMethodPermissionChecker;
import cn.omisheep.authz.core.oauth.OpenAuthLibrary;
import cn.omisheep.authz.core.resolver.AuthzHandlerRegister;
import cn.omisheep.authz.core.resolver.DecryptRequestBodyAdvice;
import cn.omisheep.authz.support.entity.Cloud;
import cn.omisheep.authz.support.entity.Docs;
import cn.omisheep.authz.support.entity.Info;
import cn.omisheep.authz.support.http.SupportServlet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * 整合測試 for {@link AuthzAutoConfiguration}
 * <p>
 * 測試 Spring Boot 自動配置是否正確創建所有必要的 Bean
 */
@SpringBootTest(classes = AuthzAutoConfigurationIT.TestApplication.class)
@TestPropertySource(properties = {
        "authz.app=test-app",
        "authz.log=DEBUG",
        "authz.cache.enable-redis=false",
        "authz.dashboard.enabled=true",
        "authz.orm=MYBATIS"
})
public class AuthzAutoConfigurationIT {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private AuthzProperties authzProperties;

    @Test
    public void testAuthzPropertiesBean() {
        // 驗證 AuthzProperties Bean 存在且配置正確
        assertThat(authzProperties).isNotNull();
        assertThat(authzProperties.getApp()).isEqualTo("test-app");
        assertThat(authzProperties.getLog()).isEqualTo(LogLevel.DEBUG);
        assertThat(authzProperties.getCache().isEnableRedis()).isFalse();
        assertThat(authzProperties.getDashboard().isEnabled()).isTrue();
    }

    @Test
    public void testCacheBean() {
        // 驗證 Cache Bean 存在
        Cache cache = applicationContext.getBean("authzCache", Cache.class);
        assertThat(cache).isNotNull();
        
        // 由於 enable-redis=false，應該使用 L1Cache
        assertThat(cache.getClass().getSimpleName()).isEqualTo("L1Cache");
    }

    @Test
    public void testPermLibraryBean() {
        // 驗證 PermLibrary Bean 存在
        PermLibrary permLibrary = applicationContext.getBean(PermLibrary.class);
        assertThat(permLibrary).isNotNull();
    }

    @Test
    public void testOpenAuthLibraryBean() {
        // 驗證 OpenAuthLibrary Bean 存在
        OpenAuthLibrary openAuthLibrary = applicationContext.getBean(OpenAuthLibrary.class);
        assertThat(openAuthLibrary).isNotNull();
    }

    @Test
    public void testUserDevicesDictBean() {
        // 驗證 UserDevicesDict Bean 存在
        UserDevicesDict userDevicesDict = applicationContext.getBean(UserDevicesDict.class);
        assertThat(userDevicesDict).isNotNull();
    }

    @Test
    public void testAuthzMethodPermissionCheckerBean() {
        // 驗證 AuthzMethodPermissionChecker Bean 存在
        AuthzMethodPermissionChecker checker = applicationContext.getBean(AuthzMethodPermissionChecker.class);
        assertThat(checker).isNotNull();
    }

    @Test
    public void testAuthzExceptionHandlerBean() {
        // 驗證 AuthzExceptionHandler Bean 存在
        AuthzExceptionHandler handler = applicationContext.getBean(AuthzExceptionHandler.class);
        assertThat(handler).isNotNull();
    }

    @Test
    public void testRSADecryptorBean() {
        // 驗證 RSADecryptor Bean 存在
        RSADecryptor decryptor = applicationContext.getBean(RSADecryptor.class);
        assertThat(decryptor).isNotNull();
    }

    @Test
    public void testDecryptHandlerBean() {
        // 驗證 DecryptHandler Bean 存在
        DecryptHandler decryptHandler = applicationContext.getBean(DecryptHandler.class);
        assertThat(decryptHandler).isNotNull();
    }

    @Test
    public void testDecryptRequestBodyAdviceBean() {
        // 驗證 DecryptRequestBodyAdvice Bean 存在
        DecryptRequestBodyAdvice advice = applicationContext.getBean(DecryptRequestBodyAdvice.class);
        assertThat(advice).isNotNull();
    }

    @Test
    public void testAuthzHandlerRegisterBean() {
        // 驗證 AuthzHandlerRegister Bean 存在
        AuthzHandlerRegister register = applicationContext.getBean(AuthzHandlerRegister.class);
        assertThat(register).isNotNull();
    }

    @Test
    public void testAuCoreInitializationBean() {
        // 驗證 AuCoreInitialization Bean 存在
        AuCoreInitialization initialization = applicationContext.getBean(AuCoreInitialization.class);
        assertThat(initialization).isNotNull();
    }

    @Test
    public void testDashboardBeansWhenEnabled() {
        // 驗證儀表板相關 Bean 存在（當 dashboard.enabled=true）
        Info info = applicationContext.getBean(Info.class);
        assertThat(info).isNotNull();
        
        Docs docs = applicationContext.getBean("authz-docs", Docs.class);
        assertThat(docs).isNotNull();
        
        Cloud cloud = applicationContext.getBean("authz-cloud", Cloud.class);
        assertThat(cloud).isNotNull();
        
        // 驗證 SupportServlet 註冊
        assertThat(applicationContext.containsBean("DashboardServlet")).isTrue();
    }

    @Test
    public void testAuthzHttpFilterBean() {
        // 驗證 AuthzHttpFilter Bean 存在
        Object filterBean = applicationContext.getBean("AuthzHttpFilter");
        assertThat(filterBean).isNotNull();
    }

    @Test
    public void testChannelNamesInitialization() {
        // 驗證通道名稱是否正確初始化
        // 這些是靜態欄位，我們可以通過反射檢查
        try {
            Class<?> versionMessageClass = Class.forName("cn.omisheep.authz.core.msg.VersionMessage");
            Object channelField = ReflectionTestUtils.getField(versionMessageClass, "CHANNEL");
            assertThat(channelField).isNotNull();
            assertThat(channelField.toString()).contains("test-app");
            
            Class<?> cacheMessageClass = Class.forName("cn.omisheep.authz.core.msg.CacheMessage");
            Object cacheChannelField = ReflectionTestUtils.getField(cacheMessageClass, "CHANNEL");
            assertThat(cacheChannelField).isNotNull();
            assertThat(cacheChannelField.toString()).contains("test-app");
            
            Class<?> requestMessageClass = Class.forName("cn.omisheep.authz.core.msg.RequestMessage");
            Object requestChannelField = ReflectionTestUtils.getField(requestMessageClass, "CHANNEL");
            assertThat(requestChannelField).isNotNull();
            assertThat(requestChannelField.toString()).contains("test-app");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @org.springframework.boot.context.properties.EnableConfigurationProperties({AuthzProperties.class})
    @org.springframework.context.annotation.Import(AuthzAutoConfiguration.class)
    static class TestApplication {
        
        @Bean
        public RedisConnectionFactory redisConnectionFactory() {
            // 提供一個模擬的 RedisConnectionFactory，因為我們禁用了 Redis
            return mock(RedisConnectionFactory.class);
        }
        
        @Bean(name = "authzRedisTemplate")
        public RedisTemplate<String, Object> redisTemplate() {
            // 提供一個模擬的 RedisTemplate
            return mock(RedisTemplate.class);
        }
    }
}
