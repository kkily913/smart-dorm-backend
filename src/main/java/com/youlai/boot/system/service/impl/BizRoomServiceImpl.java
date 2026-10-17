package com.youlai.boot.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.system.mapper.BizRoomMapper;
import com.youlai.boot.system.model.entity.BizRoom;
import com.youlai.boot.system.service.IBizRoomService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 房间业务服务实现类
 */
@Service
public class BizRoomServiceImpl
        extends ServiceImpl<BizRoomMapper, BizRoom>
        implements IBizRoomService {

    @Override
    public Page<BizRoom> getPage(
            Page<BizRoom> page,
            Long buildingId,
            String roomNumber) {

        // 构建查询条件
        LambdaQueryWrapper<BizRoom> wrapper = new LambdaQueryWrapper<>();

        // 根据楼宇ID查询
        wrapper.eq(buildingId != null, BizRoom::getBuildingId, buildingId);

        // 根据房间号模糊查询
        wrapper.like(
                StringUtils.hasText(roomNumber),
                BizRoom::getRoomNumber,
                roomNumber
        );

        // 执行分页查询
        return this.page(page, wrapper);
    }

    @Override
    public boolean add(BizRoom room) {
        return this.save(room);
    }

    @Override
    public boolean update(Long id, BizRoom room) {
        room.setId(id);
        return this.updateById(room);
    }

    @Override
    public boolean deleteByIds(Long[] ids) {
        return this.removeByIds(java.util.Arrays.asList(ids));
    }
}
