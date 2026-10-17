package com.youlai.boot.system.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

    /**
     * 楼宇信息实体类
     *
     * @author youlai
     * @since 2026-08-21
     */
    @Data
    @TableName("biz_building")
    @Schema(description = "楼宇信息对象")
    public class BizBuilding {

        @Schema(description = "主键ID")
        @TableId(value = "id", type = IdType.AUTO)
        private Long id;

        @Schema(description = "楼宇名称")
        private String name;

        @Schema(description = "楼宇编码")
        private String code;

        @Schema(description = "性别限制(0:不限 1:男 2:女)")
        private Integer genderLimit;

        @Schema(description = "总楼层数")
        private Integer floorCount;

        @Schema(description = "负责宿管ID")
        private Long managerId;

        @Schema(description = "状态(0:停用 1:正常)")
        private Integer status;

        @Schema(description = "备注")
        private String remark;

        @Schema(description = "创建时间")
        private LocalDateTime createTime;

        @Schema(description = "更新时间")
        private LocalDateTime updateTime;
    }

