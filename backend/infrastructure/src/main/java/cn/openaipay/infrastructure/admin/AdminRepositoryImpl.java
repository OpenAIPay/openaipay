package cn.openaipay.infrastructure.admin;

import cn.openaipay.domain.admin.model.AdminAccount;
import cn.openaipay.domain.admin.model.AdminModule;
import cn.openaipay.domain.admin.model.AdminMenu;
import cn.openaipay.domain.admin.model.AdminPermission;
import cn.openaipay.domain.admin.model.AdminRole;
import cn.openaipay.domain.admin.repository.AdminRepository;
import cn.openaipay.infrastructure.admin.dataobject.AdminAccountDO;
import cn.openaipay.infrastructure.admin.dataobject.AdminRbacAdminRoleDO;
import cn.openaipay.infrastructure.admin.dataobject.AdminRbacModuleDO;
import cn.openaipay.infrastructure.admin.dataobject.AdminRbacPermissionDO;
import cn.openaipay.infrastructure.admin.dataobject.AdminRbacRoleDO;
import cn.openaipay.infrastructure.admin.dataobject.AdminRbacRoleMenuDO;
import cn.openaipay.infrastructure.admin.dataobject.AdminRbacRolePermissionDO;
import cn.openaipay.infrastructure.admin.mapper.AdminAccountMapper;
import cn.openaipay.infrastructure.admin.mapper.AdminMenuMapper;
import cn.openaipay.infrastructure.admin.mapper.AdminRbacAdminRoleMapper;
import cn.openaipay.infrastructure.admin.mapper.AdminRbacModuleMapper;
import cn.openaipay.infrastructure.admin.mapper.AdminRbacPermissionMapper;
import cn.openaipay.infrastructure.admin.mapper.AdminRbacRoleMenuMapper;
import cn.openaipay.infrastructure.admin.mapper.AdminRbacRolePermissionMapper;
import cn.openaipay.infrastructure.admin.mapper.AdminRbacRoleMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * 后台管理仓储实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Repository
public class AdminRepositoryImpl implements AdminRepository {

    /** 超级管理角色常量 */
    private static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";

    /** AdminAccountMapper组件 */
    private final AdminAccountMapper adminAccountMapper;
    /** AdminMenuMapper组件 */
    private final AdminMenuMapper adminMenuMapper;
    /** AdminRbac模块Persistence组件 */
    private final AdminRbacModuleMapper adminRbacModuleMapper;
    /** AdminRbacPermissionMapper组件 */
    private final AdminRbacPermissionMapper adminRbacPermissionMapper;
    /** AdminRbacRoleMapper组件 */
    private final AdminRbacRoleMapper adminRbacRoleMapper;
    /** AdminRbacAdminRoleMapper组件 */
    private final AdminRbacAdminRoleMapper adminRbacAdminRoleMapper;
    /** AdminRbacRolePermissionMapper组件 */
    private final AdminRbacRolePermissionMapper adminRbacRolePermissionMapper;
    /** AdminRbacRoleMenuMapper组件 */
    private final AdminRbacRoleMenuMapper adminRbacRoleMenuMapper;

    public AdminRepositoryImpl(AdminAccountMapper adminAccountMapper,
                               AdminMenuMapper adminMenuMapper,
                               AdminRbacModuleMapper adminRbacModuleMapper,
                               AdminRbacPermissionMapper adminRbacPermissionMapper,
                               AdminRbacRoleMapper adminRbacRoleMapper,
                               AdminRbacAdminRoleMapper adminRbacAdminRoleMapper,
                               AdminRbacRolePermissionMapper adminRbacRolePermissionMapper,
                               AdminRbacRoleMenuMapper adminRbacRoleMenuMapper) {
        this.adminAccountMapper = adminAccountMapper;
        this.adminMenuMapper = adminMenuMapper;
        this.adminRbacModuleMapper = adminRbacModuleMapper;
        this.adminRbacPermissionMapper = adminRbacPermissionMapper;
        this.adminRbacRoleMapper = adminRbacRoleMapper;
        this.adminRbacAdminRoleMapper = adminRbacAdminRoleMapper;
        this.adminRbacRolePermissionMapper = adminRbacRolePermissionMapper;
        this.adminRbacRoleMenuMapper = adminRbacRoleMenuMapper;
    }

    /**
     * 按用户名查找记录。
     */
    @Override
    public Optional<AdminAccount> findByUsername(String username) {
        return adminAccountMapper.findByUsername(username).map(this::toDomainAccount);
    }

    /**
     * 按后台ID查找记录。
     */
    @Override
    public Optional<AdminAccount> findByAdminId(Long adminId) {
        return adminAccountMapper.findByAdminId(adminId).map(this::toDomainAccount);
    }

    /**
     * 查询全部后台账号。
     */
    @Override
    public List<AdminAccount> findAllAccounts() {
        return adminAccountMapper.findAllByOrderByAdminIdAsc().stream()
                .map(this::toDomainAccount)
                .toList();
    }

    /**
     * 保存账户信息。
     */
    @Override
    @Transactional
    public AdminAccount saveAccount(AdminAccount account) {
        AdminAccountDO entity = adminAccountMapper.findByAdminId(account.getAdminId())
                .orElse(new AdminAccountDO());
        fillAccountDO(entity, account);
        return toDomainAccount(adminAccountMapper.save(entity));
    }

    /**
     * 查找ALL菜单信息。
     */
    @Override
    public List<AdminMenu> findAllVisibleMenus() {
        return toDomainMenus(adminMenuMapper.findByVisibleTrueOrderBySortNoAscIdAsc());
    }

    /**
     * 按后台ID查找菜单信息。
     */
    @Override
    public List<AdminMenu> findVisibleMenusByAdminId(Long adminId) {
        List<String> roleCodes = findRoleCodesByAdminId(adminId);
        if (roleCodes.isEmpty()) {
            return List.of();
        }
        if (containsSuperAdmin(roleCodes)) {
            return toDomainMenus(adminMenuMapper.findByVisibleTrueOrderBySortNoAscIdAsc());
        }

        Set<String> menuCodes = adminRbacRoleMenuMapper.findByRoleCodeIn(roleCodes)
                .stream()
                .map(AdminRbacRoleMenuDO::getMenuCode)
                .filter(this::hasText)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
        if (menuCodes.isEmpty()) {
            return List.of();
        }

        return toDomainMenus(adminMenuMapper.findByMenuCodeInAndVisibleTrueOrderBySortNoAscIdAsc(menuCodes));
    }

    /**
     * 按后台ID查找角色编码。
     */
    @Override
    public List<String> findRoleCodesByAdminId(Long adminId) {
        if (adminId == null || adminId <= 0) {
            return List.of();
        }

        Set<String> activeRoleCodes = adminRbacRoleMapper.findByRoleStatusIgnoreCaseOrderByRoleCodeAsc("ACTIVE")
                .stream()
                .map(AdminRbacRoleDO::getRoleCode)
                .filter(this::hasText)
                .map(code -> code.toUpperCase(Locale.ROOT))
                .collect(LinkedHashSet::new, Set::add, Set::addAll);

        return adminRbacAdminRoleMapper.findByAdminIdOrderByRoleCodeAsc(adminId)
                .stream()
                .map(AdminRbacAdminRoleDO::getRoleCode)
                .filter(this::hasText)
                .map(code -> code.toUpperCase(Locale.ROOT))
                .filter(activeRoleCodes::contains)
                .distinct()
                .toList();
    }

    /**
     * 按后台ID查找权限编码。
     */
    @Override
    public List<String> findPermissionCodesByAdminId(Long adminId) {
        List<String> roleCodes = findRoleCodesByAdminId(adminId);
        if (roleCodes.isEmpty()) {
            return List.of();
        }
        if (containsSuperAdmin(roleCodes)) {
            return adminRbacPermissionMapper.findAllByOrderByModuleCodeAscPermissionCodeAsc()
                    .stream()
                    .map(AdminRbacPermissionDO::getPermissionCode)
                    .filter(this::hasText)
                    .toList();
        }

        Set<String> permissionCodes = adminRbacRolePermissionMapper.findByRoleCodeIn(roleCodes)
                .stream()
                .map(AdminRbacRolePermissionDO::getPermissionCode)
                .filter(this::hasText)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
        if (permissionCodes.isEmpty()) {
            return List.of();
        }

        return adminRbacPermissionMapper
                .findByPermissionCodeInOrderByModuleCodeAscPermissionCodeAsc(new ArrayList<>(permissionCodes))
                .stream()
                .map(AdminRbacPermissionDO::getPermissionCode)
                .filter(this::hasText)
                .toList();
    }

    /**
     * 查找模块信息。
     */
    @Override
    public List<AdminModule> findModules() {
        return adminRbacModuleMapper.findAllByOrderBySortNoAscIdAsc()
                .stream()
                .map(entity -> new AdminModule(
                        entity.getModuleCode(),
                        entity.getModuleName(),
                        entity.getModuleDesc(),
                        Boolean.TRUE.equals(entity.getEnabled()),
                        entity.getSortNo()
                ))
                .toList();
    }

    /**
     * 查找角色信息。
     */
    @Override
    public List<AdminRole> findRoles() {
        return adminRbacRoleMapper.findAllByOrderByRoleCodeAsc()
                .stream()
                .map(entity -> new AdminRole(
                        entity.getRoleCode(),
                        entity.getRoleName(),
                        entity.getRoleScope(),
                        entity.getRoleStatus(),
                        Boolean.TRUE.equals(entity.getBuiltin()),
                        entity.getRoleDesc()
                ))
                .toList();
    }

    /**
     * 查找权限信息。
     */
    @Override
    public List<AdminPermission> findPermissions(String moduleCode) {
        String normalizedModuleCode = normalize(moduleCode);
        List<AdminRbacPermissionDO> permissions = normalizedModuleCode == null
                ? adminRbacPermissionMapper.findAllByOrderByModuleCodeAscPermissionCodeAsc()
                : adminRbacPermissionMapper.findByModuleCodeOrderByPermissionCodeAsc(normalizedModuleCode);

        return permissions.stream()
                .map(entity -> new AdminPermission(
                        entity.getPermissionCode(),
                        entity.getPermissionName(),
                        entity.getModuleCode(),
                        entity.getResourceType(),
                        entity.getHttpMethod(),
                        entity.getPathPattern(),
                        entity.getPermissionDesc()
                ))
                .toList();
    }

    /**
     * 按角色编码查找菜单编码。
     */
    @Override
    public List<String> findMenuCodesByRoleCode(String roleCode) {
        String normalizedRoleCode = normalizeRoleCode(roleCode);

        return adminRbacRoleMenuMapper.findByRoleCodeOrderByMenuCodeAsc(normalizedRoleCode)
                .stream()
                .map(AdminRbacRoleMenuDO::getMenuCode)
                .filter(this::hasText)
                .toList();
    }

    /**
     * 按角色编码查找权限编码。
     */
    @Override
    public List<String> findPermissionCodesByRoleCode(String roleCode) {
        String normalizedRoleCode = normalizeRoleCode(roleCode);

        return adminRbacRolePermissionMapper.findByRoleCodeOrderByPermissionCodeAsc(normalizedRoleCode)
                .stream()
                .map(AdminRbacRolePermissionDO::getPermissionCode)
                .filter(this::hasText)
                .toList();
    }

    /**
     * 处理后台角色信息。
     */
    @Override
    @Transactional
    public void replaceAdminRoles(Long adminId, List<String> roleCodes, String operator) {
        if (adminId == null || adminId <= 0) {
            throw new IllegalArgumentException("adminId must be greater than 0");
        }

        adminRbacAdminRoleMapper.deleteByAdminId(adminId);
        List<String> normalizedRoleCodes = normalizeAndDistinct(roleCodes, true);
        if (normalizedRoleCodes.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        String createdBy = defaultOperator(operator);
        List<AdminRbacAdminRoleDO> entities = normalizedRoleCodes.stream()
                .map(roleCode -> {
                    AdminRbacAdminRoleDO entity = new AdminRbacAdminRoleDO();
                    entity.setAdminId(adminId);
                    entity.setRoleCode(roleCode);
                    entity.setCreatedBy(createdBy);
                    entity.setCreatedAt(now);
                    return entity;
                })
                .toList();
        adminRbacAdminRoleMapper.saveAll(entities);
    }

    /**
     * 处理角色权限信息。
     */
    @Override
    @Transactional
    public void replaceRolePermissions(String roleCode, List<String> permissionCodes, String operator) {
        String normalizedRoleCode = normalizeRoleCode(roleCode);
        adminRbacRolePermissionMapper.deleteByRoleCode(normalizedRoleCode);

        List<String> normalizedPermissionCodes = normalizeAndDistinct(permissionCodes, false);
        if (normalizedPermissionCodes.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        String createdBy = defaultOperator(operator);
        List<AdminRbacRolePermissionDO> entities = normalizedPermissionCodes.stream()
                .map(permissionCode -> {
                    AdminRbacRolePermissionDO entity = new AdminRbacRolePermissionDO();
                    entity.setRoleCode(normalizedRoleCode);
                    entity.setPermissionCode(permissionCode);
                    entity.setCreatedBy(createdBy);
                    entity.setCreatedAt(now);
                    return entity;
                })
                .toList();
        adminRbacRolePermissionMapper.saveAll(entities);
    }

    /**
     * 处理角色菜单信息。
     */
    @Override
    @Transactional
    public void replaceRoleMenus(String roleCode, List<String> menuCodes, String operator) {
        String normalizedRoleCode = normalizeRoleCode(roleCode);
        adminRbacRoleMenuMapper.deleteByRoleCode(normalizedRoleCode);

        List<String> normalizedMenuCodes = normalizeAndDistinct(menuCodes, false);
        if (normalizedMenuCodes.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        String createdBy = defaultOperator(operator);
        List<AdminRbacRoleMenuDO> entities = normalizedMenuCodes.stream()
                .map(menuCode -> {
                    AdminRbacRoleMenuDO entity = new AdminRbacRoleMenuDO();
                    entity.setRoleCode(normalizedRoleCode);
                    entity.setMenuCode(menuCode);
                    entity.setCreatedBy(createdBy);
                    entity.setCreatedAt(now);
                    return entity;
                })
                .toList();
        adminRbacRoleMenuMapper.saveAll(entities);
    }

    private List<AdminMenu> toDomainMenus(List<cn.openaipay.infrastructure.admin.dataobject.AdminMenuDO> entities) {
        return entities.stream()
                .map(entity -> new AdminMenu(
                        entity.getMenuCode(),
                        entity.getParentCode(),
                        entity.getMenuName(),
                        entity.getPath(),
                        entity.getIcon(),
                        entity.getSortNo(),
                        Boolean.TRUE.equals(entity.getVisible())
                ))
                .toList();
    }

    private AdminAccount toDomainAccount(AdminAccountDO entity) {
        return new AdminAccount(
                entity.getAdminId(),
                entity.getUsername(),
                entity.getDisplayName(),
                entity.getPasswordSha256(),
                entity.getAccountStatus(),
                entity.getLastLoginAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private void fillAccountDO(AdminAccountDO entity, AdminAccount account) {
        LocalDateTime now = LocalDateTime.now();
        entity.setAdminId(account.getAdminId());
        entity.setUsername(account.getUsername());
        entity.setDisplayName(account.getDisplayName());
        entity.setPasswordSha256(account.getPasswordSha256());
        entity.setAccountStatus(account.getAccountStatus());
        entity.setLastLoginAt(account.getLastLoginAt());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(account.getCreatedAt() == null ? now : account.getCreatedAt());
        }
        entity.setUpdatedAt(account.getUpdatedAt() == null ? now : account.getUpdatedAt());
    }

    private boolean containsSuperAdmin(List<String> roleCodes) {
        return roleCodes.stream().anyMatch(code -> SUPER_ADMIN_ROLE.equalsIgnoreCase(code));
    }

    private List<String> normalizeAndDistinct(List<String> values, boolean uppercase) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            String text = normalize(value);
            if (text == null) {
                continue;
            }
            normalized.add(uppercase ? text.toUpperCase(Locale.ROOT) : text);
        }
        return new ArrayList<>(normalized);
    }

    private String normalizeRoleCode(String roleCode) {
        String normalized = normalize(roleCode);
        if (normalized == null) {
            throw new IllegalArgumentException("roleCode must not be blank");
        }
        return normalized.toUpperCase(Locale.ROOT);
    }

    private String defaultOperator(String operator) {
        String normalized = normalize(operator);
        return normalized == null ? "system" : normalized;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
