package com.youlai.boot.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youlai.boot.system.model.entity.BizStudent;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生信息 Mapper
 */
@Mapper
public interface BizStudentMapper extends BaseMapper<BizStudent> {
}