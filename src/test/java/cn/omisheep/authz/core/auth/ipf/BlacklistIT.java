package cn.omisheep.authz.core.auth.ipf;

import cn.omisheep.authz.core.AuthzManager;
import cn.omisheep.authz.core.msg.AuthzModifier;
import cn.omisheep.authz.core.tk.AccessToken;
import cn.omisheep.authz.core.tk.GrantType;
import cn.omisheep.commons.util.TimeUtils;
import cn.omisheep.web.entity.ResponseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.text.ParseException;
import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 整合測試 for Blacklist
 * 使用 Mockito 模擬靜態方法
 */
class BlacklistIT {

    @BeforeEach
    void setUp() {
        // 清理黑名單以確保測試隔離
        clearAllBlacklists();
    }

    private void clearAllBlacklists() {
        // 清理 IP 黑名單
        Set<Blacklist.IP> ipSet = Blacklist.IP.list();
        for (Blacklist.IP ip : ipSet) {
            Blacklist.IP.remove(ip.getIp());
        }

        // 清理使用者黑名單
        Set<Blacklist.User> userSet = Blacklist.User.list();
        for (Blacklist.User user : userSet) {
            Blacklist.User.remove(user.getUserId(), user.getDeviceType(), user.getDeviceId());
        }

        // 清理 IP 範圍黑名單
        Set<Blacklist.IPRangeDeny> ipRangeSet = Blacklist.IPRangeDeny.list();
        for (Blacklist.IPRangeDeny ipRange : ipRangeSet) {
            Blacklist.IPRangeDeny.remove(ipRange.getValue());
        }
    }

    @Test
    void testIPBlacklistBasicOperations() {
        // 測試 IP 加入黑名單
        String ip = "192.168.1.100";
        long duration = 3600000L; // 1小時
        
        // 加入黑名單
        Blacklist.IP.update(ip, duration);
        
        // 檢查是否在黑名單中
        assertFalse(Blacklist.check(ip), "IP 應該在黑名單中，檢查應返回 false");
        assertFalse(Blacklist.IP.check(ip), "IP.check 應該返回 false");
        
        // 獲取黑名單項目
        Blacklist.IP ipEntry = Blacklist.IP.get(ip);
        assertNotNull(ipEntry, "應該能獲取到 IP 黑名單項目");
        assertEquals(ip, ipEntry.getIp(), "IP 應該匹配");
        
        // 從黑名單移除
        Blacklist.IP.remove(ip);
        
        // 檢查是否已移除
        assertTrue(Blacklist.check(ip), "IP 應該已從黑名單移除，檢查應返回 true");
        assertNull(Blacklist.IP.get(ip), "獲取已移除的 IP 應該返回 null");
    }

    @Test
    void testIPBlacklistWithDate() {
        // 測試使用 Date 物件設定黑名單
        String ip = "10.0.0.1";
        Date futureDate = new Date(System.currentTimeMillis() + 3600000L); // 1小時後
        
        Blacklist.IP.update(ip, futureDate);
        
        assertFalse(Blacklist.check(ip), "IP 應該在黑名單中");
        
        // 清理
        Blacklist.IP.remove(ip);
    }

    @Test
    void testUserBlacklistBasicOperations() {
        // 測試使用者黑名單
        String userId = "user123";
        String deviceType = "web";
        String deviceId = "device456";
        long duration = 1800000L; // 30分鐘
        
        // 加入使用者黑名單（無裝置）
        Blacklist.User.update(userId, null, null, duration);
        
        // 檢查使用者是否在黑名單中
        assertFalse(Blacklist.User.check(userId, null, null), "使用者應該在黑名單中");
        
        // 獲取使用者黑名單項目
        Blacklist.User userEntry = Blacklist.User.getUser(userId);
        assertNotNull(userEntry, "應該能獲取到使用者黑名單項目");
        assertEquals(userId, userEntry.getUserId(), "使用者 ID 應該匹配");
        
        // 加入特定裝置黑名單
        Blacklist.User.update(userId, deviceType, deviceId, duration);
        
        // 檢查特定裝置是否在黑名單中
        assertFalse(Blacklist.User.check(userId, deviceType, deviceId), "特定裝置應該在黑名單中");
        
        // 檢查其他裝置應該不受影響
        assertTrue(Blacklist.User.check(userId, "mobile", "otherDevice"), "其他裝置不應該在黑名單中");
        
        // 從黑名單移除
        Blacklist.User.remove(userId, deviceType, deviceId);
        Blacklist.User.remove(userId, null, null);
        
        // 檢查是否已移除
        assertTrue(Blacklist.User.check(userId, null, null), "使用者應該已從黑名單移除");
    }

    @Test
    void testUserBlacklistWithDate() {
        // 測試使用者黑名單使用 Date
        String userId = "user456";
        Date futureDate = new Date(System.currentTimeMillis() + 1800000L); // 30分鐘後
        
        Blacklist.User.update(userId, null, null, futureDate);
        
        assertFalse(Blacklist.User.check(userId, null, null), "使用者應該在黑名單中");
        
        // 清理
        Blacklist.User.remove(userId, null, null);
    }

    @Test
    void testIPRangeBlacklist() {
        // 測試 IP 範圍黑名單
        String ipRange = "192.168.1.0/24";
        long duration = 3600000L;
        
        // 加入 IP 範圍黑名單
        Blacklist.IPRangeDeny.update(ipRange, duration);
        
        // 檢查範圍內的 IP
        assertFalse(Blacklist.IPRangeDeny.check("192.168.1.100"), "IP 範圍內的地址應該被拒絕");
        assertFalse(Blacklist.check("192.168.1.200"), "Blacklist.check 應該返回 false");
        
        // 檢查範圍外的 IP
        assertTrue(Blacklist.IPRangeDeny.check("192.168.2.100"), "IP 範圍外的地址應該被允許");
        
        // 獲取 IP 範圍黑名單列表
        Set<Blacklist.IPRangeDeny> ipRangeList = Blacklist.IPRangeDeny.list();
        assertFalse(ipRangeList.isEmpty(), "IP 範圍黑名單列表不應該為空");
        
        // 從黑名單移除
        Blacklist.IPRangeDeny.remove(ipRange);
        
        // 檢查是否已移除
        assertTrue(Blacklist.IPRangeDeny.check("192.168.1.100"), "IP 範圍應該已從黑名單移除");
    }

    @Test
    void testCheckWithAccessToken() {
        // 測試帶有 AccessToken 的檢查
        String ip = "172.16.0.1";
        String userId = "user789";
        String deviceType = "mobile";
        String deviceId = "device789";
        
        // 建立 AccessToken - 使用正確的構造函數
        AccessToken accessToken = new AccessToken(
            "tokenId", "token", "accessTokenId", 3600000L, 
            System.currentTimeMillis() + 3600000L, GrantType.AUTHORIZATION_CODE, "clientId", 
            "scope", userId, deviceType, deviceId
        );
        
        // 先確保不在黑名單中
        assertTrue(Blacklist.check(ip, accessToken), "初始狀態應該通過檢查");
        
        // 將使用者加入黑名單
        Blacklist.User.update(userId, deviceType, deviceId, 3600000L);
        
        // 現在應該被拒絕
        assertFalse(Blacklist.check(ip, accessToken), "使用者黑名單應該導致檢查失敗");
        
        // 清理
        Blacklist.User.remove(userId, deviceType, deviceId);
    }

    @Test
    void testCheckWithNullParameters() {
        // 測試帶有空參數的檢查
        assertTrue(Blacklist.check("192.168.1.1", null, null, null), "空參數應該通過檢查");
        assertTrue(Blacklist.check("192.168.1.1", (AccessToken) null), "空 AccessToken 應該通過檢查");
        assertTrue(Blacklist.check("192.168.1.1"), "簡單 IP 檢查應該通過");
    }

    @Test
    void testModifyMethod() throws ParseException {
        // 使用 Mockito 模擬 AuthzManager.operate 方法
        try (MockedStatic<AuthzManager> authzManagerMock = Mockito.mockStatic(AuthzManager.class)) {
            // 測試 modify 方法
            AuthzModifier modifier = new AuthzModifier()
                    .setTarget(AuthzModifier.Target.BLACKLIST);
            
            // 測試 IP 黑名單操作
            AuthzModifier.BlacklistInfo ipInfo = new AuthzModifier.BlacklistInfo()
                    .setType(AuthzModifier.BlacklistInfo.TYPE.IP)
                    .setOp(AuthzModifier.BlacklistInfo.OP.UPDATE)
                    .setIp("10.0.0.100")
                    .setTime(3600000L);
            
            modifier.setBlacklistInfo(ipInfo);
            
            // 執行修改
            ResponseResult<?> result = Blacklist.modify(modifier);
            
            assertNotNull(result, "結果不應該為 null");
            
            // 驗證 AuthzManager.operate 被調用
            authzManagerMock.verify(() -> 
                AuthzManager.operate(any(AuthzModifier.class))
            );
            
            // 檢查 IP 是否在黑名單中（由於模擬，實際不會加入）
            // 所以我們不檢查 Blacklist.check
            
            // 清理 - 使用模擬的移除操作
            AuthzModifier.BlacklistInfo removeInfo = new AuthzModifier.BlacklistInfo()
                    .setType(AuthzModifier.BlacklistInfo.TYPE.IP)
                    .setOp(AuthzModifier.BlacklistInfo.OP.REMOVE)
                    .setIp("10.0.0.100");
            
            modifier.setBlacklistInfo(removeInfo);
            Blacklist.modify(modifier);
            
            // 再次驗證 AuthzManager.operate 被調用
            authzManagerMock.verify(() -> 
                AuthzManager.operate(any(AuthzModifier.class)), times(2)
            );
        }
    }

    @Test
    void testReadAllMethod() {
        // 測試 readAll 方法
        // 先加入一些測試數據
        Blacklist.IP.update("192.168.1.1", 3600000L);
        Blacklist.User.update("testUser", null, null, 3600000L);
        Blacklist.IPRangeDeny.update("10.0.0.0/8", 3600000L);
        
        // 讀取所有黑名單
        var allBlacklists = Blacklist.readAll();
        
        assertNotNull(allBlacklists, "readAll 不應該返回 null");
        assertTrue(allBlacklists.containsKey("ipBlacklist"), "應該包含 ipBlacklist");
        assertTrue(allBlacklists.containsKey("userBlacklist"), "應該包含 userBlacklist");
        assertTrue(allBlacklists.containsKey("ipRangeBlacklist"), "應該包含 ipRangeBlacklist");
        
        // 清理
        Blacklist.IP.remove("192.168.1.1");
        Blacklist.User.remove("testUser", null, null);
        Blacklist.IPRangeDeny.remove("10.0.0.0/8");
    }

    @Test
    void testTimeMetaExpiration() throws InterruptedException {
        // 測試時間過期功能
        String ip = "192.168.0.1";
        long shortDuration = 100L; // 100毫秒
        
        // 加入黑名單（短時間）
        Blacklist.IP.update(ip, shortDuration);
        
        // 立即檢查應該在黑名單中
        assertFalse(Blacklist.check(ip), "IP 應該在黑名單中");
        
        // 等待過期
        Thread.sleep(200L);
        
        // 過期後應該不在黑名單中
        assertTrue(Blacklist.check(ip), "IP 應該已過期並從黑名單移除");
    }

    @Test
    void testListMethods() {
        // 測試各種 list 方法
        // 加入測試數據
        Blacklist.IP.update("192.168.1.10", 3600000L);
        Blacklist.IP.update("192.168.1.20", 3600000L);
        
        Blacklist.User.update("user1", null, null, 3600000L);
        Blacklist.User.update("user2", "web", "device1", 3600000L);
        
        Blacklist.IPRangeDeny.update("172.16.0.0/16", 3600000L);
        
        // 測試 IP.list()
        Set<Blacklist.IP> ipList = Blacklist.IP.list();
        assertNotNull(ipList);
        assertEquals(2, ipList.size(), "應該有 2 個 IP 在黑名單中");
        
        // 測試 User.list()
        Set<Blacklist.User> userList = Blacklist.User.list();
        assertNotNull(userList);
        assertEquals(2, userList.size(), "應該有 2 個使用者/裝置在黑名單中");
        
        // 測試 User.list(userId)
        Set<Blacklist.User> user1List = Blacklist.User.list("user1");
        assertNotNull(user1List);
        assertEquals(1, user1List.size(), "user1 應該有 1 個黑名單項目");
        
        // 測試 IPRangeDeny.list()
        Set<Blacklist.IPRangeDeny> ipRangeList = Blacklist.IPRangeDeny.list();
        assertNotNull(ipRangeList);
        assertEquals(1, ipRangeList.size(), "應該有 1 個 IP 範圍在黑名單中");
        
        // 清理
        Blacklist.IP.remove("192.168.1.10");
        Blacklist.IP.remove("192.168.1.20");
        Blacklist.User.remove("user1", null, null);
        Blacklist.User.remove("user2", "web", "device1");
        Blacklist.IPRangeDeny.remove("172.16.0.0/16");
    }
}
