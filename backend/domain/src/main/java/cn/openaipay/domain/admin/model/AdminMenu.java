package cn.openaipay.domain.admin.model;

/**
 * 后台管理菜单模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class AdminMenu {

    /** 菜单编码 */
    private final String menuCode;
    /** 父级编码 */
    private final String parentCode;
    /** 菜单名称 */
    private final String menuName;
    /** 路径 */
    private final String path;
    /** 图标 */
    private final String icon;
    /** 排序编号 */
    private final Integer sortNo;
    /** 可见 */
    private final boolean visible;

    public AdminMenu(String menuCode,
                     String parentCode,
                     String menuName,
                     String path,
                     String icon,
                     Integer sortNo,
                     boolean visible) {
        this.menuCode = menuCode;
        this.parentCode = parentCode;
        this.menuName = menuName;
        this.path = path;
        this.icon = icon;
        this.sortNo = sortNo;
        this.visible = visible;
    }

    /**
     * 获取菜单编码。
     */
    public String getMenuCode() {
        return menuCode;
    }

    /**
     * 获取编码。
     */
    public String getParentCode() {
        return parentCode;
    }

    /**
     * 获取菜单信息。
     */
    public String getMenuName() {
        return menuName;
    }

    /**
     * 获取业务数据。
     */
    public String getPath() {
        return path;
    }

    /**
     * 获取业务数据。
     */
    public String getIcon() {
        return icon;
    }

    /**
     * 获取NO信息。
     */
    public Integer getSortNo() {
        return sortNo;
    }

    /**
     * 判断是否业务数据。
     */
    public boolean isVisible() {
        return visible;
    }
}
