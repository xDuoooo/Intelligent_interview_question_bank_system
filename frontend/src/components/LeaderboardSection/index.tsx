import React from "react";
import { Activity, Flame, Trophy } from "lucide-react";
import UserAvatar from "@/components/UserAvatar";
import UserProfileHoverCard from "@/components/UserProfileHoverCard";

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
      <div className="flex flex-col gap-4 px-4 lg:flex-row lg:items-end lg:justify-between">
        <div className="space-y-3">
          <div className="flex items-center gap-2 text-amber-500 font-black uppercase tracking-[0.2em] text-xs">
            <span className="h-1.5 w-1.5 rounded-full bg-amber-500" />
            <span>Leaderboard</span>
          </div>
          <h2 className="text-3xl font-black tracking-tighter text-slate-900 sm:text-5xl">智能榜单</h2>
        </div>
        <div className="flex flex-wrap gap-2">
          {boardList.map((board) => (
            <div
              key={`summary-${board.key}`}
              className="inline-flex items-center gap-2 rounded-full border border-slate-200 bg-white px-4 py-2 text-sm font-bold text-slate-500 shadow-sm"
            >
              {iconMap[board.key || "overall"] || <Trophy className="h-4 w-4 text-primary" />}
              <span>{board.metricLabel}</span>
            </div>
          ))}
        </div>
      </div>

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-3">
        {boardList.map((board) => (
          <div
            key={board.key}
            className="rounded-[2.5rem] border border-slate-100 bg-white p-7 shadow-2xl shadow-slate-200/40"
          >
            <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
              <div className="min-w-0">
                <div className="flex items-center gap-3 text-lg font-black text-slate-900">
                  <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl bg-slate-50">
                    {iconMap[board.key || "overall"] || <Trophy className="h-5 w-5 text-primary" />}
                  </div>
                  <span className="truncate">{board.title}</span>
                </div>
                <p className="mt-3 text-sm leading-6 text-slate-500">{board.description}</p>
              </div>
              <div className="inline-flex shrink-0 items-center rounded-2xl bg-slate-50 px-4 py-2 text-sm font-black text-slate-500">
                {board.metricLabel}
              </div>
            </div>

            {board.currentUserItem ? (
              <div className="mt-6 rounded-[1.75rem] border border-primary/15 bg-primary/5 p-4">
                <div className="mb-3 text-xs font-black uppercase tracking-wider text-primary">我的位置</div>
                <div className="grid grid-cols-1 gap-3 sm:grid-cols-[minmax(0,1fr)_auto_auto] sm:items-center">
                  <div className="min-w-0">
                    <UserProfileHoverCard user={board.currentUserItem} placement="topLeft">
                      <div className="flex items-center gap-3">
                        <UserAvatar
                          src={board.currentUserItem.userAvatar}
                          name={board.currentUserItem.userName}
                          size={38}
                        />
                        <div className="min-w-0">
                          <div className="truncate font-semibold text-slate-800 transition-colors hover:text-primary">
                            {board.currentUserItem.userName || "当前用户"}
                          </div>
                          <div className="mt-1 text-sm text-slate-500">继续刷题就能稳定往前冲</div>
                        </div>
                      </div>
                    </UserProfileHoverCard>
                  </div>
                  <div className="rounded-2xl bg-white px-4 py-3 text-center shadow-sm">
                    <div className="text-xs font-bold text-slate-400">当前排名</div>
                    <div className="mt-1 text-lg font-black text-slate-900">#{board.currentUserItem.rank || "-"}</div>
                  </div>
                  <div className="rounded-2xl bg-white px-4 py-3 text-center shadow-sm">
                    <div className="text-xs font-bold text-slate-400">{board.currentUserItem.metricText}</div>
                    <div className="mt-1 text-lg font-black text-slate-900">{board.currentUserItem.metricValue || 0}</div>
                  </div>
                </div>
              </div>
            ) : (
              <div className="mt-6 rounded-[1.75rem] border border-dashed border-slate-200 px-4 py-4 text-sm text-slate-400">
                登录并开始刷题后，你的排名也会出现在这里。
              </div>
            )}

            <div className="mt-6 space-y-3">
              {(board.rankingList || []).slice(0, 5).map((item) => (
                <div
                  key={`${board.key}-${item.userId}`}
                  className={`rounded-2xl border px-4 py-4 ${getRankStyle(item.rank)}`}
                >
                  <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                    <UserProfileHoverCard user={item} placement="topLeft">
                      <div className="min-w-0 flex items-center gap-3">
                        <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl bg-slate-900 text-sm font-black text-white">
                          {item.rank}
                        </div>
                        <UserAvatar src={item.userAvatar} name={item.userName} size={38} />
                        <div className="min-w-0">
                          <div className="truncate font-semibold text-slate-800 transition-colors hover:text-primary">
                            {item.userName || "匿名用户"}
                          </div>
                          <div className="text-xs text-slate-500">{item.userRole === "admin" ? "管理员" : "学习者"}</div>
                        </div>
                      </div>
                    </UserProfileHoverCard>
                    <div className="flex items-center justify-between gap-3 sm:justify-end">
                      <div className="text-xs font-bold text-slate-400">{item.metricText}</div>
                      <div className="inline-flex min-w-[88px] items-center justify-center rounded-2xl bg-white/80 px-4 py-2 text-base font-black text-slate-900 shadow-sm">
                        {item.metricValue || 0}
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}
