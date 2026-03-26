"use client";
import React from "react";
import Link from "next/link";
import { ChevronRight } from "lucide-react";
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
    </div>
  );
};

export default QuestionList;

