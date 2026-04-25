import Link from "next/link";
import { headers } from "next/headers";
import { Activity, ArrowLeft, BookOpen, BriefcaseBusiness, CalendarClock, Flame, MapPin, NotebookPen, PenSquare, Sparkles } from "lucide-react";
import { getUserProfileVoByIdUsingGet } from "@/api/userController";
import { listQuestionBankVoByPageUsingPost } from "@/api/questionBankController";
import { listQuestionVoByPageUsingPost } from "@/api/questionController";
import QuestionBankList from "@/components/QuestionBankList";
import TagList from "@/components/TagList";
import UserAvatar from "@/components/UserAvatar";
import PublicAchievementStrip from "@/app/user/[id]/components/PublicAchievementStrip";
import PublicLearningInsights from "@/app/user/[id]/components/PublicLearningInsights";
import PublicLearningHeatmap from "@/app/user/[id]/components/PublicLearningHeatmap";
import PublicProfileOwnerActions from "@/app/user/[id]/components/PublicProfileOwnerActions";
import UserRelationPanel from "@/app/user/[id]/components/UserRelationPanel";

export const dynamic = "force-dynamic";

function isProfileFieldVisible(profile: API.UserProfileVO | undefined, field: string) {
  const visibleFields = profile?.profileVisibleFieldList;
  return !Array.isArray(visibleFields) || visibleFields.includes(field);
}

function formatDate(date?: string) {
  if (!date) {
    return "最近加入";
  }
  try {
    return new Date(date).toLocaleDateString("zh-CN", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
    });
  } catch {
    return date;
  }
}

export default async function PublicUserProfilePage({
  params,
}: {
  params: { id: string };
}) {
  const userId = params.id;
  if (!userId || !/^\d+$/.test(userId)) {
    return (
      <div className="py-24 text-center text-slate-400">
        无效的用户主页地址
      </div>
    );
  }

  const requestOptions = {
    headers: {
      cookie: headers().get("cookie") || "",
    },
  };

  const [profileResult, questionResult, questionBankResult] = await Promise.allSettled([
    getUserProfileVoByIdUsingGet({ id: userId }, requestOptions),
    listQuestionVoByPageUsingPost(
      {
        userId: userId as any,
        reviewStatus: 1,
        pageSize: 6,
        sortField: "createTime",
        sortOrder: "descend",
      },
      requestOptions,
    ),
    listQuestionBankVoByPageUsingPost(
      {
        userId: userId as any,
        reviewStatus: 1,
        pageSize: 6,
        sortField: "createTime",
        sortOrder: "descend",
      },
      requestOptions,
    ),
  ]);

  let profile: API.UserProfileVO | undefined;
  let questionList: API.QuestionVO[] = [];
  let questionBankList: API.QuestionBankVO[] = [];

  if (profileResult.status === "fulfilled") {
    const res = profileResult.value as API.BaseResponseUserProfileVO_;
    profile = res.data;
  }

  if (questionResult.status === "fulfilled") {
    const res = questionResult.value as API.BaseResponsePageQuestionVO_;
    questionList = res.data?.records || [];
  }

  if (questionBankResult.status === "fulfilled") {
    const res = questionBankResult.value as API.BaseResponsePageQuestionBankVO_;
    questionBankList = res.data?.records || [];
  }

  if (!profile?.user) {
    return (
      <div className="space-y-6 py-24 text-center">
        <div className="mx-auto flex h-20 w-20 items-center justify-center rounded-full border border-slate-100 bg-white shadow-xl shadow-slate-200/40">
          <Sparkles className="h-9 w-9 text-primary" />
        </div>
        <div className="space-y-2">
          <h1 className="text-2xl font-black text-slate-900">这个主页暂时无法访问</h1>
          <p className="text-slate-500">该用户可能不存在，或者当前资料未公开。</p>
        </div>
        <Link
          href="/"
          className="inline-flex h-11 items-center justify-center rounded-full bg-primary px-6 font-bold text-white shadow-lg shadow-primary/20 transition-all hover:scale-105 active:scale-95"
        >
          返回首页
        </Link>
      </div>
    );
  }

  const statCards = [
    isProfileFieldVisible(profile, "stats") ? {
      key: "practice",
      label: "累计刷题",
      value: profile.totalQuestionCount || 0,
      icon: <BookOpen className="h-5 w-5 text-primary" />,
    } : null,
    isProfileFieldVisible(profile, "stats") ? {
      key: "mastered",
      label: "已掌握题目",
      value: profile.masteredQuestionCount || 0,
      icon: <Sparkles className="h-5 w-5 text-emerald-500" />,
    } : null,
    isProfileFieldVisible(profile, "stats") ? {
      key: "active",
      label: "活跃天数",
      value: profile.activeDays || 0,
      icon: <Activity className="h-5 w-5 text-sky-500" />,
    } : null,
    isProfileFieldVisible(profile, "stats") ? {
      key: "streak",
      label: "连续学习",
      value: profile.currentStreak || 0,
      icon: <Flame className="h-5 w-5 text-rose-500" />,
    } : null,
    isProfileFieldVisible(profile, "content") ? {
      key: "submission",
      label: "公开题目",
      value: profile.approvedQuestionCount || 0,
      icon: <PenSquare className="h-5 w-5 text-amber-500" />,
    } : null,
    isProfileFieldVisible(profile, "content") ? {
      key: "bank",
      label: "公开题库",
      value: profile.approvedQuestionBankCount || 0,
      icon: <NotebookPen className="h-5 w-5 text-violet-500" />,
    } : null,
  ].filter(Boolean);
  const hasProfileBasics =
    isProfileFieldVisible(profile, "city") ||
    isProfileFieldVisible(profile, "career") ||
    isProfileFieldVisible(profile, "joinTime");
  const showActivity = isProfileFieldVisible(profile, "activity");
  const showContent = isProfileFieldVisible(profile, "content");
  const showRelation = isProfileFieldVisible(profile, "relation");
  const showStats = isProfileFieldVisible(profile, "stats");

  return (
    <div className="space-y-8 pb-20">
      <Link
        href="/"
        className="inline-flex items-center gap-2 px-2 text-sm font-bold text-slate-400 transition-colors hover:text-primary"
      >
        <ArrowLeft className="h-4 w-4" />
        返回首页
      </Link>

      <section className="overflow-hidden rounded-[2.5rem] border border-slate-100 bg-white p-8 shadow-2xl shadow-slate-200/40 sm:p-12">
        <div className="flex flex-col gap-8 lg:flex-row lg:items-start lg:justify-between">
          <div className="flex min-w-0 flex-1 items-start gap-5">
            <UserAvatar
              src={profile.user.userAvatar}
              name={profile.user.userName}
              size={88}
              className="ring-4 ring-slate-50"
            />
            <div className="min-w-0">
              <div className="flex flex-wrap items-center gap-3">
                <h1 className="text-3xl font-black tracking-tight text-slate-900 whitespace-normal break-words sm:text-4xl">
                  {profile.user.userName || "匿名用户"}
                </h1>
                {profile.user.userRole === "admin" ? (
                  <span className="rounded-full bg-amber-50 px-3 py-1 text-xs font-black uppercase tracking-wider text-amber-600">
                    ADMIN
                  </span>
                ) : null}
              </div>
              {isProfileFieldVisible(profile, "profile") ? (
                <p className="mt-4 max-w-3xl text-base leading-8 text-slate-500">
                  {profile.user.userProfile || "这位用户还没有填写个人简介。"}
                </p>
              ) : null}
              {hasProfileBasics ? (
                <div className="mt-5 flex flex-wrap gap-3 text-sm font-medium text-slate-500">
                {isProfileFieldVisible(profile, "city") ? (
                  <span className="inline-flex items-center gap-1.5 rounded-full bg-slate-50 px-4 py-2">
                    <MapPin className="h-4 w-4 text-primary" />
                    最近登录城市：{profile.user.city || "暂未识别"}
                  </span>
                ) : null}
                {isProfileFieldVisible(profile, "career") && profile.user.careerDirection ? (
                  <span className="inline-flex items-center gap-1.5 rounded-full bg-violet-50 px-4 py-2 text-violet-700">
                    <BriefcaseBusiness className="h-4 w-4" />
                    {profile.user.careerDirection}
                  </span>
                ) : null}
                {isProfileFieldVisible(profile, "joinTime") ? (
                  <span className="inline-flex items-center gap-1.5 rounded-full bg-slate-50 px-4 py-2">
                    <CalendarClock className="h-4 w-4 text-primary" />
                    加入于 {formatDate(profile.user.createTime)}
                  </span>
                ) : null}
                </div>
              ) : null}
              {isProfileFieldVisible(profile, "tags") && profile.user.interestTagList?.length ? (
                <div className="mt-4 flex flex-wrap gap-2">
                  {profile.user.interestTagList.map((tag) => (
                    <span
                      key={tag}
                      className="rounded-full border border-slate-200 bg-slate-50 px-3 py-1 text-xs font-semibold text-slate-600"
                    >
                      {tag}
                    </span>
                  ))}
                </div>
              ) : null}
            </div>
          </div>

          <div className="rounded-[2rem] border border-primary/10 bg-primary/5 px-5 py-4 text-sm leading-7 text-slate-600 lg:max-w-sm">
            这里展示的是用户选择公开的资料、学习数据和内容贡献。部分模块可能会根据对方的公开主页设置隐藏。
            <PublicProfileOwnerActions userId={profile.user.id} />
          </div>
        </div>

        {statCards.length ? (
        <div className="mt-8 grid grid-cols-2 gap-4 lg:grid-cols-3 xl:grid-cols-6">
          {statCards.map((item) => (
            <div
              key={item!.key}
              className="rounded-[1.75rem] border border-slate-100 bg-slate-50/70 px-5 py-5"
            >
              <div className="flex items-center gap-2 text-sm font-bold text-slate-400">
                {item!.icon}
                <span>{item!.label}</span>
              </div>
              <div className="mt-3 text-3xl font-black tracking-tight text-slate-900">
                {item!.value}
              </div>
            </div>
          ))}
        </div>
        ) : null}

        {showRelation ? (
        <UserRelationPanel
          user={profile.user}
          initialFollowerCount={profile.followerCount}
          initialFollowingCount={profile.followingCount}
          initialHasFollowed={profile.hasFollowed}
        />
        ) : null}
      </section>

      {showStats ? (
      <section className="rounded-[2.5rem] border border-slate-100 bg-white p-8 shadow-2xl shadow-slate-200/40">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <div className="text-xs font-black uppercase tracking-[0.2em] text-primary">
              Learning Profile
            </div>
            <h2 className="mt-3 text-2xl font-black tracking-tight text-slate-900">
              成就进度与刷题轨迹
            </h2>
          </div>
          <div className="text-sm text-slate-400">
            {new Date().getFullYear()} 年
          </div>
        </div>

        <div className="mt-6">
          <PublicLearningInsights profile={profile} />
        </div>

        <div className="mt-5 grid gap-5 xl:grid-cols-[minmax(0,0.95fr)_minmax(0,1.05fr)]">
          <PublicAchievementStrip achievementList={profile.achievementList} />
          <PublicLearningHeatmap recordList={profile.questionHistoryRecordList} />
        </div>
      </section>
      ) : null}

      {showActivity ? (
      <section className="rounded-[2.5rem] border border-slate-100 bg-white p-8 shadow-2xl shadow-slate-200/40">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <div className="text-xs font-black uppercase tracking-[0.2em] text-primary">
              Recent Activity
            </div>
            <h2 className="mt-3 text-2xl font-black tracking-tight text-slate-900">
              公开学习动态
            </h2>
          </div>
          <div className="text-sm text-slate-400">
            公开展示最近刷题、题目与题库动态
          </div>
        </div>

        <div className="mt-6 grid gap-4">
          {profile.recentActivityList?.length ? (
            profile.recentActivityList.map((activity, index) => (
              <div
                key={`${activity.type}-${activity.targetId || index}`}
                className="rounded-[1.75rem] border border-slate-100 bg-slate-50/70 px-5 py-5"
              >
                <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                  <div className="min-w-0">
                    <div className="flex flex-wrap items-center gap-2">
                      <span className="rounded-full bg-primary/10 px-3 py-1 text-xs font-black uppercase tracking-wider text-primary">
                        {activity.badge || "动态"}
                      </span>
                      <div className="text-lg font-black text-slate-900 whitespace-normal break-words">
                        {activity.title}
                      </div>
                    </div>
                    <div className="mt-3 text-sm leading-7 text-slate-500">
                      {activity.description}
                    </div>
                    {activity.targetUrl ? (
                      <Link
                        href={activity.targetUrl}
                        className="mt-4 inline-flex items-center gap-2 text-sm font-bold text-primary"
                      >
                        查看详情
                        <NotebookPen className="h-4 w-4" />
                      </Link>
                    ) : null}
                  </div>
                  <div className="shrink-0 text-sm text-slate-400">
                    {formatDate(activity.activityTime)}
                  </div>
                </div>
              </div>
            ))
          ) : (
            <div className="rounded-[1.75rem] border border-dashed border-slate-200 px-6 py-12 text-center text-sm text-slate-400">
              这位用户暂时还没有公开学习动态。
            </div>
          )}
        </div>
      </section>
      ) : null}

      {showContent ? (
      <section className="rounded-[2.5rem] border border-slate-100 bg-white p-8 shadow-2xl shadow-slate-200/40">
        {questionBankList.length ? (
          <div className="mb-10 space-y-6">
            <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
              <div>
                <div className="text-xs font-black uppercase tracking-[0.2em] text-primary">
                  Public Banks
                </div>
                <h2 className="mt-3 text-2xl font-black tracking-tight text-slate-900">
                  公开题库
                </h2>
              </div>
              <div className="text-sm text-slate-400">
                只展示通过审核后对外公开的题库
              </div>
            </div>
            <QuestionBankList questionBankList={questionBankList} />
          </div>
        ) : null}

        <div className={questionBankList.length ? "border-t border-slate-100 pt-10" : ""}>
        <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <div className="text-xs font-black uppercase tracking-[0.2em] text-primary">
              Latest Contributions
            </div>
            <h2 className="mt-3 text-2xl font-black tracking-tight text-slate-900">
              最近公开题目
            </h2>
          </div>
          <div className="text-sm text-slate-400">
            已通过审核的题目会展示在这里
          </div>
        </div>

        <div className="mt-6 grid grid-cols-1 gap-4">
          {questionList.length ? (
            questionList.map((question) => (
              <Link
                key={question.id}
                href={`/question/${question.id}`}
                className="rounded-[1.75rem] border border-slate-100 bg-slate-50/70 px-5 py-5 transition-all hover:-translate-y-0.5 hover:border-primary/20 hover:bg-white hover:shadow-xl hover:shadow-slate-200/30"
              >
                <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
                  <div className="min-w-0">
                    <div className="text-lg font-black text-slate-900 whitespace-normal break-words">
                      {question.title}
                    </div>
                    <div className="mt-3 line-clamp-2 text-sm leading-6 text-slate-500">
                      {question.content}
                    </div>
                    <div className="mt-4">
                      <TagList tagList={question.tagList} />
                    </div>
                    {question.difficulty ? (
                      <div className="mt-3">
                        <span
                          className={`inline-flex rounded-full px-3 py-1 text-xs font-bold ${
                            question.difficulty === "简单"
                              ? "bg-emerald-50 text-emerald-700"
                              : question.difficulty === "困难"
                                ? "bg-orange-50 text-orange-700"
                                : "bg-blue-50 text-blue-700"
                          }`}
                        >
                          题目难度：{question.difficulty}
                        </span>
                      </div>
                    ) : null}
                  </div>
                  <div className="shrink-0 text-sm text-slate-400">
                    发布于 {formatDate(question.createTime)}
                  </div>
                </div>
              </Link>
            ))
          ) : (
            <div className="rounded-[1.75rem] border border-dashed border-slate-200 px-6 py-12 text-center text-sm text-slate-400">
              这位用户暂时还没有公开题目。
            </div>
          )}
        </div>
        </div>
      </section>
      ) : null}
    </div>
  );
}
