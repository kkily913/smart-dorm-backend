package com.youlai.boot.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.system.mapper.BizStudentMapper;
import com.youlai.boot.system.model.entity.BizStudent;
import com.youlai.boot.system.service.IBizStudentService;
import org.springframework.stereotype.Service;

/**
 * 学生信息 Service 实现
 */
@Service
public class BizStudentServiceImpl
        extends ServiceImpl<BizStudentMapper, BizStudent>
        implements IBizStudentService {
}
