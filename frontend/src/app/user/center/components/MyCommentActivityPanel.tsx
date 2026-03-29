import React, { useMemo, useState } from "react";
import { Heart, MessageCircle } from "lucide-react";
import MyLikedCommentList from "./MyLikedCommentList";
import MyReplyCommentList from "./MyReplyCommentList";

type CommentActivityTabKey = "liked" | "replied";

const activityTabs: Array<{
  key: CommentActivityTabKey;
  label: string;
  icon: React.ReactNode;
  description: string;
}> = [
  {
    key: "liked",
    label: "我点赞过的",
    icon: <Heart size={16} />,
    description: "收纳你曾经点过赞的评论，方便回看那些有共鸣的解题思路。",
  },
  {
    key: "replied",
    label: "我回复过的",
    icon: <MessageCircle size={16} />,
    description: "集中查看你参与过互动的评论，继续追踪讨论上下文。",
  },
];

export default function MyCommentActivityPanel() {
  const [activeTab, setActiveTab] = useState<CommentActivityTabKey>("liked");

  const currentTab = useMemo(
    () => activityTabs.find((item) => item.key === activeTab) || activityTabs[0],
    [activeTab],
  );

  return (
    <div className="space-y-6">
      <div className="rounded-[2rem] border border-slate-100 bg-white p-6 shadow-xl shadow-slate-200/30">
        <div className="text-xs font-black uppercase tracking-[0.2em] text-primary">Comment Trail</div>
        <h3 className="mt-2 text-2xl font-black text-slate-900">评论足迹</h3>
        <p className="mt-2 text-sm leading-7 text-slate-500">
          这里会统一记录你在题目评论区里的互动，包括点赞过的评论，以及你主动回复过的内容。
        </p>

        <div className="mt-6 flex flex-wrap gap-3">
          {activityTabs.map((item) => {
            const active = item.key === activeTab;
            return (
              <button
                key={item.key}
                type="button"
                onClick={() => setActiveTab(item.key)}
                className={`inline-flex items-center gap-2 rounded-full px-4 py-2.5 text-sm font-black transition ${
                  active
                    ? "bg-primary text-white shadow-lg shadow-primary/20"
                    : "border border-slate-200 bg-white text-slate-500 hover:border-primary/30 hover:text-primary"
                }`}
              >
                {item.icon}
                {item.label}
              </button>
            );
          })}
        </div>

        <div className="mt-5 rounded-[1.5rem] border border-slate-200 bg-slate-50/70 px-4 py-4 text-sm font-medium leading-7 text-slate-500">
          <span className="font-black text-slate-700">{currentTab.label}</span>
          <span className="ml-2">{currentTab.description}</span>
        </div>
      </div>

      {activeTab === "liked" && <MyLikedCommentList />}
      {activeTab === "replied" && <MyReplyCommentList />}
    </div>
  );
}
