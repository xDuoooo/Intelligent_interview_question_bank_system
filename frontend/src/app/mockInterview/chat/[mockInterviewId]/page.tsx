"use client";

import React, { useCallback, useEffect, useMemo, useState } from "react";
import { Button, Card, Empty, Input, List, Progress, Skeleton, Tag, Typography, message } from "antd";
import {
  Briefcase,
  BrainCircuit,
  ChevronRight,
  ClipboardCheck,
  Clock3,
  Flag,
  Radar,
  Sparkles,
} from "lucide-react";
import {
  getMockInterviewByIdUsingGet,
  handleMockInterviewEventUsingPost,
} from "@/api/mockInterviewController";
import "./index.css";

const { Title, Paragraph, Text } = Typography;

interface Message {
  content: string;
  isAI: boolean;
  timestamp: number;
  round?: number;
  stage?: string;
}

interface RoundRecord {
  round?: number;
  question?: string;
  answer?: string;
  shortComment?: string;
  focus?: string;
  score?: number;
  communicationScore?: number;
  technicalScore?: number;
  problemSolvingScore?: number;
}

interface InterviewReport {
  expectedRounds?: number;
  completedRounds?: number;
  overallScore?: number;
  summary?: string;
  communicationScore?: number;
  technicalScore?: number;
  problemSolvingScore?: number;
  strengths?: string[];
  improvements?: string[];
  suggestedTopics?: string[];
  roundRecords?: RoundRecord[];
}

interface MockInterviewDetail extends API.MockInterview {
  parsedMessages?: Message[];
  parsedReport?: InterviewReport | null;
}

const statusMap: Record<number, { text: string; color: string }> = {
  0: { text: "待开始", color: "orange" },
  1: { text: "进行中", color: "green" },
  2: { text: "已结束", color: "red" },
};

function safeParseJson<T>(value?: string | null): T | null {
  if (!value) {
    return null;
  }
  try {
    return JSON.parse(value) as T;
  } catch {
    return null;
  }
}

function formatTime(timestamp?: number) {
  if (!timestamp) {
    return "";
  }
  return new Date(timestamp).toLocaleTimeString("zh-CN", {
    hour: "2-digit",
    minute: "2-digit",
  });
}

export default function InterviewRoomPage({ params }: { params: { mockInterviewId: string } }) {
  const { mockInterviewId } = params;
  const interviewId = Number(mockInterviewId);
  const [submitting, setSubmitting] = useState(false);
  const [loading, setLoading] = useState(true);
  const [inputMessage, setInputMessage] = useState("");
  const [interview, setInterview] = useState<MockInterviewDetail>();
  const [messages, setMessages] = useState<Message[]>([]);

  const loadInterview = useCallback(async (silent = false) => {
    if (!interviewId) {
      message.error("面试记录不存在");
      return;
    }
    if (!silent) {
      setLoading(true);
    }
    try {
      const res = await getMockInterviewByIdUsingGet({ id: interviewId });
      const data = (res.data || {}) as MockInterviewDetail;
      data.parsedMessages = safeParseJson<Message[]>(data.messages) || [];
      data.parsedReport = safeParseJson<InterviewReport>(data.report) || null;
      setInterview(data);
      setMessages(data.parsedMessages || []);
    } catch (error: any) {
      message.error(error?.message || "加载面试数据失败");
    } finally {
      if (!silent) {
        setLoading(false);
      }
    }
  }, [interviewId]);

  useEffect(() => {
    void loadInterview();
  }, [loadInterview]);

  const status = statusMap[interview?.status ?? 0] || statusMap[0];
  const isStarted = interview?.status === 1;
  const isEnded = interview?.status === 2;
  const report = interview?.parsedReport;
  const expectedRounds = interview?.expectedRounds || report?.expectedRounds || 5;
  const currentRound = interview?.currentRound || report?.completedRounds || 0;
  const progressPercent = Math.min(100, Math.round((currentRound / Math.max(1, expectedRounds)) * 100));

  const latestRoundRecord = useMemo(() => {
    const roundRecords = report?.roundRecords || [];
    return roundRecords.length ? roundRecords[roundRecords.length - 1] : null;
  }, [report?.roundRecords]);

  const handleEvent = async (eventType: "start" | "chat" | "end", msg?: string) => {
    setSubmitting(true);
    const hide = message.loading(eventType === "chat" ? "面试官正在组织下一轮问题..." : "正在更新面试状态...");
    try {
      await handleMockInterviewEventUsingPost({
        event: eventType,
        id: interview?.id,
        message: msg,
      });
      hide();
      if (eventType === "start") {
        message.success("面试已开始");
      }
      if (eventType === "end") {
        message.success("已生成最终面试报告");
      }
      await loadInterview(true);
    } catch (error: any) {
      hide();
      message.error(error?.message || "操作失败");
    } finally {
      setSubmitting(false);
    }
  };

  const sendMessage = async () => {
    if (!inputMessage.trim()) {
      return;
    }
    await handleEvent("chat", inputMessage.trim());
    setInputMessage("");
  };

  const infoItems = [
    {
      icon: <Briefcase size={14} />,
      label: interview?.jobPosition || "未命名岗位",
    },
    {
      icon: <Flag size={14} />,
      label: interview?.interviewType || "技术深挖",
    },
    {
      icon: <Clock3 size={14} />,
      label: `${interview?.workExperience || "经验不限"} / ${interview?.difficulty || "中等"}`,
    },
    {
      icon: <Radar size={14} />,
      label: interview?.techStack || "通用后端",
    },
  ];

  if (loading) {
    return (
      <div id="interviewRoomPage" className="max-width-content">
        <div className="space-y-4">
          <Skeleton active paragraph={{ rows: 4 }} />
          <Skeleton active paragraph={{ rows: 8 }} />
        </div>
      </div>
    );
  }

  if (!interview) {
    return (
      <div id="interviewRoomPage" className="max-width-content">
        <Empty description="这场模拟面试不存在或无法访问" />
      </div>
    );
  }

  return (
    <div id="interviewRoomPage" className="max-width-content">
      <div className="interview-shell">
        <section className="interview-main">
          <Card className="hero-card">
            <div className="hero-header">
              <div>
                <div className="hero-eyebrow">
                  <BrainCircuit size={14} />
                  AI Mock Interview
                </div>
                <Title level={2} className="!mb-2 !mt-4 !text-slate-900">
                  模拟面试 #{interview.id}
                </Title>
                <Paragraph className="!mb-0 text-slate-500">
                  这一轮会尽量按真实面试节奏追问。建议你用“背景 - 方案 - 结果 - 复盘”的结构回答。
                </Paragraph>
              </div>
              <Tag color={status.color} className="status-tag">
                {status.text}
              </Tag>
            </div>

            <div className="hero-meta">
              {infoItems.map((item) => (
                <span className="meta-pill" key={item.label}>
                  {item.icon}
                  {item.label}
                </span>
              ))}
            </div>

            <div className="hero-actions">
              <Button
                type="primary"
                onClick={() => handleEvent("start")}
                disabled={isStarted || isEnded}
                loading={submitting}
                className="action-button"
              >
                开始面试
              </Button>
              <Button
                danger
                onClick={() => handleEvent("end")}
                disabled={!isStarted || isEnded}
                loading={submitting}
                className="action-button"
              >
                结束并生成报告
              </Button>
            </div>
          </Card>

          <Card className="message-card">
            <div className="section-heading">
              <div>
                <div className="section-eyebrow">Interview Flow</div>
                <Title level={4} className="!mb-0 !mt-2">
                  面试过程
                </Title>
              </div>
              <Text className="text-slate-400">
                当前已完成 {currentRound} / {expectedRounds} 轮
              </Text>
            </div>

            <div className="message-list">
              {messages.length ? (
                <List
                  dataSource={messages}
                  split={false}
                  renderItem={(item) => (
                    <List.Item
                      className={item.isAI ? "message-row ai" : "message-row user"}
                    >
                      <div className={`message-bubble ${item.isAI ? "ai" : "user"}`}>
                        <div className="message-head">
                          <span className="speaker">{item.isAI ? "面试官" : "候选人"}</span>
                          {item.round ? <span className="round-tag">第 {item.round} 轮</span> : null}
                        </div>
                        <div className="message-content">{item.content}</div>
                        <div className="message-time">{formatTime(item.timestamp)}</div>
                      </div>
                    </List.Item>
                  )}
                />
              ) : (
                <Empty description="还没有开始这场面试" image={Empty.PRESENTED_IMAGE_SIMPLE} />
              )}
            </div>

            <div className="input-area">
              <Input.TextArea
                value={inputMessage}
                onChange={(e) => setInputMessage(e.target.value)}
                placeholder="输入你的回答。建议尽量具体，给出业务背景、技术方案、结果指标和复盘。"
                disabled={!isStarted || isEnded}
                rows={4}
                onPressEnter={(e) => {
                  if (!e.shiftKey) {
                    e.preventDefault();
                    void sendMessage();
                  }
                }}
              />
              <Button
                type="primary"
                onClick={sendMessage}
                loading={submitting}
                disabled={!isStarted || isEnded}
                className="send-button"
              >
                发送回答
              </Button>
            </div>
          </Card>
        </section>

        <aside className="interview-side">
          <Card className="side-card">
            <div className="section-heading compact">
              <div>
                <div className="section-eyebrow">Progress</div>
                <Title level={5} className="!mb-0 !mt-2">
                  轮次进度
                </Title>
              </div>
              <span className="score-pill">{progressPercent}%</span>
            </div>
            <Progress percent={progressPercent} showInfo={false} strokeColor="#1677ff" />
            <div className="metric-grid">
              <div className="metric-card">
                <span>计划轮次</span>
                <strong>{expectedRounds}</strong>
              </div>
              <div className="metric-card">
                <span>已完成</span>
                <strong>{currentRound}</strong>
              </div>
              <div className="metric-card">
                <span>当前状态</span>
                <strong>{status.text}</strong>
              </div>
              <div className="metric-card">
                <span>最新总分</span>
                <strong>{report?.overallScore || latestRoundRecord?.score || "--"}</strong>
              </div>
            </div>
          </Card>

          <Card className="side-card">
            <div className="section-heading compact">
              <div>
                <div className="section-eyebrow">Round Insight</div>
                <Title level={5} className="!mb-0 !mt-2">
                  最近一轮反馈
                </Title>
              </div>
              <ClipboardCheck size={18} className="text-primary" />
            </div>
            {latestRoundRecord ? (
              <div className="round-feedback">
                <div className="feedback-score">
                  <span>本轮评分</span>
                  <strong>{latestRoundRecord.score || 0}</strong>
                </div>
                <Paragraph className="!mb-3 text-slate-600">
                  {latestRoundRecord.shortComment || "这一轮反馈将在你完成回答后显示。"}
                </Paragraph>
                <div className="feedback-focus">
                  <div className="focus-label">下一步可加强：</div>
                  <div className="focus-text">{latestRoundRecord.focus || "继续补充项目细节和设计取舍。"}</div>
                </div>
              </div>
            ) : (
              <Empty description="回答第一轮后，这里会显示本轮反馈" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            )}
          </Card>

          <Card className="side-card">
            <div className="section-heading compact">
              <div>
                <div className="section-eyebrow">Final Report</div>
                <Title level={5} className="!mb-0 !mt-2">
                  最终报告
                </Title>
              </div>
              <Sparkles size={18} className="text-amber-500" />
            </div>
            {isEnded && report ? (
              <div className="report-panel">
                <div className="report-overview">
                  <div className="overall-score">{report.overallScore || 0}</div>
                  <div>
                    <div className="report-label">综合评分</div>
                    <Paragraph className="!mb-0 text-slate-500">
                      {report.summary || "面试总结已生成。"}
                    </Paragraph>
                  </div>
                </div>

                <div className="dimension-list">
                  {[
                    { label: "表达能力", value: report.communicationScore || 0 },
                    { label: "技术深度", value: report.technicalScore || 0 },
                    { label: "问题分析", value: report.problemSolvingScore || 0 },
                  ].map((item) => (
                    <div className="dimension-item" key={item.label}>
                      <div className="dimension-head">
                        <span>{item.label}</span>
                        <strong>{item.value}</strong>
                      </div>
                      <Progress percent={item.value} showInfo={false} strokeColor="#0f172a" />
                    </div>
                  ))}
                </div>

                <div className="report-block">
                  <div className="block-title">亮点</div>
                  <ul>
                    {(report.strengths || []).map((item) => (
                      <li key={item}>{item}</li>
                    ))}
                  </ul>
                </div>

                <div className="report-block">
                  <div className="block-title">改进建议</div>
                  <ul>
                    {(report.improvements || []).map((item) => (
                      <li key={item}>{item}</li>
                    ))}
                  </ul>
                </div>

                <div className="report-block">
                  <div className="block-title">建议继续准备</div>
                  <div className="topic-list">
                    {(report.suggestedTopics || []).map((item) => (
                      <span className="topic-tag" key={item}>
                        {item}
                      </span>
                    ))}
                  </div>
                </div>
              </div>
            ) : (
              <div className="report-placeholder">
                <Paragraph className="!mb-0 text-slate-500">
                  面试结束后，这里会生成结构化复盘报告。你可以先专注回答问题，结束时再查看完整反馈。
                </Paragraph>
              </div>
            )}
          </Card>

          {(report?.roundRecords || []).length ? (
            <Card className="side-card">
              <div className="section-heading compact">
                <div>
                  <div className="section-eyebrow">Round Review</div>
                  <Title level={5} className="!mb-0 !mt-2">
                    逐轮记录
                  </Title>
                </div>
              </div>
              <div className="round-record-list">
                {(report?.roundRecords || []).map((item) => (
                  <div className="round-record-item" key={item.round}>
                    <div className="record-head">
                      <strong>第 {item.round} 轮</strong>
                      <span>{item.score || 0} 分</span>
                    </div>
                    <div className="record-question">{item.question}</div>
                    <div className="record-comment">
                      {item.shortComment}
                      <ChevronRight size={14} />
                    </div>
                  </div>
                ))}
              </div>
            </Card>
          ) : null}
        </aside>
      </div>
    </div>
  );
}
