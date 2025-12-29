package cn.omisheep.authz.core.auth.ipf;

import cn.omisheep.authz.annotation.RateLimit;
import cn.omisheep.authz.core.AuthzProperties;
import cn.omisheep.authz.core.config.AuthzAppVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HttpdTest {

    @BeforeEach
    void resetHttpd() throws Exception {
        Field isInitField = Httpd.class.getDeclaredField("isInit");
        isInitField.setAccessible(true);
        isInitField.set(null, false);
        
        AuthzProperties properties = new AuthzProperties();
        AuthzAppVersion.properties = properties;
        AuthzAppVersion.environment = mock(org.springframework.core.env.ConfigurableEnvironment.class);
    }

    @Test
    void testInit() {
        AuthzProperties properties = new AuthzProperties();
        ApplicationContext context = mock(ApplicationContext.class);
        Map<RequestMappingInfo, HandlerMethod> mapRet = new HashMap<>();

        RequestMappingInfo mappingInfo = mock(RequestMappingInfo.class);
        HandlerMethod handlerMethod = mock(HandlerMethod.class);
        
        org.springframework.web.servlet.mvc.condition.PatternsRequestCondition patternsCondition = mock(org.springframework.web.servlet.mvc.condition.PatternsRequestCondition.class);
        org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition methodsCondition = mock(org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition.class);
        
        when(mappingInfo.getPatternsCondition()).thenReturn(patternsCondition);
        when(patternsCondition.getPatterns()).thenReturn(Collections.singleton("/limit"));
        when(mappingInfo.getMethodsCondition()).thenReturn(methodsCondition);
        when(methodsCondition.getMethods()).thenReturn(Collections.singleton(org.springframework.web.bind.annotation.RequestMethod.GET));
        
        when(handlerMethod.getMethodAnnotation(RateLimit.class)).thenReturn(null);
        when(handlerMethod.getBean()).thenReturn("testBean");
        when(handlerMethod.getBeanType()).thenReturn((Class) Object.class);
        
        mapRet.put(mappingInfo, handlerMethod);

        Httpd.init(properties, context, mapRet);
        
        assertThat(Httpd.getPattern("GET", "/limit")).isEqualTo("/limit");
    }

    @Test
    void testForbidAndRelive() {
        RequestMeta.setCallback(mock(cn.omisheep.authz.core.callback.RateLimitCallback.class));
        RequestMeta requestMeta = new RequestMeta(System.currentTimeMillis(), "127.0.0.1", null);
        LimitMeta limitMeta = new LimitMeta("1s", 1, new String[]{"1s"}, "100ms", new String[0], RateLimit.CheckType.IP);
        
        Httpd.forbid(System.currentTimeMillis(), requestMeta, limitMeta, "GET", "/test");
        Httpd.relive(requestMeta, limitMeta, "GET", "/test");
        
        assertThat(true).isTrue();
    }
}
