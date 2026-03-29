"use client";

import React, { useRef, useState } from "react";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import { Button, message, Modal, Popconfirm, Space, Tag } from "antd";
import Link from "next/link";
import { Edit3, FilePlus2, Trash2 } from "lucide-react";
import ProTable from "@/components/DynamicProTable";
import PostEditorForm from "@/components/PostEditorForm";
import {
  addPostUsingPost,
  deletePostUsingPost,
  editPostUsingPost,
  listPostVoByPageUsingPost,
} from "@/api/postController";

const PostAdminPage: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [editingPost, setEditingPost] = useState<API.PostVO | undefined>();
  const [createVisible, setCreateVisible] = useState(false);
  const [saving, setSaving] = useState(false);

  const handleDelete = async (id?: number) => {
    if (!id) {
      return;
    }
    const hide = message.loading("正在删除帖子...");
    try {
      await deletePostUsingPost({ id });
      hide();
      message.success("删除成功");
      actionRef.current?.reload();
    } catch (error: any) {
      hide();
      message.error(error?.message || "删除失败");
    }
  };

  const columns: ProColumns<API.PostVO>[] = [
    {
      title: "ID",
      dataIndex: "id",
      width: 90,
      hideInForm: true,
    },
    {
      title: "标题",
      dataIndex: "title",
      valueType: "text",
      render: (_, record) => (
        <Link href={`/post/${record.id}`} className="font-black text-slate-800 transition-colors hover:text-primary">
          {record.title}
        </Link>
      ),
    },
    {
      title: "标签",
      dataIndex: "tags",
      hideInForm: true,
      hideInSearch: true,
      render: (_, record) => (
        <div className="flex flex-wrap gap-2">
          {record.tagList?.map((tag) => (
            <Tag key={tag} className="m-0 rounded-full border-slate-200 bg-slate-50 px-3 py-1">
              {tag}
            </Tag>
          ))}
        </div>
      ),
    },
    {
      title: "作者",
      dataIndex: ["user", "userName"],
      hideInSearch: true,
      render: (_, record) => record.user?.userName || `用户 ${record.userId || "-"}`,
    },
    {
      title: "作者 ID",
      dataIndex: "userId",
      valueType: "digit",
      hideInTable: true,
    },
    {
      title: "点赞",
      dataIndex: "thumbNum",
      valueType: "digit",
      width: 90,
      hideInSearch: true,
    },
    {
      title: "收藏",
      dataIndex: "favourNum",
      valueType: "digit",
      width: 90,
      hideInSearch: true,
    },
    {
      title: "创建时间",
      dataIndex: "createTime",
      valueType: "dateTime",
      hideInSearch: true,
      width: 180,
    },
    {
      title: "操作",
      dataIndex: "option",
      valueType: "option",
      width: 180,
      render: (_, record) => (
        <Space size="middle">
          <button
            onClick={() => setEditingPost(record)}
            className="flex items-center gap-1.5 text-primary transition-colors hover:text-primary/80 font-bold"
          >
            <Edit3 className="h-4 w-4" />
            编辑
          </button>
          <Popconfirm
            title="确认删除帖子"
            description="删除后无法恢复，请谨慎操作。"
            okText="确认删除"
            cancelText="取消"
            okButtonProps={{ danger: true }}
            onConfirm={() => handleDelete(record.id)}
          >
            <button className="flex items-center gap-1.5 text-red-500 transition-colors hover:text-red-600 font-bold">
              <Trash2 className="h-4 w-4" />
              删除
            </button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div className="space-y-8 animate-in fade-in duration-700">
      <div className="flex flex-col gap-6 rounded-[2.5rem] border border-white bg-white/70 p-8 shadow-2xl shadow-slate-200/40 backdrop-blur-xl md:flex-row md:items-center md:justify-between">
        <div className="space-y-3">
          <div className="flex items-center gap-2 text-xs font-black uppercase tracking-[0.2em] text-primary">
            <span className="h-2 w-2 rounded-full bg-primary" />
            Post Community
          </div>
          <h1 className="text-3xl font-black tracking-tight text-slate-900 sm:text-4xl">帖子管理</h1>
          <p className="text-lg font-medium text-slate-500">统一维护经验帖内容，补齐社区运营和治理能力。</p>
        </div>
        <div className="flex flex-wrap gap-3">
          <Link href="/posts/create">
            <Button className="h-12 rounded-2xl px-6 font-black">
              前台发帖页
            </Button>
          </Link>
          <Button
            type="primary"
            icon={<FilePlus2 size={16} />}
            className="h-12 rounded-2xl px-6 font-black"
            onClick={() => setCreateVisible(true)}
          >
            新建帖子
          </Button>
        </div>
      </div>

      <div className="rounded-[2.5rem] border border-slate-100 bg-white p-4 shadow-2xl shadow-slate-200/40 sm:p-6">
        <ProTable<API.PostVO>
          headerTitle={null}
          actionRef={actionRef}
          rowKey="id"
          search={{ labelWidth: 80 }}
          columns={columns}
          request={async (params, sort) => {
            const sortField = Object.keys(sort || {})?.[0];
            const sortOrder = sortField ? sort?.[sortField] : undefined;
            const res = await listPostVoByPageUsingPost({
              current: params.current,
              pageSize: params.pageSize,
              title: params.title,
              userId: params.userId,
              sortField,
              sortOrder,
            } as API.PostQueryRequest);
            return {
              data: res.data?.records || [],
              success: true,
              total: Number(res.data?.total || 0),
            };
          }}
          pagination={{ pageSize: 10 }}
        />
      </div>

      <Modal
        title={null}
        open={createVisible}
        onCancel={() => setCreateVisible(false)}
        footer={null}
        width={960}
        destroyOnClose
      >
        <div className="space-y-6 pt-4">
          <div>
            <div className="text-xs font-black uppercase tracking-[0.2em] text-primary">Create Post</div>
            <h3 className="mt-2 text-2xl font-black text-slate-900">新增经验帖</h3>
          </div>
          <PostEditorForm
            submitText="创建帖子"
            submitting={saving}
            onSubmit={async (values) => {
              setSaving(true);
              try {
                await addPostUsingPost(values);
                message.success("帖子创建成功");
                setCreateVisible(false);
                actionRef.current?.reload();
              } catch (error: any) {
                message.error(error?.message || "创建失败");
              } finally {
                setSaving(false);
              }
            }}
          />
        </div>
      </Modal>

      <Modal
        title={null}
        open={!!editingPost}
        onCancel={() => setEditingPost(undefined)}
        footer={null}
        width={960}
        destroyOnClose
      >
        <div className="space-y-6 pt-4">
          <div>
            <div className="text-xs font-black uppercase tracking-[0.2em] text-primary">Edit Post</div>
            <h3 className="mt-2 text-2xl font-black text-slate-900">编辑帖子</h3>
          </div>
          <PostEditorForm
            initialValues={{
              title: editingPost?.title || "",
              content: editingPost?.content || "",
              tags: editingPost?.tagList || [],
            }}
            submitText="保存修改"
            submitting={saving}
            onSubmit={async (values) => {
              if (!editingPost?.id) {
                return;
              }
              setSaving(true);
              try {
                await editPostUsingPost({
                  id: editingPost.id,
                  title: values.title,
                  content: values.content,
                  tags: values.tags,
                });
                message.success("帖子更新成功");
                setEditingPost(undefined);
                actionRef.current?.reload();
              } catch (error: any) {
                message.error(error?.message || "更新失败");
              } finally {
                setSaving(false);
              }
            }}
          />
        </div>
      </Modal>
    </div>
  );
};

export default PostAdminPage;
