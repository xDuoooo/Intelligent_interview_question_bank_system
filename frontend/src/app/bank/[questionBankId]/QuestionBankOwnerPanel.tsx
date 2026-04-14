"use client";

import React, { useMemo, useState } from "react";
import Link from "next/link";
import { Alert, Button, Space, Tag, Typography, message } from "antd";
import { Clock3, EyeOff, SendHorizonal, ShieldAlert, ShieldCheck } from "lucide-react";
import { submitQuestionBankReviewUsingPost } from "@/api/questionBankController";
import {
  QUESTION_REVIEW_STATUS_COLOR_MAP,
  QUESTION_REVIEW_STATUS_ENUM,
  QUESTION_REVIEW_STATUS_TEXT_MAP,
} from "@/constants/question";
import { formatDateTime } from "@/lib/utils";

const { Paragraph, Text } = Typography;

interface Props {
  questionBankId: string | number;
  reviewStatus?: number;
  reviewMessage?: string;
  reviewTime?: string;
  isOwner?: boolean;
  isAdmin?: boolean;
}

const QuestionBankOwnerPanel: React.FC<Props> = ({
  questionBankId,
  reviewStatus,
  reviewMessage,
  reviewTime,
  isOwner = false,
  isAdmin = false,
}) => {
  const [submitting, setSubmitting] = useState(false);
  const status = Number(reviewStatus ?? QUESTION_REVIEW_STATUS_ENUM.APPROVED);
  const canSubmitReview =
    isOwner &&
    (status === QUESTION_REVIEW_STATUS_ENUM.PRIVATE || status === QUESTION_REVIEW_STATUS_ENUM.REJECTED);

  const statusSummary = useMemo(() => {
    if (status === QUESTION_REVIEW_STATUS_ENUM.PRIVATE) {
      return {
        title: "当前题库仍是私有草稿",
        description: "现在只有你和管理员能看到这份题库。准备公开后，再主动提交审核会更稳。",
        icon: <EyeOff className="h-5 w-5 text-slate-500" />,
      };
    }
    if (status === QUESTION_REVIEW_STATUS_ENUM.PENDING) {
      return {
        title: "题库已提交审核",
        description: "审核通过前，这份题库还不会出现在首页、公开题库列表和他人可见页面里。",
        icon: <Clock3 className="h-5 w-5 text-amber-500" />,
      };
    }
    if (status === QUESTION_REVIEW_STATUS_ENUM.REJECTED) {
      return {
        title: "题库暂未通过审核",
        description: "你可以根据审核意见调整内容，然后重新提交审核。",
        icon: <ShieldAlert className="h-5 w-5 text-red-500" />,
      };
    }
    return {
      title: "题库已经公开",
      description: "现在其他用户可以在公开题库列表、首页推荐和你的公开主页里看到它。",
      icon: <ShieldCheck className="h-5 w-5 text-emerald-500" />,
    };
  }, [status]);

  const handleSubmitReview = async () => {
    if (!questionBankId || submitting) {
      return;
    }
    setSubmitting(true);
    const hide = message.loading("正在提交题库审核");
    try {
      await submitQuestionBankReviewUsingPost({ id: questionBankId });
      hide();
      message.success("题库已提交审核，刷新后可查看最新状态");
      window.location.reload();
    } catch (error: any) {
      hide();
      message.error("提交审核失败，" + (error?.message || "请稍后重试"));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section className="rounded-[2.5rem] border border-slate-100 bg-white p-8 shadow-2xl shadow-slate-200/30">
      <div className="flex flex-col gap-6 lg:flex-row lg:items-start lg:justify-between">
        <div className="space-y-4">
          <div className="flex flex-wrap items-center gap-3">
            <Tag
              color={QUESTION_REVIEW_STATUS_COLOR_MAP[status] || "default"}
              className="m-0 rounded-full px-4 py-1.5 text-sm font-bold"
            >
              {QUESTION_REVIEW_STATUS_TEXT_MAP[status] || "未知状态"}
            </Tag>
            {reviewTime ? (
              <Text type="secondary" className="inline-flex items-center gap-1 text-sm">
                <Clock3 className="h-4 w-4" />
                最近审核：{formatDateTime(reviewTime)}
              </Text>
            ) : null}
          </div>
          <div className="space-y-2">
            <div className="flex items-center gap-2 text-lg font-black text-slate-900">
              {statusSummary.icon}
              <span>{statusSummary.title}</span>
            </div>
            <Paragraph className="!mb-0 max-w-3xl text-sm leading-7 text-slate-500">
              {isOwner ? statusSummary.description : "你当前正在以管理员身份查看这份题库。"}
            </Paragraph>
          </div>
        </div>

        <Space wrap size="middle">
          {canSubmitReview ? (
            <Button
              type="primary"
              size="large"
              icon={<SendHorizonal className="h-4 w-4" />}
              loading={submitting}
              onClick={handleSubmitReview}
              className="rounded-full px-6 font-bold"
            >
              {status === QUESTION_REVIEW_STATUS_ENUM.REJECTED ? "重新提交审核" : "提交审核"}
            </Button>
          ) : null}
          {isOwner ? (
            <Link href="/user/center?tab=banks">
              <Button size="large" className="rounded-full px-6 font-bold">
                前往我的题库
              </Button>
            </Link>
          ) : null}
          {isAdmin ? (
            <Link href="/admin/bank">
              <Button size="large" className="rounded-full px-6 font-bold">
                前往题库审核
              </Button>
            </Link>
          ) : null}
        </Space>
      </div>

      {status !== QUESTION_REVIEW_STATUS_ENUM.APPROVED && reviewMessage ? (
        <div className="mt-6">
          <Alert
            type={status === QUESTION_REVIEW_STATUS_ENUM.REJECTED ? "error" : "info"}
            showIcon
            message="审核说明"
            description={reviewMessage}
            className="rounded-3xl border-0 bg-slate-50"
          />
        </div>
      ) : null}
    </section>
  );
};

export default QuestionBankOwnerPanel;
