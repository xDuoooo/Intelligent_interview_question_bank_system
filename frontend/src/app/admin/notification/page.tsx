"use client";

import React, { useEffect, useMemo, useState } from "react";
import {
  adminSendNotificationUsingPost,
  deleteNotificationUsingPost,
  listNotificationByPageUsingPost,
} from "@/api/notificationController";
import { formatDateTime } from "@/lib/utils";
import {
  Alert,
  Button,
  Card,
  Form,
  Input,
  InputNumber,
  message,
  Popconfirm,
  Radio,
  Select,
  Space,
  Table,
  Tag,
  Typography,
} from "antd";
import { BellRing, Megaphone, Send, UserRound } from "lucide-react";

const { Paragraph, Text, Title } = Typography;
const { TextArea } = Input;

type SendScope = "single" | "user" | "all";

interface NotificationRecord {
  id: number;
  userId: number;
  title: string;
  content: string;
  type: string;
  status: number;
  targetId?: number;
  createTime?: string;
}

interface NotificationQueryState {
  current: number;
  pageSize: number;
  userId?: number;
  title?: string;
  type?: string;
  status?: number;
}

const DEFAULT_QUERY: NotificationQueryState = {
  current: 1,
  pageSize: 10,
};

const NOTIFICATION_TEMPLATES = [
  {
    key: "feature",
    label: "功能上新",
    type: "system_announcement",
    title: "系统功能更新通知",
    content:
      "平台已上线新的功能模块，欢迎前往体验。若你在使用中遇到任何问题，也欢迎通过站内反馈告知我们，我们会持续优化体验。",
  },
  {
    key: "maintenance",
    label: "维护提醒",
    type: "operation_notice",
    title: "系统维护提醒",
    content:
      "平台将在近期进行功能维护和体验优化，维护期间部分功能可能短暂不可用。请提前保存重要内容，感谢理解与支持。",
  },
  {
    key: "activity",
    label: "活动通知",
    type: "activity_notice",
    title: "学习活动提醒",
    content:
      "新的学习活动已经开启，欢迎前往题库、社区或模拟面试模块参与。坚持打卡和练习，也许会解锁新的成长成就。",
  },
  {
    key: "learning",
    label: "学习鼓励",
    type: "learning_notice",
    title: "继续保持学习节奏",
    content:
      "你最近的学习状态很不错，继续保持刷题、复盘和模拟面试的节奏，会更快形成稳定的面试表达和知识掌握。",
  },
];

export default function AdminNotificationPage() {
  const [sendForm] = Form.useForm();
  const [queryForm] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [query, setQuery] = useState<NotificationQueryState>(DEFAULT_QUERY);
  const [data, setData] = useState<NotificationRecord[]>([]);
  const [total, setTotal] = useState(0);
  const scope = Form.useWatch<SendScope>("scope", sendForm) || "single";

  const statusOptions = useMemo(
    () => [
      { label: "全部状态", value: undefined },
      { label: "未读", value: 0 },
      { label: "已读", value: 1 },
    ],
    [],
  );

  const loadData = async (nextQuery?: Partial<NotificationQueryState>) => {
    const mergedQuery = { ...query, ...nextQuery };
    setLoading(true);
    try {
      const res: any = await listNotificationByPageUsingPost({
        current: mergedQuery.current,
        pageSize: mergedQuery.pageSize,
        userId: mergedQuery.userId,
        title: mergedQuery.title,
        type: mergedQuery.type,
        status: mergedQuery.status,
        sortField: "createTime",
        sortOrder: "descend",
      });
      const pageData = res?.data;
      setData(pageData?.records || []);
      setTotal(pageData?.total || 0);
      setQuery(mergedQuery);
    } catch (error: any) {
      message.error(error?.message || "加载通知记录失败");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData(DEFAULT_QUERY);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleSend = async (values: any) => {
    setSubmitting(true);
    try {
      const res = await adminSendNotificationUsingPost({
        scope: values.scope,
        userId: values.userId,
        title: values.title,
        content: values.content,
        type: values.type,
        targetId: values.targetId,
      });
      message.success(`通知发送成功，本次写入 ${res.data || 0} 条记录`);
      sendForm.resetFields();
      sendForm.setFieldsValue({ scope: "single", type: "system_announcement" });
      loadData({ current: 1 });
    } catch (error: any) {
      message.error(error?.message || "发送通知失败");
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteNotificationUsingPost({ id });
      message.success("通知删除成功");
      loadData();
    } catch (error: any) {
      message.error(error?.message || "删除通知失败");
    }
  };

  const applyTemplate = (template: (typeof NOTIFICATION_TEMPLATES)[number]) => {
    sendForm.setFieldsValue({
      type: template.type,
      title: template.title,
      content: template.content,
    });
  };

  const columns = [
    {
      title: "通知 ID",
      dataIndex: "id",
      width: 110,
    },
    {
      title: "接收用户",
      dataIndex: "userId",
      width: 110,
      render: (value: number) => <Text strong>#{value}</Text>,
    },
    {
      title: "标题 / 内容",
      dataIndex: "title",
      render: (_: any, record: NotificationRecord) => (
        <div className="min-w-[260px]">
          <div className="font-bold text-slate-800">{record.title}</div>
          <Paragraph className="mb-0 mt-1 text-slate-500" ellipsis={{ rows: 2, expandable: false }}>
            {record.content}
          </Paragraph>
        </div>
      ),
    },
    {
      title: "类型",
      dataIndex: "type",
      width: 150,
      render: (value: string) => <Tag color="blue">{value || "system"}</Tag>,
    },
    {
      title: "状态",
      dataIndex: "status",
      width: 110,
      render: (value: number) =>
        value === 1 ? <Tag color="green">已读</Tag> : <Tag color="gold">未读</Tag>,
    },
    {
      title: "目标 ID",
      dataIndex: "targetId",
      width: 110,
      render: (value?: number) => value || "-",
    },
    {
      title: "创建时间",
      dataIndex: "createTime",
      width: 180,
      render: (value?: string) => formatDateTime(value),
    },
    {
      title: "操作",
      dataIndex: "action",
      width: 110,
      render: (_: any, record: NotificationRecord) => (
        <Popconfirm
          title="确认删除这条通知？"
          okText="删除"
          cancelText="取消"
          onConfirm={() => handleDelete(record.id)}
        >
          <Button danger type="link">
            删除
          </Button>
        </Popconfirm>
      ),
    },
  ];

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
        <Card
          className="rounded-[2rem] border-0 shadow-lg shadow-slate-200/50"
          title={
            <span className="flex items-center gap-2 text-lg font-black text-slate-800">
              <Send className="h-5 w-5 text-primary" />
              发送通知
            </span>
          }
        >
          <Form
            form={sendForm}
            layout="vertical"
            initialValues={{ scope: "single", type: "system_announcement" }}
            onFinish={handleSend}
          >
            <Form.Item label="发送范围" name="scope" rules={[{ required: true, message: "请选择发送范围" }]}>
              <Radio.Group optionType="button" buttonStyle="solid">
                <Radio.Button value="single">指定用户</Radio.Button>
                <Radio.Button value="user">广播普通用户</Radio.Button>
                <Radio.Button value="all">广播全站</Radio.Button>
              </Radio.Group>
            </Form.Item>

            {scope === "single" ? (
              <Form.Item
                label="目标用户 ID"
                name="userId"
                rules={[{ required: true, message: "请输入目标用户 ID" }]}
              >
                <InputNumber className="!w-full" min={1} placeholder="例如 1" />
              </Form.Item>
            ) : (
              <Alert
                className="mb-6 rounded-2xl"
                type="success"
                showIcon
                message={scope === "user" ? "会发送给全部普通用户" : "会发送给全站未封禁用户（含管理员）"}
              />
            )}

            <Form.Item label="常用模板">
              <div className="flex flex-wrap gap-2">
                {NOTIFICATION_TEMPLATES.map((template) => (
                  <Button
                    key={template.key}
                    onClick={() => applyTemplate(template)}
                    className="rounded-2xl font-bold"
                  >
                    {template.label}
                  </Button>
                ))}
              </div>
              <div className="mt-2 text-xs leading-6 text-slate-400">
                选择模板后会自动回填标题、类型和内容，你仍然可以继续手动修改。
              </div>
            </Form.Item>

            <Form.Item label="通知标题" name="title" rules={[{ required: true, message: "请输入通知标题" }]}>
              <Input placeholder="例如：系统功能更新通知" maxLength={80} />
            </Form.Item>

            <Form.Item label="通知类型" name="type" rules={[{ required: true, message: "请输入通知类型" }]}>
              <Select
                options={[
                  { label: "系统公告", value: "system_announcement" },
                  { label: "运营通知", value: "operation_notice" },
                  { label: "活动提醒", value: "activity_notice" },
                  { label: "学习提醒", value: "learning_notice" },
                  { label: "自定义类型", value: "custom_notice" },
                ]}
              />
            </Form.Item>

            <Form.Item label="关联业务 ID（选填）" name="targetId">
              <InputNumber className="!w-full" min={1} placeholder="例如题目 ID / 帖子 ID" />
            </Form.Item>

            <Form.Item label="通知内容" name="content" rules={[{ required: true, message: "请输入通知内容" }]}>
              <TextArea rows={6} maxLength={1000} placeholder="请输入发送给用户的通知内容" showCount />
            </Form.Item>

            <Space>
              <Button type="primary" htmlType="submit" loading={submitting} size="large">
                立即发送
              </Button>
              <Button
                size="large"
                onClick={() => sendForm.setFieldsValue({ scope: "single", userId: undefined, targetId: undefined, title: "", content: "", type: "system_announcement" })}
              >
                清空表单
              </Button>
            </Space>
          </Form>
        </Card>

        <Card
          className="rounded-[2rem] border-0 shadow-lg shadow-slate-200/50"
          title={
            <span className="flex items-center gap-2 text-lg font-black text-slate-800">
              <Megaphone className="h-5 w-5 text-cyan-500" />
              通知记录
            </span>
          }
          extra={<Text type="secondary">共 {total} 条</Text>}
        >
          <Form
            form={queryForm}
            layout="vertical"
            onFinish={(values) => loadData({ ...DEFAULT_QUERY, ...values, current: 1 })}
          >
            <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
              <Form.Item className="mb-0" label="用户 ID" name="userId">
                <InputNumber className="!w-full" min={1} placeholder="全部用户" />
              </Form.Item>
              <Form.Item className="mb-0" label="标题关键词" name="title">
                <Input placeholder="支持模糊搜索" />
              </Form.Item>
              <Form.Item className="mb-0" label="通知类型" name="type">
                <Input placeholder="如 system_announcement" />
              </Form.Item>
              <Form.Item className="mb-0" label="状态" name="status">
                <Select allowClear options={statusOptions.filter((item) => item.value !== undefined)} placeholder="全部状态" />
              </Form.Item>
            </div>
            <Space className="mt-4">
              <Button type="primary" htmlType="submit" loading={loading}>
                查询记录
              </Button>
              <Button
                onClick={() => {
                  queryForm.resetFields();
                  loadData(DEFAULT_QUERY);
                }}
              >
                重置
              </Button>
            </Space>
          </Form>

          <Table<NotificationRecord>
            className="mt-6"
            rowKey="id"
            loading={loading}
            columns={columns}
            dataSource={data}
            scroll={{ x: 980 }}
            pagination={{
              current: query.current,
              pageSize: query.pageSize,
              total,
              showSizeChanger: true,
              onChange: (page, pageSize) => loadData({ ...query, current: page, pageSize }),
            }}
          />
        </Card>
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
