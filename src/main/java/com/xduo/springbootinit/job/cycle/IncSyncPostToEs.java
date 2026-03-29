package com.xduo.springbootinit.job.cycle;

import com.xduo.springbootinit.annotation.DistributedLock;
import com.xduo.springbootinit.esdao.PostEsDao;
import com.xduo.springbootinit.mapper.PostMapper;
import com.xduo.springbootinit.model.dto.post.PostEsDTO;
import com.xduo.springbootinit.model.entity.Post;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.collection.CollUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 增量同步帖子到 es

 */
@Component
@ConditionalOnProperty(prefix = "app.es-sync.post", name = "incremental-enabled", havingValue = "true")
@Slf4j
public class IncSyncPostToEs {

    @Resource
    private PostMapper postMapper;

    @Resource
    private PostEsDao postEsDao;

    @Value("${app.es-sync.post.incremental-lookback-minutes:5}")
    private long lookbackMinutes;

    /**
     * 每分钟执行一次
     */
    @DistributedLock(key = "IncSyncPostToEs", leaseTime = 30000, waitTime = 10000)
    @Scheduled(fixedRateString = "${app.es-sync.post.incremental-fixed-rate-ms:60000}")
    public void run() {
        // 查询近 5 分钟内的数据
        long lookbackMillis = Math.max(1, lookbackMinutes) * 60 * 1000L;
        Date fiveMinutesAgoDate = new Date(new Date().getTime() - lookbackMillis);
        List<Post> postList = postMapper.listPostWithDelete(fiveMinutesAgoDate);
        if (CollUtil.isEmpty(postList)) {
            log.info("no inc post");
            return;
        }
        List<Long> deleteIdList = postList.stream()
                .filter(post -> Integer.valueOf(1).equals(post.getIsDelete()))
                .map(Post::getId)
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(deleteIdList)) {
            postEsDao.deleteAllById(deleteIdList);
        }
        List<PostEsDTO> postEsDTOList = postList.stream()
                .filter(post -> !Integer.valueOf(1).equals(post.getIsDelete()))
                .map(PostEsDTO::objToDto)
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(postEsDTOList)) {
            log.info("only delete post sync, total {}", deleteIdList.size());
            return;
        }
        final int pageSize = 500;
        int total = postEsDTOList.size();
        log.info("IncSyncPostToEs start, total {}", total);
        for (int i = 0; i < total; i += pageSize) {
            int end = Math.min(i + pageSize, total);
            log.info("sync from {} to {}", i, end);
            postEsDao.saveAll(postEsDTOList.subList(i, end));
        }
        log.info("IncSyncPostToEs end, total {}", total);
    }
}
