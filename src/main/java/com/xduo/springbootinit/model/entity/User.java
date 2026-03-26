package com.xduo.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户
 * @TableName user
 */
@TableName(value ="user")
public class User implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 微信开放平台id
     */
    private String unionId;

    /**
     * 公众号openId
     */
    private String mpOpenId;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    /**
     * id
     */
    public Long getId() {
        return id;
    }

    /**
     * id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 账号
     */
    public String getUserAccount() {
        return userAccount;
    }

    /**
     * 账号
     */
    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }

    /**
     * 密码
     */
    public String getUserPassword() {
        return userPassword;
    }

    /**
     * 密码
     */
    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    /**
     * 微信开放平台id
     */
    public String getUnionId() {
        return unionId;
    }

    /**
     * 微信开放平台id
     */
    public void setUnionId(String unionId) {
        this.unionId = unionId;
    }

    /**
     * 公众号openId
     */
    public String getMpOpenId() {
        return mpOpenId;
    }

    /**
     * 公众号openId
     */
    public void setMpOpenId(String mpOpenId) {
        this.mpOpenId = mpOpenId;
    }

    /**
     * 用户昵称
     */
    public String getUserName() {
        return userName;
    }

    /**
     * 用户昵称
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * 用户头像
     */
    public String getUserAvatar() {
        return userAvatar;
    }

    /**
     * 用户头像
     */
    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    /**
     * 用户简介
     */
    public String getUserProfile() {
        return userProfile;
    }

    /**
     * 用户简介
     */
    public void setUserProfile(String userProfile) {
        this.userProfile = userProfile;
    }

    /**
     * 用户角色：user/admin/ban
     */
    public String getUserRole() {
        return userRole;
    }

    /**
     * 用户角色：user/admin/ban
     */
    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    /**
     * 编辑时间
     */
    public Date getEditTime() {
        return editTime;
    }

    /**
     * 编辑时间
     */
    public void setEditTime(Date editTime) {
        this.editTime = editTime;
    }

    /**
     * 创建时间
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 创建时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 更新时间
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * 更新时间
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 是否删除
     */
    public Integer getIsDelete() {
        return isDelete;
    }

    /**
     * 是否删除
     */
    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }
}