"use client";

import React, { useEffect, useState } from "react";
import { List, Card, Badge, Button, Space, Typography, message, Pagination, Empty } from "antd";
import { Bell, CheckCheck, ChevronRight } from "lucide-react";
import { useRouter } from "next/navigation";
import {
  listMyNotificationVOByPageUsingPost,
  readAllNotificationUsingPost,
  readNotificationUsingPost,
} from "@/api/notificationController";
import dayjs from "dayjs";

const { Title, Text } = Typography;

/**
 * 我的通知页面
 * @constructor
 */
const UserNotificationsPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [dataList, setDataList] = useState<API.NotificationVO[]>([]);
  const [total, setTotal] = useState(0);
  const [current, setCurrent] = useState(1);
  const pageSize = 10;
  const router = useRouter();

  // 加载通知数据
  const loadData = async (page = 1) => {
    setLoading(true);
    try {
      const res = await listMyNotificationVOByPageUsingPost({
        current: page,
        pageSize,
        sortField: "createTime",
        sortOrder: "descend",
      });
      if (res.data) {
        const data = res.data as API.PageNotificationVO_;
        setDataList(data.records || []);
        setTotal(data.total || 0);
      }
    } catch (e: any) {
      message.error("加载通知失败：" + e.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData(current);
  }, [current]);

  // 标记单条已读并跳转
  const handleRead = async (item: API.NotificationVO) => {
    const { id, targetId, status } = item;
    if (!id) return;
    
    // 如果未读，先标已读
    if (status === 0) {
      try {
        await readNotificationUsingPost({ id });
        loadData(current);
      } catch (e) {
        // 忽略错误，优先跳转
      }
    }

    // 跳转
    if (targetId) {
      router.push(`/question/${targetId}`);
    }
  };

  // 全部标记已读
  const handleReadAll = async () => {
    try {
      const res = await readAllNotificationUsingPost();
      if (res.data) {
        message.success("已将所有通知标记为已读");
        loadData(1);
        setCurrent(1);
      }
    } catch (e: any) {
      message.error("操作失败：" + e.message);
    }
  };

  return (
    <div style={{ maxWidth: 900, margin: "0 auto", padding: "24px 16px" }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 24 }}>
        <Space size="middle">
          <div style={{ 
            width: 40, 
            height: 40, 
            borderRadius: 8, 
            backgroundColor: "#e6f7ff", 
            display: "flex", 
            alignItems: "center", 
            justifyContent: "center",
            color: "#1890ff"
          }}>
            <Bell size={24} />
          </div>
          <Title level={3} style={{ margin: 0 }}>我的通知</Title>
        </Space>
        {total > 0 && (
          <Button 
            icon={<CheckCheck size={16} />} 
            onClick={handleReadAll}
            disabled={loading}
          >
            全部标记已读
          </Button>
        )}
      </div>

      <Card bodyStyle={{ padding: 0 }} bordered={false}>
        <List
          loading={loading}
          dataSource={dataList}
          locale={{
            emptyText: <Empty description="暂无通知" image={Empty.PRESENTED_IMAGE_SIMPLE} />
          }}
          renderItem={(item) => (
            <List.Item
              className="hover:bg-gray-50 transition-colors cursor-pointer"
              style={{ padding: "20px 24px", opacity: item.status === 1 ? 0.7 : 1 }}
              onClick={() => handleRead(item)}
              extra={<ChevronRight size={18} className="text-gray-300" />}
            >
              <List.Item.Meta
                avatar={
                  <Badge dot={item.status === 0} offset={[-2, 2]}>
                    <div style={{ 
                      width: 44, 
                      height: 44, 
                      borderRadius: "50%", 
                      backgroundColor: item.status === 0 ? "#1890ff15" : "#f5f5f5",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      color: item.status === 0 ? "#1890ff" : "#bfbfbf"
                    }}>
                      <Bell size={20} />
                    </div>
                  </Badge>
                }
                title={
                  <Space>
                    <Text strong={item.status === 0} style={{ fontSize: 16 }}>{item.title}</Text>
                    <Text type="secondary" style={{ fontSize: 13, marginLeft: 8 }}>
                      {dayjs(item.createTime).format("YYYY-MM-DD HH:mm")}
                    </Text>
                  </Space>
                }
                description={
                  <div style={{ marginTop: 4, color: "rgba(0, 0, 0, 0.65)", fontSize: 14 }}>
                    {item.content}
                  </div>
                }
              />
            </List.Item>
          )}
        />
        {total > pageSize && (
          <div style={{ padding: "24px", textAlign: "right", borderTop: "1px solid #f0f0f0" }}>
            <Pagination
              current={current}
              total={total}
              pageSize={pageSize}
              onChange={(page) => setCurrent(page)}
              showSizeChanger={false}
            />
          </div>
        )}
      </Card>
    </div>
  );
};

export default UserNotificationsPage;
