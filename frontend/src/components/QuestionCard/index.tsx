"use client";
import React from "react";
import TagList from "@/components/TagList";
import MdViewer from "@/components/MdViewer";
import useAddUserSignInRecord from "@/hooks/useAddUserSignInRecord";
import { Sparkles, CheckCircle2 } from "lucide-react";

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

  // 签到
  useAddUserSignInRecord();

  return (
    <div className="space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-700">
      {/* Question Main Card */}
      <section className="bg-white rounded-[2.5rem] border border-slate-100 shadow-2xl shadow-slate-200/50 p-8 sm:p-12 relative overflow-hidden">
        <div className="absolute top-0 right-0 p-6 opacity-10">
           <Sparkles className="h-20 w-20 text-primary" />
        </div>
        
        <div className="space-y-6 relative z-10">
          <h1 className="text-3xl sm:text-4xl font-black text-foreground leading-tight">
            {question.title}
          </h1>
          
          <TagList tagList={question.tagList} />
          
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
           <h2 className="text-2xl font-black tracking-tight text-slate-900">推荐答案</h2>
        </div>
        
        <div className="prose prose-slate max-w-none prose-pre:bg-slate-50 prose-pre:border prose-pre:border-slate-200 antialiased">
           <MdViewer value={question.answer} />
        </div>
      </section>
    </div>
  );
};

export default QuestionCard;

