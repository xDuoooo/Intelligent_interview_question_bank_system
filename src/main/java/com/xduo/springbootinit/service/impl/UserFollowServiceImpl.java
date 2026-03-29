package com.xduo.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xduo.springbootinit.common.ErrorCode;
import com.xduo.springbootinit.constant.UserConstant;
import com.xduo.springbootinit.exception.BusinessException;
import com.xduo.springbootinit.exception.ThrowUtils;
import com.xduo.springbootinit.mapper.UserFollowMapper;
import com.xduo.springbootinit.mapper.UserMapper;
import com.xduo.springbootinit.model.entity.User;
import com.xduo.springbootinit.model.entity.UserFollow;
import com.xduo.springbootinit.model.vo.UserVO;
import com.xduo.springbootinit.service.NotificationService;
import com.xduo.springbootinit.service.UserFollowService;
import com.xduo.springbootinit.service.UserService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DuplicateKeyException;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户关注关系服务实现
 */
@Service
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow> implements UserFollowService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserService userService;

    @Resource
    private NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean followUser(long userId, long followUserId) {
        validateFollowAction(userId, followUserId);
        synchronized (("user_follow:" + userId).intern()) {
            QueryWrapper<UserFollow> queryWrapper = buildRelationQueryWrapper(userId, followUserId);
            if (this.count(queryWrapper) > 0) {
                return true;
            }
            UserFollow userFollow = new UserFollow();
            userFollow.setUserId(userId);
            userFollow.setFollowUserId(followUserId);
            boolean result;
            try {
                result = this.save(userFollow);
            } catch (DuplicateKeyException duplicateKeyException) {
                return true;
            }
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

            User currentUser = userMapper.selectById(userId);
            if (currentUser != null) {
                String displayName = StringUtils.defaultIfBlank(currentUser.getUserName(), "有用户");
                notificationService.sendNotification(
                        followUserId,
                        "你收到了一个新关注",
                        displayName + " 关注了你，快去看看 Ta 的主页吧。",
                        "user_follow",
                        userId
                );
            }
            return true;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unfollowUser(long userId, long followUserId) {
        validateFollowAction(userId, followUserId);
        synchronized (("user_follow:" + userId).intern()) {
            QueryWrapper<UserFollow> queryWrapper = buildRelationQueryWrapper(userId, followUserId);
            if (this.count(queryWrapper) == 0) {
                return false;
            }
            boolean result = this.remove(queryWrapper);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
            return false;
        }
    }

    @Override
    public boolean hasFollowed(Long userId, Long followUserId) {
        if (userId == null || followUserId == null || userId <= 0 || followUserId <= 0 || Objects.equals(userId, followUserId)) {
            return false;
        }
        return this.count(buildRelationQueryWrapper(userId, followUserId)) > 0;
    }

    @Override
    public long getFollowerCount(long userId) {
        return baseMapper.countVisibleFollower(userId);
    }

    @Override
    public long getFollowingCount(long userId) {
        return baseMapper.countVisibleFollowing(userId);
    }

    @Override
    public Page<UserVO> listFollowerUserVOByPage(long userId, long current, long pageSize) {
        long total = getFollowerCount(userId);
        Page<UserVO> page = new Page<>(current, pageSize, total);
        if (total <= 0) {
            return page;
        }
        long offset = Math.max(0L, (current - 1) * pageSize);
        List<Long> userIdList = baseMapper.listVisibleFollowerUserIds(userId, offset, pageSize);
        page.setRecords(buildOrderedUserVOList(userIdList));
        return page;
    }

    @Override
    public Page<UserVO> listFollowingUserVOByPage(long userId, long current, long pageSize) {
        long total = getFollowingCount(userId);
        Page<UserVO> page = new Page<>(current, pageSize, total);
        if (total <= 0) {
            return page;
        }
        long offset = Math.max(0L, (current - 1) * pageSize);
        List<Long> userIdList = baseMapper.listVisibleFollowingUserIds(userId, offset, pageSize);
        page.setRecords(buildOrderedUserVOList(userIdList));
        return page;
    }

    /**
     * 构造稳定顺序的用户视图列表
     */
    private List<UserVO> buildOrderedUserVOList(List<Long> userIdList) {
        if (CollectionUtils.isEmpty(userIdList)) {
            return Collections.emptyList();
        }
        List<User> userList = userService.listByIds(userIdList);
        Map<Long, User> userMap = userList.stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (left, right) -> left));
        return userIdList.stream()
                .map(userMap::get)
                .filter(Objects::nonNull)
                .map(userService::getUserVO)
                .collect(Collectors.toList());
    }

    /**
     * 校验关注动作
     */
    private void validateFollowAction(long userId, long followUserId) {
        ThrowUtils.throwIf(userId <= 0 || followUserId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(userId == followUserId, ErrorCode.PARAMS_ERROR, "不能关注自己");
        User followUser = userMapper.selectById(followUserId);
        ThrowUtils.throwIf(followUser == null, ErrorCode.NOT_FOUND_ERROR, "目标用户不存在");
        ThrowUtils.throwIf(UserConstant.BAN_ROLE.equals(followUser.getUserRole()), ErrorCode.NOT_FOUND_ERROR, "目标用户不存在");
    }

    /**
     * 构造关系查询条件
     */
    private QueryWrapper<UserFollow> buildRelationQueryWrapper(long userId, long followUserId) {
        QueryWrapper<UserFollow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("followUserId", followUserId);
        return queryWrapper;
    }
}
