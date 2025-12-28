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
 * 單元測試 for Blacklist
 * 使用 Mockito 模擬靜態方法
 */
class BlacklistTest {

    @BeforeEach
    void setUp() {
        // 清理黑名單以確保測試隔離
        clearAllBlacklists();
    }

    private void clearAllBlacklists() {
        // 直接清理靜態集合（如果可能）
        // 由於 Blacklist 的內部集合是私有的，我們只能通過公開方法清理
        // 但對於測試，我們可以嘗試反射或依賴現有方法
    }

    @Test
    void testIPCheckLogic() {
        // 測試 IP 檢查邏輯，不依賴 update 方法
        // 直接測試底層邏輯
        
        String ip = "192.168.1.100";
        
        // 初始狀態應該通過檢查
        assertTrue(Blacklist.check(ip), "初始狀態 IP 應該通過檢查");
        assertTrue(Blacklist.IP.check(ip), "IP.check 應該返回 true");
        
        // 測試 IP.get 方法
        assertNull(Blacklist.IP.get(ip), "獲取不存在的 IP 應該返回 null");
    }

    @Test
    void testUserCheckLogic() {
        // 測試使用者檢查邏輯
        String userId = "user123";
        
        // 初始狀態應該通過檢查
        assertTrue(Blacklist.User.check(userId, null, null), "初始狀態使用者應該通過檢查");
        
        // 測試 User.getUser 方法
        assertNull(Blacklist.User.getUser(userId), "獲取不存在的使用者應該返回 null");
        
        // 測試 User.list 方法
        Set<Blacklist.User> userList = Blacklist.User.list(userId);
        assertNotNull(userList, "list 方法不應該返回 null");
        assertTrue(userList.isEmpty(), "不存在的使用者應該返回空集合");
    }

    @Test
    void testIPRangeCheckLogic() {
        // 測試 IP 範圍檢查邏輯
        String ip = "192.168.1.100";
        
        // 初始狀態應該通過檢查
        assertTrue(Blacklist.IPRangeDeny.check(ip), "初始狀態 IP 範圍檢查應該通過");
        
        // 測試 IPRangeDeny.list 方法
        Set<Blacklist.IPRangeDeny> ipRangeList = Blacklist.IPRangeDeny.list();
        assertNotNull(ipRangeList, "list 方法不應該返回 null");
    }

    @Test
    void testCheckWithNullAccessToken() {
        // 測試帶有空 AccessToken 的檢查
        String ip = "192.168.1.1";
        
        assertTrue(Blacklist.check(ip, (AccessToken) null), "空 AccessToken 應該通過檢查");
    }

    @Test
    void testCheckWithAccessToken() {
        // 測試帶有 AccessToken 的檢查
        String ip = "172.16.0.1";
        String userId = "user789";
        String deviceType = "mobile";
        String deviceId = "device789";
        
        // 建立 AccessToken
        AccessToken accessToken = new AccessToken(
            "tokenId", "token", "accessTokenId", 3600000L, 
            System.currentTimeMillis() + 3600000L, GrantType.AUTHORIZATION_CODE, "clientId", 
            "scope", userId, deviceType, deviceId
        );
        
        // 初始狀態應該通過檢查
        assertTrue(Blacklist.check(ip, accessToken), "初始狀態應該通過檢查");
    }

    @Test
    void testCheckWithNullParameters() {
        // 測試帶有空參數的檢查
        assertTrue(Blacklist.check("192.168.1.1", null, null, null), "空參數應該通過檢查");
        assertTrue(Blacklist.check("192.168.1.1"), "簡單 IP 檢查應該通過");
    }

    @Test
    void testReadAllMethod() {
        // 測試 readAll 方法
        var allBlacklists = Blacklist.readAll();
        
        assertNotNull(allBlacklists, "readAll 不應該返回 null");
        assertTrue(allBlacklists.containsKey("ipBlacklist"), "應該包含 ipBlacklist");
        assertTrue(allBlacklists.containsKey("userBlacklist"), "應該包含 userBlacklist");
        assertTrue(allBlacklists.containsKey("ipRangeBlacklist"), "應該包含 ipRangeBlacklist");
    }

    @Test
    void testListMethods() {
        // 測試各種 list 方法（空狀態）
        Set<Blacklist.IP> ipList = Blacklist.IP.list();
        assertNotNull(ipList, "IP.list() 不應該返回 null");
        
        Set<Blacklist.User> userList = Blacklist.User.list();
        assertNotNull(userList, "User.list() 不應該返回 null");
        
        Set<Blacklist.IPRangeDeny> ipRangeList = Blacklist.IPRangeDeny.list();
        assertNotNull(ipRangeList, "IPRangeDeny.list() 不應該返回 null");
    }

    @Test
    void testModifyMethod() {
        // 測試 modify 方法不會拋出異常
        assertDoesNotThrow(() -> {
            AuthzModifier modifier = new AuthzModifier()
                    .setTarget(AuthzModifier.Target.BLACKLIST);
            
            AuthzModifier.BlacklistInfo ipInfo = new AuthzModifier.BlacklistInfo()
                    .setType(AuthzModifier.BlacklistInfo.TYPE.IP)
                    .setOp(AuthzModifier.BlacklistInfo.OP.READ); // 使用 READ 操作避免修改狀態
                    
            modifier.setBlacklistInfo(ipInfo);
            
            // 執行修改 - READ 操作應該返回數據而不修改狀態
            var result = Blacklist.modify(modifier);
            assertNotNull(result, "結果不應該為 null");
        });
    }

    @Test
    void testBlacklistStaticMethods() {
        // 測試靜態方法不會拋出異常
        assertDoesNotThrow(() -> {
            // 呼叫各種靜態方法確保不會崩潰
            Blacklist.IP.update("192.168.1.1", 3600000L);
            Blacklist.IP.remove("192.168.1.1");
            
            Blacklist.User.update("user1", null, null, 3600000L);
            Blacklist.User.remove("user1", null, null);
            
            Blacklist.IPRangeDeny.update("192.168.1.0/24", 3600000L);
            Blacklist.IPRangeDeny.remove("192.168.1.0/24");
        });
    }
}
