import React from "react";
import { Activity, Flame, Trophy } from "lucide-react";

interface Props {
  leaderboard?: API.GlobalLeaderboardVO;
}

const iconMap: Record<string, React.ReactNode> = {
  overall: <Trophy className="h-5 w-5 text-amber-500" />,
  active: <Activity className="h-5 w-5 text-emerald-500" />,
  streak: <Flame className="h-5 w-5 text-rose-500" />,
};

function getRankStyle(rank?: number) {
  if (rank === 1) {
    return "bg-amber-50 border-amber-200 text-amber-700";
  }
  if (rank === 2) {
    return "bg-slate-100 border-slate-200 text-slate-700";
  }
  if (rank === 3) {
    return "bg-orange-50 border-orange-200 text-orange-700";
  }
  return "bg-white border-slate-100 text-slate-700";
}

/**
 * 全站榜单展示区
 */
export default function LeaderboardSection({ leaderboard }: Props) {
  const boardList = leaderboard?.boardList || [];

  if (!boardList.length) {
    return null;
  }

  return (
    <section className="space-y-10">
      <div className="flex items-end justify-between px-4">
        <div className="space-y-3">
          <div className="flex items-center gap-2 text-amber-500 font-black uppercase tracking-[0.2em] text-xs">
            <span className="h-1.5 w-1.5 rounded-full bg-amber-500" />
            <span>Leaderboard</span>
          </div>
          <h2 className="text-3xl font-black tracking-tighter text-slate-900 sm:text-5xl">智能榜单</h2>
        </div>
        <div className="text-sm font-bold text-slate-400">全站综合榜 + 活跃榜 + 连续冲刺榜</div>
      </div>

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-3">
        {boardList.map((board) => (
          <div
            key={board.key}
            className="rounded-[2.5rem] border border-slate-100 bg-white p-7 shadow-2xl shadow-slate-200/40"
          >
            <div className="flex items-start justify-between gap-4">
              <div>
                <div className="flex items-center gap-2 text-lg font-black text-slate-900">
                  {iconMap[board.key || "overall"] || <Trophy className="h-5 w-5 text-primary" />}
                  {board.title}
                </div>
                <p className="mt-3 text-sm leading-6 text-slate-500">{board.description}</p>
              </div>
              <div className="rounded-full bg-slate-50 px-3 py-1 text-xs font-black text-slate-500">
                {board.metricLabel}
              </div>
            </div>

            <div className="mt-6 space-y-3">
              {(board.rankingList || []).slice(0, 5).map((item) => (
                <div
                  key={`${board.key}-${item.userId}`}
                  className={`flex items-center justify-between gap-4 rounded-2xl border px-4 py-3 ${getRankStyle(item.rank)}`}
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
              ))}
            </div>

            {board.currentUserItem ? (
              <div className="mt-6 rounded-2xl border border-dashed border-primary/20 bg-primary/5 px-4 py-3">
                <div className="text-xs font-black uppercase tracking-wider text-primary">我的位置</div>
                <div className="mt-2 flex items-center justify-between gap-4">
                  <div>
                    <div className="font-semibold text-slate-800">{board.currentUserItem.userName || "当前用户"}</div>
                    <div className="text-sm text-slate-500">第 {board.currentUserItem.rank || "-"} 名</div>
                  </div>
                  <div className="text-right">
                    <div className="font-black text-slate-900">{board.currentUserItem.metricValue || 0}</div>
                    <div className="text-xs text-slate-500">{board.currentUserItem.metricText}</div>
                  </div>
                </div>
              </div>
            ) : (
              <div className="mt-6 rounded-2xl border border-dashed border-slate-200 px-4 py-3 text-sm text-slate-400">
                登录并开始刷题后，你的排名也会出现在这里。
              </div>
            )}
          </div>
        ))}
      </div>
    </section>
  );
}
