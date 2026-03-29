"use client";

import Link from "next/link";
import TagList from "@/components/TagList";
import UserAvatar from "@/components/UserAvatar";
import { CalendarClock, ChevronRight, Heart, ThumbsUp } from "lucide-react";

interface Props {
  postList: API.PostVO[];
}

export default function PostList({ postList = [] }: Props) {
  return (
    <div className="grid gap-4">
      {postList.map((item) => (
        <Link
          key={item.id}
          href={`/post/${item.id}`}
          className="group flex flex-col gap-4 rounded-[2rem] border border-slate-100 bg-white p-6 transition-all duration-300 hover:border-primary/30 hover:shadow-xl hover:shadow-primary/5"
        >
          <div className="flex items-start justify-between gap-4">
            <div className="min-w-0 flex-1">
              <div className="line-clamp-2 text-xl font-black text-slate-900 transition-colors group-hover:text-primary">
                {item.title}
              </div>
              <div className="mt-3 line-clamp-3 text-sm leading-6 text-slate-500">
                {item.content}
              </div>
            </div>
            <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl bg-slate-50 transition-colors group-hover:bg-primary/10">
              <ChevronRight className="h-5 w-5 text-slate-400 transition-transform group-hover:translate-x-0.5 group-hover:text-primary" />
            </div>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <TagList tagList={item.tagList} />
          </div>

          <div className="flex flex-wrap items-center justify-between gap-3 border-t border-slate-100 pt-4">
            <div className="flex items-center gap-3">
              <UserAvatar src={item.user?.userAvatar} name={item.user?.userName} size={34} />
              <div className="min-w-0">
                <div className="truncate text-sm font-bold text-slate-700">{item.user?.userName || "匿名用户"}</div>
                <div className="mt-1 flex items-center gap-1 text-xs text-slate-400">
                  <CalendarClock className="h-3.5 w-3.5" />
                  {item.createTime ? new Date(item.createTime).toLocaleDateString("zh-CN") : "刚刚"}
                </div>
              </div>
            </div>
            <div className="flex items-center gap-4 text-sm text-slate-400">
              <span className="inline-flex items-center gap-1">
                <ThumbsUp className="h-4 w-4" />
                {item.thumbNum || 0}
              </span>
              <span className="inline-flex items-center gap-1">
                <Heart className="h-4 w-4" />
                {item.favourNum || 0}
              </span>
            </div>
          </div>
        </Link>
      ))}
    </div>
  );
}
