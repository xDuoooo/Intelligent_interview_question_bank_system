package com.xduo.springbootinit.service.impl;

import static com.xduo.springbootinit.constant.UserConstant.USER_LOGIN_STATE;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xduo.springbootinit.common.ErrorCode;
import com.xduo.springbootinit.constant.CommonConstant;
import com.xduo.springbootinit.constant.RedisConstant;
import com.xduo.springbootinit.exception.BusinessException;
import com.xduo.springbootinit.mapper.UserMapper;
import com.xduo.springbootinit.model.dto.user.UserQueryRequest;
import com.xduo.springbootinit.model.entity.User;
import com.xduo.springbootinit.model.enums.UserRoleEnum;
import com.xduo.springbootinit.model.vo.LoginUserVO;
import com.xduo.springbootinit.model.vo.UserVO;
import com.xduo.springbootinit.satoken.DeviceUtils;
import com.xduo.springbootinit.service.UserService;
import com.xduo.springbootinit.utils.SqlUtils;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBitSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * 用户服务实现
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "xduo";

    @Resource
    private RedissonClient redissonClient;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码不相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            // 默认昵称等于账号
            user.setUserName(userAccount);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            // 注册成功后自动登录
            StpUtil.login(user.getId(), DeviceUtils.getRequestDevice(request));
            StpUtil.getSession().set(USER_LOGIN_STATE, user);
            return user.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 3. 登录即注册逻辑
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        User user = this.baseMapper.selectOne(queryWrapper);

        // 用户不存在，则自动注册
        if (user == null) {
            log.info("user not exists, auto registering: {}", userAccount);
            this.userRegister(userAccount, userPassword, userPassword, request);
            // 注册后再查询一次
            user = this.baseMapper.selectOne(queryWrapper);
        } else {
            // 用户已存在，校验密码
            if (!user.getUserPassword().equals(encryptPassword)) {
                log.info("user login failed, userAccount cannot match userPassword");
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已存在且密码错误");
            }
        }

        // 4. Sa-Token 登录，并指定设备，同端登录互斥
        StpUtil.login(user.getId(), DeviceUtils.getRequestDevice(request));
        StpUtil.getSession().set(USER_LOGIN_STATE, user);

        return this.getLoginUserVO(user);
    }

    @Override
    public LoginUserVO userLoginBySocial(String platform, String socialId, String nickname, String avatar, HttpServletRequest request) {
        // 1. 根据社交平台的 ID 查找用户
        // 统一使用 unionId 存储社交平台唯一标识
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("unionId", socialId);
        User user = this.getOne(queryWrapper);

        // 2. 如果不存在，则注册
        if (user == null) {
            user = new User();
            user.setUnionId(socialId);
            user.setUserAccount(socialId);
            // 初始随机密码
            user.setUserPassword(DigestUtils.md5DigestAsHex(("social_" + socialId).getBytes()));
            user.setUserName(nickname);
            user.setUserAvatar(avatar);
            boolean result = this.save(user);
            if (!result) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "社交登录失败，自动注册异常");
            }
        }

        // 3. 记录登录态
        StpUtil.login(user.getId(), DeviceUtils.getRequestDevice(request));
        StpUtil.getSession().set(USER_LOGIN_STATE, user);

        return this.getLoginUserVO(user);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object loginId = StpUtil.getLoginIdDefaultNull();
        if (loginId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询
        User currentUser = this.getById((Serializable) loginId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }


    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 先判断是否已登录
        Object loginId = StpUtil.getLoginIdDefaultNull();
        if (loginId == null) {
            return null;
        }
        // 从数据库查询
        return this.getById((Serializable) loginId);
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 获取当前登录用户
        User loginUser = this.getLoginUserPermitNull(request);
        return isAdmin(loginUser);
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        StpUtil.checkLogin();
        // 移除登录态
        StpUtil.logout();
        return true;
    }


    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String unionId = userQueryRequest.getUnionId();
        String mpOpenId = userQueryRequest.getMpOpenId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(unionId), "unionId", unionId);
        queryWrapper.eq(StringUtils.isNotBlank(mpOpenId), "mpOpenId", mpOpenId);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 添加用户签到记录
     *
     * @param userId 用户签到
     * @return 当前是否已签到成功
     */
    public boolean addUserSignIn(long userId) {
        LocalDate date = LocalDate.now();
        String key = RedisConstant.getUserSignInRedisKey(date.getYear(), userId);
        RBitSet signInBitSet = redissonClient.getBitSet(key);
        // 获取当前日期是一年中的第几天，作为偏移量（从 1 开始计数）
        int offset = date.getDayOfYear();
        // 检查当天是否已经签到
        if (!signInBitSet.get(offset)) {
            // 如果当天还未签到，则设置
            return signInBitSet.set(offset, true);
        }
        // 当天已签到
        return true;
    }

    //    @Override
//    public List<Integer> getUserSignInRecord(long userId, Integer year) {
//        if (year == null) {
//            LocalDate date = LocalDate.now();
//            year = date.getYear();
//        }
//        String key = RedisConstant.getUserSignInRedisKey(year, userId);
//        RBitSet signInBitSet = redissonClient.getBitSet(key);
//        // 加载 BitSet 到内存中，避免后续读取时发送多次请求
//        BitSet bitSet = signInBitSet.asBitSet();
//        // 统计签到的日期
//        List<Integer> dayList = new ArrayList<>();
//        // 获取当前年份的总天数
//        int totalDays = Year.of(year).length();
//        // 依次获取每一天的签到状态
//        for (int dayOfYear = 1; dayOfYear <= totalDays; dayOfYear++) {
//            // 获取 value：当天是否有刷题
//            boolean hasRecord = bitSet.get(dayOfYear);
//            if (hasRecord) {
//                dayList.add(dayOfYear);
//            }
//        }
//        return dayList;
//    }
    @Override
    public List<Integer> getUserSignInRecord(long userId, Integer year) {
        if (year == null) {
            LocalDate date = LocalDate.now();
            year = date.getYear();
        }
        String key = RedisConstant.getUserSignInRedisKey(year, userId);
        RBitSet signInBitSet = redissonClient.getBitSet(key);
        // 加载 BitSet 到内存中，避免后续读取时发送多次请求
        BitSet bitSet = signInBitSet.asBitSet();
        // 统计签到的日期
        List<Integer> dayList = new ArrayList<>();
        // 从索引 0 开始查找下一个被设置为 1 的位
        int index = bitSet.nextSetBit(0);
        while (index >= 0) {
            dayList.add(index);
            // 查找下一个被设置为 1 的位
            index = bitSet.nextSetBit(index + 1);
        }
        return dayList;
    }

    @Override
    public void changePassword(com.xduo.springbootinit.model.dto.user.UserChangePasswordRequest request, User loginUser) {
        String oldPassword = request.getOldPassword();
        String newPassword = request.getNewPassword();
        String checkPassword = request.getCheckPassword();
        if (StringUtils.isAnyBlank(oldPassword, newPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (newPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新密码过短");
        }
        if (!newPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 校验旧密码
        String encryptOldPassword = DigestUtils.md5DigestAsHex((SALT + oldPassword).getBytes());
        if (!encryptOldPassword.equals(loginUser.getUserPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "旧密码错误");
        }
        // 更新密码
        String encryptNewPassword = DigestUtils.md5DigestAsHex((SALT + newPassword).getBytes());
        User user = new User();
        user.setId(loginUser.getId());
        user.setUserPassword(encryptNewPassword);
        this.updateById(user);
    }


    @Override
    public void checkUserNameUnique(String userName, Long userId) {
        if (StringUtils.isBlank(userName)) {
            return;
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userName", userName);
        queryWrapper.ne(userId != null, "id", userId);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "昵称已存在");
        }
    }

}
