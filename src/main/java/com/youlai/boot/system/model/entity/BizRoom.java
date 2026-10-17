package com.youlai.boot.system.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 房间信息实体
 */
@Data
@TableName("biz_room")
public class BizRoom {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属楼宇ID
     */
    private Long buildingId;

    /**
     * 房间号
     */
    private String roomNumber;

    /**
     * 楼层
     */
    private Integer floorNum;

    /**
     * 床位总数
     */
    private Integer capacity;

    /**
     * 已入住人数
     */
    private Integer occupiedCount;

    /**
     * 房型
     * 1:四人间 2:六人间 3:二人间
     */
    private Integer roomType;

    /**
     * 状态
     * 0:维修中 1:正常
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer isDeleted;
}