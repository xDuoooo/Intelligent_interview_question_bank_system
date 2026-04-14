"use client";

import React, { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import {
  Alert,
  Button,
  Card,
  Empty,
  Form,
  Input,
  List,
  message,
  Modal,
  Pagination,
  Popconfirm,
  Select,
  Space,
  Typography,
} from "antd";
import {
  addQuestionBankUsingPost,
  deleteQuestionBankUsingPost,
  editQuestionBankUsingPost,
  listMyQuestionBankVoByPageUsingPost,
} from "@/api/questionBankController";
import { BookOpen, Image as ImageIcon, PencilLine, Plus, Trash2 } from "lucide-react";
import { formatDateTime } from "@/lib/utils";
import ManageQuestionBankQuestionsModal from "./ManageQuestionBankQuestionsModal";

const { Title, Paragraph, Text } = Typography;

type QuestionBankFormValues = {
  title: string;
  description?: string;
  picture?: string;
};

type FilterState = {
  searchText?: string;
  sortKey: string;
};

const SORT_OPTIONS = [
  { label: "最新创建", value: "createTime_desc" },
  { label: "最近更新", value: "updateTime_desc" },
  { label: "标题 A-Z", value: "title_asc" },
  { label: "标题 Z-A", value: "title_desc" },
];

const getSortParams = (sortKey: string) => {
  const [sortField, sortOrderKey] = sortKey.split("_");
  return {
    sortField,
    sortOrder: sortOrderKey === "asc" ? "ascend" : "descend",
  };
};

interface QuestionBankModalProps {
  open: boolean;
  questionBank?: API.QuestionBankVO;
  onCancel: () => void;
  onSuccess: () => void;
}

const QuestionBankModal: React.FC<QuestionBankModalProps> = ({
  open,
  questionBank,
  onCancel,
  onSuccess,
}) => {
  const [form] = Form.useForm<QuestionBankFormValues>();
  const [submitting, setSubmitting] = useState(false);
  const isEdit = Boolean(questionBank?.id);

  useEffect(() => {
    if (open) {
      form.setFieldsValue({
        title: questionBank?.title || "",
        description: questionBank?.description || "",
        picture: questionBank?.picture || "",
      });
    } else {
      form.resetFields();
    }
  }, [form, open, questionBank]);

  const handleSubmit = async () => {
    const values = await form.validateFields();
    setSubmitting(true);
    const hide = message.loading(isEdit ? "正在保存题库修改" : "正在创建题库");
    try {
      if (isEdit && questionBank?.id) {
        await editQuestionBankUsingPost({
          id: questionBank.id,
          ...values,
        });
      } else {
        await addQuestionBankUsingPost(values);
      }
      hide();
      message.success(isEdit ? "题库修改成功" : "题库创建成功");
      onSuccess();
    } catch (error: any) {
      hide();
      message.error((isEdit ? "保存失败，" : "创建失败，") + (error?.message || "请稍后重试"));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal
      title={isEdit ? "修改题库" : "新增题库"}
      open={open}
      onCancel={onCancel}
      onOk={handleSubmit}
      okText={isEdit ? "保存修改" : "创建题库"}
      cancelText="取消"
      confirmLoading={submitting}
      destroyOnClose
      width={720}
    >
      <Form form={form} layout="vertical">
        <Form.Item
          label="题库标题"
          name="title"
          rules={[
            { required: true, message: "请输入题库标题" },
            { max: 80, message: "题库标题不能超过 80 个字符" },
          ]}
        >
          <Input placeholder="例如：Java 后端高频面试题库" />
        </Form.Item>
        <Form.Item
          label="题库简介"
          name="description"
          rules={[{ max: 300, message: "题库简介不能超过 300 个字符" }]}
        >
          <Input.TextArea
            rows={5}
            placeholder="可以描述题库适用方向、包含内容和适合人群，方便自己后续管理。"
          />
        </Form.Item>
        <Form.Item
          label="封面图片链接"
          name="picture"
          rules={[{ type: "url", warningOnly: true, message: "建议填写合法的图片链接地址" }]}
          extra="可选，支持填写公开图片链接作为题库封面。"
        >
          <Input placeholder="https://example.com/question-bank-cover.png" />
        </Form.Item>
      </Form>
    </Modal>
  );
};

const MyQuestionBankPanel: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [questionBankList, setQuestionBankList] = useState<API.QuestionBankVO[]>([]);
  const [current, setCurrent] = useState(1);
  const [pageSize, setPageSize] = useState(6);
  const [total, setTotal] = useState(0);
  const [modalVisible, setModalVisible] = useState(false);
  const [currentQuestionBank, setCurrentQuestionBank] = useState<API.QuestionBankVO>();
  const [manageModalVisible, setManageModalVisible] = useState(false);
  const [managedQuestionBank, setManagedQuestionBank] = useState<API.QuestionBankVO>();
  const [filters, setFilters] = useState<FilterState>({
    sortKey: "createTime_desc",
  });

  const loadData = async (
    nextCurrent = current,
    nextPageSize = pageSize,
    nextFilters: FilterState = filters,
  ) => {
    setLoading(true);
    try {
      const { sortField, sortOrder } = getSortParams(nextFilters.sortKey);
      const res = await listMyQuestionBankVoByPageUsingPost({
        current: nextCurrent,
        pageSize: nextPageSize,
        searchText: nextFilters.searchText?.trim() || undefined,
        sortField,
        sortOrder,
      });
      setQuestionBankList(res.data?.records || []);
      setTotal(Number(res.data?.total) || 0);
      setCurrent(nextCurrent);
      setPageSize(nextPageSize);
    } catch (error: any) {
      message.error("加载我的题库失败，" + (error?.message || "请稍后重试"));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadData(1, pageSize);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const helperText = useMemo(() => {
    if (!questionBankList.length) {
      return "你还没有创建自己的题库，可以先新建一个方向题库，把相关题目慢慢沉淀进去。";
    }
    const withCoverCount = questionBankList.filter((item) => item.picture).length;
    return withCoverCount > 0
      ? `当前页有 ${withCoverCount} 个题库已经设置封面图，后续浏览和管理会更清晰。`
      : "给重点题库补上封面和简介，会更方便后续区分不同方向的内容。";
  }, [questionBankList]);

  const handleDelete = async (id?: string | number) => {
    if (!id) {
      return;
    }
    const hide = message.loading("正在删除题库");
    try {
      await deleteQuestionBankUsingPost({ id });
      hide();
      message.success("题库已删除");
      const nextCurrent = questionBankList.length === 1 && current > 1 ? current - 1 : current;
      if (nextCurrent !== current) {
        setCurrent(nextCurrent);
      } else {
        void loadData(nextCurrent, pageSize, filters);
      }
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
              我的题库
            </Title>
            <Paragraph type="secondary" style={{ marginBottom: 0 }}>
              这里适合沉淀你自己整理的专项题库，后续可以继续往题库里补题、补封面和补说明。
            </Paragraph>
          </div>
          <Button
            type="primary"
            icon={<Plus className="h-4 w-4" />}
            onClick={() => {
              setCurrentQuestionBank(undefined);
              setModalVisible(true);
            }}
          >
            新增题库
          </Button>
        </div>
      </Card>

      <Card className="rounded-[2rem] border border-slate-100 shadow-lg shadow-slate-200/40">
        <div className="flex flex-col gap-4">
          <div className="flex flex-col gap-2 lg:flex-row lg:items-center lg:justify-between">
            <div>
              <Title level={5} style={{ margin: 0 }}>
                排序与筛选
              </Title>
              <Paragraph type="secondary" style={{ marginBottom: 0 }}>
                支持按关键词查找题库，并切换列表排序方式。
              </Paragraph>
            </div>
            <Text type="secondary">共 {total} 个题库</Text>
          </div>
          <div className="grid gap-3 xl:grid-cols-[1.6fr_1fr_auto]">
            <Input
              allowClear
              placeholder="搜索题库标题或简介"
              value={filters.searchText}
              onChange={(event) => {
                setFilters((prev) => ({
                  ...prev,
                  searchText: event.target.value,
                }));
              }}
              onPressEnter={() => void loadData(1, pageSize, filters)}
            />
            <Select
              value={filters.sortKey}
              options={SORT_OPTIONS}
              onChange={(value) => {
                const nextFilters = {
                  ...filters,
                  sortKey: value,
                };
                setFilters(nextFilters);
                void loadData(1, pageSize, nextFilters);
              }}
            />
            <Button
              onClick={() => void loadData(1, pageSize, filters)}
              loading={loading}
            >
              搜索
            </Button>
          </div>
        </div>
      </Card>

      <Alert
        type="info"
        showIcon
        message="题库管理提示"
        description={helperText}
        className="rounded-[1.5rem] border border-blue-100/70 bg-blue-50/70"
      />

      <Card className="rounded-[2rem] border border-slate-100 shadow-lg shadow-slate-200/40">
        <List
          loading={loading}
          locale={{
            emptyText: (
              <Empty
                description="还没有创建题库"
                image={Empty.PRESENTED_IMAGE_SIMPLE}
              />
            ),
          }}
          dataSource={questionBankList}
          renderItem={(questionBank) => (
            <List.Item className="!px-0">
              <Card className="w-full rounded-[1.75rem] border border-slate-100 shadow-sm shadow-slate-200/30">
                <div className="flex flex-col gap-5 lg:flex-row lg:items-start lg:justify-between">
                  <div className="flex min-w-0 flex-1 gap-4">
                    <div className="flex h-20 w-20 shrink-0 items-center justify-center overflow-hidden rounded-[1.5rem] border border-slate-100 bg-slate-50">
                      {questionBank.picture ? (
                        // eslint-disable-next-line @next/next/no-img-element
                        <img
                          src={questionBank.picture}
                          alt={questionBank.title || "题库封面"}
                          className="h-full w-full object-cover"
                        />
                      ) : (
                        <BookOpen className="h-8 w-8 text-slate-300" />
                      )}
                    </div>
                    <div className="min-w-0 flex-1 space-y-3">
                      <div className="space-y-2">
                        <Title level={5} style={{ margin: 0 }}>
                          {questionBank.title}
                        </Title>
                        <Paragraph type="secondary" className="!mb-0">
                          {questionBank.description || "这个题库还没有补充简介，后续可以补充适用方向和内容范围。"}
                        </Paragraph>
                      </div>
                      <Space wrap size={[8, 8]}>
                        <Text type="secondary">创建于 {formatDateTime(questionBank.createTime)}</Text>
                        <Text type="secondary">更新于 {formatDateTime(questionBank.updateTime)}</Text>
                        {questionBank.picture ? (
                          <Text type="secondary" className="inline-flex items-center gap-1">
                            <ImageIcon className="h-3.5 w-3.5" /> 已配置封面
                          </Text>
                        ) : null}
                      </Space>
                    </div>
                  </div>
                  <Space wrap>
                    <Link href={`/bank/${questionBank.id}`}>
                      <Button>查看题库</Button>
                    </Link>
                    <Button
                      type="primary"
                      ghost
                      onClick={() => {
                        setManagedQuestionBank(questionBank);
                        setManageModalVisible(true);
                      }}
                    >
                      管理题目
                    </Button>
                    <Button
                      icon={<PencilLine className="h-4 w-4" />}
                      onClick={() => {
                        setCurrentQuestionBank(questionBank);
                        setModalVisible(true);
                      }}
                    >
                      编辑
                    </Button>
                    <Popconfirm
                      title="确认删除这个题库吗？"
                      description="删除后题库本身会移除，请确认内容已备份。"
                      okText="删除"
                      cancelText="取消"
                      onConfirm={() => handleDelete(questionBank.id)}
                    >
                      <Button danger icon={<Trash2 className="h-4 w-4" />}>
                        删除
                      </Button>
                    </Popconfirm>
                  </Space>
                </div>
              </Card>
            </List.Item>
          )}
        />

        {total > pageSize ? (
          <div className="mt-6 flex justify-end">
            <Pagination
              current={current}
              pageSize={pageSize}
              total={total}
              showSizeChanger
              pageSizeOptions={["6", "12", "18"]}
              onChange={(page, size) => {
                void loadData(page, size, filters);
              }}
            />
          </div>
        ) : null}
      </Card>

      <QuestionBankModal
        open={modalVisible}
        questionBank={currentQuestionBank}
        onCancel={() => setModalVisible(false)}
        onSuccess={() => {
          setModalVisible(false);
          setCurrentQuestionBank(undefined);
          void loadData(1, pageSize, filters);
        }}
      />

      <ManageQuestionBankQuestionsModal
        open={manageModalVisible}
        questionBank={managedQuestionBank}
        onCancel={() => {
          setManageModalVisible(false);
          setManagedQuestionBank(undefined);
        }}
      />
    </div>
  );
};

export default MyQuestionBankPanel;
