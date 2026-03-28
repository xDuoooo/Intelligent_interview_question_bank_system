import React, { useEffect, useState } from "react";
import { Button, Card, Col, Empty, InputNumber, Progress, Row, Space, Statistic, Switch, Tag, Typography, message } from "antd";
import { BookOutlined, CheckCircleOutlined, HeartOutlined } from "@ant-design/icons";
import { FireOutlined, CalendarOutlined, TrophyOutlined } from "@ant-design/icons";
import {
  getMyLearningGoalUsingGet,
  getMyQuestionStatsUsingGet,
  updateMyLearningGoalUsingPost
} from "@/api/userQuestionHistoryController";

/**
 * 学习数据看板
 * @constructor
 */
const LearningDataDashboard: React.FC = () => {
  const { Text, Title, Paragraph } = Typography;
  const [stats, setStats] = useState<any>({});
  const [dailyTarget, setDailyTarget] = useState(3);
  const [reminderEnabled, setReminderEnabled] = useState(true);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const fetchStats = async () => {
    try {
      const res = await getMyQuestionStatsUsingGet();
      setStats(res.data || {});
    } catch (error) {
      console.error("获取统计数据失败", error);
    }
  };

  const fetchGoal = async () => {
    try {
      const res = await getMyLearningGoalUsingGet();
      const data: API.LearningGoalData = res.data ?? {};
      setDailyTarget(Number(data.dailyTarget || 3));
      setReminderEnabled(Boolean(data.reminderEnabled));
    } catch (error) {
      console.error("获取学习目标失败", error);
    }
  };

  const loadData = async () => {
    setLoading(true);
    await Promise.all([fetchStats(), fetchGoal()]);
    setLoading(false);
  };

  useEffect(() => {
    loadData();
  }, []);

  const saveGoal = async () => {
    setSaving(true);
    try {
      await updateMyLearningGoalUsingPost({
        dailyTarget,
        reminderEnabled,
      });
      message.success("学习目标已更新");
      await loadData();
    } catch (error: any) {
      message.error("更新失败：" + (error?.message || "请稍后重试"));
    } finally {
      setSaving(false);
    }
  };

  const achievementList = stats.achievementList || [];
  const todayCount = Number(stats.todayCount || 0);
  const target = Number(stats.dailyTarget || dailyTarget || 3);
  const goalPercent = target > 0 ? Math.min(100, Math.round((todayCount / target) * 100)) : 0;

  return (
    <div style={{ marginBottom: 24 }} className="space-y-6">
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={8}>
          <Card bordered={false} loading={loading} className="stats-card">
            <Statistic
              title="累计刷题题数"
              value={stats.totalCount || 0}
              prefix={<BookOutlined style={{ color: "#1890ff" }} />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card bordered={false} loading={loading} className="stats-card">
            <Statistic
              title="已掌握"
              value={stats.masteredCount || 0}
              valueStyle={{ color: "#3f8600" }}
              prefix={<CheckCircleOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card bordered={false} loading={loading} className="stats-card">
            <Statistic
              title="收藏题目"
              value={stats.favourCount || 0}
              valueStyle={{ color: "#cf1322" }}
              prefix={<HeartOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card bordered={false} loading={loading} className="stats-card">
            <Statistic
              title="当前连续天数"
              value={stats.currentStreak || 0}
              valueStyle={{ color: "#fa8c16" }}
              prefix={<FireOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card bordered={false} loading={loading} className="stats-card">
            <Statistic
              title="累计活跃天数"
              value={stats.activeDays || 0}
              valueStyle={{ color: "#13c2c2" }}
              prefix={<CalendarOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card bordered={false} loading={loading} className="stats-card">
            <Statistic
              title="今日已完成"
              value={todayCount}
              suffix={`/ ${target}`}
              valueStyle={{ color: stats.goalCompletedToday ? "#3f8600" : "#722ed1" }}
              prefix={<TrophyOutlined />}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={10}>
          <Card loading={loading} bordered={false} className="stats-card">
            <div className="space-y-5">
              <div>
                <Title level={5} style={{ marginBottom: 8 }}>每日学习目标</Title>
                <Paragraph type="secondary" style={{ marginBottom: 0 }}>
                  设定每天的刷题目标，系统会在晚上 8 点对未达标用户触发站内提醒，并在已配置邮箱时同步发送邮件提醒。
                </Paragraph>
              </div>
              <Progress percent={goalPercent} strokeColor={stats.goalCompletedToday ? "#52c41a" : "#1677ff"} />
              <Space direction="vertical" size="middle" style={{ width: "100%" }}>
                <div className="flex items-center justify-between">
                  <Text strong>每日目标</Text>
                  <InputNumber
                    min={1}
                    max={200}
                    value={dailyTarget}
                    onChange={(value) => setDailyTarget(Number(value || 1))}
                  />
                </div>
                <div className="flex items-center justify-between">
                  <Text strong>晚上 8 点提醒</Text>
                  <Switch checked={reminderEnabled} onChange={setReminderEnabled} />
                </div>
                <Button type="primary" loading={saving} onClick={saveGoal}>
                  保存目标
                </Button>
              </Space>
            </div>
          </Card>
        </Col>
        <Col xs={24} lg={14}>
          <Card loading={loading} bordered={false} className="stats-card">
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <Title level={5} style={{ marginBottom: 0 }}>成就进度</Title>
                <Tag color="blue">数据驱动激励</Tag>
              </div>
              {achievementList.length === 0 ? (
                <Empty description="还没有成就数据" image={Empty.PRESENTED_IMAGE_SIMPLE} />
              ) : (
                <div className="grid grid-cols-1 gap-3">
                  {achievementList.map((item: any) => (
                    <div
                      key={item.key}
                      className={`rounded-2xl border p-4 transition-all ${item.achieved ? "border-emerald-200 bg-emerald-50/70" : "border-slate-200 bg-slate-50/70"}`}
                    >
                      <div className="flex items-start justify-between gap-3">
                        <div>
                          <div className="font-semibold text-slate-800">{item.title}</div>
                          <div className="text-sm text-slate-500 mt-1">{item.description}</div>
                        </div>
                        <Tag color={item.achieved ? "success" : "default"}>
                          {item.achieved ? "已达成" : `${item.current}/${item.target}`}
                        </Tag>
                      </div>
                      <Progress
                        percent={Math.round((Number(item.progress || 0) / Number(item.target || 1)) * 100)}
                        showInfo={false}
                        strokeColor={item.achieved ? "#52c41a" : "#1677ff"}
                        style={{ marginTop: 12, marginBottom: 0 }}
                      />
                    </div>
                  ))}
                </div>
              )}
            </div>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default LearningDataDashboard;
