"use client";

import React from "react";
import dynamic from "next/dynamic";
import { Skeleton } from "antd";
import type { ProTableProps } from "@ant-design/pro-components";

const InnerProTable = dynamic(
  async () => {
    const mod = await import("@ant-design/pro-components");
    return mod.ProTable as React.ComponentType<any>;
  },
  {
    ssr: false,
    loading: () => (
      <div className="rounded-[2rem] border border-slate-100 bg-white p-6">
        <Skeleton active paragraph={{ rows: 8 }} />
      </div>
    ),
  },
);

export default function DynamicProTable<
  DataType extends Record<string, any>,
  Params extends Record<string, any> = Record<string, any>,
  ValueType = "text",
>(props: ProTableProps<DataType, Params, ValueType>) {
  return <InnerProTable {...props} />;
}
