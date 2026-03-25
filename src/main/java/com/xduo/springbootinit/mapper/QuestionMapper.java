package com.xduo.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xduo.springbootinit.model.entity.Question;
import org.apache.ibatis.annotations.Mapper;

/**
 * 针对表【question(题目)】的数据库操作Mapper
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

}
