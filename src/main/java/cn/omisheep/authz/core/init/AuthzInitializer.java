package cn.omisheep.authz.core.init;

import org.springframework.context.ApplicationContext;

/**
 * Interface for components that require initialization after Spring context is ready.
 * Implementations can define initialization order via getOrder().
 * 
 * @author refactored for testability
 * @since 1.2.14
 */
public interface AuthzInitializer {
    
    /**
     * Initialize the component with the given ApplicationContext.
     * 
     * @param context the Spring ApplicationContext
     */
    void initialize(ApplicationContext context);
    
    /**
     * Get the initialization order. Lower values are initialized first.
     * 
     * @return the order value (default 0)
     */
    default int getOrder() {
        return 0;
    }
    
    /**
     * Get the name of this initializer for logging purposes.
     * 
     * @return the initializer name
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
}
