package com.xduo.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 单题 AI 判题结果
 */
@Data
public class QuestionAnswerEvaluateVO implements Serializable {

    /**
     * 语音答题转写文本
     */
    private String transcript;

    /**
     * 总分（0 - 100）
     */
    private Integer score;

    /**
     * 评级
     */
    private String level;

    /**
     * 总评
     */
    private String summary;

    /**
     * 优点
     */
    private List<String> strengthList;

    /**
     * 改进建议
     */
    private List<String> improvementList;

    /**
     * 漏答知识点
     */
    private List<String> missedPointList;

    /**
     * 追问建议
     */
    private List<String> followUpQuestionList;

    /**
     * 参考建议
     */
    private String referenceSuggestion;

    /**
     * 分析来源：ai / heuristic
     */
    private String analysisSource;

    private static final long serialVersionUID = 1L;
}
