package com.xduo.springbootinit.model.vo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xduo.springbootinit.model.entity.Question;
import com.xduo.springbootinit.model.entity.QuestionBank;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 题库视图
 */
@Data
public class QuestionBankVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 描述
     */
    private String description;

    /**
     * 图片
     */
    private String picture;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 审核状态：0-待审核 1-已通过 2-已驳回
     */
    private Integer reviewStatus;

    /**
     * 审核意见
     */
    private String reviewMessage;

    /**
     * 审核人 id
     */
    private Long reviewUserId;

    /**
     * 审核时间
     */
    private Date reviewTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建用户信息
     */
    private UserVO user;

    /**
     * 是否要关联查询题目列表
     */
    private Page<Question> questionPage;


    /**
     * 封装类转对象
     */
    public static QuestionBank voToObj(QuestionBankVO questionBankVO) {
        if (questionBankVO == null) {
            return null;
        }
        QuestionBank questionBank = new QuestionBank();
        BeanUtils.copyProperties(questionBankVO, questionBank);
        return questionBank;
    }

    /**
     * 对象转封装类
     */
    public static QuestionBankVO objToVo(QuestionBank questionBank) {
        if (questionBank == null) {
            return null;
        }
        QuestionBankVO questionBankVO = new QuestionBankVO();
        BeanUtils.copyProperties(questionBank, questionBankVO);
        return questionBankVO;
    }

    private static final long serialVersionUID = 1L;
}
