package com.erahub.controller.system;

import com.alibaba.excel.EasyExcel;
import com.erahub.common.annotation.ControllerEndpoint;
import com.erahub.common.error.system.SystemException;
import com.erahub.common.excel.model.system.MenuExcel;
import com.erahub.common.excel.model.system.UserExcel;
import com.erahub.common.model.system.Menu;
import com.erahub.common.response.ResponseBean;
import com.erahub.common.utils.ListMapUtils;
import com.erahub.common.vo.system.MenuNodeVO;
import com.erahub.common.vo.system.MenuVO;
import com.erahub.system.service.MenuService;
import com.wuwenze.poi.ExcelKit;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author lipeng
 * @Date 2020/3/10 11:51
 * @Version 1.0
 **/
@Api(tags = "系统模块-菜单权限相关接口")
@RequestMapping("/system/menu")
@RestController
public class MenuController {

    @Autowired
    private MenuService menuService;

    /**
     * 加载菜单树
     *
     * @return
     */
    @ApiOperation(value = "加载菜单树", notes = "获取所有菜单树，以及展开项")
    @RequiresPermissions({"menu:select"})
    @GetMapping("/tree")
    public ResponseBean<Map<String, Object>> tree() {
        List<MenuNodeVO> menuTree = menuService.findMenuTree(false);
        List<Long> ids = menuService.findOpenIds();
        Map<String, Object> map = new HashMap<>();
        map.put("tree", menuTree);
        map.put("open", ids);
        return ResponseBean.success(map);
    }

    /**
     * 新增菜单/按钮
     *
     * @return
     */
    @ControllerEndpoint(exceptionMessage = "新增菜单/按钮失败", operation = "新增菜单/按钮")
    @ApiOperation(value = "新增菜单")
    @RequiresPermissions({"menu:add"})
    @PostMapping("/add")
    public ResponseBean<Map<String, Object>> add(@RequestBody @Validated MenuVO menuVO) throws SystemException {
        Menu node = menuService.add(menuVO);
        Map<String, Object> map = new HashMap<>();
        map.put("id", node.getId());
        map.put("menuName", node.getMenuName());
        map.put("children", new ArrayList<>());
        map.put("icon", node.getIcon());
        return ResponseBean.success(map);
    }

    /**
     * 删除菜单/按钮
     *
     * @param id
     * @return
     */
    @ControllerEndpoint(exceptionMessage = "删除菜单/按钮失败", operation = "删除菜单/按钮")
    @ApiOperation(value = "删除菜单", notes = "根据id删除菜单节点")
    @RequiresPermissions({"menu:delete"})
    @DeleteMapping("/delete/{id}")
    public ResponseBean delete(@PathVariable Long id) throws SystemException {
        menuService.delete(id);
        return ResponseBean.success();
    }

    /**
     * 菜单详情
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "菜单详情", notes = "根据id编辑菜单，获取菜单详情")
    @RequiresPermissions({"menu:edit"})
    @GetMapping("/edit/{id}")
    public ResponseBean<MenuVO> edit(@PathVariable Long id) throws SystemException {
        MenuVO menuVO = menuService.edit(id);
        return ResponseBean.success(menuVO);
    }

    /**
     * 更新菜单
     *
     * @param id
     * @param menuVO
     * @return
     */
    @ControllerEndpoint(exceptionMessage = "更新菜单失败", operation = "更新菜单")
    @ApiOperation(value = "更新菜单", notes = "根据id更新菜单节点")
    @RequiresPermissions({"menu:update"})
    @PutMapping("/update/{id}")
    public ResponseBean update(@PathVariable Long id, @RequestBody @Validated MenuVO menuVO) throws SystemException {
        menuService.update(id, menuVO);
        return ResponseBean.success();
    }

    /**
     * 导出excel
     * @param response
     */
    @ApiOperation(value = "导出excel", notes = "导出所有菜单的excel表格")
    @PostMapping("excel")
    @RequiresPermissions("menu:export")
    @ControllerEndpoint(exceptionMessage = "导出Excel失败",operation = "导出菜单excel")
    public void export(HttpServletResponse response) throws IOException {
        List<Menu> menus = this.menuService.findAll();
        List<MenuExcel> menuExcels = new ArrayList<>();
        ListMapUtils.copyList(menus,menuExcels,MenuExcel.class);

        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
//        String fileName = URLEncoder.encode("测试", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''菜单列表.xlsx");
        EasyExcel.write(response.getOutputStream(), MenuExcel.class).sheet("菜单列表").doWrite(menuExcels);
    }

}
