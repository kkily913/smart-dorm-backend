package com.youlai.boot.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.common.result.Result;
import com.youlai.boot.system.model.entity.BizBuilding;
import com.youlai.boot.system.service.IBizBuildingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 楼宇管理控制器
 */
@Tag(name = "楼宇管理")
@RestController
@RequestMapping("/api/v1/system/buildings")
@RequiredArgsConstructor
public class BizBuildingController {

    private final IBizBuildingService bizBuildingService;

    @Operation(summary = "分页查询楼宇列表")
    @GetMapping("/page")
    @PreAuthorize("@ss.hasPerm('biz:building:list')")
    public Result<Page<BizBuilding>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name) {
        Page<BizBuilding> page = new Page<>(pageNum, pageSize);
        return Result.success(bizBuildingService.getPage(page, name));
    }

    @Operation(summary = "新增楼宇")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('biz:building:create')")
    public Result<Boolean> add(@RequestBody BizBuilding building) {
        return Result.success(bizBuildingService.add(building));
    }

    @Operation(summary = "修改楼宇")
    @PutMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('biz:building:update')")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody BizBuilding building) {
        return Result.success(bizBuildingService.update(id, building));
    }

    @Operation(summary = "删除楼宇")
    @DeleteMapping("/{ids}")
    @PreAuthorize("@ss.hasPerm('biz:building:delete')")
    public Result<Boolean> delete(@PathVariable Long[] ids) {
        return Result.success(bizBuildingService.deleteByIds(ids));
    }
}
