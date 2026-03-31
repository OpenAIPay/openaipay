package cn.openaipay.domain.admin.model;

/**
 * 后台管理模块模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class AdminModule {

    /** 模块编码 */
    private final String moduleCode;
    /** 模块名称 */
    private final String moduleName;
    /** 模块描述 */
    private final String moduleDesc;
    /** 启用开关 */
    private final boolean enabled;
    /** 排序编号 */
    private final Integer sortNo;

    public AdminModule(String moduleCode,
                       String moduleName,
                       String moduleDesc,
                       boolean enabled,
                       Integer sortNo) {
        this.moduleCode = moduleCode;
        this.moduleName = moduleName;
        this.moduleDesc = moduleDesc;
        this.enabled = enabled;
        this.sortNo = sortNo;
    }

    /**
     * 获取模块编码。
     */
    public String getModuleCode() {
        return moduleCode;
    }

    /**
     * 获取模块信息。
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * 获取模块信息。
     */
    public String getModuleDesc() {
        return moduleDesc;
    }

    /**
     * 判断是否业务数据。
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 获取NO信息。
     */
    public Integer getSortNo() {
        return sortNo;
    }
}
