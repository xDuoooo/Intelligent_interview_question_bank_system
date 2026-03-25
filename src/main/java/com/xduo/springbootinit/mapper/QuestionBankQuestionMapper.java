package com.xduo.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xduo.springbootinit.model.entity.QuestionBankQuestion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 针对表【question_bank_question(题库题目)】的数据库操作Mapper
 */
@Mapper
public interface QuestionBankQuestionMapper extends BaseMapper<QuestionBankQuestion> {

}
