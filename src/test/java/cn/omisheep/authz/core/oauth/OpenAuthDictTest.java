package cn.omisheep.authz.core.oauth;

import cn.omisheep.authz.annotation.OAuthScope;
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
class OpenAuthDictTest {

    @BeforeEach
    void resetOpenAuthDict() throws Exception {
        Field isInitField = OpenAuthDict.class.getDeclaredField("isInit");
        isInitField.setAccessible(true);
        isInitField.set(null, false);
        
        AuthzProperties properties = new AuthzProperties();
        AuthzAppVersion.properties = properties;
        AuthzAppVersion.environment = mock(org.springframework.core.env.ConfigurableEnvironment.class);
    }

    @Test
    void testInit() {
        ApplicationContext context = mock(ApplicationContext.class);
        Map<RequestMappingInfo, HandlerMethod> mapRet = new HashMap<>();

        RequestMappingInfo mappingInfo = mock(RequestMappingInfo.class);
        HandlerMethod handlerMethod = mock(HandlerMethod.class);
        
        org.springframework.web.servlet.mvc.condition.PatternsRequestCondition patternsCondition = mock(org.springframework.web.servlet.mvc.condition.PatternsRequestCondition.class);
        org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition methodsCondition = mock(org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition.class);
        
        when(mappingInfo.getPatternsCondition()).thenReturn(patternsCondition);
        when(patternsCondition.getPatterns()).thenReturn(Collections.singleton("/oauth"));
        when(mappingInfo.getMethodsCondition()).thenReturn(methodsCondition);
        when(methodsCondition.getMethods()).thenReturn(Collections.singleton(org.springframework.web.bind.annotation.RequestMethod.GET));
        
        when(context.getBeansWithAnnotation(OAuthScope.class)).thenReturn(Collections.emptyMap());
        when(context.getBeansWithAnnotation(cn.omisheep.authz.annotation.OAuthScopeBasic.class)).thenReturn(Collections.emptyMap());
        
        when(handlerMethod.getBean()).thenReturn("testBean");
        try {
            when(handlerMethod.getMethod()).thenReturn(Object.class.getMethod("toString"));
        } catch (NoSuchMethodException e) {
            // should not happen
        }
        
        mapRet.put(mappingInfo, handlerMethod);

        OpenAuthDict.init(context, mapRet);
        
        assertThat(OpenAuthDict.getSrc()).isEmpty();
    }
}
