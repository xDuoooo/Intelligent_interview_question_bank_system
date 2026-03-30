import React, { useMemo, useState } from "react";
import { Button } from "antd";
import { Heart, MessageSquareReply, MessageSquareText, ThumbsUp } from "lucide-react";
import Link from "next/link";
import MyFavourPostList from "./MyFavourPostList";
import MyPostList from "./MyPostList";
import MyReplyPostCommentList from "./MyReplyPostCommentList";
import MyThumbPostList from "./MyThumbPostList";

type CommunityTabKey = "published" | "reply" | "favour" | "thumb";

const communityTabs: Array<{
  key: CommunityTabKey;
  label: string;
  icon: React.ReactNode;
  description: string;
}> = [
  {
    key: "published",
    label: "我发布的",
    icon: <MessageSquareText size={16} />,
    description: "查看自己发布过的经验帖、审核状态和后续编辑入口。",
  },
  {
    key: "reply",
    label: "我回复的",
    icon: <MessageSquareReply size={16} />,
    description: "把你在社区里回复过的内容集中起来，方便回看互动上下文和审核状态。",
  },
  {
    key: "favour",
    label: "我收藏的",
    icon: <Heart size={16} />,
    description: "把你收藏过的优质经验帖集中收在一起，方便二次回看。",
  },
  {
    key: "thumb",
    label: "我点赞的",
    icon: <ThumbsUp size={16} />,
    description: "保留你点过赞的帖子记录，回头找内容会更快。",
  },
];

export default function MyCommunityPostPanel() {
  const [activeTab, setActiveTab] = useState<CommunityTabKey>("published");

  const currentTab = useMemo(
    () => communityTabs.find((item) => item.key === activeTab) || communityTabs[0],
    [activeTab],
  );

  return (
    <div className="space-y-6">
      <div className="rounded-[2rem] border border-slate-100 bg-white p-6 shadow-xl shadow-slate-200/30">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
          <div>
            <div className="text-xs font-black uppercase tracking-[0.2em] text-primary">Community Footprint</div>
            <h3 className="mt-2 text-2xl font-black text-slate-900">社区足迹</h3>
            <p className="mt-2 text-sm leading-7 text-slate-500">
            这里统一收纳你在社区里的发布、回复、收藏和点赞行为，不用再分散去找。
            </p>
          </div>
          <Link href="/posts/create">
            <Button type="primary" className="h-11 rounded-2xl px-6 font-black">
              去发布经验帖
            </Button>
          </Link>
        </div>

        <div className="mt-6 flex flex-wrap gap-3">
          {communityTabs.map((item) => {
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

      {activeTab === "published" && <MyPostList />}
      {activeTab === "reply" && <MyReplyPostCommentList />}
      {activeTab === "favour" && <MyFavourPostList />}
      {activeTab === "thumb" && <MyThumbPostList />}
    </div>
  );
}
