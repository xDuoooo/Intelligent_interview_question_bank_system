package com.xduo.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xduo.springbootinit.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 针对表【user(用户)】的数据库操作Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
