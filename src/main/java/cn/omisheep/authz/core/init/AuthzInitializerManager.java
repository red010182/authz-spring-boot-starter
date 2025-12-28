package cn.omisheep.authz.core.init;

import cn.omisheep.authz.core.util.LogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Comparator;
import java.util.List;

/**
 * Manages the initialization of all AuthzInitializer components.
 * Initializes components in order based on their getOrder() values.
 * 
 * @author refactored for testability
 * @since 1.2.14
 */
@Component
public class AuthzInitializerManager {
    
    private final ApplicationContext applicationContext;
    private final List<AuthzInitializer> initializers;
    
    @Autowired
    public AuthzInitializerManager(ApplicationContext applicationContext,
                                   @Autowired(required = false) List<AuthzInitializer> initializers) {
        this.applicationContext = applicationContext;
        this.initializers = initializers != null ? initializers : List.of();
    }
    
    @PostConstruct
    public void initializeAll() {
        if (initializers.isEmpty()) {
            LogUtils.debug("No AuthzInitializer components found");
            return;
        }
        
        LogUtils.info("Initializing {} AuthzInitializer components", initializers.size());
        
        initializers.stream()
                .sorted(Comparator.comparingInt(AuthzInitializer::getOrder))
                .forEach(initializer -> {
                    try {
                        LogUtils.debug("Initializing: {}", initializer.getName());
                        initializer.initialize(applicationContext);
                        LogUtils.debug("Initialized: {}", initializer.getName());
                    } catch (Exception e) {
                        LogUtils.error("Failed to initialize {}: {}", initializer.getName(), e.getMessage());
                        throw new RuntimeException("Initialization failed for " + initializer.getName(), e);
                    }
                });
        
        LogUtils.info("All AuthzInitializer components initialized successfully");
    }
    
    /**
     * Get the list of initializers (for testing purposes).
     */
    public List<AuthzInitializer> getInitializers() {
        return initializers;
    }
}
