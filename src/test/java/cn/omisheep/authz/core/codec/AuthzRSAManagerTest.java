package cn.omisheep.authz.core.codec;

import cn.omisheep.authz.core.util.LogUtils;
import cn.omisheep.commons.util.RSAHelper;
import cn.omisheep.commons.util.TaskBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.concurrent.ScheduledFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthzRSAManagerTest {

    private MockedStatic<RSAHelper> rsaHelperMock;
    private MockedStatic<TaskBuilder> taskBuilderMock;
    private MockedStatic<LogUtils> logUtilsMock;
    private ScheduledFuture<?> mockScheduledFuture;

    @BeforeEach
    void setUp() throws Exception {
        rsaHelperMock = Mockito.mockStatic(RSAHelper.class);
        taskBuilderMock = Mockito.mockStatic(TaskBuilder.class);
        logUtilsMock = Mockito.mockStatic(LogUtils.class);
        mockScheduledFuture = mock(ScheduledFuture.class);

        // Reset static fields
        resetStaticFields();
    }

    @AfterEach
    void tearDown() throws Exception {
        rsaHelperMock.close();
        taskBuilderMock.close();
        logUtilsMock.close();
        resetStaticFields();
    }

    private void resetStaticFields() throws Exception {
        // Reset static fields using reflection
        Field scheduledFutureField = AuthzRSAManager.class.getDeclaredField("scheduledFuture");
        scheduledFutureField.setAccessible(true);
        scheduledFutureField.set(null, null);

        Field autoField = AuthzRSAManager.class.getDeclaredField("auto");
        autoField.setAccessible(true);
        autoField.set(null, false);

        Field auKeyPairField = AuthzRSAManager.class.getDeclaredField("auKeyPair");
        auKeyPairField.setAccessible(true);
        auKeyPairField.set(null, null);

        Field timeField = AuthzRSAManager.class.getDeclaredField("time");
        timeField.setAccessible(true);
        timeField.set(null, null);
    }

    @Test
    void testConstructorThrowsException() {
        // Test that constructor throws UnsupportedOperationException
        assertThrows(UnsupportedOperationException.class, () -> {
            try {
                Constructor<AuthzRSAManager> constructor = AuthzRSAManager.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            } catch (Exception e) {
                if (e.getCause() instanceof UnsupportedOperationException) {
                    throw (UnsupportedOperationException) e.getCause();
                }
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void testSetTime() throws Exception {
        // Test setTime method
        AuthzRSAManager.setTime("0/30 * * * * ?");
        
        Field timeField = AuthzRSAManager.class.getDeclaredField("time");
        timeField.setAccessible(true);
        String timeValue = (String) timeField.get(null);
        
        assertEquals("0/30 * * * * ?", timeValue);
    }

    @Test
    void testSetAutoTrue() {
        // Mock TaskBuilder.schedule to return a ScheduledFuture
        taskBuilderMock.when(() -> TaskBuilder.schedule(any(Runnable.class), anyString()))
                .thenReturn(mockScheduledFuture);

        // Set time first
        AuthzRSAManager.setTime("0/30 * * * * ?");
        
        // Test setAuto with true
        AuthzRSAManager.setAuto(true);

        // Verify that schedule was called
        taskBuilderMock.verify(() -> TaskBuilder.schedule(any(Runnable.class), eq("0/30 * * * * ?")));
    }

    @Test
    void testSetAutoFalseWhenScheduledFutureExists() {
        // First set auto to true with a scheduled future
        taskBuilderMock.when(() -> TaskBuilder.schedule(any(Runnable.class), anyString()))
                .thenReturn(mockScheduledFuture);
        
        AuthzRSAManager.setTime("0/30 * * * * ?");
        AuthzRSAManager.setAuto(true);

        // Now set auto to false - according to the source code, this should NOT cancel the scheduled future
        AuthzRSAManager.setAuto(false);

        // Verify that cancel was NOT called (setAuto(false) doesn't cancel)
        verify(mockScheduledFuture, never()).cancel(true);
    }

    @Test
    void testSetAutoFalseWhenNoScheduledFuture() {
        // Set auto to false without having a scheduled future
        AuthzRSAManager.setAuto(false);
        
        // Should not throw any exception
        assertTrue(true);
    }

    @Test
    void testSetAuKeyPair() throws Exception {
        // Test setAuKeyPair - this will create a real RSAKeyPair
        AuthzRSAManager.setAuKeyPair("public-key", "private-key");

        // Verify fields were set
        Field auKeyPairField = AuthzRSAManager.class.getDeclaredField("auKeyPair");
        auKeyPairField.setAccessible(true);
        RSAHelper.RSAKeyPair actualKeyPair = (RSAHelper.RSAKeyPair) auKeyPairField.get(null);
        
        assertNotNull(actualKeyPair);
        assertEquals("public-key", actualKeyPair.getPublicKey());
        assertEquals("private-key", actualKeyPair.getPrivateKey());

        Field autoField = AuthzRSAManager.class.getDeclaredField("auto");
        autoField.setAccessible(true);
        boolean autoValue = (boolean) autoField.get(null);
        
        assertFalse(autoValue);
    }

    @Test
    void testRefreshKeyGroup() {
        // Mock RSAHelper.genKeyPair()
        RSAHelper.RSAKeyPair mockKeyPair = mock(RSAHelper.RSAKeyPair.class);
        when(mockKeyPair.getPublicKey()).thenReturn("new-public-key");
        when(mockKeyPair.getPrivateKey()).thenReturn("new-private-key");
        
        rsaHelperMock.when(RSAHelper::genKeyPair).thenReturn(mockKeyPair);

        // Test refreshKeyGroup
        AuthzRSAManager.refreshKeyGroup();

        // Verify genKeyPair was called
        rsaHelperMock.verify(RSAHelper::genKeyPair);
        
        // Verify LogUtils.debug was called
        logUtilsMock.verify(() -> LogUtils.debug(anyString(), any()));
    }

    @Test
    void testEncrypt() throws Exception {
        // Setup auKeyPair
        RSAHelper.RSAKeyPair mockKeyPair = mock(RSAHelper.RSAKeyPair.class);
        when(mockKeyPair.getPublicKey()).thenReturn("public-key");
        
        Field auKeyPairField = AuthzRSAManager.class.getDeclaredField("auKeyPair");
        auKeyPairField.setAccessible(true);
        auKeyPairField.set(null, mockKeyPair);

        // Mock RSAHelper.encrypt
        rsaHelperMock.when(() -> RSAHelper.encrypt("plaintext", "public-key"))
                .thenReturn("encrypted-text");

        // Test encrypt
        String result = AuthzRSAManager.encrypt("plaintext");

        assertEquals("encrypted-text", result);
        rsaHelperMock.verify(() -> RSAHelper.encrypt("plaintext", "public-key"));
    }

    @Test
    void testDecrypt() throws Exception {
        // Setup auKeyPair
        RSAHelper.RSAKeyPair mockKeyPair = mock(RSAHelper.RSAKeyPair.class);
        when(mockKeyPair.getPrivateKey()).thenReturn("private-key");
        
        Field auKeyPairField = AuthzRSAManager.class.getDeclaredField("auKeyPair");
        auKeyPairField.setAccessible(true);
        auKeyPairField.set(null, mockKeyPair);

        // Mock RSAHelper.decrypt
        rsaHelperMock.when(() -> RSAHelper.decrypt("encrypted-text", "private-key"))
                .thenReturn("decrypted-text");

        // Test decrypt
        String result = AuthzRSAManager.decrypt("encrypted-text");

        assertEquals("decrypted-text", result);
        rsaHelperMock.verify(() -> RSAHelper.decrypt("encrypted-text", "private-key"));
    }

    @Test
    void testGetPublicKeyString() throws Exception {
        // Setup auKeyPair
        RSAHelper.RSAKeyPair mockKeyPair = mock(RSAHelper.RSAKeyPair.class);
        when(mockKeyPair.getPublicKey()).thenReturn("public-key-string");
        
        Field auKeyPairField = AuthzRSAManager.class.getDeclaredField("auKeyPair");
        auKeyPairField.setAccessible(true);
        auKeyPairField.set(null, mockKeyPair);

        // Test getPublicKeyString
        String result = AuthzRSAManager.getPublicKeyString();

        assertEquals("public-key-string", result);
    }

    @Test
    void testGetPrivateKeyString() throws Exception {
        // Setup auKeyPair
        RSAHelper.RSAKeyPair mockKeyPair = mock(RSAHelper.RSAKeyPair.class);
        when(mockKeyPair.getPrivateKey()).thenReturn("private-key-string");
        
        Field auKeyPairField = AuthzRSAManager.class.getDeclaredField("auKeyPair");
        auKeyPairField.setAccessible(true);
        auKeyPairField.set(null, mockKeyPair);

        // Test getPrivateKeyString
        String result = AuthzRSAManager.getPrivateKeyString();

        assertEquals("private-key-string", result);
    }

    @Test
    void testSetAutoCancelsExistingScheduledFuture() throws Exception {
        // First set up a scheduled future
        taskBuilderMock.when(() -> TaskBuilder.schedule(any(Runnable.class), anyString()))
                .thenReturn(mockScheduledFuture);
        
        AuthzRSAManager.setTime("0/30 * * * * ?");
        AuthzRSAManager.setAuto(true);

        // Verify scheduled future was set
        Field scheduledFutureField = AuthzRSAManager.class.getDeclaredField("scheduledFuture");
        scheduledFutureField.setAccessible(true);
        assertNotNull(scheduledFutureField.get(null));

        // Now set auto to true again (should cancel existing)
        AuthzRSAManager.setAuto(true);

        // Verify cancel was called
        verify(mockScheduledFuture).cancel(true);
    }

    @Test
    void testSetAuKeyPairCancelsScheduledFuture() throws Exception {
        // First set up a scheduled future
        taskBuilderMock.when(() -> TaskBuilder.schedule(any(Runnable.class), anyString()))
                .thenReturn(mockScheduledFuture);
        
        AuthzRSAManager.setTime("0/30 * * * * ?");
        AuthzRSAManager.setAuto(true);

        // Now set auKeyPair (should cancel scheduled future)
        AuthzRSAManager.setAuKeyPair("public-key", "private-key");

        // Verify cancel was called
        verify(mockScheduledFuture).cancel(true);

        // Verify scheduled future is null
        Field scheduledFutureField = AuthzRSAManager.class.getDeclaredField("scheduledFuture");
        scheduledFutureField.setAccessible(true);
        assertNull(scheduledFutureField.get(null));
    }

    @Test
    void testSetAuKeyPairWithNullScheduledFuture() {
        // Test setAuKeyPair when scheduledFuture is null
        // Should not throw any exception
        AuthzRSAManager.setAuKeyPair("public-key", "private-key");
        
        assertTrue(true);
    }
}
