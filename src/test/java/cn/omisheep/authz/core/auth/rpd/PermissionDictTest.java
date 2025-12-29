package cn.omisheep.authz.core.auth.rpd;

import cn.omisheep.authz.core.AuthzProperties;
import cn.omisheep.authz.annotation.Auth;
import cn.omisheep.authz.annotation.Certificated;
import cn.omisheep.authz.annotation.IPRangeLimit;
import cn.omisheep.authz.core.AuthzResult;
import cn.omisheep.authz.core.auth.PermLibrary;
import cn.omisheep.authz.core.cache.Cache;
import cn.omisheep.authz.core.msg.AuthzModifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.lang.reflect.Field;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

/**
 * 單元測試 for PermissionDict
 * 使用 Mockito 模擬靜態依賴
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PermissionDictTest {

    @BeforeEach
    void resetPermissionDict() throws Exception {
        Field isInitField = PermissionDict.class.getDeclaredField("isInit");
        isInitField.setAccessible(true);
        isInitField.set(null, false);
        
        AuthzProperties properties = new AuthzProperties();
        cn.omisheep.authz.core.config.AuthzAppVersion.properties = properties;
        cn.omisheep.authz.core.config.AuthzAppVersion.environment = mock(org.springframework.core.env.ConfigurableEnvironment.class);
    }

    @Test
    void testInit() {
        ApplicationContext context = mock(ApplicationContext.class);
        PermLibrary permLibrary = mock(PermLibrary.class);
        Cache cache = mock(Cache.class);
        Map<RequestMappingInfo, HandlerMethod> mapRet = new HashMap<>();

        // Mock a handler method
        RequestMappingInfo mappingInfo = mock(RequestMappingInfo.class);
        HandlerMethod handlerMethod = mock(HandlerMethod.class);
        
        org.springframework.web.servlet.mvc.condition.PatternsRequestCondition patternsCondition = mock(org.springframework.web.servlet.mvc.condition.PatternsRequestCondition.class);
        org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition methodsCondition = mock(org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition.class);
        
        when(mappingInfo.getPatternsCondition()).thenReturn(patternsCondition);
        when(patternsCondition.getPatterns()).thenReturn(Collections.singleton("/test"));
        when(mappingInfo.getMethodsCondition()).thenReturn(methodsCondition);
        when(methodsCondition.getMethods()).thenReturn(Collections.singleton(org.springframework.web.bind.annotation.RequestMethod.GET));
        
        when(handlerMethod.getBean()).thenReturn("testBean");
        when(handlerMethod.getBeanType()).thenReturn((Class) Object.class);
        when(handlerMethod.getMethod()).thenReturn(Object.class.getDeclaredMethods()[0]);
        when(handlerMethod.getMethodParameters()).thenReturn(new org.springframework.core.MethodParameter[0]);
        
        mapRet.put(mappingInfo, handlerMethod);

        // Mock beans with annotations
        Map<String, Object> authBeans = new HashMap<>();
        authBeans.put("testBean", new Object());
        when(context.getBeansWithAnnotation(Auth.class)).thenReturn(authBeans);
        when(context.getBeansWithAnnotation(Certificated.class)).thenReturn(Collections.emptyMap());
        when(context.getBeansWithAnnotation(IPRangeLimit.class)).thenReturn(Collections.emptyMap());

        PermissionDict.init(context, permLibrary, cache, mapRet);
        
        assertThat(PermissionDict.isSupportNative()).isTrue();
    }

    @Test
    void testIsSupportNative() {
        // 測試預設值
        boolean supportNative = PermissionDict.isSupportNative();
        assertThat(supportNative).isTrue(); // 預設為 true
    }

    @Test
    void testGetPermSeparator() {
        // 測試預設分隔符
        String separator = PermissionDict.getPermSeparator();
        assertThat(separator).isEqualTo(",");
    }

    @Test
    void testSetPermSeparator() {
        // 測試設定分隔符
        PermissionDict.setPermSeparator(";");
        String separator = PermissionDict.getPermSeparator();
        assertThat(separator).isEqualTo(";");
        
        // 恢復預設值
        PermissionDict.setPermSeparator(",");
    }

    @Test
    void testGetControllerBeanNameWithNonExistentType() {
        // 測試取得不存在的 controller bean name
        String result = PermissionDict.getControllerBeanName("NonExistentType");
        assertThat(result).isNull();
    }

    @Test
    void testPutParam() {
        // 測試 putParam 方法
        String api = "test-api";
        String method = "GET";
        
        // 第一次呼叫應該建立空的 map
        PermissionDict.putParam(api, method);
        
        // 第二次呼叫應該不會有異常
        PermissionDict.putParam(api, method);
        
        // 測試帶有 paramMetadata 的 putParam
        String paramName = "testParam";
        ParamMetadata paramMetadata = ParamMetadata.of(String.class, ParamMetadata.ParamType.REQUEST_PARAM, null);
        PermissionDict.putParam(api, method, paramName, paramMetadata);
        
        // 驗證沒有異常拋出
        assertThat(true).isTrue();
    }

    @Test
    void testModifyWithNullTarget() {
        // 測試 modify 方法，target 為 null
        AuthzModifier modifier = new AuthzModifier();
        modifier.setTarget(null);
        
        Object result = PermissionDict.modify(modifier);
        assertThat(result).isInstanceOf(AuthzResult.class);
        assertThat((AuthzResult) result).isEqualTo(AuthzResult.FAIL);
    }

    @Test
    void testModifyAPIWithInvalidParameters() {
        // 測試修改 API 權限，使用無效參數
        AuthzModifier modifier = new AuthzModifier();
        modifier.setTarget(AuthzModifier.Target.API);
        modifier.setOperate(AuthzModifier.Operate.GET);
        // api 和 method 為 null
        
        Object result = PermissionDict.modifyAPI(modifier);
        // 應該返回 Map 或 ResponseResult，不是 AuthzResult
        assertThat(result).isNotNull();
    }

    @Test
    void testModifyParamWithInvalidParameters() {
        // 測試修改參數權限，使用無效參數
        AuthzModifier modifier = new AuthzModifier();
        modifier.setTarget(AuthzModifier.Target.PARAMETER);
        modifier.setOperate(AuthzModifier.Operate.GET);
        // 缺少必要參數
        
        Object result = PermissionDict.modifyParam(modifier);
        // 應該返回 AuthzResult.FAIL 或其他
        assertThat(result).isNotNull();
    }

    @Test
    void testModifyDataWithInvalidParameters() {
        // 測試修改資料權限，使用無效參數
        AuthzModifier modifier = new AuthzModifier();
        modifier.setTarget(AuthzModifier.Target.DATA_ROW);
        modifier.setOperate(AuthzModifier.Operate.GET);
        // className 為 null
        
        Object result = PermissionDict.modifyData(modifier);
        // 應該返回 Map 或 ResponseResult
        assertThat(result).isNotNull();
    }

    @Test
    void testModifyAPILoginTargetWithInvalidParameters() {
        // 測試修改 API 登入權限，使用無效參數
        AuthzModifier modifier = new AuthzModifier();
        modifier.setTarget(AuthzModifier.Target.LOGIN);
        modifier.setOperate(AuthzModifier.Operate.ADD);
        modifier.setApi("test-api");
        modifier.setMethod("GET");
        modifier.setValue(true); // 設定登入
        
        Object result = PermissionDict.modifyAPI(modifier);
        // 應該返回 ResponseResult
        assertThat(result).isNotNull();
    }

    @Test
    void testModifyAPIAddWithValidParameters() {
        // 測試修改 API 權限，ADD 操作
        AuthzModifier modifier = new AuthzModifier();
        modifier.setTarget(AuthzModifier.Target.API);
        modifier.setOperate(AuthzModifier.Operate.ADD);
        modifier.setApi("test-api");
        modifier.setMethod("GET");
        
        // 建立 Set<Set<String>>
        Set<Set<String>> requireRoles = new HashSet<>();
        requireRoles.add(new HashSet<>(Arrays.asList("admin")));
        Set<Set<String>> excludeRoles = new HashSet<>();
        excludeRoles.add(new HashSet<>(Arrays.asList("guest")));
        Set<Set<String>> requirePermissions = new HashSet<>();
        requirePermissions.add(new HashSet<>(Arrays.asList("read")));
        Set<Set<String>> excludePermissions = new HashSet<>();
        excludePermissions.add(new HashSet<>(Arrays.asList("write")));
        
        modifier.setRequireRoles(requireRoles);
        modifier.setExcludeRoles(excludeRoles);
        modifier.setRequirePermissions(requirePermissions);
        modifier.setExcludePermissions(excludePermissions);
        
        Object result = PermissionDict.modifyAPI(modifier);
        // 應該返回 ResponseResultMap
        assertThat(result).isNotNull();
    }

    @Test
    void testModifyParamAddWithValidParameters() {
        // 測試修改參數權限，ADD 操作
        AuthzModifier modifier = new AuthzModifier();
        modifier.setTarget(AuthzModifier.Target.PARAMETER);
        modifier.setOperate(AuthzModifier.Operate.ADD);
        modifier.setApi("test-api");
        modifier.setMethod("GET");
        modifier.setValue("paramName");
        modifier.setIndex(0);
        
        // 建立 Set<Set<String>>
        Set<Set<String>> requireRoles = new HashSet<>();
        requireRoles.add(new HashSet<>(Arrays.asList("admin")));
        Set<Set<String>> excludeRoles = new HashSet<>();
        excludeRoles.add(new HashSet<>(Arrays.asList("guest")));
        Set<Set<String>> requirePermissions = new HashSet<>();
        requirePermissions.add(new HashSet<>(Arrays.asList("read")));
        Set<Set<String>> excludePermissions = new HashSet<>();
        excludePermissions.add(new HashSet<>(Arrays.asList("write")));
        
        modifier.setRequireRoles(requireRoles);
        modifier.setExcludeRoles(excludeRoles);
        modifier.setRequirePermissions(requirePermissions);
        modifier.setExcludePermissions(excludePermissions);
        
        // 需要先建立 param metadata
        PermissionDict.putParam("test-api", "GET", "paramName", 
            ParamMetadata.of(String.class, ParamMetadata.ParamType.REQUEST_PARAM, null));
        
        Object result = PermissionDict.modifyParam(modifier);
        // 應該返回 ResponseResult 或 AuthzResult
        assertThat(result).isNotNull();
    }
}
