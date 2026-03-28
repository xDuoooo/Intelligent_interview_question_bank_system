package com.xduo.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 题目搜索日志
 */
@TableName(value = "question_search_log")
@Data
public class QuestionSearchLog implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 搜索用户 id
     */
    private Long userId;

    /**
     * 搜索关键词
     */
    private String searchText;

    /**
     * 搜索来源
     */
    private String source;

    /**
     * 命中结果数
     */
    private Integer resultCount;

    /**
     * 是否无结果
     */
    private Integer hasNoResult;

    /**
     * 搜索 IP
     */
    private String ip;

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
    @TableLogic
    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}
