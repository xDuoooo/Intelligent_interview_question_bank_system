"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import { Button, Card, Empty, List, Spin, Tag, Typography, message } from "antd";
import { ArrowRight, BrainCircuit, Briefcase, Clock3, Sparkles } from "lucide-react";
import { listMockInterviewVoByPageUsingPost } from "@/api/mockInterviewController";

const { Title, Paragraph, Text } = Typography;

const statusMap: Record<number, { text: string; color: string }> = {
  0: { text: "待开始", color: "orange" },
  1: { text: "进行中", color: "green" },
  2: { text: "已结束", color: "red" },
};

export default function MockInterviewHomePage() {
  const [loading, setLoading] = useState(true);
  const [interviewList, setInterviewList] = useState<API.MockInterview[]>([]);

  const loadMyInterviews = async () => {
    setLoading(true);
    try {
      const res = await listMockInterviewVoByPageUsingPost({
        current: 1,
        pageSize: 10,
        sortField: "createTime",
        sortOrder: "descend",
      });
      setInterviewList(res.data?.records || []);
    } catch (error: any) {
      message.error(error?.message || "加载模拟面试记录失败");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadMyInterviews();
  }, []);

  return (
    <div className="max-width-content space-y-8">
      <Card className="rounded-[2rem] border border-slate-100 shadow-2xl shadow-slate-200/50 overflow-hidden">
        <div className="relative px-2 py-4 sm:p-6">
          <div className="absolute right-0 top-0 opacity-5 p-8">
            <BrainCircuit className="h-32 w-32 text-slate-900" />
          </div>
          <div className="relative z-10 max-w-3xl space-y-5">
            <div className="inline-flex items-center gap-2 rounded-full bg-primary/10 px-4 py-2 text-xs font-black uppercase tracking-widest text-primary">
              <Sparkles className="h-3.5 w-3.5" />
              AI Mock Interview
            </div>
            <Title level={2} className="!mb-0 !font-black !text-slate-900">
              用完整对话流模拟真实技术面试
            </Title>
            <Paragraph className="!mb-0 text-base font-medium text-slate-500">
              选择目标岗位、经验和难度后，系统会生成连续追问，并在结束时自动给出评价建议，方便你把“刷题”真正转成“答题表达能力”。
            </Paragraph>
            <div className="flex flex-wrap gap-4 pt-2">
              <Link href="/mockInterview/add">
                <Button type="primary" size="large" className="h-12 rounded-2xl px-6 font-bold">
                  发起新的模拟面试
                </Button>
              </Link>
              <Link href="/questions">
                <Button size="large" className="h-12 rounded-2xl px-6 font-bold">
                  先去刷题热身
                </Button>
              </Link>
            </div>
          </div>
        </div>
      </Card>

      <Card
        title={<span className="text-lg font-black text-slate-800">我的模拟面试记录</span>}
        className="rounded-[2rem] border border-slate-100 shadow-xl shadow-slate-200/40"
      >
        {loading ? (
          <div className="py-16 text-center">
            <Spin />
          </div>
        ) : interviewList.length === 0 ? (
          <Empty
            description="还没有模拟面试记录，先创建一场试试看"
            image={Empty.PRESENTED_IMAGE_SIMPLE}
          >
            <Link href="/mockInterview/add">
              <Button type="primary">立即创建</Button>
            </Link>
          </Empty>
        ) : (
          <List
            dataSource={interviewList}
            split={false}
            renderItem={(item) => {
              const status = statusMap[item.status ?? 0] || statusMap[0];
              return (
                <List.Item className="!px-0">
                  <Card className="w-full rounded-2xl border border-slate-100 shadow-sm">
                    <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
                      <div className="space-y-3">
                        <div className="flex flex-wrap items-center gap-3">
                          <Title level={4} className="!mb-0 !text-slate-900">
                            {item.jobPosition || "未命名模拟面试"}
                          </Title>
                          <Tag color={status.color}>{status.text}</Tag>
                        </div>
                        <div className="flex flex-wrap gap-4 text-sm text-slate-500">
                          <span className="inline-flex items-center gap-1.5">
                            <Briefcase size={14} />
                            {item.workExperience || "经验不限"}
                          </span>
                          <span className="inline-flex items-center gap-1.5">
                            <Clock3 size={14} />
                            难度：{item.difficulty || "中等"}
                          </span>
                        </div>
                        <Text className="text-slate-400">
                          最近更新时间：{item.updateTime ? new Date(item.updateTime).toLocaleString() : "-"}
                        </Text>
                      </div>
                      <Link href={`/mockInterview/chat/${item.id}`}>
                        <Button type="primary" className="h-11 rounded-2xl px-5 font-bold">
                          进入会话
                          <ArrowRight size={16} />
                        </Button>
                      </Link>
                    </div>
                  </Card>
                </List.Item>
              );
            }}
          />
        )}
      </Card>
    </div>
  );
}
