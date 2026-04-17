import React from "react";
import {
  Activity,
  ArrowUpRight,
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
    badge: string;
    glow: string;
    panel: string;
    champion: string;
    labelBg: string;
  }
> = {
  overall: {
    icon: <Trophy className="h-5 w-5" />,
    accent: "from-amber-400 to-orange-500",
    accentSoft: "from-amber-50/40 via-white to-orange-50/40",
    accentText: "text-amber-700",
    badge: "border-amber-200/50 bg-amber-50/50 text-amber-700",
    glow: "bg-amber-300/20",
    panel: "border-amber-100/60 bg-white/60",
    champion: "bg-gradient-to-br from-amber-400 to-orange-500 shadow-amber-200/50",
    labelBg: "bg-amber-100/50",
  },
  active: {
    icon: <Activity className="h-5 w-5" />,
    accent: "from-emerald-400 to-teal-500",
    accentSoft: "from-emerald-50/40 via-white to-teal-50/40",
    accentText: "text-emerald-700",
    badge: "border-emerald-200/50 bg-emerald-50/50 text-emerald-700",
    glow: "bg-emerald-300/20",
    panel: "border-emerald-100/60 bg-white/60",
    champion: "bg-gradient-to-br from-emerald-400 to-teal-500 shadow-emerald-200/50",
    labelBg: "bg-emerald-100/50",
  },
  streak: {
    icon: <Flame className="h-5 w-5" />,
    accent: "from-rose-400 to-orange-500",
    accentSoft: "from-rose-50/40 via-white to-orange-50/40",
    accentText: "text-rose-700",
    badge: "border-rose-200/50 bg-rose-50/50 text-rose-700",
    glow: "bg-rose-300/20",
    panel: "border-rose-100/60 bg-white/60",
    champion: "bg-gradient-to-br from-rose-400 to-orange-500 shadow-rose-200/50",
    labelBg: "bg-rose-100/50",
  },
};

function getBoardTheme(key?: string) {
  return boardThemes[key || "overall"] || boardThemes.overall;
}

function getRankBadgeClass(rank?: number) {
  if (rank === 1) {
    return "bg-gradient-to-br from-amber-500 to-orange-500 text-white shadow-lg shadow-amber-200/70";
  }
  if (rank === 2) {
    return "bg-gradient-to-br from-slate-700 to-slate-500 text-white shadow-lg shadow-slate-200/70";
  }
  if (rank === 3) {
    return "bg-gradient-to-br from-orange-500 to-amber-500 text-white shadow-lg shadow-orange-200/70";
  }
  return "bg-slate-100 text-slate-600";
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
    <section className="space-y-12">

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-2 2xl:grid-cols-3 2xl:gap-4">
        {boardList.map((board) => {
          const theme = getBoardTheme(board.key);
          const champion = board.rankingList?.[0];
          const rankingList = board.rankingList || [];
          const secondaryList = champion ? rankingList.slice(1, 6) : rankingList.slice(0, 5);

          return (
            <article
              key={board.key}
              className="group relative flex h-full flex-col overflow-hidden rounded-[3rem] border border-slate-100 bg-white p-7 shadow-2xl shadow-slate-200/40 transition-all hover:shadow-slate-200/60"
            >
              <div className={`absolute -right-10 top-10 h-44 w-44 rounded-full blur-[80px] opacity-60 transition-all group-hover:opacity-100 ${theme.glow}`} />

              <div className={`relative overflow-hidden rounded-[2.5rem] border bg-gradient-to-br p-6 ${theme.panel} ${theme.accentSoft}`}>
                <div className="flex items-start justify-between">
                  <div className="space-y-4">
                    <div className="flex items-center gap-4">
                      <span className={`flex h-14 w-14 items-center justify-center rounded-[1.2rem] bg-gradient-to-br ${theme.accent} text-white shadow-xl transform transition-transform group-hover:scale-110`}>
                        {theme.icon}
                      </span>
                      <div>
                        <div className="text-2xl font-black tracking-tight text-slate-900">{board.title}</div>
                        <div className="mt-1 flex items-center gap-2">
                          <div className={`h-1.5 w-1.5 rounded-full animate-pulse ${theme.accent.split(' ')[1].replace('to-', 'bg-')}`} />
                          <div className="text-[10px] font-extrabold uppercase tracking-[0.2em] text-slate-400">
                            {board.metricLabel}
                          </div>
                        </div>
                      </div>
                    </div>
                    <p className="max-w-[280px] text-sm leading-6 font-medium text-slate-500/80">
                      {board.description || "基于近期学习行为实时刷新，展示当前表现最突出的学习者。"}
                    </p>
                  </div>
                </div>

                {champion ? (
                  <div className="mt-8 relative overflow-hidden rounded-[2rem] border border-white/60 bg-white/40 p-1.5 backdrop-blur-md shadow-xl shadow-slate-200/10">
                    <div className="flex items-center gap-4 p-4">
                      <div className="relative">
                        <UserAvatar src={champion.userAvatar} name={champion.userName} size={64} />
                        <div className={`absolute -bottom-1 -right-1 flex h-7 w-7 items-center justify-center rounded-full border-2 border-white text-white shadow-lg ${theme.champion}`}>
                          <Medal className="h-3.5 w-3.5" />
                        </div>
                      </div>
                      <div className="min-w-0 flex-[4]">
                        <div className="flex items-center gap-2">
                          <span className="truncate text-xl font-black text-slate-900 2xl:whitespace-normal 2xl:line-clamp-2">
                            {champion.userName || "匿名用户"}
                          </span>
                          {champion.userRole === "admin" && (
                            <span className="shrink-0 rounded-md bg-slate-900 px-1.5 py-0.5 text-[9px] font-black uppercase tracking-wider text-white">
                              Admin
                            </span>
                          )}
                        </div>
                        <div className="mt-1 text-xs font-bold text-primary/80">
                          {champion.userRole === "admin" ? "领跑全站" : "正在卫冕中..."}
                        </div>
                      </div>
                      <div className={`shrink-0 text-right ml-4 ${theme.accentText}`}>
                        <div className="text-[9px] font-black uppercase tracking-[0.15em] opacity-60">
                          Champion
                        </div>
                        <div className="mt-0.5 text-2xl font-black tracking-tighter">
                          {champion.metricValue || 0}
                        </div>
                      </div>
                    </div>
                  </div>
                ) : (
                  <div className="mt-8 rounded-[2rem] border border-dashed border-slate-200 bg-white/30 px-6 py-8 text-center backdrop-blur-sm">
                    <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-2xl bg-slate-100 text-slate-400">
                      <Trophy className="h-6 w-6" />
                    </div>
                    <div className="mt-4 text-xs font-bold text-slate-400 tracking-wider uppercase">虚位以待</div>
                  </div>
                )}
              </div>

              <div className="mt-6 flex flex-1 flex-col gap-4">
                {board.currentUserItem ? (
                  <div className={`rounded-[2.2rem] border bg-white p-5 shadow-sm transition-all hover:shadow-md ${theme.panel}`}>
                    <div className="flex items-center justify-between mb-4">
                      <div className={`flex items-center gap-2 text-[10px] font-black tracking-[0.2em] uppercase ${theme.accentText}`}>
                        <Sparkles className="h-3 w-3" />
                        My Position
                      </div>
                      <div className="inline-flex items-center gap-1 rounded-full bg-slate-900 px-3 py-1 text-[9px] font-black uppercase tracking-[0.1em] text-white">
                        # {board.currentUserItem.rank || "-"}
                      </div>
                    </div>

                    <div className="flex items-center gap-4">
                      <UserAvatar
                        src={board.currentUserItem.userAvatar}
                        name={board.currentUserItem.userName}
                        size={44}
                      />
                      <div className="min-w-0 flex-[4]">
                        <div className="truncate text-base font-black text-slate-900 2xl:min-w-[200px] 2xl:whitespace-normal 2xl:line-clamp-2">
                          {board.currentUserItem.userName || "当前用户"}
                        </div>
                        <div className="mt-0.5 text-[11px] font-medium text-slate-400">
                          再多做一点，表现更稳定
                        </div>
                      </div>
                      <div className="shrink-0 text-right ml-4">
                        <div className="text-[9px] font-black uppercase tracking-[0.1em] text-slate-400">
                          SCORE
                        </div>
                        <div className="text-xl font-black text-slate-900">
                          {board.currentUserItem.metricValue || 0}
                        </div>
                      </div>
                    </div>
                  </div>
                ) : (
                  <div className="rounded-[2rem] border border-dashed border-slate-200 bg-slate-50/50 p-6 text-center">
                    <div className="mb-0 text-xs font-bold leading-relaxed text-slate-400">
                      登录并开始刷题后<br />你的成绩会出现在这里
                    </div>
                  </div>
                )}

                <div className="space-y-4 mt-4">
                  {secondaryList.map((item: any) => (
                    <div
                      key={`${board.key}-${item.userId}`}
                      className={`rounded-[2.2rem] border bg-white p-5 shadow-sm transition-all hover:shadow-md ${theme.panel}`}
                    >
                      <div className="flex items-center justify-between mb-4">
                        <div className={`flex items-center gap-2 text-[10px] font-black tracking-[0.2em] uppercase ${theme.accentText}`}>
                          <Medal className="h-3 w-3" />
                          Rank
                        </div>
                        <div className={`inline-flex items-center gap-1 rounded-full px-3 py-1 text-[9px] font-black uppercase tracking-[0.1em] text-white ${getRankBadgeClass(item.rank).split(' ')[0]}`}>
                          # {item.rank}
                        </div>
                      </div>

                      <div className="flex items-center gap-4">
                        <UserAvatar
                          src={item.userAvatar}
                          name={item.userName}
                          size={44}
                        />
                        <div className="min-w-0 flex-[4]">
                          <div className="truncate text-base font-black text-slate-900 2xl:min-w-[200px] 2xl:whitespace-normal 2xl:line-clamp-2">
                            {item.userName || "匿名用户"}
                          </div>
                          <div className="mt-0.5 text-[11px] font-medium text-slate-400">
                            {item.userRole === "admin" ? "官方领跑" : "榜上有名"}
                          </div>
                        </div>
                        <div className="shrink-0 text-right ml-4">
                          <div className="text-[9px] font-black uppercase tracking-[0.1em] text-slate-400">
                            SCORE
                          </div>
                          <div className="text-xl font-black text-slate-900">
                            {item.metricValue || 0}
                          </div>
                        </div>
                      </div>
                    </div>
                  ))}

                  {!rankingList.length ? (
                    <div className="rounded-[2rem] border border-dashed border-slate-200 px-4 py-8 text-center text-sm text-slate-400">
                      这条赛道还没有形成榜单，立刻开始刷题占领榜首！
                    </div>
                  ) : null}
                </div>
              </div>
            </article>
          );
        })}
      </div>
    </section>
  );
}
