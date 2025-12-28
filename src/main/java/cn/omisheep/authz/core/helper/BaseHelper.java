package cn.omisheep.authz.core.helper;

import cn.omisheep.authz.core.AuthzProperties;
import cn.omisheep.authz.core.auth.PermLibrary;
import cn.omisheep.authz.core.auth.deviced.UserDevicesDict;
import cn.omisheep.authz.core.cache.Cache;
import cn.omisheep.authz.core.oauth.OpenAuthLibrary;
import cn.omisheep.authz.core.AuthzContext;
import org.springframework.context.ApplicationContext;

/**
 * @author zhouxinchen
 * @since 1.2.0
 */
@SuppressWarnings("rawtypes")
public abstract class BaseHelper {
    protected static ApplicationContext ctx;
    protected static AuthzProperties    properties;
    protected static UserDevicesDict    userDevicesDict;
    protected static Cache              cache;
    protected static PermLibrary        permLibrary;
    protected static OpenAuthLibrary    openAuthLibrary;

    public static void initHelper(ApplicationContext applicationContext) {
        ctx             = applicationContext;
        try {
            properties      = ctx.getBean(AuthzProperties.class);
        } catch (Exception e) { /* ignore for partial testing */ }
        try {
            userDevicesDict = ctx.getBean(UserDevicesDict.class);
        } catch (Exception e) { /* ignore */ }
        try {
            cache           = ctx.getBean("authzCache", Cache.class);
        } catch (Exception e) { /* ignore */ }
        try {
            permLibrary     = ctx.getBean(PermLibrary.class);
        } catch (Exception e) { /* ignore */ }
        try {
            openAuthLibrary = ctx.getBean(OpenAuthLibrary.class);
        } catch (Exception e) { /* ignore */ }
    }
}
