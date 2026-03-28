"use client";
import React, { useState } from "react";
import { 
  Button, 
  Form, 
  Input, 
  InputNumber, 
  message, 
  Card, 
  Typography, 
  Space,
  Divider,
  Badge,
  Alert
} from "antd";
import { 
  Wand2, 
  Sparkles, 
  ArrowLeft, 
  CheckCircle2, 
  Loader2,
  BrainCircuit,
  Database
} from "lucide-react";
import Link from "next/link";
import { aiGenerateQuestionsUsingPost } from "@/api/questionController";

const { Title, Text, Paragraph } = Typography;

/**
 * AI 题目智能生成页面
 */
const AiGenerateQuestionPage: React.FC = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [successCount, setSuccessCount] = useState<number | null>(null);

  /**
   * 提交生成请求
   */
  const doSubmit = async (values: API.QuestionAIGenerateRequest) => {
    setLoading(true);
    setSuccessCount(null);
    try {
      // @ts-ignore
      const res = await aiGenerateQuestionsUsingPost(values);
      if (res.data) {
        setSuccessCount(Number(res.data));
        message.success(`成功生成 ${res.data} 道题目！`);
        form.resetFields();
      }
    } catch (error: any) {
      message.error("生成失败：" + error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-700">
      {/* Header */}
      <div className="flex items-center justify-between px-2">
        <Link 
          href="/admin/question"
          className="group flex items-center gap-2 text-sm font-bold text-slate-500 hover:text-primary transition-colors"
        >
          <ArrowLeft className="h-4 w-4 group-hover:-translate-x-1 transition-transform" />
          返回题目管理
        </Link>
        <Badge count="Beta" offset={[10, 0]} color="#7c3aed">
          <div className="flex items-center gap-2 text-slate-400 font-bold text-xs uppercase tracking-widest">
            Advanced AI Engine
          </div>
        </Badge>
      </div>

      {/* Hero Card */}
      <Card 
        className="bg-white/70 backdrop-blur-xl rounded-[2.5rem] border border-white shadow-2xl shadow-slate-200/50 relative overflow-hidden"
        bodyStyle={{ padding: "3rem" }}
      >
        <div className="absolute top-0 right-0 p-12 opacity-5 rotate-12">
           <BrainCircuit className="h-40 w-40 text-slate-900" />
        </div>
        
        <div className="relative z-10 space-y-6">
           <div className="inline-flex items-center gap-2 bg-primary/10 text-primary px-4 py-2 rounded-full font-black text-xs uppercase tracking-widest">
              <Sparkles className="h-3 w-3 animate-pulse" />
              Intelligence Content Generator
           </div>
           <Title level={2} className="!text-slate-900 !font-black !m-0 !text-3xl sm:!text-4xl tracking-tight">
             AI 智能面试题生成
           </Title>
           <Text className="text-slate-500 text-lg font-medium block max-w-xl">
             利用先进大模型技术，根据您设定的知识点方向，一键批量产出结构化、高质量的面试题目并自动入库。
           </Text>
        </div>
      </Card>

      <Row gutter={24}>
        <Col xs={24} lg={14}>
          <Card className="rounded-[2rem] border-slate-100 shadow-xl shadow-slate-200/50 p-6 sm:p-10 h-full">
            <Form 
              form={form} 
              layout="vertical" 
              onFinish={doSubmit}
              initialValues={{ number: 10 }}
              requiredMark={false}
            >
              <Form.Item 
                label={<span className="font-bold text-slate-700">知识点/技术方向</span>} 
                name="questionType"
                rules={[{ required: true, message: '请输入题目方向，如 Java、Spring Boot' }]}
              >
                <Input 
                  placeholder="例如：Go 并发编程、Kafka 核心原理..." 
                  className="h-14 rounded-2xl bg-slate-50 border-slate-100 focus:bg-white font-medium text-lg px-6"
                />
              </Form.Item>
              
              <Form.Item 
                label={<span className="font-bold text-slate-700">生成数量</span>} 
                name="number"
                rules={[{ required: true }]}
              >
                <InputNumber 
                  min={1} 
                  max={20} 
                  className="w-full h-14 rounded-2xl bg-slate-50 border-slate-100 flex items-center px-4"
                  size="large"
                />
              </Form.Item>

              <div className="pt-6">
                <Button
                  loading={loading}
                  type="primary"
                  htmlType="submit"
                  size="large"
                  className="w-full h-16 rounded-[1.25rem] bg-primary border-none shadow-xl shadow-primary/25 font-black text-xl flex items-center justify-center gap-3 hover:scale-[1.02] transition-transform active:scale-95"
                >
                  {loading ? (
                    <>正在构建知识体系...</>
                  ) : (
                    <>
                      <Wand2 className="h-6 w-6" />
                      立即开始生成
                    </>
                  )}
                </Button>
              </div>
            </Form>

            {successCount !== null && (
              <div className="mt-8 animate-in zoom-in duration-500">
                <Alert
                  message={
                    <div className="flex items-center gap-3 py-2">
                       <CheckCircle2 className="h-6 w-6 text-emerald-500" />
                       <div>
                          <Text className="font-black text-emerald-800 text-lg block">生成成功！</Text>
                          <Text className="text-emerald-700 font-medium opacity-80">已成功将 {successCount} 道题目入库，您可以在题目管理中查看。</Text>
                       </div>
                    </div>
                  }
                  type="success"
                  className="bg-emerald-50 border-emerald-100 rounded-2xl"
                />
              </div>
            )}
          </Card>
        </Col>

        <Col xs={24} lg={10}>
          <div className="space-y-6 h-full">
            <Card className="rounded-[2rem] border-slate-100 shadow-xl shadow-slate-200/50 p-8 bg-slate-50/50 backdrop-blur-sm h-full">
              <Title level={4} className="!font-black !text-slate-800 mb-6 flex items-center gap-2">
                <Database className="h-5 w-5 text-primary" />
                使用指南
              </Title>
              <div className="space-y-6">
                 <div>
                   <Text className="font-bold text-slate-600 mb-2 block uppercase text-xs tracking-widest">关键词选择</Text>
                   <Paragraph className="text-slate-500 font-medium">
                     建议输入具体且清晰的技术点，如 &quot;Redis 分布式锁实现&quot; 会比单纯的 &quot;Redis&quot; 效果更好。
                   </Paragraph>
                 </div>
                 <Divider className="my-0 border-slate-100" />
                 <div>
                   <Text className="font-bold text-slate-600 mb-2 block uppercase text-xs tracking-widest">生成逻辑</Text>
                   <Paragraph className="text-slate-500 font-medium">
                     系统会自动构造多维度的 Prompt，要求 AI 输出包含题目描述、标签以及详细参考答案的 JSON。
                   </Paragraph>
                 </div>
                 <Divider className="my-0 border-slate-100" />
                 <div className="bg-primary/10 p-4 rounded-2xl border border-primary/10">
                    <Text className="text-primary font-bold text-sm block">💡 提示</Text>
                    <Text className="text-primary/70 text-xs font-medium">单次建议生成量不超过 20 道，过大可能导致请求超时或 AI 响应被截断。</Text>
                 </div>
              </div>
            </Card>
          </div>
        </Col>
      </Row>
    </div>
  );
};

import { Row, Col } from "antd";
export default AiGenerateQuestionPage;
