import React from "react";
import { getQuestionVoByIdUsingGet } from "@/api/questionController";
import QuestionCard from "@/components/QuestionCard";
import Link from "next/link";
import { Sparkles } from "lucide-react";
import { headers } from "next/headers";

/**
 * 题目详情页
 * @constructor
 */
export default async function QuestionPage({ params }: { params: { questionId: string } }) {
  const { questionId } = params;

  // 获取题目详情
  let question: API.QuestionVO | undefined = undefined;
  let isNotLogin = false;
  
  try {
    const res = (await getQuestionVoByIdUsingGet({
      id: Number(questionId),
    }, {
      headers: {
        cookie: headers().get("cookie") || "",
      }
    })) as unknown as API.BaseResponseQuestionVO_;
    
    if (res.code === 40100) {
      isNotLogin = true;
    } else {
      question = res.data;
    }
  } catch (e) {
    console.error("获取题目详情失败", e);
  }
  
  // 错误处理
  if (!question) {
    const errorTitle = isNotLogin ? "您还没有登录" : "获取题目详情失败";
    const errorDesc = isNotLogin ? "请先登录后再查看题目详情" : "该题目可能已被移除或权限不足";
    
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] space-y-4">
        <div className="h-20 w-20 rounded-full bg-red-50 flex items-center justify-center border border-red-100">
           <span className="text-4xl text-primary"><Sparkles className="h-10 w-10" /></span>
        </div>
        <h1 className="text-xl font-bold text-foreground">{errorTitle}</h1>
        <p className="text-muted-foreground">{errorDesc}</p>
        <Link 
          href={isNotLogin ? `/user/login?redirect=/question/${questionId}` : "/questions"} 
          className="h-11 px-8 rounded-full bg-primary text-primary-foreground font-bold flex items-center justify-center transition-all shadow-lg shadow-primary/20 hover:scale-105 active:scale-95"
        >
          {isNotLogin ? "立即登录" : "返回题目列表"}
        </Link>
      </div>
    );
  }

  return (
    <div id="questionPage" className="max-w-5xl mx-auto pb-20">
      <QuestionCard question={question} />
    </div>
  );
}

