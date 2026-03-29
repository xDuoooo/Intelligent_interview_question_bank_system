package com.xduo.springbootinit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xduo.springbootinit.model.entity.UserFollow;
import com.xduo.springbootinit.model.vo.UserVO;

/**
 * 用户关注关系服务
 */
public interface UserFollowService extends IService<UserFollow> {

    /**
     * 关注用户
     *
     * @param userId       当前用户 id
     * @param followUserId 目标用户 id
     * @return 是否已处于关注状态
     */
    boolean followUser(long userId, long followUserId);

    /**
     * 取消关注用户
     *
     * @param userId       当前用户 id
     * @param followUserId 目标用户 id
     * @return 当前是否已取消关注
     */
    boolean unfollowUser(long userId, long followUserId);

    /**
     * 当前用户是否已关注目标用户
     */
    boolean hasFollowed(Long userId, Long followUserId);

    /**
     * 获取粉丝数量
     */
    long getFollowerCount(long userId);

    /**
     * 获取关注数量
     */
    long getFollowingCount(long userId);

    /**
     * 分页获取粉丝列表
     */
    Page<UserVO> listFollowerUserVOByPage(long userId, long current, long pageSize);

    /**
     * 分页获取关注列表
     */
    Page<UserVO> listFollowingUserVOByPage(long userId, long current, long pageSize);
}
