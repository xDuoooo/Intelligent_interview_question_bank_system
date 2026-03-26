package com.xduo.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xduo.springbootinit.model.entity.QuestionCommentLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论点赞 Mapper
 */
@Mapper
public interface QuestionCommentLikeMapper extends BaseMapper<QuestionCommentLike> {
}
