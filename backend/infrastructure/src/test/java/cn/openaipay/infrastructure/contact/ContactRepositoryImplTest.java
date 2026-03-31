package cn.openaipay.infrastructure.contact;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import cn.openaipay.domain.contact.model.ContactSearchProfile;
import cn.openaipay.infrastructure.contact.dataobject.ContactFriendshipDO;
import cn.openaipay.infrastructure.contact.mapper.ContactBlacklistMapper;
import cn.openaipay.infrastructure.contact.mapper.ContactFriendshipMapper;
import cn.openaipay.infrastructure.contact.mapper.ContactRequestMapper;
import cn.openaipay.infrastructure.user.dataobject.UserAccountDO;
import cn.openaipay.infrastructure.user.dataobject.UserProfileDO;
import cn.openaipay.infrastructure.user.mapper.UserAccountMapper;
import cn.openaipay.infrastructure.user.mapper.UserProfileMapper;
import cn.openaipay.infrastructure.user.mapper.UserPrivacySettingMapper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ContactRepositoryImplTest 业务模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/16
 */
@ExtendWith(MockitoExtension.class)
class ContactRepositoryImplTest {

    /** 所属用户ID */
    private static final Long OWNER_USER_ID = 880100068483692100L;

    /** 联系人请求信息 */
    @Mock
    private ContactRequestMapper contactRequestMapper;
    /** 联系人信息 */
    @Mock
    private ContactFriendshipMapper contactFriendshipMapper;
    /** 联系人信息 */
    @Mock
    private ContactBlacklistMapper contactBlacklistMapper;
    /** 用户信息 */
    @Mock
    private UserAccountMapper userAccountMapper;
    /** 用户资料信息 */
    @Mock
    private UserProfileMapper userProfileMapper;
    /** 用户配置信息 */
    @Mock
    private UserPrivacySettingMapper userPrivacySettingMapper;

    /** 仓储信息 */
    private ContactRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new ContactRepositoryImpl(
                contactRequestMapper,
                contactFriendshipMapper,
                contactBlacklistMapper,
                userAccountMapper,
                userProfileMapper,
                userPrivacySettingMapper
        );
        when(userPrivacySettingMapper.selectList(any())).thenReturn(List.of());
    }

    @Test
    void searchProfilesShouldSupportRemarkNicknameRealNameAndMobileWithinFriendships() {
        mockFriendships(
                friendship(880100068483692101L, "小顾"),
                friendship(880100068483692102L, "同事"),
                friendship(880100068483692103L, null)
        );
        mockAccounts(
                account(880100068483692101L, "20880001"),
                account(880100068483692102L, "20880002"),
                account(880100068483692103L, "20880003")
        );
        mockProfiles(
                profile(880100068483692101L, "顾郡", "13811112222", "顾*"),
                profile(880100068483692102L, "祁欣", "13833334444", "王*然"),
                profile(880100068483692103L, "林泽楷", "13800138000", "林*楷")
        );

        List<ContactSearchProfile> byRemark = repository.searchProfiles(OWNER_USER_ID, "小顾", 10);
        List<ContactSearchProfile> byRealName = repository.searchProfiles(OWNER_USER_ID, "王*然", 10);
        List<ContactSearchProfile> byMobile = repository.searchProfiles(OWNER_USER_ID, "13800138000", 10);

        assertEquals(List.of(880100068483692101L), byRemark.stream().map(ContactSearchProfile::userId).toList());
        assertEquals("顾*", byRemark.get(0).maskedRealName());
        assertEquals(List.of(880100068483692102L), byRealName.stream().map(ContactSearchProfile::userId).toList());
        assertEquals(List.of(880100068483692103L), byMobile.stream().map(ContactSearchProfile::userId).toList());
    }

    @Test
    void searchProfilesShouldPreferExactRemarkMatchBeforePartialNicknameMatch() {
        mockFriendships(
                friendship(880100068483692101L, "小顾"),
                friendship(880100068483692102L, null)
        );
        mockAccounts(
                account(880100068483692101L, "20880001"),
                account(880100068483692102L, "20880002")
        );
        mockProfiles(
                profile(880100068483692101L, "顾郡", "13811112222", "顾*"),
                profile(880100068483692102L, "小顾同学", "13833334444", "王*然")
        );

        List<ContactSearchProfile> results = repository.searchProfiles(OWNER_USER_ID, "小顾", 10);

        assertEquals(
                List.of(880100068483692101L, 880100068483692102L),
                results.stream().map(ContactSearchProfile::userId).toList()
        );
    }

    private void mockFriendships(ContactFriendshipDO... friendships) {
        List<ContactFriendshipDO> rows = List.of(friendships);
        when(contactFriendshipMapper.findAllByOwnerUserId(OWNER_USER_ID)).thenReturn(rows);
    }

    private void mockAccounts(UserAccountDO... accounts) {
        List<UserAccountDO> rows = List.of(accounts);
        when(userAccountMapper.selectList(any())).thenReturn(rows);
        when(userAccountMapper.findByUserIds(anyList())).thenAnswer(invocation -> {
            List<Long> userIds = invocation.getArgument(0);
            return rows.stream().filter(account -> userIds.contains(account.getUserId())).toList();
        });
    }

    private void mockProfiles(UserProfileDO... profiles) {
        List<UserProfileDO> rows = List.of(profiles);
        when(userProfileMapper.selectList(any())).thenReturn(rows);
        when(userProfileMapper.findByUserIds(anyList())).thenAnswer(invocation -> {
            List<Long> userIds = invocation.getArgument(0);
            return rows.stream().filter(profile -> userIds.contains(profile.getUserId())).toList();
        });
    }

    private ContactFriendshipDO friendship(Long friendUserId, String remark) {
        ContactFriendshipDO entity = new ContactFriendshipDO();
        entity.setOwnerUserId(OWNER_USER_ID);
        entity.setFriendUserId(friendUserId);
        entity.setRemark(remark);
        return entity;
    }

    private UserAccountDO account(Long userId, String aipayUid) {
        UserAccountDO entity = new UserAccountDO();
        entity.setUserId(userId);
        entity.setAipayUid(aipayUid);
        entity.setLoginId("login-" + userId);
        return entity;
    }

    private UserProfileDO profile(Long userId, String nickname, String mobile, String maskedRealName) {
        UserProfileDO entity = new UserProfileDO();
        entity.setUserId(userId);
        entity.setNickname(nickname);
        entity.setMobile(mobile);
        entity.setMaskedRealName(maskedRealName);
        entity.setAvatarUrl("/api/media/" + userId + ".png");
        return entity;
    }
}
