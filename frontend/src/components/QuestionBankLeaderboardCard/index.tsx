import React from "react";
import { Crown, Users } from "lucide-react";

interface Props {
  leaderboard?: API.QuestionBankLeaderboardVO;
}

/**
 * 题库榜单卡片
 */
export default function QuestionBankLeaderboardCard({ leaderboard }: Props) {
  const rankingList = leaderboard?.rankingList || [];

  if (!leaderboard) {
    return null;
  }

  return (
    <section className="rounded-[2.5rem] border border-slate-100 bg-white p-8 shadow-2xl shadow-slate-200/40">
      <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
        <div>
          <div className="flex items-center gap-2 text-lg font-black text-slate-900">
            <Crown className="h-5 w-5 text-amber-500" />
            题库排行榜
          </div>
          <p className="mt-3 max-w-2xl text-sm leading-6 text-slate-500">
            {leaderboard.description || "根据当前题库内的刷题数量，展示最活跃的学习者。"}
          </p>
        </div>
        <div className="inline-flex items-center gap-2 rounded-full bg-slate-50 px-4 py-2 text-sm font-bold text-slate-500">
          <Users className="h-4 w-4 text-primary" />
          {leaderboard.metricLabel || "完成题数"}
        </div>
      </div>

      <div className="mt-6 grid grid-cols-1 gap-3">
        {rankingList.length ? (
          rankingList.slice(0, 6).map((item) => (
            <div
              key={`bank-rank-${item.userId}`}
              className="flex items-center justify-between gap-4 rounded-2xl border border-slate-100 bg-slate-50/70 px-4 py-3"
            >
              <div className="min-w-0 flex items-center gap-3">
                <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl bg-slate-900 text-sm font-black text-white">
                  {item.rank}
                </div>
                <div className="min-w-0">
                  <div className="truncate font-semibold text-slate-800">{item.userName || "匿名用户"}</div>
                  <div className="text-xs text-slate-500">{item.userRole === "admin" ? "管理员" : "学习者"}</div>
                </div>
              </div>
              <div className="shrink-0 text-right">
                <div className="font-black text-slate-900">{item.metricValue || 0}</div>
                <div className="text-xs text-slate-500">{item.metricText}</div>
              </div>
            </div>
          ))
        ) : (
          <div className="rounded-2xl border border-dashed border-slate-200 px-4 py-8 text-center text-sm text-slate-400">
            当前题库还没有形成榜单，先去刷几道题试试看。
          </div>
        )}
      </div>

      {leaderboard.currentUserItem ? (
        <div className="mt-6 rounded-2xl border border-dashed border-primary/20 bg-primary/5 px-4 py-3">
          <div className="text-xs font-black uppercase tracking-wider text-primary">我在本题库的排名</div>
          <div className="mt-2 flex items-center justify-between gap-4">
            <div>
              <div className="font-semibold text-slate-800">{leaderboard.currentUserItem.userName || "当前用户"}</div>
              <div className="text-sm text-slate-500">第 {leaderboard.currentUserItem.rank || "-"} 名</div>
            </div>
            <div className="text-right">
              <div className="font-black text-slate-900">{leaderboard.currentUserItem.metricValue || 0}</div>
              <div className="text-xs text-slate-500">{leaderboard.currentUserItem.metricText}</div>
            </div>
          </div>
        </div>
      ) : null}
    </section>
  );
}
