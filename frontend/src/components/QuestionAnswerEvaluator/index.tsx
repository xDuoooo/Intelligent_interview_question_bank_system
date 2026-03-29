"use client";

import React, { useMemo, useState } from "react";
import { useSelector } from "react-redux";
import { Button, Card, Empty, Input, Space, Tag, Typography, message } from "antd";
import { Lightbulb, MessageSquare, Sparkles, Target } from "lucide-react";
import { RootState } from "@/stores";
import { evaluateQuestionAnswerUsingPost } from "@/api/questionController";

const { Paragraph, Text, Title } = Typography;

interface Props {
  questionId: number;
  questionTitle?: string;
}

function renderLevelTagColor(level?: string) {
  if (level === "优秀") {
    return "success";
  }
  if (level === "良好") {
    return "processing";
  }
  if (level === "合格") {
    return "warning";
  }
  return "default";
}

function SectionList({
  title,
  items,
  icon,
}: {
  title: string;
  items?: string[];
  icon: React.ReactNode;
}) {
  if (!items?.length) {
    return null;
  }
  return (
    <div className="rounded-[1.75rem] border border-slate-100 bg-slate-50/70 p-5">
      <div className="flex items-center gap-2 text-sm font-black text-slate-700">
        {icon}
        {title}
      </div>
      <div className="mt-4 space-y-3">
        {items.map((item, index) => (
          <div
            key={`${title}-${index}`}
            className="flex items-start gap-3 text-sm leading-6 text-slate-600"
          >
            <span className="mt-1 inline-flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-white text-[11px] font-black text-primary shadow-sm">
              {index + 1}
            </span>
            <span>{item}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

/**
 * 单题 AI 判题面板
 */
export default function QuestionAnswerEvaluator({ questionId, questionTitle }: Props) {
  const loginUser = useSelector((state: RootState) => state.loginUser);
  const [answerContent, setAnswerContent] = useState("");
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<API.QuestionAnswerEvaluateVO>();

  const isLogin = useMemo(() => Boolean(loginUser?.id), [loginUser?.id]);

  const handleEvaluate = async () => {
    const trimmedAnswer = answerContent.trim();
    if (!trimmedAnswer) {
      message.warning("先输入你的作答内容，再让 AI 帮你判题");
      return;
    }
    setLoading(true);
    try {
      const res = await evaluateQuestionAnswerUsingPost({
        questionId,
        answerContent: trimmedAnswer,
      });
      setResult(res.data);
      message.success("判题完成，可以开始复盘了");
    } catch (error: any) {
      message.error("判题失败：" + (error?.message || "请稍后重试"));
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card
      className="rounded-[2.5rem] border border-slate-100 shadow-2xl shadow-slate-200/40"
      bodyStyle={{ padding: "2rem 2rem 1.75rem" }}
    >
      <div className="flex flex-col gap-6">
        <div className="flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between">
          <div>
            <div className="flex items-center gap-2 text-lg font-black text-slate-800">
              <Sparkles className="h-5 w-5 text-primary" />
              AI 单题判题
            </div>
            <Paragraph className="!mb-0 !mt-2 text-slate-500">
              建议先自己作答，再让 AI 从覆盖度、结构化表达和技术深度三个角度帮你打分和追问。
            </Paragraph>
          </div>
          <Tag color="gold" className="m-0 rounded-full px-3 py-1">
            先作答，再看参考答案
          </Tag>
        </div>

        {!isLogin ? (
          <div className="rounded-[1.75rem] border border-dashed border-slate-200 bg-slate-50/70 px-6 py-10 text-center">
            <Empty
              description="登录后可以体验 AI 单题判题与追问反馈"
              image={Empty.PRESENTED_IMAGE_SIMPLE}
            />
          </div>
        ) : (
          <>
            <div className="rounded-[1.75rem] border border-slate-100 bg-slate-50/60 p-4 text-sm leading-6 text-slate-500">
              <div className="font-bold text-slate-700">作答建议</div>
              <div className="mt-2">
                先用 3 到 5 句话回答“核心原理是什么、怎么做、适用在什么场景、有什么取舍”，这样 AI 给出的反馈会更准确。
                {questionTitle ? ` 当前题目：${questionTitle}` : ""}
              </div>
            </div>

            <Input.TextArea
              value={answerContent}
              onChange={(event) => setAnswerContent(event.target.value)}
              rows={8}
              maxLength={5000}
              showCount
              placeholder="先用自己的语言写出作答内容，再让 AI 给你评分和改进建议..."
              className="rounded-[2rem] bg-slate-50/70"
            />

            <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <Text className="text-sm text-slate-400">
                想更像真实面试，可以先按“首先 / 然后 / 最后”分点作答，再补充一个实际场景。
              </Text>
              <Space wrap>
                <Button
                  onClick={() => {
                    setAnswerContent("");
                    setResult(undefined);
                  }}
                  className="rounded-2xl"
                >
                  清空作答
                </Button>
                <Button
                  type="primary"
                  onClick={handleEvaluate}
                  loading={loading}
                  className="rounded-2xl"
                >
                  开始判题
                </Button>
              </Space>
            </div>

            {result ? (
              <div className="space-y-5 rounded-[2rem] border border-primary/10 bg-primary/[0.03] p-5">
                <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
                  <div>
                    <div className="text-sm font-bold uppercase tracking-[0.2em] text-primary/70">
                      AI Feedback
                    </div>
                    <Title level={3} className="!mb-2 !mt-2 !text-slate-900">
                      {result.summary || "这次作答已经有基础了，接下来重点把关键点讲得更完整。"}
                    </Title>
                    <Space wrap>
                      <Tag color={renderLevelTagColor(result.level)} className="rounded-full px-3 py-1">
                        {result.level || "待加强"}
                      </Tag>
                      <Tag color={result.analysisSource === "ai" ? "processing" : "default"} className="rounded-full px-3 py-1">
                        {result.analysisSource === "ai" ? "AI 结构化判题" : "本地兜底评估"}
                      </Tag>
                    </Space>
                  </div>
                  <div className="min-w-[148px] rounded-[1.75rem] border border-slate-100 bg-white px-5 py-4 text-center shadow-sm">
                    <div className="text-xs font-bold uppercase tracking-[0.2em] text-slate-400">
                      综合分
                    </div>
                    <div className="mt-2 text-4xl font-black text-slate-900">
                      {result.score ?? 0}
                    </div>
                    <div className="mt-1 text-sm text-slate-400">/ 100</div>
                  </div>
                </div>

                <div className="grid gap-4 lg:grid-cols-2">
                  <SectionList
                    title="回答亮点"
                    items={result.strengthList}
                    icon={<Sparkles className="h-4 w-4 text-emerald-500" />}
                  />
                  <SectionList
                    title="改进建议"
                    items={result.improvementList}
                    icon={<Lightbulb className="h-4 w-4 text-amber-500" />}
                  />
                  <SectionList
                    title="漏答点"
                    items={result.missedPointList}
                    icon={<Target className="h-4 w-4 text-rose-500" />}
                  />
                  <SectionList
                    title="继续追问"
                    items={result.followUpQuestionList}
                    icon={<MessageSquare className="h-4 w-4 text-sky-500" />}
                  />
                </div>

                {result.referenceSuggestion ? (
                  <div className="rounded-[1.75rem] border border-dashed border-primary/20 bg-white/70 px-5 py-4 text-sm leading-6 text-slate-600">
                    <span className="font-black text-slate-800">复盘建议：</span>
                    {result.referenceSuggestion}
                  </div>
                ) : null}
              </div>
            ) : null}
          </>
        )}
      </div>
    </Card>
  );
}
