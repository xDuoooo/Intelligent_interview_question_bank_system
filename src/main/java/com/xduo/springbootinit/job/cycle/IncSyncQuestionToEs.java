package com.xduo.springbootinit.job.cycle;

import cn.hutool.core.collection.CollUtil;
import com.xduo.springbootinit.annotation.DistributedLock;
import com.xduo.springbootinit.esdao.QuestionEsDao;
import com.xduo.springbootinit.mapper.QuestionMapper;
import com.xduo.springbootinit.model.dto.question.QuestionEsDTO;
import com.xduo.springbootinit.model.entity.Question;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(prefix = "app.es-sync.question", name = "incremental-enabled", havingValue = "true")
@Slf4j
public class IncSyncQuestionToEs {

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private QuestionEsDao questionEsDao;

    @Value("${app.es-sync.question.incremental-lookback-minutes:5}")
    private long lookbackMinutes;

    /**
     * 每分钟执行一次
     */
    @DistributedLock(key = "IncSyncQuestionToEs", leaseTime = 30000, waitTime = 10000)
    @Scheduled(fixedRateString = "${app.es-sync.question.incremental-fixed-rate-ms:60000}")
    public void run() {
        // 查询近 5 分钟内的数据
        long lookbackMillis = Math.max(1, lookbackMinutes) * 60 * 1000L;
        Date fiveMinutesAgoDate = new Date(new Date().getTime() - lookbackMillis);
        List<Question> questionList = questionMapper.listQuestionWithDelete(fiveMinutesAgoDate);
        if (CollUtil.isEmpty(questionList)) {
            log.info("no inc question");
            return;
        }
        List<Long> deleteIdList = questionList.stream()
                .filter(question -> Integer.valueOf(1).equals(question.getIsDelete()))
                .map(Question::getId)
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(deleteIdList)) {
            questionEsDao.deleteAllById(deleteIdList);
        }
        List<QuestionEsDTO> questionEsDTOList = questionList.stream()
                .filter(question -> !Integer.valueOf(1).equals(question.getIsDelete()))
                .map(QuestionEsDTO::objToDto)
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(questionEsDTOList)) {
            log.info("only delete question sync, total {}", deleteIdList.size());
            return;
        }
        final int pageSize = 500;
        int total = questionEsDTOList.size();
        log.info("IncSyncQuestionToEs start, total {}", total);
        for (int i = 0; i < total; i += pageSize) {
            int end = Math.min(i + pageSize, total);
            log.info("sync from {} to {}", i, end);
            questionEsDao.saveAll(questionEsDTOList.subList(i, end));
        }
        log.info("IncSyncQuestionToEs end, total {}", total);
    }
}
