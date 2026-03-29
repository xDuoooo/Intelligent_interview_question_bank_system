package com.xduo.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xduo.springbootinit.model.dto.post.PostQueryRequest;
import com.xduo.springbootinit.model.entity.Post;
import com.xduo.springbootinit.model.vo.PostVO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 帖子服务

 */
public interface PostService extends IService<Post> {

    /**
     * 校验
     *
     * @param post
     * @param add
     */
    void validPost(Post post, boolean add);

    /**
     * 获取查询条件
     *
     * @param postQueryRequest
     * @return
     */
    QueryWrapper<Post> getQueryWrapper(PostQueryRequest postQueryRequest);

    /**
     * 从 ES 查询
     *
     * @param postQueryRequest
     * @return
     */
    Page<Post> searchFromEs(PostQueryRequest postQueryRequest);

    /**
     * 同步帖子到 ES
     *
     * @param post 帖子
     */
    void syncPostToEs(Post post);

    /**
     * 从 ES 删除帖子
     *
     * @param postId 帖子 id
     */
    void deletePostFromEs(Long postId);

    /**
     * 获取帖子封装
     *
     * @param post
     * @param request
     * @return
     */
    PostVO getPostVO(Post post, HttpServletRequest request);

    /**
     * 分页获取帖子封装
     *
     * @param postPage
     * @param request
     * @return
     */
    Page<PostVO> getPostVOPage(Page<Post> postPage, HttpServletRequest request);

    /**
     * 获取热门帖子列表
     */
    java.util.List<PostVO> listHotPostVO(int size, HttpServletRequest request);

    /**
     * 获取精选帖子列表
     */
    java.util.List<PostVO> listFeaturedPostVO(int size, HttpServletRequest request);

    /**
     * 获取相关帖子列表
     */
    java.util.List<PostVO> listRelatedPostVO(long postId, int size, HttpServletRequest request);
}
