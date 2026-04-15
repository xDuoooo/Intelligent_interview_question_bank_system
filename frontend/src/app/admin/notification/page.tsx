"use client";

import React from "react";
import dynamic from "next/dynamic";
import { Alert, Card, Skeleton, Typography } from "antd";
import { BellRing, Megaphone, UserRound } from "lucide-react";

const { Paragraph, Title } = Typography;

const NotificationSendCard = dynamic(() => import("./components/NotificationSendCard"), {
  ssr: false,
  loading: () => (
    <div className="rounded-[2rem] border border-slate-100 bg-white p-6 shadow-lg shadow-slate-200/40">
      <Skeleton active paragraph={{ rows: 10 }} />
    </div>
  ),
});

const NotificationRecordCard = dynamic(() => import("./components/NotificationRecordCard"), {
  ssr: false,
  loading: () => (
    <div className="rounded-[2rem] border border-slate-100 bg-white p-6 shadow-lg shadow-slate-200/40">
      <Skeleton active paragraph={{ rows: 12 }} />
    </div>
  ),
});

export default function AdminNotificationPage() {
  const [refreshToken, setRefreshToken] = React.useState(0);

  return (
    <div className="space-y-8">
      <section className="rounded-[2rem] border border-slate-200 bg-white/85 p-8 shadow-lg shadow-slate-200/50">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
          <div className="max-w-3xl">
            <div className="mb-3 inline-flex items-center gap-2 rounded-full bg-cyan-50 px-4 py-2 text-xs font-black uppercase tracking-[0.3em] text-cyan-600">
              <BellRing className="h-4 w-4" />
              Notification Hub
            </div>
            <Title level={2} className="!mb-3 !text-slate-900">
              通知管理
            </Title>
            <Paragraph className="!mb-0 !text-base !leading-7 !text-slate-500">
              在这里统一发送站内通知，并回看发送记录。适合发布系统公告、功能提醒、活动通知，或者针对单个用户补发重要消息。
            </Paragraph>
          </div>
          <Alert
            className="max-w-xl rounded-3xl border-0 bg-slate-50"
            type="info"
            showIcon
            message="支持单用户发送、广播普通用户、广播全站未封禁用户"
            description="广播会直接写入通知表；如果只是自动通知开关关闭，不影响管理员主动发送。"
          />
        </div>
      </section>

      <div className="grid gap-6 xl:grid-cols-[1.05fr_1.35fr]">
        <NotificationSendCard onSent={() => setRefreshToken((value) => value + 1)} />
        <NotificationRecordCard refreshToken={refreshToken} />
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        <Card className="rounded-[2rem] border-0 bg-slate-50 shadow-none">
          <div className="flex items-center gap-3">
            <div className="rounded-2xl bg-primary/10 p-3 text-primary">
              <UserRound className="h-5 w-5" />
            </div>
            <div>
              <div className="text-sm font-bold text-slate-800">指定用户通知</div>
              <div className="text-xs text-slate-500">适合处理审核结果、人工补发、客服跟进。</div>
            </div>
          </div>
        </Card>
        <Card className="rounded-[2rem] border-0 bg-slate-50 shadow-none">
          <div className="flex items-center gap-3">
            <div className="rounded-2xl bg-cyan-100 p-3 text-cyan-600">
              <Megaphone className="h-5 w-5" />
            </div>
            <div>
              <div className="text-sm font-bold text-slate-800">广播普通用户</div>
              <div className="text-xs text-slate-500">适合功能上新、学习活动、运营公告。</div>
            </div>
          </div>
        </Card>
        <Card className="rounded-[2rem] border-0 bg-slate-50 shadow-none">
          <div className="flex items-center gap-3">
            <div className="rounded-2xl bg-amber-100 p-3 text-amber-600">
              <BellRing className="h-5 w-5" />
            </div>
            <div>
              <div className="text-sm font-bold text-slate-800">记录可回溯</div>
              <div className="text-xs text-slate-500">支持按标题、用户、类型、状态快速回看通知历史。</div>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
}
