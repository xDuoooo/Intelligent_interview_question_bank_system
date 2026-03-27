package com.xduo.springbootinit.service.impl;


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
import com.xduo.springbootinit.utils.AliyunSmsUtils;
import com.xduo.springbootinit.utils.NetUtils;
import com.xduo.springbootinit.utils.SqlUtils;
import com.xduo.springbootinit.exception.ThrowUtils;
import org.redisson.api.RAtomicLong;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.DateUnit;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import static com.xduo.springbootinit.constant.UserConstant.*;

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
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Resource
    private AliyunSmsUtils aliyunSmsUtils;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword,
            HttpServletRequest request) {
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || !userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短或两次密码不一致");
        }
        synchronized (userAccount.intern()) {
            long count = this.count(new QueryWrapper<User>().eq("userAccount", userAccount));
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
            }
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes()));
            user.setUserName("智面用户_" + RandomUtil.randomNumbers(4));
            user.setUserRole(UserRoleEnum.USER.getValue());
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");
            }
            return user.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        User user = this
                .getOne(new QueryWrapper<User>().eq("userAccount", userAccount).eq("userPassword", encryptPassword));
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误");
        }
        StpUtil.login(user.getId(), DeviceUtils.getRequestDevice(request));
        StpUtil.getSession().set(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    @Override
    public LoginUserVO userLoginBySocial(String platform, String socialId, String nickname, String avatar,
            HttpServletRequest request) {
        User user = this.getOne(new QueryWrapper<User>().eq("unionId", socialId));
        if (user == null) {
            user = new User();
            user.setUnionId(socialId);
            user.setUserAccount("u_" + RandomUtil.randomString(8));
            user.setUserPassword(DigestUtils.md5DigestAsHex(("social_" + socialId).getBytes()));
            user.setUserName(nickname);
            user.setUserAvatar(avatar);
            user.setUserRole(UserRoleEnum.USER.getValue());
            this.save(user);
        }
        StpUtil.login(user.getId(), DeviceUtils.getRequestDevice(request));
        StpUtil.getSession().set(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    @Override
    public void sendVerificationCode(com.xduo.springbootinit.model.dto.user.UserSendCodeRequest userSendCodeRequest,
            HttpServletRequest request) {
        String target = userSendCodeRequest.getTarget();
        Integer type = userSendCodeRequest.getType();
        String captcha = userSendCodeRequest.getCaptcha();
        String captchaUuid = userSendCodeRequest.getCaptchaUuid();

        ThrowUtils.throwIf(StringUtils.isAnyBlank(target, captcha, captchaUuid), ErrorCode.PARAMS_ERROR,
                "参数不全，请完成图形码验证");

        // 1. 图形码校验
        String captchaKey = RedisConstant.getUserCaptchaRedisKey(captchaUuid);
        String savedCaptcha = stringRedisTemplate.opsForValue().get(captchaKey);
        if (savedCaptcha == null || !savedCaptcha.equalsIgnoreCase(captcha)) {
            log.warn("图形验证码匹配失败: target={}, input={}, saved={}", target, captcha, savedCaptcha);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图形验证码错误或已过期");
        }
        stringRedisTemplate.delete(captchaKey);

        // 2. 格式校验
        if (type == 1) {
            // 邮箱正则
            if (!target.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$")) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
            }
        } else if (type == 2) {
            // 手机号正则
            if (!target.matches("^1[3-9]\\d{9}$")) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式不正确");
            }
        }

        // 3. 限流校验 (IP 10/日, 目标 5/日)
        String ip = NetUtils.getIpAddress(request);
        RAtomicLong ipCounter = redissonClient.getAtomicLong(RedisConstant.getUserIpLimitRedisKey(ip));
        RAtomicLong phoneCounter = redissonClient.getAtomicLong(RedisConstant.getUserPhoneLimitRedisKey(target));
        if (ipCounter.get() >= 10) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "今日发送次数已达上限");
        }
        if (phoneCounter.get() >= 5) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该号码今日发送次数已达上限");
        }

        // 4. 60s 频率限制
        String limitKey = RedisConstant.getUserCodeSendLimitRedisKey(target);
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(limitKey))) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "发送过于频繁，请 60s 后重试");
        }

        // 5. 发送逻辑
        String code = RandomUtil.randomNumbers(6);
        boolean sendResult;
        if (type == 1) {
            sendResult = sendEmail(target, code);
            if (!sendResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "邮件服务器响应异常，请检查配置");
            }
        } else {
            sendResult = aliyunSmsUtils.sendMessage(target, code);
            if (!sendResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "短信发送失败，请检查阿里云配置及签名/模版 ID");
            }
        }

        // 6. 存入 Redis 并更新限流
        stringRedisTemplate.opsForValue().set(RedisConstant.getUserLoginCodeRedisKey(target), code, 5,
                java.util.concurrent.TimeUnit.MINUTES);
        stringRedisTemplate.opsForValue().set(limitKey, "1", 60, java.util.concurrent.TimeUnit.SECONDS);

        long secondsToMidnight = DateUtil.between(new Date(), DateUtil.endOfDay(new Date()), DateUnit.SECOND);
        ipCounter.incrementAndGet();
        ipCounter.expire(java.time.Duration.ofSeconds(secondsToMidnight));
        phoneCounter.incrementAndGet();
        phoneCounter.expire(java.time.Duration.ofSeconds(secondsToMidnight));
    }

    @Override
    public LoginUserVO userCodeLogin(com.xduo.springbootinit.model.dto.user.UserCodeLoginRequest userCodeLoginRequest,
            HttpServletRequest request) {
        String target = userCodeLoginRequest.getTarget();
        String code = userCodeLoginRequest.getCode();
        Integer type = userCodeLoginRequest.getType();

        String codeKey = RedisConstant.getUserLoginCodeRedisKey(target);
        String cachedCode = stringRedisTemplate.opsForValue().get(codeKey);
        if (cachedCode == null || !cachedCode.equals(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误或已过期");
        }
        stringRedisTemplate.delete(codeKey);

        synchronized (target.intern()) {
            User user = this.getOne(new QueryWrapper<User>().eq(type == 1 ? "email" : "phone", target));
            if (user == null) {
                user = new User();
                if (type == 1)
                    user.setEmail(target);
                else
                    user.setPhone(target);
                user.setUserAccount("u_" + RandomUtil.randomString(8));
                user.setUserName("智面用户_" + RandomUtil.randomNumbers(4));
                user.setUserPassword(DigestUtils.md5DigestAsHex((SALT + "12345678").getBytes()));
                user.setUserRole(UserRoleEnum.USER.getValue());
                this.save(user);
            }
            StpUtil.login(user.getId(), DeviceUtils.getRequestDevice(request));
            StpUtil.getSession().set(USER_LOGIN_STATE, user);
            return this.getLoginUserVO(user);
        }
    }

    private boolean sendEmail(String email, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("智面平台 - 验证码");
            message.setText("您的验证码为：" + code + "，5 分钟内有效。如非本人操作请忽略。");
            javaMailSender.send(message);
            return true;
        } catch (Exception e) {
            log.error("邮件发送异常", e);
            return false;
        }
    }

    @Override
    public void checkUserNameUnique(String userName, Long userId) {
        if (StringUtils.isBlank(userName))
            return;
        long count = this.count(new QueryWrapper<User>().eq("userName", userName).ne(userId != null, "id", userId));
        if (count > 0)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "昵称已存在");
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null)
            return null;
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null)
            return null;
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollUtil.isEmpty(userList))
            return new ArrayList<>();
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        Long id = userQueryRequest.getId();
        String unionId = userQueryRequest.getUnionId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.eq(StringUtils.isNotBlank(unionId), "unionId", unionId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder == null || sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (!StpUtil.isLogin()) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        User user = (User) StpUtil.getSession().get(USER_LOGIN_STATE);
        if (user == null) {
            user = this.getById((Serializable) StpUtil.getLoginIdAsLong());
            if (user == null) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
            }
            StpUtil.getSession().set(USER_LOGIN_STATE, user);
        }
        return user;
    }

    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        if (!StpUtil.isLogin()) {
            return null;
        }
        return (User) StpUtil.getSession().get(USER_LOGIN_STATE);
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        User user = this.getLoginUserPermitNull(request);
        return isAdmin(user);
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
            return true;
        }
        return false;
    }

    @Override
    public boolean addUserSignIn(long userId) {
        // 签到逻辑 (简单示例：在 Redis 中记录今日是否已签到)
        String key = RedisConstant.getUserSignInRedisKey(LocalDate.now().getYear(), userId);
        int dayOfYear = LocalDate.now().getDayOfYear();
        // 简化实现：略过复杂的签到逻辑，直接返回 true
        log.info("用户签到: userId={}, day={}, key={}", userId, dayOfYear, key);
        return true;
    }

    @Override
    public List<Integer> getUserSignInRecord(long userId, Integer year) {
        // 简化实现
        return new ArrayList<>();
    }

    @Override
    public void changePassword(com.xduo.springbootinit.model.dto.user.UserChangePasswordRequest userChangePasswordRequest, User loginUser) {
        String oldPassword = userChangePasswordRequest.getOldPassword();
        String newPassword = userChangePasswordRequest.getNewPassword();
        String encryptOld = DigestUtils.md5DigestAsHex((SALT + oldPassword).getBytes());
        if (!loginUser.getUserPassword().equals(encryptOld)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "旧密码错误");
        }
        User user = new User();
        user.setId(loginUser.getId());
        user.setUserPassword(DigestUtils.md5DigestAsHex((SALT + newPassword).getBytes()));
        this.updateById(user);
        StpUtil.logout(); // 修改密码后强制重新登录
    }
}
