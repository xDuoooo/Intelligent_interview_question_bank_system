"use client";
import React, { useState } from "react";
import TagList from "@/components/TagList";
import MdViewer from "@/components/MdViewer";
import useAddUserSignInRecord from "@/hooks/useAddUserSignInRecord";
import { Sparkles, CheckCircle2, Heart } from "lucide-react";
import CommentSection from "@/components/CommentSection";
import { Button, message, Segmented, Space, Typography } from "antd";
import { doQuestionFavourUsingPost } from "@/api/questionFavourController";
import { addQuestionHistoryUsingPost } from "@/api/userQuestionHistoryController";
import QuestionRecommendPanel from "@/components/QuestionRecommendPanel";

const { Text } = Typography;

interface Props {
  question: API.QuestionVO;
}

/**
 * 题目卡片
 * @param props
 * @constructor
 */
const QuestionCard = (props: Props) => {
  const { question } = props;

  // 状态维护
  const [hasFavour, setHasFavour] = useState(question.hasFavour);
  const [favourNum, setFavourNum] = useState(question.favourNum || 0);

  // 签到
  useAddUserSignInRecord();

  // 收藏处理
  const onFavourClick = async () => {
    try {
      const res = await doQuestionFavourUsingPost({
        questionId: Number(question.id),
      });
      if (res.data) {
        setHasFavour(!hasFavour);
        setFavourNum(hasFavour ? favourNum - 1 : favourNum + 1);
        message.success(hasFavour ? "取消收藏成功" : "收藏成功");
      }
    } catch (error: any) {
      message.error("操作失败，" + error.message);
    }
  };

  // 掌握程度处理
  const onStatusChange = async (status: any) => {
    // 映射状态文本到数字
    const statusMap: Record<string, number> = {
      浏览: 0,
      掌握: 1,
      困难: 2,
    };
    try {
      await addQuestionHistoryUsingPost({
        questionId: Number(question.id),
        status: statusMap[status],
      });
      message.success("状态已更新");
    } catch (error: any) {
      message.error("更新状态失败，" + error.message);
    }
  };

  return (
    <div className="space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-700">
      {/* Question Main Card */}
      <section className="bg-white rounded-[2.5rem] border border-slate-100 shadow-2xl shadow-slate-200/50 p-8 sm:p-12 relative overflow-hidden">
        <div className="absolute top-0 right-0 p-6 opacity-10">
          <Sparkles className="h-20 w-20 text-primary" />
        </div>

        <div className="space-y-6 relative z-10">
          <div className="flex justify-between items-start gap-4">
            <h1 className="text-3xl sm:text-4xl font-black text-foreground leading-tight">
              {question.title}
            </h1>
            <Space direction="vertical" align="end">
              <Button
                type="text"
                size="large"
                icon={
                  <Heart
                    className={`h-6 w-6 ${hasFavour ? "fill-red-500 text-red-500" : "text-slate-400"}`}
                  />
                }
                onClick={onFavourClick}
              >
                {favourNum > 0 && <span className="ml-1 text-slate-500">{favourNum}</span>}
              </Button>
            </Space>
          </div>

          <div className="flex flex-wrap items-center gap-6">
            <TagList tagList={question.tagList} />
            <div className="flex items-center gap-2">
              <Text type="secondary" style={{ fontSize: 13 }}>学习状态：</Text>
              <Segmented
                options={["浏览", "掌握", "困难"]}
                onChange={onStatusChange}
              />
            </div>
          </div>

          <div className="prose prose-slate max-w-none prose-headings:font-black prose-headings:text-foreground/90 pt-4">
            <MdViewer value={question.content} />
          </div>
        </div>
      </section>

      {/* Answer Card */}
      <section className="bg-white rounded-[2.5rem] border border-slate-100 shadow-2xl shadow-slate-200/50 p-8 sm:p-12 relative overflow-hidden">
        <div className="flex items-center gap-3 mb-8 border-b border-slate-100 pb-6">
          <div className="h-10 w-10 rounded-xl bg-primary/10 flex items-center justify-center border border-primary/20">
            <CheckCircle2 className="h-6 w-6 text-primary" />
          </div>
          <h2 className="text-2xl font-black tracking-tight text-slate-900">
            推荐答案
          </h2>
        </div>

        <div className="prose prose-slate max-w-none prose-pre:bg-slate-50 prose-pre:border prose-pre:border-slate-200 antialiased">
          <MdViewer value={question.answer} />
        </div>
      </section>

      <QuestionRecommendPanel questionId={Number(question.id)} />

      {/* Discussion Section */}
      <CommentSection questionId={Number(question.id)} />
    </div>
  );
};

export default QuestionCard;
