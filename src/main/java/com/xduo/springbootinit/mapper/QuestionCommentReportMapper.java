package com.xduo.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xduo.springbootinit.model.entity.QuestionCommentReport;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论举报 Mapper
 */
@Mapper
public interface QuestionCommentReportMapper extends BaseMapper<QuestionCommentReport> {
}
