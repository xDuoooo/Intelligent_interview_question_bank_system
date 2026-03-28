import React, { useCallback, useEffect, useState } from "react";
import { Empty, List, message } from "antd";
import Link from "next/link";
import { listMyQuestionHistoryByPageUsingGet } from "@/api/userQuestionHistoryController";
import TagList from "@/components/TagList";
import dayjs from "dayjs";

interface Props {
  limit?: number;
}

/**
 * 我的刷题记录列表
 */
const LearningHistoryList: React.FC<Props> = ({ limit }) => {
  const [dataList, setDataList] = useState<API.UserQuestionHistoryVO[]>([]);
  const [total, setTotal] = useState<number>(0);
  const [loading, setLoading] = useState<boolean>(true);
  const [params, setParams] = useState({ current: 1, pageSize: limit || 12 });

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const res = await listMyQuestionHistoryByPageUsingGet({
        ...params,
      });
      const pageData = res.data as any;
      setDataList(pageData?.records || []);
      setTotal(Number(pageData?.total) || 0);
    } catch (e: any) {
      message.error("获取数据失败，" + e.message);
    } finally {
      setLoading(false);
    }
  }, [params]);

  useEffect(() => {
    void fetchData();
  }, [fetchData]);

  return (
    <List
      loading={loading}
      itemLayout="horizontal"
      dataSource={dataList}
      locale={{
        emptyText: <Empty description="还没有刷题记录，去做几道题试试看" image={Empty.PRESENTED_IMAGE_SIMPLE} />,
      }}
      pagination={limit ? false : {
        onChange: (page) => setParams({ ...params, current: page }),
        current: params.current,
        pageSize: params.pageSize,
        total: total,
      }}
      renderItem={(item) => {
        const question = item.question;
        if (!question) return null;
        return (
          <List.Item
            extra={
              <div style={{ color: "rgba(0, 0, 0, 0.45)" }} className="text-xs">
                练习时间：{dayjs(item.updateTime).format("YYYY-MM-DD HH:mm")}
              </div>
            }
          >
            <List.Item.Meta
              title={<Link href={`/question/${question.id}`} className="font-semibold text-slate-700 hover:text-primary">{question.title}</Link>}
              description={<TagList tagList={question.tagList} />}
            />
          </List.Item>
        );
      }}
    />
  );
};

export default LearningHistoryList;
