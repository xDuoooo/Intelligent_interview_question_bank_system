"use client";

import React, { useRef, useState } from "react";
import CreateModal from "./components/CreateModal";
import UpdateModal from "./components/UpdateModal";
import {
  deleteQuestionBankUsingPost,
  listQuestionBankByPageUsingPost,
} from "@/api/questionBankController";
import { Plus, Trash2, Edit3, Briefcase } from "lucide-react";
import { ProTable } from "@ant-design/pro-components";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import { Button, message, Space, Typography, Image } from "antd";

/**
 * 题库管理页面
 * @constructor
 */
const QuestionBankAdminPage: React.FC = () => {
  const [createModalVisible, setCreateModalVisible] = useState<boolean>(false);
  const [updateModalVisible, setUpdateModalVisible] = useState<boolean>(false);
  const actionRef = useRef<ActionType>();
  const [currentRow, setCurrentRow] = useState<API.QuestionBank>();

  /**
   * 删除节点
   */
  const handleDelete = async (row: API.QuestionBank) => {
    const hide = message.loading("正在删除");
    if (!row) return true;
    try {
      await deleteQuestionBankUsingPost({ id: row.id as any });
      hide();
      message.success("删除成功");
      actionRef?.current?.reload();
      return true;
    } catch (error: any) {
      hide();
      message.error("删除失败，" + error.message);
      return false;
    }
  };

  /**
   * 表格列配置
   */
  const columns: ProColumns<API.QuestionBank>[] = [
    {
      title: "ID",
      dataIndex: "id",
      valueType: "text",
      hideInForm: true,
      width: 80,
    },
    {
      title: "标题",
      dataIndex: "title",
      valueType: "text",
      render: (text) => <span className="font-bold text-slate-700">{text}</span>,
    },
    {
      title: "描述",
      dataIndex: "description",
      valueType: "text",
      ellipsis: true,
    },
    {
      title: "图片",
      dataIndex: "picture",
      valueType: "image",
      fieldProps: { width: 64 },
      hideInSearch: true,
      render: (_, record) => (
        <Image 
          src={record.picture} 
          width={64} 
          className="rounded-xl border border-slate-100 shadow-sm object-cover" 
          fallback="https://placehold.co/100x100?text=No+Image"
        />
      ),
    },
    {
      title: "创建时间",
      sorter: true,
      dataIndex: "createTime",
      valueType: "dateTime",
      hideInSearch: true,
      hideInForm: true,
    },
    {
      title: "操作",
      dataIndex: "option",
      valueType: "option",
      render: (_, record) => (
        <Space size="middle">
          <button
            onClick={() => {
              setCurrentRow(record);
              setUpdateModalVisible(true);
            }}
            className="flex items-center gap-1.5 text-primary hover:text-primary/80 font-bold transition-colors"
          >
            <Edit3 className="h-4 w-4" />
            修改
          </button>
          <button
            onClick={() => handleDelete(record)}
            className="flex items-center gap-1.5 text-red-500 hover:text-red-600 font-bold transition-colors"
          >
            <Trash2 className="h-4 w-4" />
            删除
          </button>
        </Space>
      ),
    },
  ];

  return (
    <div className="space-y-8 animate-in fade-in duration-700">
      {/* Premium Admin Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-slate-900 rounded-[2.5rem] p-8 sm:p-12 text-white shadow-2xl relative overflow-hidden">
        <div className="absolute top-0 right-0 p-8 opacity-10">
           <Briefcase className="h-24 w-24 text-primary" />
        </div>
        <div className="relative z-10 space-y-2">
           <div className="flex items-center gap-2 text-primary font-black uppercase tracking-wider text-xs">
              <span className="h-2 w-2 rounded-full bg-primary animate-pulse" />
              ADMIN CONTROL PANEL
           </div>
           <h1 className="text-3xl sm:text-4xl font-black tracking-tight">题库资源管理</h1>
           <p className="text-slate-400 font-medium">维护平台核心题库内容，上传封面并完善相关描述。</p>
        </div>
        <div className="relative z-10">
          <button
            onClick={() => setCreateModalVisible(true)}
            className="flex items-center gap-2 bg-primary hover:bg-primary/90 text-primary-foreground h-14 px-8 rounded-2xl font-black text-lg transition-all shadow-xl shadow-primary/25 hover:scale-105 active:scale-95"
          >
            <Plus className="h-6 w-6" />
            创建题库
          </button>
        </div>
      </div>

      {/* Table Container */}
      <div className="bg-white rounded-[2.5rem] border border-slate-100 shadow-2xl shadow-slate-200/50 overflow-hidden p-4 sm:p-6 pb-12 ant-table-premium">
        <ProTable<API.QuestionBank>
          headerTitle={null}
          actionRef={actionRef}
          rowKey="id"
          search={{
            labelWidth: "auto",
            defaultCollapsed: false,
            className: "admin-search-form",
          }}
          request={async (params, sort, filter) => {
            const sortField = Object.keys(sort)?.[0];
            const sortOrder = sort?.[sortField] ?? undefined;
            // @ts-ignore
            const res = await listQuestionBankByPageUsingPost({
              ...params,
              sortField,
              sortOrder,
              ...filter,
            } as API.QuestionBankQueryRequest);
            return {
              success: res.code === 0,
              data: res.data?.records || [],
              total: Number(res.data?.total) || 0,
            };
          }}
          columns={columns}
        />
      </div>

      <CreateModal
        visible={createModalVisible}
        columns={columns}
        onSubmit={() => {
          setCreateModalVisible(false);
          actionRef.current?.reload();
        }}
        onCancel={() => setCreateModalVisible(false)}
      />
      <UpdateModal
        visible={updateModalVisible}
        columns={columns}
        oldData={currentRow}
        onSubmit={() => {
          setUpdateModalVisible(false);
          setCurrentRow(undefined);
          actionRef.current?.reload();
        }}
        onCancel={() => setUpdateModalVisible(false)}
      />
    </div>
  );
};

export default QuestionBankAdminPage;

