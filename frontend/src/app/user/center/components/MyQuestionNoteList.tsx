"use client";

import React, { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import { Button, Empty, List, Skeleton, Tag, Typography } from "antd";
import { BookOpen, NotebookPen } from "lucide-react";
import { listMyNoteByPageUsingPost } from "@/api/userQuestionNoteController";
import TagList from "@/components/TagList";

const { Paragraph, Text, Title } = Typography;

/**
 * 我的题目笔记列表
 */
export default function MyQuestionNoteList() {
  const [loading, setLoading] = useState(true);
  const [current, setCurrent] = useState(1);
  const [pageSize] = useState(10);
  const [notePage, setNotePage] = useState<API.PageUserQuestionNoteVO_>();

  const loadData = useCallback(async (nextCurrent = current) => {
    setLoading(true);
    try {
      const res = await listMyNoteByPageUsingPost({
        current: nextCurrent,
        pageSize,
      });
      setNotePage(res.data);
      setCurrent(nextCurrent);
    } finally {
      setLoading(false);
    }
  }, [current, pageSize]);

  useEffect(() => {
    void loadData(1);
  }, [loadData]);

  const records = notePage?.records || [];

  return (
    <div className="space-y-6">
      <div className="rounded-[2rem] border border-slate-100 bg-white p-6 shadow-xl shadow-slate-200/30">
        <div className="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
          <div>
            <Title level={4} style={{ margin: 0 }}>
              我的题目笔记
            </Title>
            <Paragraph type="secondary" style={{ marginBottom: 0 }}>
              这里会集中展示你记录过的私有笔记，方便回看和二次复习。
            </Paragraph>
          </div>
          <Tag color="blue" className="m-0 rounded-full px-4 py-1">
            共 {notePage?.total || 0} 条
          </Tag>
        </div>
      </div>

      <div className="rounded-[2rem] border border-slate-100 bg-white p-6 shadow-xl shadow-slate-200/30">
        {loading ? (
          <Skeleton active paragraph={{ rows: 8 }} />
        ) : records.length ? (
          <List
            dataSource={records}
            split={false}
            renderItem={(item) => (
              <List.Item className="!px-0 !py-4">
                <div className="w-full rounded-[1.75rem] border border-slate-100 bg-slate-50/70 p-5 transition-all hover:border-primary/20 hover:bg-white">
                  <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                    <div className="min-w-0 flex-1">
                      <Link href={`/question/${item.questionId}`} className="block">
                        <div className="flex items-center gap-2 text-lg font-black text-slate-900 transition-colors hover:text-primary">
                          <BookOpen className="h-4 w-4 text-primary" />
                          <span className="whitespace-normal break-words">
                            {item.question?.title || `题目 #${item.questionId}`}
                          </span>
                        </div>
                      </Link>
                      {item.question?.tagList?.length ? (
                        <div className="mt-3">
                          <TagList tagList={item.question.tagList.slice(0, 4)} />
                        </div>
                      ) : null}
                      <div className="mt-4 rounded-2xl border border-slate-100 bg-white px-4 py-4">
                        <div className="mb-2 flex items-center gap-2 text-sm font-bold text-slate-600">
                          <NotebookPen className="h-4 w-4 text-primary" />
                          我的笔记
                        </div>
                        <Paragraph className="!mb-0 whitespace-pre-wrap text-slate-600">
                          {item.content}
                        </Paragraph>
                      </div>
                    </div>
                    <div className="shrink-0 text-sm text-slate-400">
                      最近更新 {item.updateTime ? new Date(item.updateTime).toLocaleString("zh-CN") : "-"}
                    </div>
                  </div>
                </div>
              </List.Item>
            )}
          />
        ) : (
          <Empty
            description="你还没有记录任何题目笔记"
            image={Empty.PRESENTED_IMAGE_SIMPLE}
          />
        )}

        {(notePage?.total || 0) > pageSize ? (
          <div className="mt-6 flex items-center justify-end gap-3">
            <Button
              disabled={current <= 1 || loading}
              onClick={() => void loadData(current - 1)}
              className="rounded-2xl"
            >
              上一页
            </Button>
            <Text type="secondary">
              第 {current} / {notePage?.pages || 1} 页
            </Text>
            <Button
              disabled={current >= Number(notePage?.pages || 1) || loading}
              onClick={() => void loadData(current + 1)}
              className="rounded-2xl"
            >
              下一页
            </Button>
          </div>
        ) : null}
      </div>
    </div>
  );
}
