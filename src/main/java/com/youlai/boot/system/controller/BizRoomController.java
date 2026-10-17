package com.youlai.boot.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.common.result.Result;
import com.youlai.boot.system.model.entity.BizRoom;
import com.youlai.boot.system.service.IBizRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 房间管理控制器
 */
@Tag(name = "房间管理")
@RestController
@RequestMapping("/api/v1/system/rooms")
@RequiredArgsConstructor
public class BizRoomController {

    private final IBizRoomService bizRoomService;

    @Operation(summary = "分页查询房间列表")
    @GetMapping("/page")
    @PreAuthorize("@ss.hasPerm('biz:room:list')")
    public Result<Page<BizRoom>> getRoomPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) String roomNumber) {

        Page<BizRoom> page = new Page<>(pageNum, pageSize);

        return Result.success(
                bizRoomService.getPage(page, buildingId, roomNumber)
        );
    }

    @Operation(summary = "新增房间")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('biz:room:create')")
    public Result<Boolean> addRoom(@RequestBody BizRoom room) {
        return Result.success(bizRoomService.add(room));
    }

    @Operation(summary = "修改房间")
    @PutMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('biz:room:update')")
    public Result<Boolean> updateRoom(
            @PathVariable Long id,
            @RequestBody BizRoom room) {

        return Result.success(bizRoomService.update(id, room));
    }

    @Operation(summary = "删除房间")
    @DeleteMapping("/{ids}")
    @PreAuthorize("@ss.hasPerm('biz:room:delete')")
    public Result<Boolean> deleteRoom(@PathVariable Long[] ids) {
        return Result.success(bizRoomService.deleteByIds(ids));
    }
}
