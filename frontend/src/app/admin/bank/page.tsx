"use client";

import React, { useRef, useState } from "react";
import dynamic from "next/dynamic";
import {
  deleteQuestionBankUsingPost,
  listQuestionBankByPageUsingPost,
} from "@/api/questionBankController";
import { Plus, Trash2, Edit3, Briefcase } from "lucide-react";
import ProTable from "@/components/DynamicProTable";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import { message, Space, Image } from "antd";

const CreateModal = dynamic(() => import("./components/CreateModal"));
const UpdateModal = dynamic(() => import("./components/UpdateModal"));

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
          alt={record.title || "题库图片"}
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
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 bg-white/70 backdrop-blur-xl rounded-[2.5rem] p-8 sm:p-12 border border-white shadow-2xl shadow-slate-200/50 relative overflow-hidden">
        <div className="absolute top-0 right-0 p-8 opacity-5">
           <Briefcase className="h-32 w-32 text-slate-900" />
        </div>
        <div className="relative z-10 space-y-3">
           <div className="flex items-center gap-2 text-primary font-black uppercase tracking-widest text-xs">
              <span className="h-2 w-2 rounded-full bg-primary animate-pulse" />
              Content Resource Center
           </div>
           <h1 className="text-3xl sm:text-4xl font-black tracking-tight text-slate-900">题库资源管理</h1>
           <p className="text-slate-500 font-medium text-lg">精心维护平台核心题库内容，打造高质量的面试知识库。</p>
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
            try {
              const sortField = Object.keys(sort)?.[0];
              const sortOrder = sort?.[sortField] ?? undefined;
              // @ts-ignore
              const res = await listQuestionBankByPageUsingPost({
                ...params,
                sortField,
                sortOrder,
                ...filter,
              } as API.QuestionBankQueryRequest) as unknown as API.BaseResponsePageQuestionBankVO_;
              return {
                success: res.code === 0,
                data: res.data?.records || [],
                total: Number(res.data?.total) || 0,
              };
            } catch (error: any) {
              message.error(error?.message || "加载题库管理数据失败");
              return {
                success: false,
                data: [],
                total: 0,
              };
            }
          }}
          columns={columns}
        />
      </div>

      {createModalVisible && (
        <CreateModal
          visible={createModalVisible}
          columns={columns}
          onSubmit={() => {
            setCreateModalVisible(false);
            actionRef.current?.reload();
          }}
          onCancel={() => setCreateModalVisible(false)}
        />
      )}
      {updateModalVisible && currentRow && (
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
      )}
    </div>
  );
};

export default QuestionBankAdminPage;
