import React from "react";
import { ArrowUpRight, BookOpenText, Crown, Medal, Users } from "lucide-react";
import UserAvatar from "@/components/UserAvatar";
import UserProfileHoverCard from "@/components/UserProfileHoverCard";

interface Props {
  leaderboard?: API.QuestionBankLeaderboardVO;
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
 * 题库榜单卡片
 */
export default function QuestionBankLeaderboardCard({ leaderboard }: Props) {
  if (!leaderboard) {
    return null;
  }

  const rankingList = leaderboard.rankingList || [];
  const champion = rankingList[0];
  const restList = champion ? rankingList.slice(1, 6) : rankingList.slice(0, 6);

  return (
    <section className="relative overflow-hidden rounded-[2.75rem] border border-slate-100 bg-white p-6 shadow-2xl shadow-slate-200/40 sm:p-8">
      <div className="absolute -left-10 top-8 h-36 w-36 rounded-full bg-primary/8 blur-3xl" />
      <div className="absolute -right-10 bottom-0 h-44 w-44 rounded-full bg-amber-300/12 blur-3xl" />

      <div className="relative grid gap-6 xl:grid-cols-[1.15fr_0.85fr]">
        <div className="overflow-hidden rounded-[2.2rem] border border-primary/10 bg-gradient-to-br from-primary/8 via-white to-blue-50 p-5 sm:p-6">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
            <div className="space-y-3">
              <div className="inline-flex items-center gap-2 rounded-full border border-primary/15 bg-white/80 px-4 py-2 text-xs font-black uppercase tracking-[0.24em] text-primary shadow-sm">
                <Crown className="h-3.5 w-3.5" />
                Rank Arena
              </div>
              <div className="space-y-2">
                <h3 className="text-2xl font-black tracking-tight text-slate-900 sm:text-3xl">
                  题库排行榜
                </h3>
                <p className="max-w-xl text-sm leading-7 text-slate-500">
                  {leaderboard.description || "按照题库内完成题数实时排序，看看谁正在这套题库里持续输出。"}
                </p>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3 sm:min-w-[220px]">
              <div className="rounded-[1.5rem] border border-white/80 bg-white/85 px-4 py-4 shadow-lg shadow-white/50">
                <div className="flex items-center gap-2 text-[11px] font-black uppercase tracking-[0.22em] text-slate-400">
                  <Users className="h-3.5 w-3.5 text-primary" />
                  榜单维度
                </div>
                <div className="mt-3 text-lg font-black text-slate-900">
                  {leaderboard.metricLabel || "完成题数"}
                </div>
              </div>
              <div className="rounded-[1.5rem] border border-white/80 bg-slate-950 px-4 py-4 text-white shadow-lg shadow-slate-200/60">
                <div className="flex items-center gap-2 text-[11px] font-black uppercase tracking-[0.22em] text-white/55">
                  <BookOpenText className="h-3.5 w-3.5" />
                  榜首成绩
                </div>
                <div className="mt-3 text-2xl font-black tracking-tight">
                  {champion?.metricValue || 0}
                </div>
              </div>
            </div>
          </div>

          {champion ? (
            <div className="mt-6 rounded-[1.9rem] border border-white/80 bg-white/80 p-4 shadow-xl shadow-white/40 sm:p-5">
              <div className="grid gap-4 md:grid-cols-[minmax(0,1fr)_140px] md:items-center">
                <UserProfileHoverCard user={champion} placement="topLeft">
                  <div className="flex min-w-0 items-center gap-3">
                    <div className="flex h-14 w-14 shrink-0 items-center justify-center rounded-[1.35rem] bg-gradient-to-br from-amber-500 to-orange-500 text-white shadow-xl shadow-amber-200/70">
                      <Medal className="h-6 w-6" />
                    </div>
                    <UserAvatar src={champion.userAvatar} name={champion.userName} size={48} />
                    <div className="min-w-0">
                      <div className="truncate text-lg font-black text-slate-900">
                        {champion.userName || "匿名用户"}
                      </div>
                      <div className="mt-1 text-sm text-slate-500">
                        当前题库榜首，节奏非常稳定。
                      </div>
                    </div>
                  </div>
                </UserProfileHoverCard>

                <div className="rounded-[1.4rem] bg-amber-50 px-4 py-4 text-right">
                  <div className="text-[11px] font-black uppercase tracking-[0.22em] text-amber-700/70">
                    {champion.metricText || leaderboard.metricLabel || "完成题数"}
                  </div>
                  <div className="mt-2 text-3xl font-black tracking-tight text-slate-900">
                    {champion.metricValue || 0}
                  </div>
                </div>
              </div>
            </div>
          ) : (
            <div className="mt-6 rounded-[1.9rem] border border-dashed border-slate-200 px-4 py-5 text-sm text-slate-400">
              当前题库还没有形成榜单，先去刷几道题试试看。
            </div>
          )}

          {leaderboard.currentUserItem ? (
            <div className="mt-5 rounded-[1.8rem] border border-primary/10 bg-primary/5 p-4">
              <div className="mb-3 flex items-center justify-between gap-3">
                <div className="text-xs font-black uppercase tracking-[0.22em] text-primary">
                  我的题库位置
                </div>
                <div className="inline-flex items-center gap-1 rounded-full bg-white px-3 py-1 text-[11px] font-black uppercase tracking-[0.18em] text-slate-500 shadow-sm">
                  <ArrowUpRight className="h-3.5 w-3.5" />
                  继续追赶
                </div>
              </div>

              <div className="grid gap-3 sm:grid-cols-[minmax(0,1fr)_82px_110px] sm:items-center">
                <UserProfileHoverCard user={leaderboard.currentUserItem} placement="topLeft">
                  <div className="flex min-w-0 items-center gap-3">
                    <UserAvatar
                      src={leaderboard.currentUserItem.userAvatar}
                      name={leaderboard.currentUserItem.userName}
                      size={40}
                    />
                    <div className="min-w-0">
                      <div className="truncate text-base font-black text-slate-900">
                        {leaderboard.currentUserItem.userName || "当前用户"}
                      </div>
                      <div className="mt-1 text-sm text-slate-500">
                        继续完成更多题目，你的名次会很快刷新。
                      </div>
                    </div>
                  </div>
                </UserProfileHoverCard>

                <div className="rounded-2xl bg-white px-3 py-3 text-center shadow-sm">
                  <div className="text-[11px] font-bold uppercase tracking-[0.18em] text-slate-400">排名</div>
                  <div className="mt-1 text-xl font-black text-slate-900">
                    #{leaderboard.currentUserItem.rank || "-"}
                  </div>
                </div>

                <div className="rounded-2xl bg-white px-3 py-3 text-center shadow-sm">
                  <div className="truncate text-[11px] font-bold uppercase tracking-[0.16em] text-slate-400">
                    {leaderboard.currentUserItem.metricText || leaderboard.metricLabel || "完成题数"}
                  </div>
                  <div className="mt-1 text-xl font-black text-slate-900">
                    {leaderboard.currentUserItem.metricValue || 0}
                  </div>
                </div>
              </div>
            </div>
          ) : null}
        </div>

        <div className="space-y-3">
          {restList.length ? (
            restList.map((item) => (
              <div
                key={`bank-rank-${item.userId}`}
                className="rounded-[1.7rem] border border-slate-100 bg-slate-50/80 px-4 py-4 transition-all hover:-translate-y-0.5 hover:bg-white hover:shadow-lg hover:shadow-slate-200/40"
              >
                <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                  <UserProfileHoverCard user={item} placement="topLeft">
                    <div className="min-w-0 flex items-center gap-3">
                      <div
                        className={`flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl text-sm font-black ${getRankBadgeClass(item.rank)}`}
                      >
                        {item.rank}
                      </div>
                      <UserAvatar src={item.userAvatar} name={item.userName} size={40} />
                      <div className="min-w-0">
                        <div className="truncate font-black text-slate-900">
                          {item.userName || "匿名用户"}
                        </div>
                        <div className="mt-1 text-xs text-slate-500">
                          {item.userRole === "admin" ? "管理员" : "学习者"}
                        </div>
                      </div>
                    </div>
                  </UserProfileHoverCard>

                  <div className="flex items-center justify-between gap-3 sm:justify-end">
                    <div className="text-xs font-bold uppercase tracking-[0.18em] text-slate-400">
                      {item.metricText || leaderboard.metricLabel || "完成题数"}
                    </div>
                    <div className="inline-flex min-w-[96px] items-center justify-center rounded-2xl bg-white px-4 py-2 text-lg font-black text-slate-900 shadow-sm">
                      {item.metricValue || 0}
                    </div>
                  </div>
                </div>
              </div>
            ))
          ) : (
            <div className="rounded-[1.7rem] border border-dashed border-slate-200 px-4 py-8 text-center text-sm text-slate-400">
              当前题库还没有更多上榜用户，先成为第一批冲榜的人吧。
            </div>
          )}
        </div>
      </div>
    </section>
  );
}
