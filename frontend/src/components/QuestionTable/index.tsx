"use client";
import React, { useState, useEffect } from "react";
import { listQuestionVoByPageUsingPost } from "@/api/questionController";
import TagList from "@/components/TagList";
import Link from "next/link";
import { Search, ChevronLeft, ChevronRight, Loader2, Filter, Sparkles } from "lucide-react";
import { cn } from "@/lib/utils";
import { Table, Tag, Space, Button } from "antd";

interface Props {
  // 默认值（用于展示服务端渲染的数据）
  defaultQuestionList?: API.QuestionVO[];
  defaultTotal?: number;
  // 默认搜索条件
  defaultSearchParams?: API.QuestionQueryRequest;
}

/**
 * 题目表格组件
 * @constructor
 */
const QuestionTable: React.FC<Props> = (props) => {
  const { defaultQuestionList, defaultTotal, defaultSearchParams = {} } = props;
  
  const [questionList, setQuestionList] = useState<API.QuestionVO[]>(defaultQuestionList || []);
  const [total, setTotal] = useState<number>(defaultTotal || 0);
  const [params, setParams] = useState<API.QuestionQueryRequest>({
    current: 1,
    pageSize: 12,
    ...defaultSearchParams
  });
  const [loading, setLoading] = useState(false);
  const [searchText, setSearchText] = useState(defaultSearchParams.searchText || "");

  // 这里的 fetchData 作为一个通用请求函数
  const fetchData = async (currentParams = params) => {
    setLoading(true);
    try {
      const res = (await listQuestionVoByPageUsingPost({
        ...currentParams,
        searchText: searchText || undefined,
      })) as unknown as API.BaseResponsePageQuestionVO_;
      if (res.data) {
        setQuestionList(res.data.records || []);
        setTotal(res.data.total || 0);
      }
    } catch (e) {
      console.error("获取题目失败", e);
    } finally {
      setLoading(false);
    }
  };

  // 标题搜索
  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    const newParams = { ...params, current: 1 };
    setParams(newParams);
    fetchData(newParams);
  };

  // 分页点击
  const handlePageChange = (newPage: number) => {
    const newParams = { ...params, current: newPage };
    setParams(newParams);
    fetchData(newParams);
  };

  return (
    <div className="space-y-10">
      {/* Search Bar */}
      <form onSubmit={handleSearch} className="relative group max-w-2xl mx-auto">
        <Search className="absolute left-6 top-1/2 -translate-y-1/2 h-6 w-6 text-muted-foreground group-focus-within:text-primary transition-colors" />
        <input
          type="text"
          placeholder="搜索您感兴趣的面试题..."
          className="w-full h-16 pl-16 pr-36 rounded-[2rem] bg-white border border-slate-200 shadow-2xl shadow-slate-200/40 focus:border-primary focus:ring-8 focus:ring-primary/5 outline-none transition-all font-bold text-lg"
          value={searchText}
          onChange={(e) => setSearchText(e.target.value)}
        />
        <button 
          type="submit"
          disabled={loading}
          className="absolute right-3 top-1/2 -translate-y-1/2 h-10 px-8 rounded-2xl bg-primary text-primary-foreground font-black text-sm hover:scale-105 active:scale-95 transition-all shadow-lg shadow-primary/25 disabled:opacity-50"
        >
          {loading ? "搜索中..." : "实时搜索"}
        </button>
      </form>

      {/* Grid List */}
      <div className="relative min-h-[400px]">
        {loading && (
          <div className="absolute inset-0 z-20 bg-white/40 backdrop-blur-[1px] flex items-center justify-center rounded-[3rem]">
            <Loader2 className="h-12 w-12 animate-spin text-primary" />
          </div>
        )}

        <div className="grid gap-5">
          {questionList.map((item) => (
             <Link
                key={item.id}
                href={`/question/${item.id}`}
                className="group flex flex-col sm:flex-row sm:items-center justify-between p-6 sm:p-8 rounded-[2.5rem] bg-white border border-slate-100/80 hover:border-primary/40 hover:shadow-2xl hover:shadow-primary/5 transition-all duration-500"
              >
                <div className="flex flex-col gap-3 flex-1 min-w-0 pr-6">
                  <h3 className="text-xl sm:text-2xl font-black text-foreground group-hover:text-primary transition-colors truncate">
                    {item.title}
                  </h3>
                  <div className="scale-100 origin-left">
                     <TagList tagList={item.tagList} />
                  </div>
                </div>
                <div className="mt-6 sm:mt-0 flex items-center gap-5">
                   <div className="flex items-center gap-2 px-4 py-2 rounded-2xl bg-primary/5 border border-primary/10">
                      <Filter className="h-4 w-4 text-primary" />
                      <span className="text-xs font-bold text-primary">实时真题</span>
                   </div>
                   <div className="h-12 w-12 rounded-2xl bg-slate-50 group-hover:bg-primary text-slate-400 group-hover:text-white flex items-center justify-center transition-all duration-300 shadow-inner group-hover:shadow-lg group-hover:shadow-primary/30">
                      <ChevronRight className="h-6 w-6 group-hover:translate-x-0.5 transition-transform" />
                   </div>
                </div>
              </Link>
          ))}
          
          {questionList.length === 0 && !loading && (
            <div className="py-24 text-center space-y-6 bg-white/50 rounded-[3rem] border-2 border-dashed border-slate-200">
               <div className="text-7xl opacity-20 filter grayscale text-primary"><Sparkles className="h-20 w-20" /></div>
               <div className="space-y-1">
                 <p className="text-xl font-black text-foreground">没有找到相关题目</p>
                 <p className="text-sm font-medium text-muted-foreground">换个关键词搜索一下，或许会有惊喜</p>
               </div>
            </div>
          )}
        </div>
      </div>

      {/* Modern Pagination */}
      {total > (params.pageSize || 12) && (
        <div className="flex items-center justify-center gap-6 pt-12">
           <button 
             onClick={() => handlePageChange((params.current || 1) - 1)}
             disabled={(params.current || 1) === 1 || loading}
             className="h-14 w-14 rounded-2xl bg-white border border-slate-200 flex items-center justify-center hover:border-primary hover:text-primary disabled:opacity-30 disabled:hover:border-slate-200 transition-all shadow-xl shadow-slate-200/50 active:scale-90"
           >
             <ChevronLeft className="h-6 w-6" />
           </button>
           
           <div className="flex items-center gap-3">
              <span className="font-black text-2xl text-foreground">
                {params.current}
              </span>
              <span className="text-slate-300 font-bold text-xl">/</span>
              <span className="text-muted-foreground font-black text-xl">
                {Math.ceil(total / (params.pageSize || 12))}
              </span>
           </div>

           <button 
             onClick={() => handlePageChange((params.current || 1) + 1)}
             disabled={(params.current || 1) * (params.pageSize || 12) >= total || loading}
             className="h-14 w-14 rounded-2xl bg-white border border-slate-200 flex items-center justify-center hover:border-primary hover:text-primary disabled:opacity-30 disabled:hover:border-slate-200 transition-all shadow-xl shadow-slate-200/50 active:scale-90"
           >
             <ChevronRight className="h-6 w-6" />
           </button>
        </div>
      )}
    </div>
  );
};

export default QuestionTable;