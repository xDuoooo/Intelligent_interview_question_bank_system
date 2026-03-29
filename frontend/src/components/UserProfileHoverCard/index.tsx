"use client";

import React, { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { Popover, Skeleton, type PopoverProps } from "antd";
import { useSelector } from "react-redux";
import { Activity, BookOpen, Flame, MapPin, PenSquare, Sparkles } from "lucide-react";
import { getUserProfileVoByIdUsingGet } from "@/api/userController";
import UserFollowButton from "@/components/UserFollowButton";
import { cn } from "@/lib/utils";
import UserAvatar from "@/components/UserAvatar";
import { RootState } from "@/stores";

type PublicUser = Pick<
  API.UserVO,
  "id" | "userName" | "userAvatar" | "userProfile" | "userRole" | "city" | "createTime" | "careerDirection" | "interestTagList"
>;

interface Props {
  user?: PublicUser | null;
  children: React.ReactNode;
  placement?: PopoverProps["placement"];
  triggerClassName?: string;
}

const profileCache = new Map<number, API.UserProfileVO>();
const profilePromiseCache = new Map<number, Promise<API.UserProfileVO | undefined>>();

function updateCachedProfile(userId: number, updater: (profile: API.UserProfileVO) => API.UserProfileVO) {
  const currentProfile = profileCache.get(userId);
  if (!currentProfile) {
    return;
  }
  profileCache.set(userId, updater(currentProfile));
}

async function fetchUserProfile(userId: number) {
  if (profileCache.has(userId)) {
    return profileCache.get(userId);
  }
  if (profilePromiseCache.has(userId)) {
    return profilePromiseCache.get(userId);
  }
  const task = getUserProfileVoByIdUsingGet({ id: userId })
    .then((res) => {
      if (res.data) {
        profileCache.set(userId, res.data);
        return res.data;
      }
      return undefined;
    })
    .finally(() => {
      profilePromiseCache.delete(userId);
    });
  profilePromiseCache.set(userId, task);
  return task;
}

function formatJoinDate(date?: string) {
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

/**
 * 公开用户悬浮名片
 */
export default function UserProfileHoverCard({
  user,
  children,
  placement = "top",
  triggerClassName,
}: Props) {
  const loginUser = useSelector((state: RootState) => state.loginUser);
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [profile, setProfile] = useState<API.UserProfileVO | undefined>(() =>
    user?.id ? profileCache.get(user.id) : undefined,
  );
  const [loadError, setLoadError] = useState<string>("");

  useEffect(() => {
    setProfile(user?.id ? profileCache.get(user.id) : undefined);
    setLoadError("");
  }, [user?.id]);

  const canOpen = Boolean(user?.id);
  const displayUser = useMemo(() => profile?.user || user, [profile?.user, user]);
  const isSelf = Boolean(loginUser?.id && displayUser?.id && loginUser.id === displayUser.id);

  const stats = useMemo(
    () => [
      {
        key: "practice",
        label: "刷题",
        value: profile?.totalQuestionCount ?? 0,
        icon: <BookOpen className="h-4 w-4 text-primary" />,
      },
      {
        key: "mastered",
        label: "掌握",
        value: profile?.masteredQuestionCount ?? 0,
        icon: <Sparkles className="h-4 w-4 text-emerald-500" />,
      },
      {
        key: "active",
        label: "活跃",
        value: profile?.activeDays ?? 0,
        icon: <Activity className="h-4 w-4 text-sky-500" />,
      },
      {
        key: "streak",
        label: "连续",
        value: profile?.currentStreak ?? 0,
        icon: <Flame className="h-4 w-4 text-rose-500" />,
      },
    ],
    [profile],
  );

  const loadProfile = async () => {
    if (!user?.id || loading || profile) {
      return;
    }
    setLoading(true);
    setLoadError("");
    try {
      const nextProfile = await fetchUserProfile(user.id);
      setProfile(nextProfile);
      if (!nextProfile) {
        setLoadError("暂时无法加载该用户资料");
      }
    } catch (error: any) {
      setLoadError(error?.message || "加载用户资料失败");
    } finally {
      setLoading(false);
    }
  };

  const handleFollowChange = (nextFollowed: boolean) => {
    const targetUserId = user?.id;
    if (!targetUserId) {
      return;
    }
    setProfile((currentProfile) => {
      if (!currentProfile) {
        return currentProfile;
      }
      const nextProfile = {
        ...currentProfile,
        hasFollowed: nextFollowed,
        followerCount: Math.max(0, Number(currentProfile.followerCount || 0) + (nextFollowed ? 1 : -1)),
      };
      updateCachedProfile(targetUserId, () => nextProfile);
      return nextProfile;
    });
  };

  const content = (
    <div className="w-[320px] space-y-4">
      <div className="flex items-start gap-3">
        <UserAvatar
          src={displayUser?.userAvatar}
          name={displayUser?.userName}
          size={52}
        />
        <div className="min-w-0 flex-1">
          <div className="flex items-center gap-2">
            <div className="truncate text-base font-black text-slate-900">
              {displayUser?.userName || "匿名用户"}
            </div>
            {displayUser?.userRole === "admin" ? (
              <span className="rounded-full bg-amber-50 px-2 py-0.5 text-[10px] font-black uppercase tracking-wider text-amber-600">
                ADMIN
              </span>
            ) : null}
          </div>
          <p className="mt-2 line-clamp-2 text-sm leading-6 text-slate-500">
            {displayUser?.userProfile || "这个人还没有填写个人简介。"}
          </p>
        </div>
      </div>

      {loading ? (
        <Skeleton active paragraph={{ rows: 3 }} title={false} />
      ) : (
        <>
          <div className="grid grid-cols-2 gap-3">
            {stats.map((item) => (
              <div
                key={item.key}
                className="rounded-2xl border border-slate-100 bg-slate-50/70 px-3 py-3"
              >
                <div className="flex items-center gap-2 text-xs font-bold text-slate-400">
                  {item.icon}
                  <span>{item.label}</span>
                </div>
                <div className="mt-2 text-lg font-black text-slate-900">{item.value}</div>
              </div>
            ))}
          </div>

          <div className="flex items-center justify-between rounded-2xl bg-slate-50 px-4 py-3 text-xs font-medium text-slate-500">
            <span className="inline-flex items-center gap-1.5">
              <MapPin className="h-3.5 w-3.5" />
              最近登录城市：{displayUser?.city || "暂未识别"}
            </span>
            <span>加入于 {formatJoinDate(displayUser?.createTime)}</span>
          </div>

          {displayUser?.careerDirection || displayUser?.interestTagList?.length ? (
            <div className="rounded-2xl border border-dashed border-slate-200 px-4 py-3">
              {displayUser.careerDirection ? (
                <div className="text-sm font-semibold text-slate-700">方向：{displayUser.careerDirection}</div>
              ) : null}
              {displayUser.interestTagList?.length ? (
                <div className="mt-2 flex flex-wrap gap-2">
                  {displayUser.interestTagList.slice(0, 5).map((tag) => (
                    <span
                      key={tag}
                      className="rounded-full bg-slate-50 px-3 py-1 text-xs font-semibold text-slate-500"
                    >
                      {tag}
                    </span>
                  ))}
                </div>
              ) : null}
            </div>
          ) : null}

          <div className="grid grid-cols-2 gap-3">
            <div className="rounded-2xl border border-slate-100 bg-white px-4 py-3">
              <div className="text-xs font-bold text-slate-400">粉丝</div>
              <div className="mt-2 text-lg font-black text-slate-900">{profile?.followerCount ?? 0}</div>
            </div>
            <div className="rounded-2xl border border-slate-100 bg-white px-4 py-3">
              <div className="text-xs font-bold text-slate-400">关注</div>
              <div className="mt-2 text-lg font-black text-slate-900">{profile?.followingCount ?? 0}</div>
            </div>
          </div>

          <div className="rounded-2xl border border-dashed border-slate-200 px-4 py-3 text-sm text-slate-500">
            公开题目 {profile?.approvedQuestionCount ?? 0} 道，点击主页可以查看完整公开资料和最近题目。
          </div>
        </>
      )}

      {loadError ? <div className="text-sm text-red-400">{loadError}</div> : null}

      <div className={cn("grid gap-3", isSelf ? "grid-cols-1" : "grid-cols-2")}>
        {!isSelf ? (
          <UserFollowButton
            userId={displayUser?.id}
            initialFollowed={Boolean(profile?.hasFollowed)}
            onChange={handleFollowChange}
            className="h-10 rounded-xl font-bold"
          />
        ) : null}
        <Link
          href={`/user/${user?.id}`}
          prefetch={false}
          className="flex h-10 items-center justify-center rounded-xl bg-primary text-sm font-bold text-white transition-all hover:scale-[1.02] active:scale-95"
        >
          进入主页
        </Link>
      </div>
    </div>
  );

  if (!canOpen) {
    return <>{children}</>;
  }

  const triggerNode = (
    <Link
      href={`/user/${user?.id}`}
      prefetch={false}
      className={cn(
        "inline-flex min-w-0 cursor-pointer hover:no-underline",
        triggerClassName,
      )}
    >
      {children}
    </Link>
  );

  return (
    <Popover
      open={open}
      onOpenChange={(nextOpen) => {
        setOpen(nextOpen);
        if (nextOpen) {
          void loadProfile();
        }
      }}
      trigger={["hover"]}
      placement={placement}
      mouseEnterDelay={0.12}
      getPopupContainer={() => document.body}
      zIndex={1600}
      overlayClassName="user-profile-hover-card"
      content={content}
    >
      {triggerNode}
    </Popover>
  );
}
