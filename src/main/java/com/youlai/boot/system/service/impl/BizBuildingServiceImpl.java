package com.youlai.boot.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.system.mapper.BizBuildingMapper;
import com.youlai.boot.system.model.entity.BizBuilding;
import com.youlai.boot.system.service.IBizBuildingService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;

/**
 * 楼宇业务服务实现类
 */
@Service
public class BizBuildingServiceImpl extends ServiceImpl<BizBuildingMapper, BizBuilding> implements IBizBuildingService {

    @Override
    public Page<BizBuilding> getPage(Page<BizBuilding> page, String name) {
        // 构建查询条件：如果名字不为空，则模糊查询
        LambdaQueryWrapper<BizBuilding> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(name), BizBuilding::getName, name);

        // 执行分页查询
        return this.page(page, wrapper);
    }

    @Override
    public boolean add(BizBuilding building) {
        // 直接调用 MyBatis-Plus 的 save 方法
        return this.save(building);
    }

    @Override
    public boolean update(Long id, BizBuilding building) {
        // 设置ID，确保更新的是指定记录
        building.setId(id);
        // 直接调用 MyBatis-Plus 的 updateById 方法
        return this.updateById(building);
    }

    @Override
    public boolean deleteByIds(Long[] ids) {
        // 将数组转为List并批量删除
        return this.removeByIds(Arrays.asList(ids));
    }
}