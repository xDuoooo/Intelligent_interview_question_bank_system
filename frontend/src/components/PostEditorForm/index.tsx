"use client";

import React, { useEffect, useMemo, useState } from "react";
import dynamic from "next/dynamic";
import { Button, Form, Input, Select, Space, Typography } from "antd";

const MdEditor = dynamic(() => import("@/components/MdEditor"), {
  ssr: false,
  loading: () => <div className="rounded-3xl border border-slate-100 bg-slate-50 px-6 py-8 text-center text-slate-400">正在加载编辑器...</div>,
});

const DEFAULT_TAG_OPTIONS = [
  "Java",
  "前端",
  "后端",
  "MySQL",
  "Redis",
  "算法",
  "系统设计",
  "项目复盘",
  "校招",
  "面经",
  "求职经验",
];

type PostFormValues = {
  title: string;
  content: string;
  tags: string[];
};

interface Props {
  initialValues?: Partial<PostFormValues>;
  submitText?: string;
  submitting?: boolean;
  onSubmit: (values: PostFormValues) => Promise<void> | void;
}

export default function PostEditorForm({
  initialValues,
  submitText = "保存帖子",
  submitting = false,
  onSubmit,
}: Props) {
  const [form] = Form.useForm<PostFormValues>();
  const [content, setContent] = useState(initialValues?.content || "");

  useEffect(() => {
    form.setFieldsValue({
      title: initialValues?.title || "",
      tags: initialValues?.tags || [],
      content: initialValues?.content || "",
    });
    setContent(initialValues?.content || "");
  }, [form, initialValues]);

  const tagOptions = useMemo(
    () => DEFAULT_TAG_OPTIONS.map((value) => ({ label: value, value })),
    [],
  );

  return (
    <Form<PostFormValues>
      form={form}
      layout="vertical"
      onFinish={async (values) => {
        await onSubmit({
          ...values,
          content,
        });
      }}
      initialValues={{
        title: initialValues?.title || "",
        tags: initialValues?.tags || [],
        content: initialValues?.content || "",
      }}
      className="space-y-6"
    >
      <Form.Item
        label={<span className="font-bold text-slate-700">标题</span>}
        name="title"
        rules={[
          { required: true, message: "请输入帖子标题" },
          { min: 4, message: "标题至少 4 个字" },
          { max: 80, message: "标题不能超过 80 个字" },
        ]}
      >
        <Input
          size="large"
          placeholder="例如：二面被问 Redis 持久化时，我是怎么答的"
          className="rounded-2xl"
          maxLength={80}
          showCount
        />
      </Form.Item>

      <Form.Item
        label={<span className="font-bold text-slate-700">标签</span>}
        name="tags"
        rules={[{ required: true, message: "请至少选择 1 个标签" }]}
      >
        <Select
          mode="tags"
          size="large"
          options={tagOptions}
          maxCount={6}
          placeholder="请选择或输入标签，如 Java、系统设计、项目复盘"
          className="rounded-2xl"
        />
      </Form.Item>

      <Form.Item
        label={<span className="font-bold text-slate-700">内容</span>}
        required
        help="支持 Markdown，建议包含问题背景、你的做法、踩坑点和复盘。"
      >
        <div className="space-y-3">
          <MdEditor
            value={content}
            onChange={(nextValue) => {
              setContent(nextValue);
              form.setFieldValue("content", nextValue);
            }}
            placeholder={"建议结构：\n1. 面试背景或问题场景\n2. 你的回答/解决方案\n3. 面试官追问\n4. 你的复盘与建议"}
          />
          <div className="flex items-center justify-between text-xs text-slate-400">
            <span>一篇有价值的经验帖，通常会写清楚背景、过程、取舍和结果。</span>
            <span>{content.length} 字</span>
          </div>
        </div>
      </Form.Item>

      <Form.Item
        name="content"
        hidden
        rules={[
          { required: true, message: "请输入帖子内容" },
          {
            validator: async (_, value) => {
              if (!value || String(value).trim().length < 20) {
                throw new Error("内容至少 20 个字");
              }
            },
          },
        ]}
      >
        <Input />
      </Form.Item>

      <div className="rounded-3xl border border-dashed border-slate-200 bg-slate-50/80 p-4">
        <Typography.Text className="text-sm text-slate-500">
          小建议：经验帖更适合写“真实问题 + 你的答法 + 面试官追问 + 复盘”，这样更容易帮助后来的人。
        </Typography.Text>
      </div>

      <Space size={12}>
        <Button
          type="primary"
          htmlType="submit"
          loading={submitting}
          className="h-11 rounded-2xl px-8 font-black"
        >
          {submitText}
        </Button>
      </Space>
    </Form>
  );
}
