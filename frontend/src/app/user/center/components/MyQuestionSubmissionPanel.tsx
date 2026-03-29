"use client";

import React, { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { Alert, Button, Card, Empty, Form, Input, List, message, Modal, Pagination, Select, Space, Tag, Typography } from "antd";
import { addQuestionUsingPost, deleteQuestionUsingPost, editQuestionUsingPost, listMyQuestionVoByPageUsingPost } from "@/api/questionController";
import TagList from "@/components/TagList";
import {
  QUESTION_REVIEW_STATUS_COLOR_MAP,
  QUESTION_REVIEW_STATUS_ENUM,
  QUESTION_REVIEW_STATUS_TEXT_MAP,
} from "@/constants/question";
import { formatDateTime } from "@/lib/utils";

const { Title, Paragraph, Text } = Typography;

type QuestionFormValues = {
  title: string;
  tags: string[];
  content: string;
  answer: string;
};

interface SubmissionModalProps {
  open: boolean;
  question?: API.QuestionVO;
  onCancel: () => void;
  onSuccess: () => void;
}

const SubmissionModal: React.FC<SubmissionModalProps> = ({ open, question, onCancel, onSuccess }) => {
  const [form] = Form.useForm<QuestionFormValues>();
  const [submitting, setSubmitting] = useState(false);
  const isEdit = Boolean(question?.id);

  useEffect(() => {
    if (open) {
      form.setFieldsValue({
        title: question?.title || "",
        tags: question?.tagList || [],
        content: question?.content || "",
        answer: question?.answer || "",
      });
    } else {
      form.resetFields();
    }
  }, [form, open, question]);

  const handleSubmit = async () => {
    const values = await form.validateFields();
    setSubmitting(true);
    const hide = message.loading(isEdit ? "正在保存修改" : "正在提交投稿");
    try {
      if (isEdit && question?.id) {
        await editQuestionUsingPost({
          id: question.id,
          ...values,
        });
      } else {
        await addQuestionUsingPost(values);
      }
      hide();
      message.success(isEdit ? "修改已提交，题目会重新进入审核" : "投稿成功，等待管理员审核");
      onSuccess();
    } catch (error: any) {
      hide();
      message.error((isEdit ? "保存失败，" : "投稿失败，") + (error?.message || "请稍后重试"));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal
      title={isEdit ? "修改投稿题目" : "投稿题目与题解"}
      open={open}
      onCancel={onCancel}
      onOk={handleSubmit}
      okText={isEdit ? "保存并重新提交审核" : "提交审核"}
      cancelText="取消"
      confirmLoading={submitting}
      destroyOnClose
      width={760}
    >
      <Form form={form} layout="vertical">
        <Form.Item label="题目标题" name="title" rules={[{ required: true, message: "请输入题目标题" }, { max: 80, message: "标题不能超过 80 个字符" }]}>
          <Input placeholder="例如：Spring 事务失效的常见原因有哪些？" />
        </Form.Item>
        <Form.Item
          label="标签"
          name="tags"
          rules={[{ required: true, message: "请至少填写一个标签" }]}
          extra="按 Enter 可继续添加标签，例如：Java、Spring、MySQL"
        >
          <Select mode="tags" placeholder="请输入题目标签" tokenSeparators={[","]} />
        </Form.Item>
        <Form.Item label="题目内容" name="content" rules={[{ required: true, message: "请输入题目内容" }]}>
          <Input.TextArea rows={7} placeholder="请完整描述题目背景、要求和考察点，支持 Markdown 文本。" />
        </Form.Item>
        <Form.Item label="参考题解" name="answer" rules={[{ required: true, message: "请输入参考题解" }]}>
          <Input.TextArea rows={8} placeholder="请填写结构化题解或参考答案，支持 Markdown 文本。" />
        </Form.Item>
      </Form>
    </Modal>
  );
};

const MyQuestionSubmissionPanel: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [questionList, setQuestionList] = useState<API.QuestionVO[]>([]);
  const [total, setTotal] = useState(0);
  const [current, setCurrent] = useState(1);
  const [pageSize, setPageSize] = useState(5);
  const [modalVisible, setModalVisible] = useState(false);
  const [currentQuestion, setCurrentQuestion] = useState<API.QuestionVO>();

  const loadData = async (nextCurrent = current, nextPageSize = pageSize) => {
    setLoading(true);
    try {
      const res = (await listMyQuestionVoByPageUsingPost({
        current: nextCurrent,
        pageSize: nextPageSize,
        sortField: "createTime",
        sortOrder: "descend",
      })) as API.BaseResponsePageQuestionVO_;
      setQuestionList(res.data?.records || []);
      setTotal(Number(res.data?.total) || 0);
      setCurrent(nextCurrent);
      setPageSize(nextPageSize);
    } catch (error: any) {
      message.error("加载投稿记录失败，" + (error?.message || "请稍后重试"));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadData(1, pageSize);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const reviewSummary = useMemo(() => {
    const pendingCount = questionList.filter((item) => Number(item.reviewStatus ?? QUESTION_REVIEW_STATUS_ENUM.APPROVED) === QUESTION_REVIEW_STATUS_ENUM.PENDING).length;
    return pendingCount > 0 ? `当前页还有 ${pendingCount} 道题目正在等待管理员审核。` : "投稿后题目默认进入待审核，审核通过后才会在公开题库里展示。";
  }, [questionList]);

  const handleDelete = async (questionId?: number) => {
    if (!questionId) {
      return;
    }
    const hide = message.loading("正在删除投稿");
    try {
      await deleteQuestionUsingPost({ id: questionId });
      hide();
      message.success("投稿已删除");
      const nextCurrent = questionList.length === 1 && current > 1 ? current - 1 : current;
      await loadData(nextCurrent, pageSize);
    } catch (error: any) {
      hide();
      message.error("删除失败，" + (error?.message || "请稍后重试"));
    }
  };

  return (
    <div className="space-y-6">
      <Card className="rounded-[2rem] border border-slate-100 shadow-lg shadow-slate-200/40">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
          <div className="space-y-2">
            <Title level={4} style={{ margin: 0 }}>
              我的投稿题目
            </Title>
            <Paragraph type="secondary" style={{ marginBottom: 0 }}>
              可以自己提交题目和题解，管理员审核通过后才会出现在公开题库与题目列表里。
            </Paragraph>
          </div>
          <Button
            type="primary"
            onClick={() => {
              setCurrentQuestion(undefined);
              setModalVisible(true);
            }}
          >
            投稿新题目
          </Button>
        </div>
      </Card>

      <Alert
        type="info"
        showIcon
        message="审核说明"
        description={reviewSummary}
        className="rounded-2xl border-blue-100 bg-blue-50/60"
      />

      <Card className="rounded-[2rem] border border-slate-100 shadow-lg shadow-slate-200/40">
        {questionList.length === 0 && !loading ? (
          <Empty description="你还没有投稿题目，试着提交第一道题吧" image={Empty.PRESENTED_IMAGE_SIMPLE}>
            <Button
              type="primary"
              onClick={() => {
                setCurrentQuestion(undefined);
                setModalVisible(true);
              }}
            >
              立即投稿
            </Button>
          </Empty>
        ) : (
          <List
            loading={loading}
            dataSource={questionList}
            renderItem={(item) => {
              const reviewStatus = Number(item.reviewStatus ?? QUESTION_REVIEW_STATUS_ENUM.APPROVED);
              return (
                <List.Item className="px-0 py-4">
                  <Card className="w-full rounded-[1.5rem] border border-slate-100 shadow-sm">
                    <div className="space-y-4">
                      <div className="flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between">
                        <div className="space-y-3">
                          <div className="flex flex-wrap items-center gap-3">
                            <Link href={`/question/${item.id}`} className="text-lg font-black text-slate-900 hover:text-primary">
                              {item.title}
                            </Link>
                            <Tag color={QUESTION_REVIEW_STATUS_COLOR_MAP[reviewStatus] || "default"} className="rounded-full px-3 py-1 font-semibold">
                              {QUESTION_REVIEW_STATUS_TEXT_MAP[reviewStatus] || "未知状态"}
                            </Tag>
                          </div>
                          <TagList tagList={item.tagList || []} />
                          <div className="flex flex-wrap gap-4 text-sm text-slate-500">
                            <Text type="secondary">提交时间：{formatDateTime(item.createTime)}</Text>
                            <Text type="secondary">最近更新：{formatDateTime(item.updateTime)}</Text>
                            {item.reviewTime ? <Text type="secondary">审核时间：{formatDateTime(item.reviewTime)}</Text> : null}
                          </div>
                        </div>
                        <Space wrap>
                          <Link href={`/question/${item.id}`}>
                            <Button>预览</Button>
                          </Link>
                          <Button
                            onClick={() => {
                              setCurrentQuestion(item);
                              setModalVisible(true);
                            }}
                          >
                            编辑
                          </Button>
                          <Button
                            danger
                            onClick={() =>
                              Modal.confirm({
                                title: "确认删除这条投稿吗？",
                                content: "删除后将无法恢复，对应题目会从列表中移除。",
                                okText: "确认删除",
                                cancelText: "取消",
                                okButtonProps: { danger: true },
                                onOk: () => handleDelete(item.id),
                              })
                            }
                          >
                            删除
                          </Button>
                        </Space>
                      </div>
                      {item.reviewMessage ? (
                        <div className="rounded-2xl border border-amber-100 bg-amber-50/70 px-4 py-3 text-sm text-amber-800">
                          <div className="mb-1 font-semibold">审核意见</div>
                          <div>{item.reviewMessage}</div>
                        </div>
                      ) : null}
                    </div>
                  </Card>
                </List.Item>
              );
            }}
          />
        )}
        {total > pageSize ? (
          <div className="mt-6 flex justify-end">
            <Pagination
              current={current}
              pageSize={pageSize}
              total={total}
              showSizeChanger
              onChange={(page, size) => {
                void loadData(page, size);
              }}
            />
          </div>
        ) : null}
      </Card>

      <SubmissionModal
        open={modalVisible}
        question={currentQuestion}
        onCancel={() => {
          setModalVisible(false);
          setCurrentQuestion(undefined);
        }}
        onSuccess={() => {
          setModalVisible(false);
          setCurrentQuestion(undefined);
          void loadData(current, pageSize);
        }}
      />
    </div>
  );
};

export default MyQuestionSubmissionPanel;
