package com.xduo.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xduo.springbootinit.model.entity.QuestionComment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 题目评论 Mapper
 */
@Mapper
public interface QuestionCommentMapper extends BaseMapper<QuestionComment> {
}
