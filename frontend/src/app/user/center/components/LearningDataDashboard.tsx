import React, { useEffect, useState } from "react";
import { Card, Col, Row, Statistic } from "antd";
import { BookOutlined, CheckCircleOutlined, HeartOutlined } from "@ant-design/icons";
import { getMyQuestionStatsUsingGet } from "@/api/userQuestionHistoryController";

/**
 * 学习数据看板
 * @constructor
 */
const LearningDataDashboard: React.FC = () => {
  const [stats, setStats] = useState<any>({});
  const [loading, setLoading] = useState(true);

  const fetchStats = async () => {
    try {
      const res = await getMyQuestionStatsUsingGet();
      setStats(res.data || {});
    } catch (error) {
      console.error("获取统计数据失败", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStats();
  }, []);

  return (
    <div style={{ marginBottom: 24 }}>
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={8}>
          <Card bordered={false} loading={loading} className="stats-card">
            <Statistic
              title="总刷题数"
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
      </Row>
    </div>
  );
};

export default LearningDataDashboard;
