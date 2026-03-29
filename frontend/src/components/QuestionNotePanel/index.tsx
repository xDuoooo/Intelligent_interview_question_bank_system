"use client";

import React, { useCallback, useEffect, useMemo, useState } from "react";
import { useSelector } from "react-redux";
import { Button, Card, Empty, Input, Space, Tag, Typography, message } from "antd";
import { NotebookPen, Save, Trash2 } from "lucide-react";
import { RootState } from "@/stores";
import {
  deleteMyNoteUsingPost,
  getMyNoteUsingGet,
  saveMyNoteUsingPost,
} from "@/api/userQuestionNoteController";

const { Paragraph, Text } = Typography;

interface Props {
  questionId: number;
}

/**
 * 题目私有笔记面板
 */
export default function QuestionNotePanel({ questionId }: Props) {
  const loginUser = useSelector((state: RootState) => state.loginUser);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [content, setContent] = useState("");
  const [noteId, setNoteId] = useState<number | undefined>();
  const [lastUpdatedTime, setLastUpdatedTime] = useState<string>();

  const isLogin = useMemo(() => Boolean(loginUser?.id), [loginUser?.id]);

  const loadNote = useCallback(async () => {
    if (!questionId || !isLogin) {
      setContent("");
      setNoteId(undefined);
      setLastUpdatedTime(undefined);
      return;
    }
    setLoading(true);
    try {
      const res = await getMyNoteUsingGet({ questionId });
      const note = res.data;
      setContent(note?.content || "");
      setNoteId(note?.id);
      setLastUpdatedTime(note?.updateTime);
    } catch (error: any) {
      message.error("加载个人笔记失败：" + (error?.message || "请稍后重试"));
    } finally {
      setLoading(false);
    }
  }, [isLogin, questionId]);

  useEffect(() => {
    void loadNote();
  }, [loadNote]);

  const handleSave = async () => {
    const trimmedContent = content.trim();
    if (!trimmedContent) {
      message.warning("先写点内容再保存吧");
      return;
    }
    setSaving(true);
    try {
      await saveMyNoteUsingPost({
        questionId,
        content: trimmedContent,
      });
      message.success("个人笔记已保存");
      await loadNote();
    } catch (error: any) {
      message.error("保存失败：" + (error?.message || "请稍后重试"));
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    setSaving(true);
    try {
      await deleteMyNoteUsingPost({ questionId });
      setContent("");
      setNoteId(undefined);
      setLastUpdatedTime(undefined);
      message.success("个人笔记已删除");
    } catch (error: any) {
      message.error("删除失败：" + (error?.message || "请稍后重试"));
    } finally {
      setSaving(false);
    }
  };

  return (
    <Card
      className="rounded-[2.5rem] border border-slate-100 shadow-2xl shadow-slate-200/40"
      bodyStyle={{ padding: "2rem 2rem 1.5rem" }}
    >
      <div className="flex flex-col gap-4">
        <div className="flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between">
          <div>
            <div className="flex items-center gap-2 text-lg font-black text-slate-800">
              <NotebookPen className="h-5 w-5 text-primary" />
              个人笔记
            </div>
            <Paragraph className="!mb-0 !mt-2 text-slate-500">
              这里的内容只有你自己能看到，适合记录知识盲点、易错点和复盘要点。
            </Paragraph>
          </div>
          {noteId ? (
            <Tag color="processing" className="m-0 rounded-full px-3 py-1">
              最近更新 {lastUpdatedTime ? new Date(lastUpdatedTime).toLocaleString("zh-CN") : "刚刚"}
            </Tag>
          ) : null}
        </div>

        {!isLogin ? (
          <div className="rounded-[1.75rem] border border-dashed border-slate-200 bg-slate-50/70 px-6 py-10 text-center">
            <Empty
              description="登录后可以为每道题记录自己的私有笔记"
              image={Empty.PRESENTED_IMAGE_SIMPLE}
            />
          </div>
        ) : (
          <>
            <Input.TextArea
              value={content}
              onChange={(event) => setContent(event.target.value)}
              rows={8}
              maxLength={5000}
              showCount
              placeholder="记录自己的理解、易错点、追问思路、复习提醒..."
              className="rounded-[2rem] bg-slate-50/70"
              disabled={loading}
            />
            <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <Text className="text-sm text-slate-400">
                建议把“为什么错、下次怎么答、更深一层的追问点”写下来，后面复盘会很有帮助。
              </Text>
              <Space wrap>
                {noteId ? (
                  <Button
                    danger
                    icon={<Trash2 className="h-4 w-4" />}
                    onClick={handleDelete}
                    loading={saving}
                    className="rounded-2xl"
                  >
                    删除笔记
                  </Button>
                ) : null}
                <Button
                  type="primary"
                  icon={<Save className="h-4 w-4" />}
                  onClick={handleSave}
                  loading={saving}
                  className="rounded-2xl"
                >
                  保存笔记
                </Button>
              </Space>
            </div>
          </>
        )}
      </div>
    </Card>
  );
}
