"use client";

import React, { useCallback, useEffect, useMemo, useState } from "react";
import { Alert, Button, Empty, Input, List, message, Modal, Pagination, Space, Tag, Typography } from "antd";
import {
  addQuestionBankQuestionUsingPost,
  listMyQuestionBankQuestionVoByPageUsingPost,
  removeQuestionBankQuestionUsingPost,
} from "@/api/questionBankQuestionController";
import { listMyQuestionVoByPageUsingPost } from "@/api/questionController";
import {
  QUESTION_DIFFICULTY_COLOR_MAP,
  QUESTION_REVIEW_STATUS_COLOR_MAP,
  QUESTION_REVIEW_STATUS_TEXT_MAP,
} from "@/constants/question";
import TagList from "@/components/TagList";
import { formatDateTime } from "@/lib/utils";

const { Paragraph, Text, Title } = Typography;

interface Props {
  open: boolean;
  questionBank?: API.QuestionBankVO;
  onCancel: () => void;
}

const ManageQuestionBankQuestionsModal: React.FC<Props> = ({ open, questionBank, onCancel }) => {
  const [loading, setLoading] = useState(false);
  const [actionLoadingId, setActionLoadingId] = useState<string | number>();
  const [questionList, setQuestionList] = useState<API.QuestionVO[]>([]);
  const [total, setTotal] = useState(0);
  const [current, setCurrent] = useState(1);
  const [searchText, setSearchText] = useState("");
  const [joinedQuestionIdSet, setJoinedQuestionIdSet] = useState<Set<string>>(new Set());
  const pageSize = 6;

  const loadJoinedQuestionIds = useCallback(async (questionBankId?: string | number) => {
    if (!questionBankId) {
      setJoinedQuestionIdSet(new Set());
      return;
    }
    try {
      const res = await listMyQuestionBankQuestionVoByPageUsingPost({
        current: 1,
        pageSize: 200,
        questionBankId,
        sortField: "createTime",
        sortOrder: "descend",
      });
      const nextIdSet = new Set(
        (res.data?.records || [])
          .map((item) => item.questionId)
          .filter((id): id is string | number => id !== null && id !== undefined)
          .map((id) => String(id)),
      );
      setJoinedQuestionIdSet(nextIdSet);
    } catch (error: any) {
      message.error("加载题库内题目失败，" + (error?.message || "请稍后重试"));
    }
  }, []);

  const loadQuestionList = useCallback(async (
    page = 1,
    keyword = searchText,
  ) => {
    setLoading(true);
    try {
      const res = await listMyQuestionVoByPageUsingPost({
        current: page,
        pageSize,
        title: keyword.trim() || undefined,
        sortField: "updateTime",
        sortOrder: "descend",
      });
      setQuestionList(res.data?.records || []);
      setTotal(Number(res.data?.total) || 0);
      setCurrent(page);
    } catch (error: any) {
      message.error("加载我的题目失败，" + (error?.message || "请稍后重试"));
    } finally {
      setLoading(false);
    }
  }, [searchText]);

  useEffect(() => {
    if (!open || !questionBank?.id) {
      return;
    }
    void loadJoinedQuestionIds(questionBank.id);
    void loadQuestionList(1, "");
    setSearchText("");
  }, [loadJoinedQuestionIds, loadQuestionList, open, questionBank?.id]);

  const joinedCount = joinedQuestionIdSet.size;

  const helperText = useMemo(() => {
    if (!questionBank?.title) {
      return "可以从自己提交的题目里挑选内容，逐步沉淀到题库中。";
    }
    return `当前正在管理《${questionBank.title}》中的题目。已加入 ${joinedCount} 道题，可以继续补充或移除。`;
  }, [joinedCount, questionBank?.title]);

  const handleJoin = async (questionId?: string | number) => {
    if (!questionBank?.id || !questionId) {
      return;
    }
    setActionLoadingId(questionId);
    try {
      await addQuestionBankQuestionUsingPost({
        questionBankId: questionBank.id,
        questionId,
      });
      message.success("已加入题库");
      setJoinedQuestionIdSet((prev) => {
        const next = new Set(prev);
        next.add(String(questionId));
        return next;
      });
    } catch (error: any) {
      message.error("加入失败，" + (error?.message || "请稍后重试"));
    } finally {
      setActionLoadingId(undefined);
    }
  };

  const handleRemove = async (questionId?: string | number) => {
    if (!questionBank?.id || !questionId) {
      return;
    }
    setActionLoadingId(questionId);
    try {
      await removeQuestionBankQuestionUsingPost({
        questionBankId: questionBank.id,
        questionId,
      });
      message.success("已从题库移除");
      setJoinedQuestionIdSet((prev) => {
        const next = new Set(prev);
        next.delete(String(questionId));
        return next;
      });
    } catch (error: any) {
      message.error("移除失败，" + (error?.message || "请稍后重试"));
    } finally {
      setActionLoadingId(undefined);
    }
  };

  return (
    <Modal
      title="管理题库题目"
      open={open}
      onCancel={onCancel}
      footer={null}
      width={900}
      destroyOnClose
    >
      <div className="space-y-5">
        <Alert
          type="info"
          showIcon
          message="题库内容管理"
          description={helperText}
          className="rounded-2xl border border-blue-100/80 bg-blue-50/70"
        />

        <div className="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
          <div>
            <Title level={5} style={{ margin: 0 }}>
              从我的题目中选择
            </Title>
            <Paragraph type="secondary" style={{ marginBottom: 0 }}>
              当前只支持把你自己创建的题目加入自己的题库，这样数据归属会更清晰。
            </Paragraph>
          </div>
          <div className="flex w-full max-w-md gap-2">
            <Input
              allowClear
              value={searchText}
              placeholder="搜索我的题目标题"
              onChange={(event) => setSearchText(event.target.value)}
              onPressEnter={() => void loadQuestionList(1, searchText)}
            />
            <Button onClick={() => void loadQuestionList(1, searchText)} loading={loading}>
              搜索
            </Button>
          </div>
        </div>

        <List
          loading={loading}
          locale={{
            emptyText: <Empty description="还没有找到可管理的题目" image={Empty.PRESENTED_IMAGE_SIMPLE} />,
          }}
          dataSource={questionList}
          renderItem={(question) => {
            const joined = question.id !== undefined && joinedQuestionIdSet.has(String(question.id));
            return (
              <List.Item className="!px-0">
                <div className="w-full rounded-[1.5rem] border border-slate-100 bg-white p-5 shadow-sm shadow-slate-200/30">
                  <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                    <div className="min-w-0 flex-1 space-y-3">
                      <div className="space-y-2">
                        <Title level={5} style={{ margin: 0 }}>
                          {question.title}
                        </Title>
                        <Paragraph type="secondary" className="!mb-0">
                          {question.content?.slice(0, 120) || "这道题还没有补充题目内容摘要。"}
                        </Paragraph>
                      </div>
                      <Space wrap size={[8, 8]}>
                        {question.difficulty ? (
                          <Tag color={QUESTION_DIFFICULTY_COLOR_MAP[question.difficulty] || "default"}>
                            难度：{question.difficulty}
                          </Tag>
                        ) : null}
                        {question.reviewStatus !== undefined ? (
                          <Tag color={QUESTION_REVIEW_STATUS_COLOR_MAP[question.reviewStatus] || "default"}>
                            {QUESTION_REVIEW_STATUS_TEXT_MAP[question.reviewStatus] || "未知状态"}
                          </Tag>
                        ) : null}
                        <Text type="secondary">更新于 {formatDateTime(question.updateTime)}</Text>
                      </Space>
                      {question.tagList?.length ? (
                        <TagList tagList={question.tagList} />
                      ) : null}
                    </div>
                    <div className="flex shrink-0 flex-col gap-2 lg:min-w-[132px]">
                      {joined ? (
                        <Button
                          danger
                          loading={actionLoadingId === question.id}
                          onClick={() => void handleRemove(question.id)}
                        >
                          从题库移除
                        </Button>
                      ) : (
                        <Button
                          type="primary"
                          loading={actionLoadingId === question.id}
                          onClick={() => void handleJoin(question.id)}
                        >
                          加入题库
                        </Button>
                      )}
                      <Text type="secondary" className="text-center text-xs">
                        {joined ? "当前已在这个题库中" : "当前还未加入题库"}
                      </Text>
                    </div>
                  </div>
                </div>
              </List.Item>
            );
          }}
        />

        {total > pageSize ? (
          <div className="flex justify-end">
            <Pagination
              current={current}
              pageSize={pageSize}
              total={total}
              showSizeChanger={false}
              onChange={(page) => void loadQuestionList(page, searchText)}
            />
          </div>
        ) : null}
      </div>
    </Modal>
  );
};

export default ManageQuestionBankQuestionsModal;
