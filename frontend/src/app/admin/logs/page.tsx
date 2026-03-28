"use client";
import React, { useRef } from "react";
import { ProTable, ActionType, ProColumns } from "@ant-design/pro-components";
import { Card, Typography, Space, Tag, Tooltip } from "antd";
import { 
  ShieldAlert, 
  Activity, 
  User as UserIcon, 
  Terminal, 
  Globe,
  Clock
} from "lucide-react";
import request from "@/libs/request";

const { Text } = Typography;

/**
 * 管理员操作日志页面
 */
export default function AdminLogsPage() {
  const actionRef = useRef<ActionType>();

  const columns: ProColumns<any>[] = [
    {
      title: "操作者",
      dataIndex: "userName",
      render: (_, record) => (
        <Space>
           <div className="bg-slate-100 p-1.5 rounded-lg">
              <UserIcon className="h-4 w-4 text-slate-500" />
           </div>
           <div className="flex flex-col">
              <Text className="font-bold text-slate-700">{record.userName || "未知用户"}</Text>
              <Text type="secondary" className="text-[10px]">ID: {record.userId}</Text>
           </div>
        </Space>
      ),
    },
    {
      title: "操作方法",
      dataIndex: "method",
      render: (text) => (
        <Tooltip title={text}>
           <Tag className="max-w-[150px] truncate bg-slate-50 border-slate-200 font-mono text-xs text-slate-500 rounded-lg">
              {String(text).split('.').pop()}
           </Tag>
        </Tooltip>
      ),
    },
    {
      title: "IP 地址",
      dataIndex: "ip",
      render: (text) => (
        <Space className="text-slate-500 font-medium">
           <Globe className="h-3.5 w-3.5 opacity-40" />
           {text}
        </Space>
      ),
    },
    {
      title: "请求参数",
      dataIndex: "params",
      hideInTable: true, // 参数通常较长，仅在详情查看
    },
    {
      title: "操作时间",
      dataIndex: "createTime",
      valueType: "dateTime",
      sorter: true,
      render: (_, record) => (
        <Space className="text-slate-500">
           <Clock className="h-3.5 w-3.5 opacity-40" />
           {record.createTime}
        </Space>
      )
    },
  ];

  return (
    <div className="space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-700">
      {/* Header Section */}
      <Card 
        className="bg-white/70 backdrop-blur-xl rounded-[2.5rem] border border-white shadow-2xl shadow-slate-200/50 relative overflow-hidden"
        bodyStyle={{ padding: "3rem" }}
      >
        <div className="absolute top-0 right-0 p-12 opacity-5 rotate-12">
           <Terminal className="h-40 w-40 text-slate-900" />
        </div>
        <div className="relative z-10 space-y-4">
           <div className="inline-flex items-center gap-2 bg-primary/10 text-primary px-4 py-2 rounded-full font-black text-xs uppercase tracking-widest">
              <ShieldAlert className="h-3 w-3" />
              Security Audits
           </div>
           <h1 className="text-3xl sm:text-4xl font-black text-slate-900 tracking-tight">操作审计日志</h1>
           <p className="text-slate-500 font-medium max-w-xl text-lg">
             实时、全量记录管理员的所有关键写操作，确保平台管理的可追溯性与绝对安全性。
           </p>
        </div>
      </Card>

      {/* Logs Table */}
      <div className="bg-white rounded-[2.5rem] border border-slate-100 shadow-xl shadow-slate-200/50 overflow-hidden p-2 sm:p-6 pb-12">
        <ProTable<any>
          headerTitle={
            <div className="flex items-center gap-2 font-black text-slate-800">
              <Activity className="h-5 w-5 text-primary" />
              审计流水
            </div>
          }
          actionRef={actionRef}
          rowKey="id"
          search={false}
          request={async (params) => {
            const res: any = await request<any>("/api/admin/log/list/page", {
              method: "POST",
              data: {
                current: params.current,
                pageSize: params.pageSize,
              },
            });
            return {
              success: res.code === 0,
              data: res.data?.records || [],
              total: res.data?.total || 0,
            };
          }}
          columns={columns}
          pagination={{
            pageSize: 10,
            showSizeChanger: true,
          }}
        />
      </div>
    </div>
  );
}
