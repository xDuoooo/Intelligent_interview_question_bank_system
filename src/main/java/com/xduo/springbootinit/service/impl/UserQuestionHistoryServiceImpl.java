package com.xduo.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xduo.springbootinit.mapper.UserQuestionHistoryMapper;
import com.xduo.springbootinit.model.entity.UserQuestionHistory;
import com.xduo.springbootinit.service.UserQuestionHistoryService;
import org.springframework.stereotype.Service;

import java.util.Date;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xduo.springbootinit.model.entity.Question;
import com.xduo.springbootinit.model.entity.QuestionFavour;
import com.xduo.springbootinit.model.vo.QuestionVO;
import com.xduo.springbootinit.model.vo.UserQuestionHistoryVO;
import com.xduo.springbootinit.service.QuestionFavourService;
import com.xduo.springbootinit.service.QuestionService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户刷题轨迹服务实现
 */
@Service
public class UserQuestionHistoryServiceImpl extends ServiceImpl<UserQuestionHistoryMapper, UserQuestionHistory>
        implements UserQuestionHistoryService {

    @Resource
    private QuestionFavourService questionFavourService;

    @Resource
    private QuestionService questionService;

    @Override
    public boolean addQuestionHistory(long userId, long questionId, int status) {
        // 先查询是否已经有记录
        QueryWrapper<UserQuestionHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("questionId", questionId);
        UserQuestionHistory oldHistory = this.getOne(queryWrapper);

        if (oldHistory != null) {
            // 如果旧状态是更高级的状态，则更新，否则只更新时间
            if (status > oldHistory.getStatus()) {
                oldHistory.setStatus(status);
            }
            oldHistory.setUpdateTime(new Date());
            return this.updateById(oldHistory);
        } else {
            // 插入新记录
            UserQuestionHistory newHistory = new UserQuestionHistory();
            newHistory.setUserId(userId);
            newHistory.setQuestionId(questionId);
            newHistory.setStatus(status);
            return this.save(newHistory);
        }
    }

    @Override
    public Page<QuestionVO> listMyFavourQuestionByPage(Page<Question> page, long userId, HttpServletRequest request) {
        // 先查询收藏记录
        QueryWrapper<QuestionFavour> favourQueryWrapper = new QueryWrapper<>();
        favourQueryWrapper.eq("userId", userId);
        favourQueryWrapper.orderByDesc("createTime");
        Page<QuestionFavour> favourPage = questionFavourService.page(new Page<>(page.getCurrent(), page.getSize()), favourQueryWrapper);
        
        List<QuestionFavour> favourList = favourPage.getRecords();
        Page<QuestionVO> questionVOPage = new Page<>(favourPage.getCurrent(), favourPage.getSize(), favourPage.getTotal());
        if (favourList.isEmpty()) {
            return questionVOPage;
        }
        
        // 根据题目 id 查询题目详情
        Set<Long> questionIdSet = favourList.stream().map(QuestionFavour::getQuestionId).collect(Collectors.toSet());
        List<Question> questionList = questionService.listByIds(questionIdSet);
        
        // 转换为 VO 分页
        Page<Question> questionPage = new Page<>(favourPage.getCurrent(), favourPage.getSize(), favourPage.getTotal());
        questionPage.setRecords(questionList);
        return questionService.getQuestionVOPage(questionPage, request);
    }

    @Override
    public Page<UserQuestionHistoryVO> listMyQuestionHistoryByPage(Page<UserQuestionHistory> page, long userId, HttpServletRequest request) {
        QueryWrapper<UserQuestionHistory> historyQueryWrapper = new QueryWrapper<>();
        historyQueryWrapper.eq("userId", userId);
        historyQueryWrapper.orderByDesc("updateTime");
        Page<UserQuestionHistory> historyPage = this.page(page, historyQueryWrapper);
        
        List<UserQuestionHistory> historyList = historyPage.getRecords();
        Page<UserQuestionHistoryVO> voPage = new Page<>(historyPage.getCurrent(), historyPage.getSize(), historyPage.getTotal());
        if (historyList.isEmpty()) {
            return voPage;
        }
        
        Set<Long> questionIdSet = historyList.stream().map(UserQuestionHistory::getQuestionId).collect(Collectors.toSet());
        List<Question> questionList = questionService.listByIds(questionIdSet);
        Map<Long, List<Question>> questionMap = questionList.stream().collect(Collectors.groupingBy(Question::getId));
        
        List<UserQuestionHistoryVO> voList = historyList.stream().map(history -> {
            UserQuestionHistoryVO vo = new UserQuestionHistoryVO();
            org.springframework.beans.BeanUtils.copyProperties(history, vo);
            Long qId = history.getQuestionId();
            if (questionMap.containsKey(qId)) {
                vo.setQuestion(questionService.getQuestionVO(questionMap.get(qId).get(0), request));
            }
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public List<Map<String, Object>> getUserQuestionHistoryRecord(long userId, Integer year) {
        if (year == null) {
            year = java.time.LocalDate.now().getYear();
        }
        QueryWrapper<UserQuestionHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DATE(updateTime) as date", "count(*) as count");
        queryWrapper.eq("userId", userId);
        queryWrapper.apply("YEAR(updateTime) = {0}", year);
        queryWrapper.groupBy("DATE(updateTime)");
        return this.listMaps(queryWrapper);
    }

    @Override
    public Map<String, Object> getUserQuestionStats(long userId) {
        Map<String, Object> stats = new java.util.HashMap<>();
        
        // 总刷题量
        QueryWrapper<UserQuestionHistory> totalWrapper = new QueryWrapper<>();
        totalWrapper.eq("userId", userId);
        long totalCount = this.count(totalWrapper);
        stats.put("totalCount", totalCount);
        
        // 已掌握数量 (status = 1)
        QueryWrapper<UserQuestionHistory> masteredWrapper = new QueryWrapper<>();
        masteredWrapper.eq("userId", userId);
        masteredWrapper.eq("status", 1);
        long masteredCount = this.count(masteredWrapper);
        stats.put("masteredCount", masteredCount);
        
        // 收藏数量
        QueryWrapper<QuestionFavour> favourWrapper = new QueryWrapper<>();
        favourWrapper.eq("userId", userId);
        long favourCount = questionFavourService.count(favourWrapper);
        stats.put("favourCount", favourCount);
        
        return stats;
    }
}
