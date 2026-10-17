package com.youlai.boot.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.youlai.boot.system.model.entity.BizRoom;

/**
 * 房间业务服务接口
 */
public interface IBizRoomService extends IService<BizRoom> {

    /**
     * 分页查询房间
     */
    Page<BizRoom> getPage(Page<BizRoom> page, Long buildingId, String roomNumber);

    /**
     * 新增房间
     */
    boolean add(BizRoom room);

    /**
     * 修改房间
     */
    boolean update(Long id, BizRoom room);

    /**
     * 删除房间
     */
    boolean deleteByIds(Long[] ids);
}
