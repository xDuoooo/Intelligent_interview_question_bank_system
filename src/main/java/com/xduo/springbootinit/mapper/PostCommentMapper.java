package com.xduo.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xduo.springbootinit.model.entity.PostComment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 帖子评论 Mapper
 */
@Mapper
public interface PostCommentMapper extends BaseMapper<PostComment> {
}
