import Link from "next/link";
import { headers } from "next/headers";
import { ArrowLeft, BookOpen, CalendarClock, Flame, MapPin, PenSquare, Sparkles } from "lucide-react";
import { getUserProfileVoByIdUsingGet } from "@/api/userController";
import { listQuestionVoByPageUsingPost } from "@/api/questionController";
import TagList from "@/components/TagList";
import UserAvatar from "@/components/UserAvatar";
import UserRelationPanel from "@/app/user/[id]/components/UserRelationPanel";

export const dynamic = "force-dynamic";

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
  const userId = Number(params.id);
  if (!Number.isFinite(userId) || userId <= 0) {
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

  const [profileResult, questionResult] = await Promise.allSettled([
    getUserProfileVoByIdUsingGet({ id: userId }, requestOptions),
    listQuestionVoByPageUsingPost(
      {
        userId,
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

  if (profileResult.status === "fulfilled") {
    const res = profileResult.value as API.BaseResponseUserProfileVO_;
    profile = res.data;
  }

  if (questionResult.status === "fulfilled") {
    const res = questionResult.value as API.BaseResponsePageQuestionVO_;
    questionList = res.data?.records || [];
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
    {
      key: "practice",
      label: "累计刷题",
      value: profile.totalQuestionCount || 0,
      icon: <BookOpen className="h-5 w-5 text-primary" />,
    },
    {
      key: "mastered",
      label: "已掌握题目",
      value: profile.masteredQuestionCount || 0,
      icon: <Sparkles className="h-5 w-5 text-emerald-500" />,
    },
    {
      key: "streak",
      label: "连续学习",
      value: profile.currentStreak || 0,
      icon: <Flame className="h-5 w-5 text-rose-500" />,
    },
    {
      key: "submission",
      label: "公开投稿",
      value: profile.approvedQuestionCount || 0,
      icon: <PenSquare className="h-5 w-5 text-amber-500" />,
    },
  ];

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
                <h1 className="truncate text-3xl font-black tracking-tight text-slate-900 sm:text-4xl">
                  {profile.user.userName || "匿名用户"}
                </h1>
                {profile.user.userRole === "admin" ? (
                  <span className="rounded-full bg-amber-50 px-3 py-1 text-xs font-black uppercase tracking-wider text-amber-600">
                    ADMIN
                  </span>
                ) : null}
              </div>
              <p className="mt-4 max-w-3xl text-base leading-8 text-slate-500">
                {profile.user.userProfile || "这位用户还没有填写个人简介。"}
              </p>
              <div className="mt-5 flex flex-wrap gap-3 text-sm font-medium text-slate-500">
                <span className="inline-flex items-center gap-1.5 rounded-full bg-slate-50 px-4 py-2">
                  <MapPin className="h-4 w-4 text-primary" />
                  {profile.user.city || "未公开城市"}
                </span>
                <span className="inline-flex items-center gap-1.5 rounded-full bg-slate-50 px-4 py-2">
                  <CalendarClock className="h-4 w-4 text-primary" />
                  加入于 {formatDate(profile.user.createTime)}
                </span>
              </div>
            </div>
          </div>

          <div className="rounded-[2rem] border border-primary/10 bg-primary/5 px-5 py-4 text-sm leading-7 text-slate-600 lg:max-w-sm">
            这里展示的是公开资料、公开学习数据以及关注关系。你可以直接查看 Ta 的粉丝和关注列表，也可以在这里完成关注。
          </div>
        </div>

        <div className="mt-8 grid grid-cols-2 gap-4 lg:grid-cols-4">
          {statCards.map((item) => (
            <div
              key={item.key}
              className="rounded-[1.75rem] border border-slate-100 bg-slate-50/70 px-5 py-5"
            >
              <div className="flex items-center gap-2 text-sm font-bold text-slate-400">
                {item.icon}
                <span>{item.label}</span>
              </div>
              <div className="mt-3 text-3xl font-black tracking-tight text-slate-900">
                {item.value}
              </div>
            </div>
          ))}
        </div>

        <UserRelationPanel
          user={profile.user}
          initialFollowerCount={profile.followerCount}
          initialFollowingCount={profile.followingCount}
          initialHasFollowed={profile.hasFollowed}
        />
      </section>

      <section className="rounded-[2.5rem] border border-slate-100 bg-white p-8 shadow-2xl shadow-slate-200/40">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <div className="text-xs font-black uppercase tracking-[0.2em] text-primary">
              Latest Contributions
            </div>
            <h2 className="mt-3 text-2xl font-black tracking-tight text-slate-900">
              最近公开投稿
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
                    <div className="truncate text-lg font-black text-slate-900">
                      {question.title}
                    </div>
                    <div className="mt-3 line-clamp-2 text-sm leading-6 text-slate-500">
                      {question.content}
                    </div>
                    <div className="mt-4">
                      <TagList tagList={question.tagList} />
                    </div>
                  </div>
                  <div className="shrink-0 text-sm text-slate-400">
                    发布于 {formatDate(question.createTime)}
                  </div>
                </div>
              </Link>
            ))
          ) : (
            <div className="rounded-[1.75rem] border border-dashed border-slate-200 px-6 py-12 text-center text-sm text-slate-400">
              这位用户暂时还没有公开投稿的题目。
            </div>
          )}
        </div>
      </section>
    </div>
  );
}
