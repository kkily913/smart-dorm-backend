package com.youlai.boot.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youlai.boot.system.model.entity.BizBuilding;
import org.apache.ibatis.annotations.Mapper;

/**
 * 楼宇管理 Mapper 接口
 * <p>
 * 继承 MyBatis-Plus 的 BaseMapper，自动获得 CRUD 能力
 *
 * @author your_name
 */
@Mapper
public interface BizBuildingMapper extends BaseMapper<BizBuilding> {

    // 简单的增删改查 MyBatis-Plus 已经帮你实现了
    // 如果以后有复杂的 SQL（比如多表联查），可以在这里定义方法
}