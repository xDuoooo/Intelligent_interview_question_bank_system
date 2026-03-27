package com.xduo.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xduo.springbootinit.model.entity.QuestionFavour;
import com.xduo.springbootinit.model.entity.User;

/**
 * 题目收藏服务
 */
public interface QuestionFavourService extends IService<QuestionFavour> {

    /**
     * 题目收藏
     *
     * @param questionId
     * @param loginUser
     * @return
     */
    int doQuestionFavour(long questionId, User loginUser);

    /**
     * 题目收藏（内部事务）
     *
     * @param userId
     * @param questionId
     * @return
     */
    int doQuestionFavourInner(long userId, long questionId);
}
