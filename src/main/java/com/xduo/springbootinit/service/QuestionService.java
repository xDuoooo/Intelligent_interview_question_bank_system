package com.xduo.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xduo.springbootinit.model.dto.question.QuestionQueryRequest;
import com.xduo.springbootinit.model.entity.Question;
import com.xduo.springbootinit.model.vo.QuestionVO;
import com.xduo.springbootinit.model.vo.ResumeQuestionRecommendVO;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 题目服务
 */
public interface QuestionService extends IService<Question> {

    /**
     * 校验数据
     *
     * @param question
     * @param add      对创建的数据进行校验
     */
    void validQuestion(Question question, boolean add);

    /**
     * 获取查询条件
     *
     * @param questionQueryRequest
     * @return
     */
    QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest);

    /**
     * 获取题目封装
     *
     * @param question
     * @param request
     * @return
     */
    QuestionVO getQuestionVO(Question question, HttpServletRequest request);

    /**
     * 分页获取题目封装
     *
     * @param questionPage
     * @param request
     * @return
     */
    Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request);

    /**
     * 从 ES 查询题目
     *
     * @param questionQueryRequest
     * @return
     */
    Page<Question> searchFromEs(QuestionQueryRequest questionQueryRequest);

    void batchDeleteQuestions(List<Long> questionIdList);

    //分页查询题目
    Page<Question> listQuestionByPage(QuestionQueryRequest questionQueryRequest);

    /**
     * 获取个性化推荐题目
     */
    List<QuestionVO> listRecommendQuestionVOByUser(long userId, Long currentQuestionId, int size, HttpServletRequest request);

    /**
     * 获取当前题目的相关推荐
     */
    List<QuestionVO> listRelatedQuestionVO(long questionId, int size, HttpServletRequest request);

    /**
     * 根据简历内容推荐题目
     */
    ResumeQuestionRecommendVO recommendQuestionsByResume(long userId, String resumeText, int size, HttpServletRequest request);
}
