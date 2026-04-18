import React from "react";
import {
  Activity,
  Flame,
  Medal,
  ShieldCheck,
  Sparkles,
  Trophy,
} from "lucide-react";
import UserAvatar from "@/components/UserAvatar";
import UserProfileHoverCard from "@/components/UserProfileHoverCard";

interface Props {
  leaderboard?: API.GlobalLeaderboardVO;
}

const boardThemes: Record<
  string,
  {
    icon: React.ReactNode;
    accent: string;
    accentSoft: string;
    accentText: string;
    glow: string;
    panel: string;
    championBorder: string;
    championBg: string;
    rankBadge: string;
  }
> = {
  overall: {
    icon: <Trophy className="h-5 w-5" />,
    accent: "from-amber-400 to-orange-500",
    accentSoft: "from-amber-50/60 to-orange-50/60",
    accentText: "text-amber-600",
    glow: "bg-amber-300/30",
    panel: "border-amber-100/80 hover:border-amber-300/80 hover:shadow-amber-100/50",
    championBorder: "from-amber-300 to-orange-400 shadow-amber-200/50",
    championBg: "bg-amber-50/40",
    rankBadge: "text-amber-500",
  },
  active: {
    icon: <Activity className="h-5 w-5" />,
    accent: "from-emerald-400 to-teal-500",
    accentSoft: "from-emerald-50/60 to-teal-50/60",
    accentText: "text-emerald-600",
    glow: "bg-emerald-300/30",
    panel: "border-emerald-100/80 hover:border-emerald-300/80 hover:shadow-emerald-100/50",
    championBorder: "from-emerald-300 to-teal-400 shadow-emerald-200/50",
    championBg: "bg-emerald-50/40",
    rankBadge: "text-emerald-500",
  },
  streak: {
    icon: <Flame className="h-5 w-5" />,
    accent: "from-rose-400 to-orange-500",
    accentSoft: "from-rose-50/60 to-orange-50/60",
    accentText: "text-rose-600",
    glow: "bg-rose-300/30",
    panel: "border-rose-100/80 hover:border-rose-300/80 hover:shadow-rose-100/50",
    championBorder: "from-rose-300 to-orange-400 shadow-rose-200/50",
    championBg: "bg-rose-50/40",
    rankBadge: "text-rose-500",
  },
};

function getBoardTheme(key?: string) {
  return boardThemes[key || "overall"] || boardThemes.overall;
}

function getRankBadgeBg(rank: number) {
  if (rank === 2) return "bg-slate-100 text-slate-500";
  if (rank === 3) return "bg-orange-50 text-orange-600";
  return "bg-slate-50 text-slate-400";
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
    <section className="space-y-6">
      <div className="grid grid-cols-1 gap-6 xl:grid-cols-3">
        {boardList.map((board) => {
          const theme = getBoardTheme(board.key);
          const champion = board.rankingList?.[0];
          const rankingList = board.rankingList || [];
          const secondaryList = champion ? rankingList.slice(1, 6) : rankingList.slice(0, 5);

          return (
            <article
              key={board.key}
              className={`group relative flex h-full flex-col overflow-hidden rounded-[2rem] border bg-white shadow-xl shadow-slate-200/20 transition-all duration-300 hover:-translate-y-0.5 ${theme.panel}`}
            >
              {/* Soft Gradient Background */}
              <div className={`absolute inset-0 bg-gradient-to-br opacity-50 ${theme.accentSoft}`} />
              <div className={`absolute -right-20 -top-20 h-48 w-48 rounded-full blur-[80px] opacity-40 transition-opacity duration-500 group-hover:opacity-70 ${theme.glow}`} />

              <div className="relative z-10 flex-1 flex flex-col p-6">
                {/* Header */}
                <div className="flex items-center gap-4 mb-6">
                  <div className={`inline-flex h-12 w-12 items-center justify-center rounded-[1.2rem] bg-gradient-to-br ${theme.accent} text-white shadow-md`}>
                    {theme.icon}
                  </div>
                  <div>
                    <h3 className="text-lg font-bold text-slate-800">{board.title}</h3>
                    <div className="flex items-center gap-1.5 mt-0.5">
                      <div className="text-[11px] font-bold uppercase tracking-widest text-slate-400">
                        {board.metricLabel}
                      </div>
                    </div>
                  </div>
                </div>

                <div className="flex-1 flex flex-col gap-3">
                  {/* Champion Card (Top 1) */}
                  {champion ? (
                    <UserProfileHoverCard
                      user={{
                        id: champion.userId,
                        userName: champion.userName,
                        userAvatar: champion.userAvatar,
                        userRole: champion.userRole,
                      } as any}
                    >
                      <div className={`relative flex items-center justify-between rounded-2xl border border-white bg-white/80 p-4 backdrop-blur-md shadow-sm transition-all duration-300 hover:shadow-md hover:bg-white cursor-pointer ${theme.championBg}`}>
                        <div className="flex items-center gap-4 min-w-0">
                          <div className="relative shrink-0">
                            <div className="rounded-full border-2 border-white shadow-sm bg-white p-0.5">
                              <UserAvatar src={champion.userAvatar} name={champion.userName} size={52} />
                            </div>
                            <div className={`absolute -bottom-1 -right-1 flex h-6 w-6 items-center justify-center rounded-full border-2 border-white text-white shadow-sm bg-gradient-to-br ${theme.championBorder}`}>
                              <Medal className="h-3 w-3" />
                            </div>
                          </div>
                          <div className="min-w-0 flex flex-col">
                            <div className="flex items-center gap-1.5">
                              <span className="truncate text-base font-extrabold text-slate-800">
                                {champion.userName || "匿名用户"}
                              </span>
                              {champion.userRole === "admin" && (
                                <ShieldCheck className="h-3.5 w-3.5 text-emerald-500 shrink-0" />
                              )}
                            </div>
                            <div className={`mt-0.5 text-xs font-bold leading-tight ${theme.accentText}`}>
                              TOP 1
                            </div>
                          </div>
                        </div>
                        <div className="shrink-0 text-right pl-3">
                          <div className="text-xl font-black text-slate-900 tracking-tight">
                            {champion.metricValue || 0}
                          </div>
                        </div>
                      </div>
                    </UserProfileHoverCard>
                  ) : (
                    <div className="flex h-[92px] items-center justify-center rounded-2xl border border-dashed border-slate-200 bg-white/50">
                      <span className="text-xs font-semibold text-slate-400">目前无人登顶，虚位以待</span>
                    </div>
                  )}

                  {/* Following Ranks (Top 2-6) */}
                  <div className="flex flex-col gap-2 flex-1 mt-1">
                    {secondaryList.map((item: any, index: number) => {
                      const rank = item.rank || index + 2;
                      return (
                        <UserProfileHoverCard
                          key={`${board.key}-${item.userId}`}
                          user={{
                            id: item.userId,
                            userName: item.userName,
                            userAvatar: item.userAvatar,
                            userRole: item.userRole,
                          } as any}
                        >
                          <div className="group/item flex items-center justify-between rounded-xl p-2 transition-all duration-200 hover:bg-white/80 hover:shadow-sm cursor-pointer border border-transparent hover:border-slate-100">
                            <div className="flex items-center gap-3 min-w-0">
                              <div className={`flex h-6 w-6 shrink-0 items-center justify-center rounded-full text-xs font-bold ${getRankBadgeBg(rank)}`}>
                                {rank}
                              </div>
                              <UserAvatar src={item.userAvatar} name={item.userName} size={36} />
                              <div className="truncate text-sm font-bold text-slate-700 transition-colors group-hover/item:text-slate-900">
                                {item.userName || "匿名用户"}
                              </div>
                            </div>
                            <div className="shrink-0 font-bold text-slate-700 pl-3">
                              {item.metricValue || 0}
                            </div>
                          </div>
                        </UserProfileHoverCard>
                      )
                    })}
                    {!rankingList.length ? (
                      <div className="rounded-xl px-4 py-6 text-center text-xs text-slate-400">
                        这条赛道目前还没有形成榜单数据
                      </div>
                    ) : null}
                  </div>

                  {/* My Position Box */}
                  <div className="mt-2 pt-4 border-t border-slate-200/60">
                    {board.currentUserItem ? (
                      <UserProfileHoverCard
                        user={{
                          id: board.currentUserItem.userId,
                          userName: board.currentUserItem.userName,
                          userAvatar: board.currentUserItem.userAvatar,
                          userRole: board.currentUserItem.userRole,
                        } as any}
                      >
                        <div className="group/my flex items-center justify-between rounded-xl bg-white border border-slate-200 p-3 shadow-sm transition-all duration-200 hover:bg-slate-50 cursor-pointer hover:border-slate-300">
                          <div className="flex items-center gap-3 min-w-0">
                            <div className="flex flex-col items-center justify-center min-w-[28px] pl-1">
                              <Sparkles className="h-3 w-3 text-slate-400 mb-0.5" />
                              <span className="font-bold text-slate-500 text-xs">#{board.currentUserItem.rank || "-"}</span>
                            </div>
                            <UserAvatar src={board.currentUserItem.userAvatar} name={board.currentUserItem.userName} size={36} />
                            <div className="min-w-0 flex-1 ml-1">
                              <div className="truncate text-sm font-bold text-slate-800">
                                {board.currentUserItem.userName || "当前用户"}
                              </div>
                              <div className="text-[10px] uppercase font-semibold text-slate-400 mt-0.5">
                                My Score
                              </div>
                            </div>
                          </div>
                          <div className="shrink-0 font-black text-slate-800 text-lg pr-2 text-right">
                            {board.currentUserItem.metricValue || 0}
                          </div>
                        </div>
                      </UserProfileHoverCard>
                    ) : (
                      <div className="flex items-center justify-center h-[56px] rounded-xl border border-dashed border-slate-200 bg-slate-50/50 text-[11px] font-semibold text-slate-400">
                        登录并参与刷题即可上榜
                      </div>
                    )}
                  </div>

                </div>
              </div>
            </article>
          );
        })}
      </div>
    </section>
  );
}
