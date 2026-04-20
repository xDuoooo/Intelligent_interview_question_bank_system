"use client";
import React, { useEffect, useRef, useState } from "react";
import dynamic from "next/dynamic";
import TagList from "@/components/TagList";
import useAddUserSignInRecord from "@/hooks/useAddUserSignInRecord";
import UserAvatar from "@/components/UserAvatar";
import UserProfileHoverCard from "@/components/UserProfileHoverCard";
import { Sparkles, CheckCircle2, Heart, CalendarClock } from "lucide-react";
import { Button, message, Segmented, Space, Tag, Typography } from "antd";
import { doQuestionFavourUsingPost } from "@/api/questionFavourController";
import { addQuestionHistoryUsingPost } from "@/api/userQuestionHistoryController";
import { QUESTION_DIFFICULTY_COLOR_MAP } from "@/constants/question";
import { buildApiUrl } from "@/libs/request";

const { Text } = Typography;

type QuestionStatusLabel = "浏览" | "掌握" | "困难";

const QUESTION_STATUS_OPTIONS: QuestionStatusLabel[] = ["浏览", "掌握", "困难"];
const QUESTION_STATUS_VALUE_MAP: Record<QuestionStatusLabel, number> = {
  浏览: 0,
  掌握: 1,
  困难: 2,
};

const MdViewer = dynamic(() => import("@/components/MdViewer"), {
  loading: () => (
    <div className="rounded-[2rem] border border-slate-100 bg-slate-50/60 p-6 text-sm text-slate-400">
      正在渲染内容...
    </div>
  ),
});

const QuestionRecommendPanel = dynamic(() => import("@/components/QuestionRecommendPanel"), {
  loading: () => (
    <section className="rounded-[2rem] border border-slate-100 bg-white p-8 text-sm text-slate-400 shadow-xl shadow-slate-200/30">
      正在加载推荐内容...
    </section>
  ),
});

const CommentSection = dynamic(() => import("@/components/CommentSection"), {
  loading: () => (
    <section className="rounded-[2rem] border border-slate-100 bg-white p-8 text-sm text-slate-400 shadow-xl shadow-slate-200/30">
      正在加载评论区...
    </section>
  ),
});

const QuestionNotePanel = dynamic(() => import("@/components/QuestionNotePanel"), {
  loading: () => (
    <section className="rounded-[2rem] border border-slate-100 bg-white p-8 text-sm text-slate-400 shadow-xl shadow-slate-200/30">
      正在加载个人笔记...
    </section>
  ),
});

const QuestionAnswerEvaluator = dynamic(() => import("@/components/QuestionAnswerEvaluator"), {
  loading: () => (
    <section className="rounded-[2rem] border border-slate-100 bg-white p-8 text-sm text-slate-400 shadow-xl shadow-slate-200/30">
      正在加载 AI 判题...
    </section>
  ),
});

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
  const [hasFavour, setHasFavour] = useState(Boolean(question.hasFavour));
  const [favourNum, setFavourNum] = useState(question.favourNum || 0);
  const [favourLoading, setFavourLoading] = useState(false);
  const [questionStatus, setQuestionStatus] = useState<QuestionStatusLabel>("浏览");
  const [statusLoading, setStatusLoading] = useState(false);
  const studyStartTimeRef = useRef(Date.now());
  const hasReportedStudyRef = useRef(false);

  // 签到
  useAddUserSignInRecord();

  // 收藏处理
  const onFavourClick = async () => {
    if (favourLoading) {
      return;
    }
    setFavourLoading(true);
    try {
      const res = await doQuestionFavourUsingPost({
        questionId: question.id,
      });
      const delta = Number(res.data || 0);
      if (delta !== 0) {
        setHasFavour(delta > 0);
        setFavourNum((prev) => Math.max(0, prev + delta));
        message.success(delta > 0 ? "收藏成功" : "取消收藏成功");
      }
    } catch (error: any) {
      message.error("操作失败，" + error.message);
    } finally {
      setFavourLoading(false);
    }
  };

  // 掌握程度处理
  const onStatusChange = async (status: string | number) => {
    const nextStatus = status as QuestionStatusLabel;
    const previousStatus = questionStatus;
    setQuestionStatus(nextStatus);
    setStatusLoading(true);
    try {
      await addQuestionHistoryUsingPost({
        questionId: question.id,
        status: QUESTION_STATUS_VALUE_MAP[nextStatus],
      });
      message.success("状态已更新");
    } catch (error: any) {
      setQuestionStatus(previousStatus);
      message.error("更新状态失败，" + error.message);
    } finally {
      setStatusLoading(false);
    }
  };

  useEffect(() => {
    setHasFavour(Boolean(question.hasFavour));
    setFavourNum(question.favourNum || 0);
    setQuestionStatus("浏览");
  }, [question.favourNum, question.hasFavour, question.id]);

  useEffect(() => {
    studyStartTimeRef.current = Date.now();
    hasReportedStudyRef.current = false;

    const reportStudySession = () => {
      if (hasReportedStudyRef.current) {
        return;
      }
      const durationSeconds = Math.floor((Date.now() - studyStartTimeRef.current) / 1000);
      if (durationSeconds < 10) {
        return;
      }
      hasReportedStudyRef.current = true;
      const payload = JSON.stringify({
        questionId: question.id,
        durationSeconds,
      });
      try {
        fetch(buildApiUrl("/api/user_question_history/session/report"), {
          method: "POST",
          credentials: "include",
          keepalive: true,
          headers: {
            "Content-Type": "application/json",
          },
          body: payload,
        }).catch(() => undefined);
      } catch {
        // ignore silent study session reporting errors
      }
    };

    window.addEventListener("pagehide", reportStudySession);
    return () => {
      reportStudySession();
      window.removeEventListener("pagehide", reportStudySession);
    };
  }, [question.id]);

  const authorCard = question.user ? (
    <div className="inline-flex items-center gap-3 rounded-2xl border border-slate-100 bg-slate-50/80 px-4 py-3 transition-all hover:border-primary/20 hover:bg-white">
      <UserAvatar
        src={question.user.userAvatar}
        name={question.user.userName}
        size={38}
      />
      <div className="min-w-0 text-left">
        <div className="truncate text-sm font-black text-slate-800 transition-colors hover:text-primary">
          {question.user.userName || "匿名用户"}
        </div>
        <div className="mt-1 flex flex-wrap items-center gap-2 text-xs text-slate-400">
          <span>
            {question.user.userRole === "admin"
              ? "管理员"
              : question.user.userRole === "deleted"
                ? "原题目贡献者"
                : "题目贡献者"}
          </span>
          {question.createTime ? (
            <span className="inline-flex items-center gap-1">
              <CalendarClock className="h-3.5 w-3.5" />
              {new Date(question.createTime).toLocaleDateString("zh-CN")}
            </span>
          ) : null}
        </div>
      </div>
    </div>
  ) : null;

  return (
    <div className="space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-700">
      {/* Question Main Card */}
      <section className="bg-white rounded-[2.5rem] border border-slate-100 shadow-2xl shadow-slate-200/50 p-8 sm:p-12 relative overflow-hidden">
        <div className="absolute top-0 right-0 p-6 opacity-10">
          <Sparkles className="h-20 w-20 text-primary" />
        </div>

        <div className="space-y-6 relative z-10">
          <div className="flex justify-between items-start gap-4">
            <div className="space-y-4">
              <h1 className="text-3xl sm:text-4xl font-black text-foreground leading-tight">
                {question.title}
              </h1>
              {question.user?.id ? (
                <UserProfileHoverCard user={question.user} placement="bottomLeft">
                  {authorCard}
                </UserProfileHoverCard>
              ) : authorCard}
            </div>
            <Space direction="vertical" align="end">
              <Button
                type="text"
                size="large"
                loading={favourLoading}
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
            <div className="flex flex-wrap items-center gap-3">
              <TagList tagList={question.tagList} />
              {question.difficulty ? (
                <Tag color={QUESTION_DIFFICULTY_COLOR_MAP[question.difficulty] || "default"} className="rounded-full px-3 py-1">
                  难度：{question.difficulty}
                </Tag>
              ) : null}
            </div>
            <div className="flex items-center gap-2">
              <Text type="secondary" style={{ fontSize: 13 }}>学习状态：</Text>
              <Segmented
                options={QUESTION_STATUS_OPTIONS}
                value={questionStatus}
                disabled={statusLoading}
                onChange={onStatusChange}
              />
            </div>
          </div>

          <div className="prose prose-slate max-w-none prose-headings:font-black prose-headings:text-foreground/90 pt-4">
            <MdViewer value={question.content} />
          </div>
        </div>
      </section>

      <QuestionAnswerEvaluator
        questionId={question.id as string | number}
        questionTitle={question.title}
      />

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

      <QuestionNotePanel questionId={question.id as string | number} />

      <QuestionRecommendPanel questionId={question.id as string | number} />

      {/* Discussion Section */}
      <CommentSection questionId={question.id as string | number} />
    </div>
  );
};

export default QuestionCard;
