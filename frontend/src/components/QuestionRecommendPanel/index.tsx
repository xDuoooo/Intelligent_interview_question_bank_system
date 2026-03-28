"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import { Card, Empty, List, Spin, Tag, Typography, message } from "antd";
import { ArrowRight, Compass, Sparkles } from "lucide-react";
import {
  listPersonalRecommendQuestionVoUsingGet,
  listRelatedQuestionVoUsingGet,
} from "@/api/questionController";
import TagList from "@/components/TagList";

const { Title, Paragraph, Text } = Typography;

interface Props {
  questionId: number;
}

/**
 * 题目推荐面板
 */
export default function QuestionRecommendPanel({ questionId }: Props) {
  const [loading, setLoading] = useState(true);
  const [personalList, setPersonalList] = useState<API.QuestionVO[]>([]);
  const [relatedList, setRelatedList] = useState<API.QuestionVO[]>([]);

  const loadData = async () => {
    setLoading(true);
    try {
      const [personalRes, relatedRes] = await Promise.all([
        listPersonalRecommendQuestionVoUsingGet({ questionId, size: 4 }),
        listRelatedQuestionVoUsingGet({ questionId, size: 4 }),
      ]);
      setPersonalList(personalRes.data || []);
      setRelatedList(relatedRes.data || []);
    } catch (error: any) {
      message.error("加载推荐题目失败：" + (error?.message || "请稍后重试"));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!questionId) {
      return;
    }
    loadData();
  }, [questionId]);

  const renderQuestionList = (dataList: API.QuestionVO[], emptyText: string) => {
    if (loading) {
      return (
        <div className="py-14 text-center">
          <Spin />
        </div>
      );
    }
    if (!dataList.length) {
      return <Empty description={emptyText} image={Empty.PRESENTED_IMAGE_SIMPLE} />;
    }
    return (
      <List
        dataSource={dataList}
        split={false}
        renderItem={(item) => (
          <List.Item className="!px-0">
            <Link href={`/question/${item.id}`} className="block w-full">
              <div className="rounded-2xl border border-slate-100 bg-slate-50/70 p-4 transition-all hover:border-primary/30 hover:bg-white hover:shadow-sm">
                <div className="flex items-start justify-between gap-3">
                  <div className="min-w-0">
                    <div className="font-semibold text-slate-800 line-clamp-2">{item.title}</div>
                    {item.recommendReason && (
                      <Paragraph className="!mb-0 !mt-2 text-sm text-slate-500">
                        {item.recommendReason}
                      </Paragraph>
                    )}
                  </div>
                  <ArrowRight className="h-4 w-4 shrink-0 text-slate-300" />
                </div>
                {item.tagList?.length ? (
                  <div className="mt-3">
                    <TagList tagList={item.tagList.slice(0, 3)} />
                  </div>
                ) : null}
              </div>
            </Link>
          </List.Item>
        )}
      />
    );
  };

  return (
    <section className="grid grid-cols-1 gap-6 xl:grid-cols-2">
      <Card
        className="rounded-[2rem] border border-slate-100 shadow-2xl shadow-slate-200/40"
        title={
          <div className="flex items-center gap-2 font-black text-lg text-slate-800">
            <Sparkles className="h-5 w-5 text-primary" />
            猜你喜欢
          </div>
        }
        extra={<Tag color="blue">行为驱动推荐</Tag>}
      >
        <Paragraph className="text-slate-500">
          结合你的刷题记录、收藏偏好和当前题目标签，推荐下一步更值得继续攻克的题目。
        </Paragraph>
        {renderQuestionList(personalList, "暂时还没有可推荐的题目")}
      </Card>

      <Card
        className="rounded-[2rem] border border-slate-100 shadow-2xl shadow-slate-200/40"
        title={
          <div className="flex items-center gap-2 font-black text-lg text-slate-800">
            <Compass className="h-5 w-5 text-emerald-500" />
            相关题目
          </div>
        }
        extra={<Tag color="green">标签关联推荐</Tag>}
      >
        <Text className="text-slate-500">
          根据当前题目的核心标签，筛出语义最接近、适合延伸练习的关联题目。
        </Text>
        <div className="mt-4">
          {renderQuestionList(relatedList, "当前题目暂未找到更多关联题目")}
        </div>
      </Card>
    </section>
  );
}
