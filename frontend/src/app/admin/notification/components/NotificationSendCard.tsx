"use client";

import React from "react";
import { adminSendNotificationUsingPost } from "@/api/notificationController";
import {
  Alert,
  Button,
  Card,
  Form,
  Input,
  InputNumber,
  Radio,
  Select,
  Space,
  message,
} from "antd";
import { Send } from "lucide-react";

const { TextArea } = Input;

type SendScope = "single" | "user" | "all";

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

interface Props {
  onSent?: () => void;
}

export default function NotificationSendCard({ onSent }: Props) {
  const [sendForm] = Form.useForm();
  const [submitting, setSubmitting] = React.useState(false);
  const scope = Form.useWatch<SendScope>("scope", sendForm) || "single";

  const applyTemplate = (template: (typeof NOTIFICATION_TEMPLATES)[number]) => {
    sendForm.setFieldsValue({
      type: template.type,
      title: template.title,
      content: template.content,
    });
  };

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
      onSent?.();
    } catch (error: any) {
      message.error(error?.message || "发送通知失败");
    } finally {
      setSubmitting(false);
    }
  };

  return (
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
            onClick={() =>
              sendForm.setFieldsValue({
                scope: "single",
                userId: undefined,
                targetId: undefined,
                title: "",
                content: "",
                type: "system_announcement",
              })
            }
          >
            清空表单
          </Button>
        </Space>
      </Form>
    </Card>
  );
}
