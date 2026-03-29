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
  }
> = {
  overall: {
    icon: <Trophy className="h-5 w-5" />,
    accent: "from-amber-500 to-orange-500",
    accentSoft: "from-amber-50 via-white to-orange-50",
    accentText: "text-amber-700",
    badge: "border-amber-200 bg-amber-50 text-amber-700",
    glow: "bg-amber-300/25",
    panel: "border-amber-100 bg-amber-50/70",
    champion: "bg-gradient-to-br from-amber-500 to-orange-500 text-white shadow-xl shadow-amber-200/70",
  },
  active: {
    icon: <Activity className="h-5 w-5" />,
    accent: "from-emerald-500 to-teal-500",
    accentSoft: "from-emerald-50 via-white to-teal-50",
    accentText: "text-emerald-700",
    badge: "border-emerald-200 bg-emerald-50 text-emerald-700",
    glow: "bg-emerald-300/25",
    panel: "border-emerald-100 bg-emerald-50/70",
    champion: "bg-gradient-to-br from-emerald-500 to-teal-500 text-white shadow-xl shadow-emerald-200/70",
  },
  streak: {
    icon: <Flame className="h-5 w-5" />,
    accent: "from-rose-500 to-orange-500",
    accentSoft: "from-rose-50 via-white to-orange-50",
    accentText: "text-rose-700",
    badge: "border-rose-200 bg-rose-50 text-rose-700",
    glow: "bg-rose-300/25",
    panel: "border-rose-100 bg-rose-50/70",
    champion: "bg-gradient-to-br from-rose-500 to-orange-500 text-white shadow-xl shadow-rose-200/70",
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

function getBoardSummary(board: API.LeaderboardBoardVO) {
  const champion = board.rankingList?.[0];
  return champion?.metricValue || 0;
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
    <section className="space-y-8">
      <div className="relative overflow-hidden rounded-[2.75rem] border border-slate-100 bg-white px-6 py-8 shadow-2xl shadow-slate-200/40 sm:px-10 sm:py-10">
        <div className="absolute left-0 top-0 h-56 w-56 rounded-full bg-primary/6 blur-3xl" />
        <div className="absolute bottom-0 right-0 h-56 w-56 rounded-full bg-amber-300/10 blur-3xl" />

        <div className="relative flex flex-col gap-8 2xl:flex-row 2xl:items-end 2xl:justify-between">
          <div className="max-w-3xl space-y-4">
            <div className="inline-flex items-center gap-2 rounded-full border border-slate-200 bg-slate-50 px-4 py-2 text-xs font-black uppercase tracking-[0.28em] text-slate-600">
              <Sparkles className="h-3.5 w-3.5 text-primary" />
              Smart Ranking
            </div>
            <div className="space-y-3">
              <h2 className="text-3xl font-black tracking-tight text-slate-900 sm:text-5xl">
                智能榜单
              </h2>
              <p className="max-w-2xl text-sm leading-7 text-slate-500 sm:text-base">
                把综合成长、活跃频率和连续学习拆成三条独立赛道，让榜单既有竞技感，也能看清每个人最突出的学习状态。
              </p>
            </div>
          </div>

          <div className="grid gap-3 lg:grid-cols-2 2xl:min-w-[620px] 2xl:grid-cols-3">
            {boardList.map((board) => {
              const theme = getBoardTheme(board.key);
              return (
                <div
                  key={`summary-${board.key}`}
                  className={`rounded-[1.75rem] border px-4 py-4 shadow-sm backdrop-blur sm:min-h-[156px] ${theme.badge}`}
                >
                  <div className="flex items-center gap-3">
                    <span className={`flex h-10 w-10 items-center justify-center rounded-2xl bg-white/80 ${theme.accentText}`}>
                      {theme.icon}
                    </span>
                    <div className="min-w-0 flex-1">
                      <div className="text-sm font-black leading-tight text-slate-900 whitespace-normal">
                        {board.title}
                      </div>
                      <div className="mt-1 text-[11px] font-bold tracking-[0.16em] text-slate-400 whitespace-normal leading-tight">
                        {board.metricLabel}
                      </div>
                    </div>
                  </div>
                  <div className="mt-5 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
                    <div className="min-w-[110px] shrink-0">
                      <div className="text-[11px] font-bold tracking-[0.16em] text-slate-400 whitespace-nowrap">
                        当前榜首
                      </div>
                      <div className="mt-2 text-3xl font-black tracking-tight text-slate-900">
                        {getBoardSummary(board)}
                      </div>
                    </div>
                    <div className="rounded-2xl bg-white/80 px-3 py-2 shadow-sm sm:min-w-[144px] sm:text-right">
                      <div className="text-[11px] font-bold tracking-[0.16em] text-slate-400 whitespace-nowrap">
                        冠军
                      </div>
                      <div className="mt-1 text-sm font-black leading-tight text-slate-900 whitespace-normal">
                        {board.rankingList?.[0]?.userName || "待上榜"}
                      </div>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-2 2xl:grid-cols-3">
        {boardList.map((board) => {
          const theme = getBoardTheme(board.key);
          const champion = board.rankingList?.[0];
          const rankingList = board.rankingList || [];
          const secondaryList = champion ? rankingList.slice(1, 6) : rankingList.slice(0, 5);

          return (
            <article
              key={board.key}
              className="relative flex h-full flex-col overflow-hidden rounded-[2.5rem] border border-slate-100 bg-white p-6 shadow-2xl shadow-slate-200/40"
            >
              <div className={`absolute -right-10 top-10 h-40 w-40 rounded-full blur-3xl ${theme.glow}`} />

              <div className={`relative overflow-hidden rounded-[2.1rem] border bg-gradient-to-br p-5 ${theme.panel} ${theme.accentSoft}`}>
                <div className="flex flex-col gap-4 xl:flex-row xl:items-start xl:justify-between">
                  <div className="space-y-3">
                    <div className="flex items-center gap-3">
                      <span className={`flex h-12 w-12 items-center justify-center rounded-2xl bg-gradient-to-br ${theme.accent} text-white shadow-lg`}>
                        {theme.icon}
                      </span>
                      <div>
                        <div className="text-xl font-black text-slate-900">{board.title}</div>
                        <div className="mt-1 text-[11px] font-bold uppercase tracking-[0.24em] text-slate-400">
                          {board.metricLabel}
                        </div>
                      </div>
                    </div>
                    <p className="text-sm leading-6 text-slate-500">
                      {board.description || "基于近期学习行为实时刷新，展示当前表现最突出的学习者。"}
                    </p>
                  </div>

                  <div className={`inline-flex items-center gap-2 self-start rounded-full border bg-white/85 px-3 py-2 text-xs font-black ${theme.badge}`}>
                    <ShieldCheck className="h-3.5 w-3.5" />
                    动态更新
                  </div>
                </div>

                {champion ? (
                  <div className="mt-6 grid gap-4 rounded-[1.8rem] border border-white/80 bg-white/80 p-4 shadow-lg shadow-white/50 xl:grid-cols-[minmax(0,1fr)_140px] xl:items-center">
                    <UserProfileHoverCard user={champion} placement="topLeft">
                      <div className="flex min-w-0 items-center gap-3">
                        <div className={`flex h-14 w-14 shrink-0 items-center justify-center rounded-[1.25rem] ${theme.champion}`}>
                          <Medal className="h-6 w-6" />
                        </div>
                        <UserAvatar src={champion.userAvatar} name={champion.userName} size={48} />
                        <div className="min-w-0">
                          <div className="text-lg font-black leading-tight text-slate-900 whitespace-normal">
                            {champion.userName || "匿名用户"}
                          </div>
                          <div className="mt-1 text-sm text-slate-500">
                            {champion.userRole === "admin" ? "管理员领跑" : "当前榜首 · 继续保持节奏"}
                          </div>
                        </div>
                      </div>
                    </UserProfileHoverCard>

                    <div className="rounded-[1.35rem] bg-slate-950 px-4 py-4 text-white shadow-lg shadow-slate-200/60 xl:text-right">
                      <div className="text-[11px] font-bold tracking-[0.16em] leading-tight text-white/55 break-keep whitespace-nowrap">
                        {champion.metricText || board.metricLabel}
                      </div>
                      <div className="mt-2 text-3xl font-black tracking-tight">
                        {champion.metricValue || 0}
                      </div>
                    </div>
                  </div>
                ) : null}
              </div>

              <div className="mt-5 flex flex-1 flex-col gap-4">
                {board.currentUserItem ? (
                  <div className={`rounded-[1.8rem] border p-4 ${theme.panel}`}>
                    <div className="mb-3 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                      <div className={`text-xs font-black tracking-[0.16em] ${theme.accentText}`}>
                        我的站位
                      </div>
                      <div className="inline-flex items-center gap-1 rounded-full bg-white px-3 py-1 text-[11px] font-black uppercase tracking-[0.18em] text-slate-500 shadow-sm">
                        <ArrowUpRight className="h-3.5 w-3.5" />
                        继续冲榜
                      </div>
                    </div>

                    <div className="grid gap-3 xl:grid-cols-[minmax(0,1fr)_96px_132px] xl:items-center">
                      <UserProfileHoverCard user={board.currentUserItem} placement="topLeft">
                        <div className="flex min-w-0 items-center gap-3">
                          <UserAvatar
                            src={board.currentUserItem.userAvatar}
                            name={board.currentUserItem.userName}
                            size={40}
                          />
                          <div className="min-w-0">
                            <div className="text-base font-black leading-tight text-slate-900 whitespace-normal">
                              {board.currentUserItem.userName || "当前用户"}
                            </div>
                            <div className="mt-1 text-sm text-slate-500">
                              再多做一点，你的榜单表现会更稳定。
                            </div>
                          </div>
                        </div>
                      </UserProfileHoverCard>

                      <div className="rounded-2xl bg-white px-3 py-3 text-center shadow-sm">
                      <div className="text-[11px] font-bold uppercase tracking-[0.2em] text-slate-400 whitespace-nowrap">排名</div>
                        <div className="mt-1 text-xl font-black text-slate-900">
                          #{board.currentUserItem.rank || "-"}
                        </div>
                      </div>

                      <div className="rounded-2xl bg-white px-3 py-3 text-center shadow-sm">
                        <div className="text-[11px] font-bold tracking-[0.12em] leading-tight text-slate-400 break-keep whitespace-nowrap">
                          {board.currentUserItem.metricText || board.metricLabel}
                        </div>
                        <div className="mt-1 text-xl font-black text-slate-900">
                          {board.currentUserItem.metricValue || 0}
                        </div>
                      </div>
                    </div>
                  </div>
                ) : (
                  <div className="rounded-[1.8rem] border border-dashed border-slate-200 px-4 py-4 text-sm text-slate-400">
                    登录并开始刷题后，你的名次和当前成绩会出现在这里。
                  </div>
                )}

                <div className="space-y-3">
                  {secondaryList.map((item) => (
                    <div
                      key={`${board.key}-${item.userId}`}
                      className="rounded-[1.6rem] border border-slate-100 bg-slate-50/75 px-4 py-4 transition-all hover:-translate-y-0.5 hover:bg-white hover:shadow-lg hover:shadow-slate-200/40"
                    >
                      <div className="flex flex-col gap-3 xl:flex-row xl:items-center xl:justify-between">
                        <UserProfileHoverCard user={item} placement="topLeft">
                          <div className="min-w-0 flex items-center gap-3">
                            <div
                              className={`flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl text-sm font-black ${getRankBadgeClass(item.rank)}`}
                            >
                              {item.rank}
                            </div>
                            <UserAvatar src={item.userAvatar} name={item.userName} size={40} />
                            <div className="min-w-0">
                              <div className="font-black leading-tight text-slate-900 whitespace-normal">
                                {item.userName || "匿名用户"}
                              </div>
                              <div className="mt-1 text-xs text-slate-500">
                                {item.userRole === "admin" ? "管理员" : "学习者"}
                              </div>
                            </div>
                          </div>
                        </UserProfileHoverCard>

                        <div className="flex flex-col items-start gap-2 sm:flex-row sm:items-center sm:justify-between xl:min-w-[150px] xl:justify-end">
                          <div className="text-xs font-bold tracking-[0.12em] leading-tight text-slate-400 break-keep whitespace-nowrap">
                            {item.metricText || board.metricLabel}
                          </div>
                          <div className="inline-flex min-w-[96px] items-center justify-center rounded-2xl bg-white px-4 py-2 text-lg font-black text-slate-900 shadow-sm">
                            {item.metricValue || 0}
                          </div>
                        </div>
                      </div>
                    </div>
                  ))}

                  {!rankingList.length ? (
                    <div className="rounded-[1.6rem] border border-dashed border-slate-200 px-4 py-5 text-sm text-slate-400">
                      这条赛道还没有形成榜单，先开始刷题试试看。
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
