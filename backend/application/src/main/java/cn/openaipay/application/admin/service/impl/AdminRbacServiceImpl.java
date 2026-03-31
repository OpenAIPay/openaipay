package cn.openaipay.application.admin.service.impl;

import cn.openaipay.application.admin.command.AssignAdminRolesCommand;
import cn.openaipay.application.admin.command.AssignRoleMenusCommand;
import cn.openaipay.application.admin.command.AssignRolePermissionsCommand;
import cn.openaipay.application.admin.dto.AdminAccountSummaryDTO;
import cn.openaipay.application.admin.dto.AdminAuthorizationDTO;
import cn.openaipay.application.admin.dto.AdminMenuDTO;
import cn.openaipay.application.admin.dto.AdminModuleDTO;
import cn.openaipay.application.admin.dto.AdminPermissionDTO;
import cn.openaipay.application.admin.dto.AdminRoleDTO;
import cn.openaipay.application.admin.service.AdminRbacService;
import cn.openaipay.domain.admin.repository.AdminRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * 后台管理RBAC应用服务实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class AdminRbacServiceImpl implements AdminRbacService {

    /** 超级管理角色常量 */
    private static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";

    /** AdminRepository组件 */
    private final AdminRepository adminRepository;

    public AdminRbacServiceImpl(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    /**
     * 查询管理员列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<AdminAccountSummaryDTO> listAdmins() {
        return adminRepository.findAllAccounts().stream()
                .map(account -> new AdminAccountSummaryDTO(
                        String.valueOf(account.getAdminId()),
                        account.getUsername(),
                        account.getDisplayName(),
                        account.getAccountStatus(),
                        account.getLastLoginAt(),
                        adminRepository.findRoleCodesByAdminId(account.getAdminId())
                ))
                .toList();
    }

    /**
     * 查询菜单列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<AdminMenuDTO> listMenus() {
        return adminRepository.findAllVisibleMenus().stream()
                .map(menu -> new AdminMenuDTO(
                        menu.getMenuCode(),
                        menu.getParentCode(),
                        menu.getMenuName(),
                        menu.getPath(),
                        menu.getIcon(),
                        menu.getSortNo()
                ))
                .toList();
    }

    /**
     * 查询模块信息列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<AdminModuleDTO> listModules() {
        return adminRepository.findModules()
                .stream()
                .map(module -> new AdminModuleDTO(
                        module.getModuleCode(),
                        module.getModuleName(),
                        module.getModuleDesc(),
                        module.isEnabled(),
                        module.getSortNo()
                ))
                .toList();
    }

    /**
     * 查询角色信息列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<AdminRoleDTO> listRoles() {
        return adminRepository.findRoles()
                .stream()
                .map(role -> new AdminRoleDTO(
                        role.getRoleCode(),
                        role.getRoleName(),
                        role.getRoleScope(),
                        role.getRoleStatus(),
                        role.isBuiltin(),
                        role.getRoleDesc(),
                        adminRepository.findPermissionCodesByRoleCode(role.getRoleCode()),
                        adminRepository.findMenuCodesByRoleCode(role.getRoleCode())
                ))
                .toList();
    }

    /**
     * 查询权限信息列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<AdminPermissionDTO> listPermissions(String moduleCode) {
        return adminRepository.findPermissions(moduleCode)
                .stream()
                .map(permission -> new AdminPermissionDTO(
                        permission.getPermissionCode(),
                        permission.getPermissionName(),
                        permission.getModuleCode(),
                        permission.getResourceType(),
                        permission.getHttpMethod(),
                        permission.getPathPattern(),
                        permission.getPermissionDesc()
                ))
                .toList();
    }

    /**
     * 获取后台信息。
     */
    @Override
    @Transactional(readOnly = true)
    public AdminAuthorizationDTO getAdminAuthorization(Long adminId) {
        Long id = requirePositive(adminId, "adminId");
        adminRepository.findByAdminId(id)
                .orElseThrow(() -> new NoSuchElementException("admin account not found: " + id));

        return new AdminAuthorizationDTO(
                String.valueOf(id),
                adminRepository.findRoleCodesByAdminId(id),
                adminRepository.findPermissionCodesByAdminId(id),
                adminRepository.findVisibleMenusByAdminId(id)
                        .stream()
                        .map(menu -> menu.getMenuCode())
                        .toList()
        );
    }

    /**
     * 处理后台角色信息。
     */
    @Override
    @Transactional
    public void assignAdminRoles(AssignAdminRolesCommand command) {
        Long adminId = requirePositive(command.adminId(), "adminId");
        adminRepository.findByAdminId(adminId)
                .orElseThrow(() -> new NoSuchElementException("admin account not found: " + adminId));

        List<String> roleCodes = normalizeAndDistinct(command.roleCodes(), true);
        validateRoleCodes(roleCodes);
        adminRepository.replaceAdminRoles(adminId, roleCodes, normalizeOperator(command.operator()));
    }

    /**
     * 处理角色权限信息。
     */
    @Override
    @Transactional
    public void assignRolePermissions(AssignRolePermissionsCommand command) {
        String roleCode = normalizeRoleCode(command.roleCode());
        validateRoleCodes(List.of(roleCode));

        List<String> permissionCodes = normalizeAndDistinct(command.permissionCodes(), false);
        validatePermissionCodes(permissionCodes);
        adminRepository.replaceRolePermissions(roleCode, permissionCodes, normalizeOperator(command.operator()));
    }

    /**
     * 处理角色菜单信息。
     */
    @Override
    @Transactional
    public void assignRoleMenus(AssignRoleMenusCommand command) {
        String roleCode = normalizeRoleCode(command.roleCode());
        validateRoleCodes(List.of(roleCode));

        List<String> menuCodes = normalizeAndDistinct(command.menuCodes(), false);
        validateMenuCodes(menuCodes);
        adminRepository.replaceRoleMenus(roleCode, menuCodes, normalizeOperator(command.operator()));
    }

    /**
     * 判断是否存在权限信息。
     */
    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(Long adminId, String permissionCode) {
        if (adminId == null || adminId <= 0) {
            return false;
        }

        String normalizedPermissionCode = normalize(permissionCode);
        if (normalizedPermissionCode == null) {
            return false;
        }

        List<String> roleCodes = adminRepository.findRoleCodesByAdminId(adminId);
        if (roleCodes.stream().anyMatch(code -> SUPER_ADMIN_ROLE.equalsIgnoreCase(code))) {
            return true;
        }

        return adminRepository.findPermissionCodesByAdminId(adminId)
                .stream()
                .anyMatch(code -> code.equalsIgnoreCase(normalizedPermissionCode));
    }

    private void validateRoleCodes(List<String> roleCodes) {
        Set<String> existingRoleCodes = adminRepository.findRoles()
                .stream()
                .map(role -> role.getRoleCode().toUpperCase(Locale.ROOT))
                .collect(LinkedHashSet::new, Set::add, Set::addAll);

        List<String> unknownCodes = roleCodes.stream()
                .filter(code -> !existingRoleCodes.contains(code))
                .toList();
        if (!unknownCodes.isEmpty()) {
            throw new IllegalArgumentException("unknown role codes: " + String.join(",", unknownCodes));
        }
    }

    private void validatePermissionCodes(List<String> permissionCodes) {
        Set<String> existingPermissionCodes = adminRepository.findPermissions(null)
                .stream()
                .map(permission -> permission.getPermissionCode())
                .collect(LinkedHashSet::new, Set::add, Set::addAll);

        List<String> unknownCodes = permissionCodes.stream()
                .filter(code -> !existingPermissionCodes.contains(code))
                .toList();
        if (!unknownCodes.isEmpty()) {
            throw new IllegalArgumentException("unknown permission codes: " + String.join(",", unknownCodes));
        }
    }

    private void validateMenuCodes(List<String> menuCodes) {
        Set<String> existingMenuCodes = adminRepository.findAllVisibleMenus()
                .stream()
                .map(menu -> menu.getMenuCode())
                .collect(LinkedHashSet::new, Set::add, Set::addAll);

        List<String> unknownCodes = menuCodes.stream()
                .filter(code -> !existingMenuCodes.contains(code))
                .toList();
        if (!unknownCodes.isEmpty()) {
            throw new IllegalArgumentException("unknown menu codes: " + String.join(",", unknownCodes));
        }
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

    private Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }

    private String normalizeOperator(String operator) {
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
}
