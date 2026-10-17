package com.youlai.boot.system.controller;

import com.youlai.boot.common.result.PageResult;
import com.youlai.boot.common.result.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.system.model.entity.BizStudent;
import com.youlai.boot.system.service.IBizStudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 学生管理
 */
@Tag(name = "学生管理")
@RestController
@RequestMapping("/api/v1/system/students")
@RequiredArgsConstructor

public class BizStudentController {

    private final IBizStudentService studentService;

    /**
     * 分页查询学生
     */
    @Operation(summary = "学生分页查询")
    @GetMapping
    @PreAuthorize("@ss.hasPerm('biz:student:list')")
    public PageResult<BizStudent> getStudentPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) String name) {

        LambdaQueryWrapper<BizStudent> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(studentNo != null && !studentNo.isBlank(),
                        BizStudent::getStudentNo, studentNo)
                .like(name != null && !name.isBlank(),
                        BizStudent::getName, name)
                .orderByDesc(BizStudent::getCreateTime);

        Page<BizStudent> page =
                studentService.page(new Page<>(pageNum, pageSize), wrapper);

        return PageResult.success(page);
    }

    /**
     * 新增学生
     */
    @Operation(summary = "新增学生")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('biz:student:create')")
    public Result<Void> addStudent(@RequestBody BizStudent student) {
        boolean result = studentService.save(student);
        return Result.judge(result);
    }

    /**
     * 修改学生
     */
    @Operation(summary = "修改学生")
    @PutMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('biz:student:update')")
    public Result<Void> updateStudent(
            @PathVariable Long id,
            @RequestBody BizStudent student) {

        student.setId(id);

        boolean result = studentService.updateById(student);
        return Result.judge(result);
    }

    /**
     * 删除学生
     */
    @Operation(summary = "删除学生")
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('biz:student:delete')")
    public Result<Void> deleteStudent(@PathVariable Long id) {
        boolean result = studentService.removeById(id);
        return Result.judge(result);
    }
}
