import React from "react";
import { getQuestionVoByIdUsingGet } from "@/api/questionController";
import QuestionCard from "@/components/QuestionCard";
import Link from "next/link";
import { Sparkles } from "lucide-react";

/**
 * 题目详情页
 * @constructor
 */
export default async function QuestionPage({ params }: { params: { questionId: string } }) {
  const { questionId } = params;

  // 获取题目详情
  let question: API.QuestionVO | undefined = undefined;
  try {
    const res = (await getQuestionVoByIdUsingGet({
      id: Number(questionId),
    })) as unknown as API.BaseResponseQuestionVO_;
    question = res.data;
  } catch (e) {
    console.error("获取题目详情失败", e);
  }
  
  // 错误处理
  if (!question) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] space-y-4">
        <div className="h-20 w-20 rounded-full bg-red-50 flex items-center justify-center border border-red-100">
           <span className="text-4xl text-primary"><Sparkles className="h-10 w-10" /></span>
        </div>
        <h1 className="text-xl font-bold text-foreground">获取题目详情失败</h1>
        <p className="text-muted-foreground">该题目可能已被移除或权限不足</p>
        <Link href="/questions" className="text-primary font-bold hover:underline">返回题目列表</Link>
      </div>
    );
  }

  return (
    <div id="questionPage" className="max-w-5xl mx-auto pb-20">
      <QuestionCard question={question} />
    </div>
  );
}

