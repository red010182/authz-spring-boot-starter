package cn.omisheep.authz.core.auth;

import cn.omisheep.authz.core.util.LogUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultPermLibraryTest {

    @Test
    void shouldReturnEmptySetWhenGetRolesByUserId() {
        // Given
        DefaultPermLibrary permLibrary = new DefaultPermLibrary();
        String userId = "test-user-123";
        
        try (MockedStatic<LogUtils> logUtilsMock = Mockito.mockStatic(LogUtils.class)) {
            // When
            Set<String> roles = permLibrary.getRolesByUserId(userId);
            
            // Then
            assertThat(roles).isNotNull().isEmpty();
            logUtilsMock.verify(() -> LogUtils.warn("[WARN] 没有配置自定义的PermLibrary"));
        }
    }

    @Test
    void shouldReturnEmptySetWhenGetPermissionsByRole() {
        // Given
        DefaultPermLibrary permLibrary = new DefaultPermLibrary();
        String role = "test-role";
        
        try (MockedStatic<LogUtils> logUtilsMock = Mockito.mockStatic(LogUtils.class)) {
            // When
            Set<String> permissions = permLibrary.getPermissionsByRole(role);
            
            // Then
            assertThat(permissions).isNotNull().isEmpty();
            logUtilsMock.verify(() -> LogUtils.warn("[WARN] 没有配置自定义的PermLibrary"));
        }
    }

    @Test
    void shouldImplementPermLibraryInterface() {
        // Given
        DefaultPermLibrary permLibrary = new DefaultPermLibrary();
        
        // Then
        assertThat(permLibrary).isInstanceOf(PermLibrary.class);
    }
}
