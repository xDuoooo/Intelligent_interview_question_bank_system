import React, { useEffect, useState } from "react";
import { List, message } from "antd";
import Link from "next/link";
import { listMyFavourQuestionByPageUsingGet } from "@/api/userQuestionHistoryController";
import TagList from "@/components/TagList";

/**
 * 我的收藏列表
 * @constructor
 */
const MyFavourList: React.FC = () => {
  const [dataList, setDataList] = useState<API.QuestionVO[]>([]);
  const [total, setTotal] = useState<number>(0);
  const [loading, setLoading] = useState<boolean>(true);
  const [params, setParams] = useState({ current: 1, pageSize: 12 });

  const fetchData = async () => {
    setLoading(true);
    try {
      const res = await listMyFavourQuestionByPageUsingGet({
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
      renderItem={(item) => (
        <List.Item>
          <List.Item.Meta
            title={<Link href={`/question/${item.id}`}>{item.title}</Link>}
            description={<TagList tagList={item.tagList} />}
          />
        </List.Item>
      )}
    />
  );
};

export default MyFavourList;
