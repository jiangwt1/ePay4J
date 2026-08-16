package com.mavis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mavis.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {
}
