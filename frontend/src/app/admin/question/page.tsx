"use client";

import React, { useRef, useState } from "react";
import CreateModal from "./components/CreateModal";
import UpdateModal from "./components/UpdateModal";
import {
  batchDeleteQuestionsUsingPost,
  deleteQuestionUsingPost,
  listQuestionByPageUsingPost,
} from "@/api/questionController";
import { Plus, Trash2, Edit3, Database, Wand2, Link2 } from "lucide-react";
import { ProTable } from "@ant-design/pro-components";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import { Button, message, Popconfirm, Space, Table } from "antd";
import TagList from "@/components/TagList";
import UpdateBankModal from "@/app/admin/question/components/UpdateBankModal";
import BatchAddQuestionsToBankModal from "@/app/admin/question/components/BatchAddQuestionsToBankModal";
import BatchRemoveQuestionsFromBankModal from "@/app/admin/question/components/BatchRemoveQuestionsFromBankModal";

/**
 * 题目管理页面
 * @constructor
 */
const QuestionAdminPage: React.FC = () => {
  const [createModalVisible, setCreateModalVisible] = useState<boolean>(false);
  const [updateModalVisible, setUpdateModalVisible] = useState<boolean>(false);
  const [updateBankModalVisible, setUpdateBankModalVisible] = useState<boolean>(false);
  const [batchAddQuestionsToBankModalVisible, setBatchAddQuestionsToBankModalVisible] = useState<boolean>(false);
  const [batchRemoveQuestionsFromBankModalVisible, setBatchRemoveQuestionsFromBankModalVisible] = useState<boolean>(false);
  const [selectedQuestionIdList, setSelectedQuestionIdList] = useState<number[]>([]);
  const actionRef = useRef<ActionType>();
  const [currentRow, setCurrentRow] = useState<API.Question>();

  const handleDelete = async (row: API.Question) => {
    const hide = message.loading("正在删除");
    if (!row) return true;
    try {
      await deleteQuestionUsingPost({ id: row.id as any });
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

  const handleBatchDelete = async (questionIdList: number[]) => {
    const hide = message.loading("正在操作");
    try {
      await batchDeleteQuestionsUsingPost({ questionIdList });
      hide();
      message.success("操作成功");
      actionRef?.current?.reload();
    } catch (error: any) {
      hide();
      message.error("操作失败，" + error.message);
    }
  };

  const columns: ProColumns<API.Question>[] = [
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
      title: "标签",
      dataIndex: "tags",
      valueType: "select",
      fieldProps: { mode: "tags" },
      render: (_, record) => {
        const tagList = JSON.parse(record.tags || "[]");
        return <TagList tagList={tagList} />;
      },
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
            onClick={() => {
              setCurrentRow(record);
              setUpdateBankModalVisible(true);
            }}
            className="flex items-center gap-1.5 text-orange-500 hover:text-orange-600 font-bold transition-colors"
          >
            <Link2 className="h-4 w-4" />
            修改题库
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
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 bg-white/70 backdrop-blur-xl rounded-[2.5rem] p-8 sm:p-12 border border-white shadow-2xl shadow-slate-200/50 relative overflow-hidden">
        <div className="absolute top-0 right-0 p-8 opacity-5">
           <Database className="h-32 w-32 text-slate-900" />
        </div>
        <div className="relative z-10 space-y-3">
           <div className="flex items-center gap-2 text-primary font-black uppercase tracking-widest text-xs">
              <span className="h-2 w-2 rounded-full bg-primary animate-pulse" />
              Question Repository
           </div>
           <h1 className="text-3xl sm:text-4xl font-black tracking-tight text-slate-900">题目库管理</h1>
           <p className="text-slate-500 font-medium text-lg">高效组织、编辑和批量维护平台的面试题目内容。</p>
        </div>
        <div className="relative z-10 flex flex-wrap gap-3">
          <a
            href="/admin/question/ai"
            className="flex items-center gap-2 bg-slate-100 hover:bg-slate-200 text-slate-700 h-14 px-8 rounded-2xl font-black text-lg transition-all border border-slate-200 shadow-sm"
          >
            <Wand2 className="h-6 w-6 text-primary" />
            AI 生成
          </a>
          <button
            onClick={() => setCreateModalVisible(true)}
            className="flex items-center gap-2 bg-primary hover:bg-primary/90 text-primary-foreground h-14 px-8 rounded-2xl font-black text-lg transition-all shadow-xl shadow-primary/25"
          >
            <Plus className="h-6 w-6" />
            新建题目
          </button>
        </div>
      </div>

      <div className="bg-white rounded-[2.5rem] border border-slate-100 shadow-2xl shadow-slate-200/50 overflow-hidden p-4 sm:p-6 pb-12 ant-table-premium">
        <ProTable<API.Question>
          headerTitle={null}
          actionRef={actionRef}
          rowSelection={{
            selections: [Table.SELECTION_ALL, Table.SELECTION_INVERT],
          }}
          tableAlertOptionRender={({ selectedRowKeys }) => (
            <Space size={16}>
              <Button
                type="primary"
                ghost
                onClick={() => {
                  setSelectedQuestionIdList(selectedRowKeys as number[]);
                  setBatchAddQuestionsToBankModalVisible(true);
                }}
                className="rounded-xl font-bold h-10 border-primary text-primary"
              >
                批量加入题库
              </Button>
              <Button
                danger
                ghost
                onClick={() => {
                  setSelectedQuestionIdList(selectedRowKeys as number[]);
                  setBatchRemoveQuestionsFromBankModalVisible(true);
                }}
                className="rounded-xl font-bold h-10"
              >
                从题库移除
              </Button>
              <Popconfirm
                title="确认删除"
                onConfirm={() => handleBatchDelete(selectedRowKeys as number[])}
              >
                <Button danger className="rounded-xl font-bold h-10">批量删除</Button>
              </Popconfirm>
            </Space>
          )}
          request={async (params, sort, filter) => {
            const sortField = Object.keys(sort)?.[0];
            const sortOrder = sort?.[sortField] ?? undefined;
            // @ts-ignore
            const { data, code } = await listQuestionByPageUsingPost({
              ...params,
              sortField,
              sortOrder,
              ...filter,
            } as API.QuestionQueryRequest) as unknown as API.BaseResponsePageQuestionVO_;
            return {
              success: code === 0,
              data: data?.records || [],
              total: Number(data?.total) || 0,
            };
          }}
          columns={columns}
          scroll={{ x: true }}
        />
      </div>

      <CreateModal visible={createModalVisible} columns={columns} onSubmit={() => { setCreateModalVisible(false); actionRef.current?.reload(); }} onCancel={() => setCreateModalVisible(false)} />
      <UpdateModal visible={updateModalVisible} columns={columns} oldData={currentRow} onSubmit={() => { setUpdateModalVisible(false); setCurrentRow(undefined); actionRef.current?.reload(); }} onCancel={() => setUpdateModalVisible(false)} />
      <UpdateBankModal visible={updateBankModalVisible} questionId={currentRow?.id} onCancel={() => setUpdateBankModalVisible(false)} />
      <BatchAddQuestionsToBankModal visible={batchAddQuestionsToBankModalVisible} questionIdList={selectedQuestionIdList} onSubmit={() => { setBatchAddQuestionsToBankModalVisible(false); }} onCancel={() => setBatchAddQuestionsToBankModalVisible(false)} />
      <BatchRemoveQuestionsFromBankModal visible={batchRemoveQuestionsFromBankModalVisible} questionIdList={selectedQuestionIdList} onSubmit={() => { setBatchRemoveQuestionsFromBankModalVisible(false); }} onCancel={() => setBatchRemoveQuestionsFromBankModalVisible(false)} />
    </div>
  );
};

export default QuestionAdminPage;
