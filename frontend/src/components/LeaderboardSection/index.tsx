import React from "react";
import {
  Activity,
  Flame,
  Medal,
  ShieldCheck,
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
    championInner: string;
    championBorder: string;
  }
> = {
  overall: {
    icon: <Trophy className="h-5 w-5" />,
    accent: "from-amber-400 to-orange-500",
    accentSoft: "from-amber-500/10 to-orange-500/10",
    accentText: "text-amber-600",
    glow: "bg-amber-400/20",
    panel: "group-hover:border-amber-200/50",
    championInner: "bg-gradient-to-br from-amber-50 to-orange-50/50 border-amber-200/40",
    championBorder: "from-amber-400 to-orange-500 shadow-amber-300/40",
  },
  active: {
    icon: <Activity className="h-5 w-5" />,
    accent: "from-emerald-400 to-teal-500",
    accentSoft: "from-emerald-500/10 to-teal-500/10",
    accentText: "text-emerald-600",
    glow: "bg-emerald-400/20",
    panel: "group-hover:border-emerald-200/50",
    championInner: "bg-gradient-to-br from-emerald-50 to-teal-50/50 border-emerald-200/40",
    championBorder: "from-emerald-400 to-teal-500 shadow-emerald-300/40",
  },
  streak: {
    icon: <Flame className="h-5 w-5" />,
    accent: "from-rose-400 to-orange-500",
    accentSoft: "from-rose-500/10 to-orange-500/10",
    accentText: "text-rose-600",
    glow: "bg-rose-400/20",
    panel: "group-hover:border-rose-200/50",
    championInner: "bg-gradient-to-br from-rose-50 to-orange-50/50 border-rose-200/40",
    championBorder: "from-rose-400 to-orange-500 shadow-rose-300/40",
  },
};

function getBoardTheme(key?: string) {
  return boardThemes[key || "overall"] || boardThemes.overall;
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
      <div className="grid grid-cols-1 gap-8 xl:grid-cols-2 2xl:grid-cols-3">
        {boardList.map((board) => {
          const theme = getBoardTheme(board.key);
          const champion = board.rankingList?.[0];
          const rankingList = board.rankingList || [];
          const secondaryList = champion ? rankingList.slice(1, 6) : rankingList.slice(0, 5);

          return (
            <article
              key={board.key}
              className={`group relative flex h-full flex-col overflow-hidden rounded-[2.5rem] border border-slate-200/60 bg-white/60 p-7 backdrop-blur-2xl shadow-xl shadow-slate-200/30 transition-all duration-500 hover:shadow-2xl hover:-translate-y-1 ${theme.panel}`}
            >
              {/* Blur Glow Background */}
              <div className={`absolute -right-20 -top-20 h-64 w-64 rounded-full blur-[100px] opacity-40 transition-opacity duration-500 group-hover:opacity-70 ${theme.glow}`} />
              <div className={`absolute -left-20 top-1/2 h-64 w-64 rounded-full blur-[100px] opacity-20 transition-opacity duration-500 group-hover:opacity-50 ${theme.glow}`} />

              <div className="relative z-10 flex-1 flex flex-col">
                <div className="flex items-center gap-4">
                  <span className={`inline-flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br ${theme.accent} text-white shadow-lg`}>
                    {theme.icon}
                  </span>
                  <div>
                    <h3 className="text-xl font-bold tracking-tight text-slate-800">{board.title}</h3>
                    <div className="flex items-center gap-1.5 mt-1">
                      <div className={`h-1.5 w-1.5 rounded-full animate-pulse bg-gradient-to-r ${theme.accent}`} />
                      <span className="text-[11px] font-black uppercase tracking-[0.15em] text-slate-500">
                        {board.metricLabel}
                      </span>
                    </div>
                  </div>
                </div>

                <div className="mt-8 flex flex-col flex-1 gap-2">
                  {/* Champion Card */}
                  {champion ? (
                    <UserProfileHoverCard
                      user={{
                        id: champion.userId,
                        userName: champion.userName,
                        userAvatar: champion.userAvatar,
                        userRole: champion.userRole,
                      } as any}
                    >
                      <div className={`group/card relative flex items-center gap-4 overflow-hidden rounded-[2rem] border p-4 transition-all duration-300 hover:scale-[1.02] cursor-pointer mb-2 shadow-sm hover:shadow-md ${theme.championInner}`}>
                        <div className="relative shrink-0">
                          <div className={`absolute -inset-1 animate-pulse rounded-full bg-gradient-to-br opacity-50 blur-sm ${theme.championBorder}`} />
                          <div className={`relative rounded-full border-[3px] border-white shadow-sm bg-white`}>
                            <UserAvatar src={champion.userAvatar} name={champion.userName} size={60} />
                          </div>
                          <div className={`absolute -bottom-1 -right-1 flex h-7 w-7 items-center justify-center rounded-full border-2 border-white text-white shadow-lg bg-gradient-to-br ${theme.championBorder}`}>
                            <Medal className="h-3.5 w-3.5" />
                          </div>
                        </div>
                        <div className="min-w-0 flex-1">
                          <div className="flex items-center gap-2">
                            <span className="truncate text-lg font-black text-slate-900 group-hover/card:text-slate-800">
                              {champion.userName || "匿名用户"}
                            </span>
                            {champion.userRole === "admin" && (
                              <ShieldCheck className="h-4 w-4 text-emerald-500 shrink-0" />
                            )}
                          </div>
                          <div className="mt-0.5 text-[10px] font-bold text-slate-500 uppercase tracking-widest opacity-80">
                            Current Champion
                          </div>
                        </div>
                        <div className={`shrink-0 flex flex-col items-end pr-2`}>
                          <div className={`font-black tracking-tight text-2xl ${theme.accentText}`}>
                            {champion.metricValue || 0}
                          </div>
                        </div>
                        <Trophy className={`absolute -right-4 -bottom-4 h-24 w-24 opacity-[0.04] transform -rotate-12 ${theme.accentText}`} />
                      </div>
                    </UserProfileHoverCard>
                  ) : (
                    <div className="flex h-[116px] mb-2 items-center justify-center rounded-[2rem] border border-dashed border-slate-200 bg-slate-50/50">
                      <span className="text-sm font-semibold text-slate-400">目前无人登顶，虚位以待</span>
                    </div>
                  )}

                  {/* Following Ranks */}
                  <div className="flex flex-col gap-1.5 flex-1">
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
                          <div className="group/item flex items-center gap-3 rounded-2xl border border-transparent p-2.5 transition-all duration-300 hover:bg-white/80 hover:border-slate-100 hover:shadow-sm hover:scale-[1.01] cursor-pointer">
                            <div className="flex w-6 shrink-0 justify-center font-black italic text-slate-400/80">
                              {rank}
                            </div>
                            <UserAvatar src={item.userAvatar} name={item.userName} size={38} />
                            <div className="min-w-0 flex-1">
                              <div className="truncate text-[15px] font-bold text-slate-700 transition-colors group-hover/item:text-slate-900">
                                {item.userName || "匿名用户"}
                              </div>
                            </div>
                            <div className="shrink-0 font-bold text-slate-600 font-mono pr-2">
                              {item.metricValue || 0}
                            </div>
                          </div>
                        </UserProfileHoverCard>
                      )
                    })}
                  </div>

                  {/* My Position */}
                  {board.currentUserItem && (
                    <div className="mt-4 pt-4 border-t border-slate-100/80">
                      <UserProfileHoverCard
                        user={{
                          id: board.currentUserItem.userId,
                          userName: board.currentUserItem.userName,
                          userAvatar: board.currentUserItem.userAvatar,
                          userRole: board.currentUserItem.userRole,
                        } as any}
                      >
                        <div className="group/mycard flex items-center gap-3 rounded-[1.5rem] bg-slate-900 p-3.5 text-white shadow-xl shadow-slate-900/10 hover:shadow-slate-900/20 hover:scale-[1.02] transition-all duration-300 cursor-pointer border border-slate-800">
                          <div className="flex flex-col items-center justify-center min-w-[32px]">
                            <span className="text-[8px] uppercase tracking-widest text-slate-400 font-bold">My</span>
                            <span className="font-black italic text-white text-sm">#{board.currentUserItem.rank || "-"}</span>
                          </div>
                          <div className="rounded-full ring-2 ring-white/10 p-0.5">
                            <UserAvatar src={board.currentUserItem.userAvatar} name={board.currentUserItem.userName} size={36} />
                          </div>
                          <div className="min-w-0 flex-1 ml-1">
                            <div className="truncate text-sm font-bold opacity-90 group-hover/mycard:opacity-100 transition-opacity">
                              {board.currentUserItem.userName || "当前用户"}
                            </div>
                          </div>
                          <div className="shrink-0 font-black text-white text-lg pr-1 opacity-90">
                            {board.currentUserItem.metricValue || 0}
                          </div>
                        </div>
                      </UserProfileHoverCard>
                    </div>
                  )}

                  {!board.currentUserItem && (
                    <div className="mt-4 pt-4 border-t border-slate-100/80">
                      <div className="flex items-center justify-center h-[64px] rounded-[1.5rem] border border-dashed border-slate-300 bg-slate-50/50 text-xs font-semibold text-slate-400">
                        登录并开始刷题，你的排名将出现在这里
                      </div>
                    </div>
                  )}

                </div>
              </div>
            </article>
          );
        })}
      </div>
    </section>
  );
}
