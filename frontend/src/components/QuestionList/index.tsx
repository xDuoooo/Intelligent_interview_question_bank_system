"use client";
import React from "react";
import Link from "next/link";
import { ChevronRight, FileQuestion } from "lucide-react";
import TagList from "@/components/TagList";

interface Props {
  questionBankId?: number;
  questionList: API.QuestionVO[];
  cardTitle?: string;
}

/**
 * 题目列表组件
 * @param props
 * @constructor
 */
const QuestionList = (props: Props) => {
  const { questionList = [], cardTitle, questionBankId } = props;

  return (
    <div className="space-y-4">
      {cardTitle && (
        <h2 className="text-2xl font-black text-foreground mb-6 pl-4 border-l-4 border-primary">
          {cardTitle}
        </h2>
      )}
      {questionList.length ? (
        <div className="grid gap-3">
          {questionList.map((item) => (
            <Link
              key={item.id}
              href={
                questionBankId
                  ? `/bank/${questionBankId}/question/${item.id}`
                  : `/question/${item.id}`
              }
              className="group flex items-center justify-between p-5 rounded-3xl bg-white border border-slate-100 hover:border-primary/30 hover:shadow-xl hover:shadow-primary/5 transition-all duration-300"
            >
              <div className="flex flex-col gap-2 flex-1 min-w-0 pr-4">
                <span className="text-base font-bold text-foreground group-hover:text-primary transition-colors truncate">
                  {item.title}
                </span>
                <div className="scale-95 origin-left opacity-90 group-hover:opacity-100 transition-opacity">
                  <TagList tagList={item.tagList} />
                </div>
              </div>
              <div className="flex items-center justify-center h-10 w-10 rounded-2xl bg-slate-50 group-hover:bg-primary/10 transition-colors">
                <ChevronRight className="h-5 w-5 text-muted-foreground group-hover:text-primary transition-transform group-hover:translate-x-0.5" />
              </div>
            </Link>
          ))}
        </div>
      ) : (
        <div className="flex min-h-44 flex-col items-center justify-center rounded-[2rem] border border-dashed border-slate-200 bg-white/70 px-6 py-12 text-center">
          <div className="mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-slate-50 text-slate-400">
            <FileQuestion className="h-7 w-7" />
          </div>
          <div className="text-base font-black text-slate-800">暂无题目</div>
          <p className="mt-2 max-w-md text-sm leading-6 text-slate-500">当前没有可展示的题目，换个筛选条件或稍后再来看看。</p>
        </div>
      )}
    </div>
  );
};

export default QuestionList;

