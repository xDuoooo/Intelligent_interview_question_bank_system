package com.xduo.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xduo.springbootinit.model.dto.questionbankquestion.QuestionBankQuestionQueryRequest;
import com.xduo.springbootinit.model.entity.QuestionBankQuestion;
import com.xduo.springbootinit.model.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 题库题目关联服务
 */
public interface QuestionBankQuestionService extends IService<QuestionBankQuestion> {

    /**
     * 获取查询条件
     *
     * @param questionBankQuestionQueryRequest
     * @return
     */
    QueryWrapper<QuestionBankQuestion> getQueryWrapper(
            QuestionBankQuestionQueryRequest questionBankQuestionQueryRequest);

    /**
     * 分页获取题库题目关联列表
     *
     * @param questionBankQuestionQueryRequest
     * @return
     */
    Page<QuestionBankQuestion> listQuestionBankQuestionByPage(
            QuestionBankQuestionQueryRequest questionBankQuestionQueryRequest);

    void batchAddQuestionsToBank(List<Long> questionIdList, Long questionBankId, User loginUser);

    @Transactional(rollbackFor = Exception.class)
    void batchRemoveQuestionsFromBank(List<Long> questionIdList, Long questionBankId);

    void validQuestionBankQuestion(QuestionBankQuestion questionBankQuestion, boolean add);
}
