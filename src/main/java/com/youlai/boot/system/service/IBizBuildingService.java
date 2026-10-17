package com.youlai.boot.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.youlai.boot.system.model.entity.BizBuilding;

/**
 * 楼宇业务服务接口
 */
public interface IBizBuildingService extends IService<BizBuilding> {

    /**
     * 分页查询楼宇
     */
    Page<BizBuilding> getPage(Page<BizBuilding> page, String name);

    /**
     * 新增楼宇
     * @return 是否成功
     */
    boolean add(BizBuilding building);

    /**
     * 修改楼宇
     * @param id 楼宇ID
     * @param building 楼宇信息
     * @return 是否成功
     */
    boolean update(Long id, BizBuilding building);

    /**
     * 删除楼宇
     * @param ids ID列表
     * @return 是否成功
     */
    boolean deleteByIds(Long[] ids);
}