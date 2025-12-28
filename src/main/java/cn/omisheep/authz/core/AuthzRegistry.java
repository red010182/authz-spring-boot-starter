package cn.omisheep.authz.core;

import cn.omisheep.authz.core.auth.PermLibrary;
import cn.omisheep.authz.core.cache.Cache;
import cn.omisheep.authz.core.oauth.OpenAuthLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Central registry for accessing Authz components.
 * Provides both instance-based access (for testing) and static access (for backward compatibility).
 * 
 * @author refactored for testability
 * @since 1.2.14
 */
@Component
public class AuthzRegistry {
    
    private static AuthzRegistry instance;
    
    private final ApplicationContext applicationContext;
    private final AuthzProperties properties;
    private final Cache cache;
    private final PermLibrary permLibrary;
    private final OpenAuthLibrary openAuthLibrary;
    
    @Autowired
    public AuthzRegistry(ApplicationContext applicationContext,
                         AuthzProperties properties,
                         Cache cache,
                         @Autowired(required = false) PermLibrary permLibrary,
                         @Autowired(required = false) OpenAuthLibrary openAuthLibrary) {
        this.applicationContext = applicationContext;
        this.properties = properties;
        this.cache = cache;
        this.permLibrary = permLibrary;
        this.openAuthLibrary = openAuthLibrary;
        
        // Set static instance for backward compatibility
        instance = this;
    }
    
    /**
     * Get the singleton instance (for backward compatibility with static code).
     */
    public static AuthzRegistry getInstance() {
        return instance;
    }
    
    /**
     * Check if the registry has been initialized.
     */
    public static boolean isInitialized() {
        return instance != null;
    }
    
    // Instance accessors (preferred for new code and testing)
    
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
    
    public AuthzProperties getProperties() {
        return properties;
    }
    
    public Cache getCache() {
        return cache;
    }
    
    public PermLibrary getPermLibrary() {
        return permLibrary;
    }
    
    public OpenAuthLibrary getOpenAuthLibrary() {
        return openAuthLibrary;
    }
    
    /**
     * Get a bean from the application context.
     */
    public <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }
    
    /**
     * Get a bean by name from the application context.
     */
    public <T> T getBean(String name, Class<T> clazz) {
        return applicationContext.getBean(name, clazz);
    }
    
    // Static accessors (for backward compatibility)
    
    public static AuthzProperties properties() {
        return instance != null ? instance.properties : null;
    }
    
    public static Cache cache() {
        return instance != null ? instance.cache : null;
    }
    
    public static PermLibrary permLibrary() {
        return instance != null ? instance.permLibrary : null;
    }
    
    public static OpenAuthLibrary openAuthLibrary() {
        return instance != null ? instance.openAuthLibrary : null;
    }
}
