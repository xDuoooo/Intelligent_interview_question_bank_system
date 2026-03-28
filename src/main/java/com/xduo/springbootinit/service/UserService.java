package com.xduo.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xduo.springbootinit.model.dto.user.UserQueryRequest;
import com.xduo.springbootinit.model.entity.User;
import com.xduo.springbootinit.model.vo.LoginUserVO;
import com.xduo.springbootinit.model.vo.UserVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户服务
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, HttpServletRequest request);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 社交登录（通用）
     *
     * @param platform 平台
     * @param socialId 社交平台唯一标识
     * @param nickname 昵称
     * @param avatar   头像
     * @param request  请求
     * @return 登录用户信息
     */
    LoginUserVO userLoginBySocial(String platform, String socialId, String nickname, String avatar, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    User getLoginUserPermitNull(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 添加用户签到记录
     *
     * @param userId 用户 id
     * @return 当前是否已签到成功
     */
    boolean addUserSignIn(long userId);

    /**
     * 获取用户某个年份的签到记录
     *
     * @param userId 用户 id
     * @param year   年份（为空表示当前年份）
     * @return 签到记录映射
     */
    List<Integer> getUserSignInRecord(long userId, Integer year);

    /**
     * 修改密码
     *
     * @param request
     * @param loginUser
     */
    void changePassword(com.xduo.springbootinit.model.dto.user.UserChangePasswordRequest request, User loginUser);


    /**
     * 检查昵称是否唯一
     *
     * @param userName  待检查昵称
     * @param userId    当前用户 ID（排除自身）
     */
    void checkUserNameUnique(String userName, Long userId);

    /**
     * 发送验证码
     *
     * @param userSendCodeRequest 发送请求
     * @param request 请求
     */
    void sendVerificationCode(com.xduo.springbootinit.model.dto.user.UserSendCodeRequest userSendCodeRequest, HttpServletRequest request);

    /**
     * 验证码登录/注册
     *
     * @param userCodeLoginRequest 登录请求
     * @param request 请求
     * @return 登录用户信息
     */
    LoginUserVO userCodeLogin(com.xduo.springbootinit.model.dto.user.UserCodeLoginRequest userCodeLoginRequest, HttpServletRequest request);

    /**
     * 绑定手机号
     *
     * @param target 手机号
     * @param code   验证码
     * @param loginUser 当前登录用户
     */
    void bindPhone(String target, String code, User loginUser);

    /**
     * 绑定邮箱
     *
     * @param target 邮箱
     * @param code   验证码
     * @param loginUser 当前登录用户
     */
    void bindEmail(String target, String code, User loginUser);

    /**
     * 解绑手机号
     *
     * @param userId 用户 id
     */
    void unbindPhone(long userId);

    /**
     * 解绑邮箱
     *
     * @param userId 用户 id
     */
    void unbindEmail(long userId);

    /**
     * GitHub 登录/注册
     *
     * @param githubId GitHub 唯一标识
     * @param userName GitHub 昵称
     * @param userAvatar GitHub 头像
     * @return 用户信息
     */
    User githubLogin(String githubId, String userName, String userAvatar);

    /**
     * Gitee 登录/注册
     *
     * @param giteeId Gitee 唯一标识
     * @param userName Gitee 昵称
     * @param userAvatar Gitee 头像
     * @return 用户信息
     */
    User giteeLogin(String giteeId, String userName, String userAvatar);

    /**
     * Google 登录/注册
     *
     * @param googleId Google 唯一标识
     * @param userName Google 昵称
     * @param userAvatar Google 头像
     * @return 用户信息
     */
    User googleLogin(String googleId, String userName, String userAvatar);

    /**
     * 绑定 GitHub
     */
    void bindGithub(long userId, String githubId);

    /**
     * 绑定 Gitee
     */
    void bindGitee(long userId, String giteeId);

    /**
     * 绑定 Google
     */
    void bindGoogle(long userId, String googleId);

    /**
     * 解绑 GitHub
     */
    void unbindGithub(long userId);

    /**
     * 解绑 Gitee
     */
    void unbindGitee(long userId);

    /**
     * 解绑 Google
     */
    void unbindGoogle(long userId);
}
