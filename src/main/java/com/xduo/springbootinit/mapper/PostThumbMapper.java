package com.xduo.springbootinit.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xduo.springbootinit.model.entity.Post;
import com.xduo.springbootinit.model.entity.PostThumb;
import org.apache.ibatis.annotations.Param;

/**
 * 帖子点赞数据库操作
 */
public interface PostThumbMapper extends BaseMapper<PostThumb> {

    /**
     * 分页查询点赞帖子列表
     *
     * @param page
     * @param queryWrapper
     * @param thumbUserId
     * @return
     */
    Page<Post> listThumbPostByPage(IPage<Post> page, @Param(Constants.WRAPPER) Wrapper<Post> queryWrapper,
            long thumbUserId);
}
