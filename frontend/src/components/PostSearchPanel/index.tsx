"use client";

import { FormEvent, useMemo, useState } from "react";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { Search, Sparkles, X } from "lucide-react";
import { cn } from "@/lib/utils";

interface Props {
  suggestedTags?: string[];
}

function buildQueryString(params: URLSearchParams) {
  const queryString = params.toString();
  return queryString ? `?${queryString}` : "";
}

/**
 * 社区搜索面板
 */
export default function PostSearchPanel({ suggestedTags = [] }: Props) {
  const router = useRouter();
  const pathname = usePathname() || "/posts";
  const searchParams = useSearchParams();
  const [keyword, setKeyword] = useState(searchParams?.get("q") || "");

  const activeTag = searchParams?.get("tag") || "";
  const activeFeatured = searchParams?.get("featured") === "1";
  const hasFilter = Boolean(keyword.trim() || activeTag || activeFeatured);

  const normalizedSuggestedTags = useMemo(
    () => Array.from(new Set(suggestedTags.filter(Boolean))).slice(0, 8),
    [suggestedTags],
  );

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const params = new URLSearchParams(searchParams?.toString() || "");
    const nextKeyword = keyword.trim();
    if (nextKeyword) {
      params.set("q", nextKeyword);
    } else {
      params.delete("q");
    }
    params.delete("page");
    router.push(`${pathname}${buildQueryString(params)}`);
  };

  const handleToggleTag = (tag: string) => {
    const params = new URLSearchParams(searchParams?.toString() || "");
    if (activeTag === tag) {
      params.delete("tag");
    } else {
      params.set("tag", tag);
    }
    params.delete("page");
    router.push(`${pathname}${buildQueryString(params)}`);
  };

  const handleToggleFeatured = () => {
    const params = new URLSearchParams(searchParams?.toString() || "");
    if (activeFeatured) {
      params.delete("featured");
    } else {
      params.set("featured", "1");
    }
    params.delete("page");
    router.push(`${pathname}${buildQueryString(params)}`);
  };

  const handleReset = () => {
    setKeyword("");
    router.push(pathname);
  };

  return (
    <div className="space-y-4 rounded-[2.25rem] border border-slate-100 bg-white/80 p-5 shadow-xl shadow-slate-200/30 backdrop-blur">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <div className="text-sm font-black text-slate-900">搜索社区帖子</div>
          <div className="mt-1 text-xs leading-6 text-slate-500">
            支持按关键词、标签和精选状态筛选经验帖。
          </div>
        </div>
        {hasFilter ? (
          <button
            onClick={handleReset}
            className="inline-flex items-center gap-1 rounded-full border border-slate-200 bg-white px-3 py-1.5 text-xs font-bold text-slate-500 transition-colors hover:border-primary hover:text-primary"
          >
            <X className="h-3.5 w-3.5" />
            清空筛选
          </button>
        ) : null}
      </div>

      <form onSubmit={handleSubmit} className="flex flex-col gap-3 lg:flex-row">
        <label className="group flex min-w-0 flex-1 items-center gap-3 rounded-2xl border border-slate-200 bg-slate-50/70 px-4 py-3 transition-all focus-within:border-primary focus-within:bg-white focus-within:ring-4 focus-within:ring-primary/5">
          <Search className="h-4 w-4 text-slate-400 transition-colors group-focus-within:text-primary" />
          <input
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
            placeholder="搜索面经、项目复盘、系统设计经验..."
            className="w-full bg-transparent text-sm text-slate-700 outline-none placeholder:text-slate-400"
          />
        </label>
        <button
          type="submit"
          className="inline-flex h-12 items-center justify-center gap-2 rounded-2xl bg-primary px-6 text-sm font-black text-primary-foreground shadow-lg shadow-primary/20 transition-all hover:bg-primary/90"
        >
          <Search className="h-4 w-4" />
          搜索帖子
        </button>
      </form>

      <div className="flex flex-wrap items-center gap-2">
        <button
          onClick={handleToggleFeatured}
          className={cn(
            "inline-flex items-center gap-1 rounded-full border px-3 py-1.5 text-xs font-bold transition-all",
            activeFeatured
              ? "border-primary bg-primary/10 text-primary"
              : "border-slate-200 bg-white text-slate-500 hover:border-primary hover:text-primary",
          )}
        >
          <Sparkles className="h-3.5 w-3.5" />
          只看精选
        </button>

        {normalizedSuggestedTags.map((tag) => (
          <button
            key={tag}
            onClick={() => handleToggleTag(tag)}
            className={cn(
              "rounded-full border px-3 py-1.5 text-xs font-bold transition-all",
              activeTag === tag
                ? "border-primary bg-primary/10 text-primary"
                : "border-slate-200 bg-white text-slate-500 hover:border-primary hover:text-primary",
            )}
          >
            #{tag}
          </button>
        ))}
      </div>
    </div>
  );
}
