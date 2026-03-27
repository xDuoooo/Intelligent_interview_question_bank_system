import React, { useEffect, useState } from "react";
import { List, message } from "antd";
import Link from "next/link";
import { listMyQuestionHistoryByPageUsingGet } from "@/api/userQuestionHistoryController";
import TagList from "@/components/TagList";

import dayjs from "dayjs";

/**
 * 我的刷题记录列表
 * @constructor
 */
const LearningHistoryList: React.FC = () => {
  const [dataList, setDataList] = useState<API.UserQuestionHistoryVO[]>([]);
  const [total, setTotal] = useState<number>(0);
  const [loading, setLoading] = useState<boolean>(true);
  const [params, setParams] = useState({ current: 1, pageSize: 12 });

  const fetchData = async () => {
    setLoading(true);
    try {
      const res = await listMyQuestionHistoryByPageUsingGet({
        ...params,
      });
      setDataList(res.data?.records || []);
      setTotal(Number(res.data?.total) || 0);
    } catch (e: any) {
      message.error("获取数据失败，" + e.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [params]);

  return (
    <List
      loading={loading}
      itemLayout="horizontal"
      dataSource={dataList}
      pagination={{
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
              <div style={{ color: "rgba(0, 0, 0, 0.45)" }}>
                练习时间：{dayjs(item.updateTime).format("YYYY-MM-DD HH:mm")}
              </div>
            }
          >
            <List.Item.Meta
              title={<Link href={`/question/${question.id}`}>{question.title}</Link>}
              description={<TagList tagList={question.tagList} />}
            />
          </List.Item>
        );
      }}
    />
  );
};

export default LearningHistoryList;
