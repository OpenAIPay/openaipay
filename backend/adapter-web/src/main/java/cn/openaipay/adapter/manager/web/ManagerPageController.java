package cn.openaipay.adapter.manager.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理后台前端页面控制器
 *
 * 业务场景：运营与风控同学通过浏览器访问 /manager 时，后端返回 manager 模块首页。
 * 该页面中的所有数据请求均同源直连 /api/admin/** 控制器，不经过 BFF 转发。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Controller
@RequestMapping("/manager")
public class ManagerPageController {

    /**
     * 管理后台首页入口
     *
     * @return 转发到 manager 静态首页文件
     */
    @GetMapping({"", "/", "/login"})
    public String index() {
        return "forward:/manager/index.html";
    }

    /**
     * 管理后台 SPA 子路由入口（路径式路由）。
     *
     * @return 转发到 manager 静态首页文件
     */
    @GetMapping({
            "/{first:[a-zA-Z0-9\\-]+}",
            "/{first:[a-zA-Z0-9\\-]+}/{second:[a-zA-Z0-9\\-]+}",
            "/{first:[a-zA-Z0-9\\-]+}/{second:[a-zA-Z0-9\\-]+}/{third:[a-zA-Z0-9\\-]+}"
    })
    public String spaRoute() {
        return "forward:/manager/index.html";
    }
}
