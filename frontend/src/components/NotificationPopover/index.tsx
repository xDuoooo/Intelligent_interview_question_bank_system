"use client";

import React, { useEffect, useState } from "react";
import { Popover, List, Badge, Button, Typography, Space, Empty, message } from "antd";
import { Bell } from "lucide-react";
import { useRouter } from "next/navigation";
import { 
  listMyNotificationVOByPageUsingPost, 
  readAllNotificationUsingPost, 
  readNotificationUsingPost 
} from "@/api/notificationController";
import dayjs from "dayjs";
import { getNotificationTargetUrl } from "@/lib/notification";

const { Text } = Typography;

/**
 * 通知弹出层组件
 * @constructor
 */
const NotificationPopover: React.FC = () => {
  const [notifications, setNotifications] = useState<API.NotificationVO[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const router = useRouter();

  // 获取最近通知列表
  const fetchNotifications = async () => {
    setLoading(true);
    try {
      const res = await listMyNotificationVOByPageUsingPost({
        current: 1,
        pageSize: 5,
        sortField: "createTime",
        sortOrder: "descend",
      });
      if (res.data) {
        const data = res.data as API.PageNotificationVO_;
        setNotifications(data.records || []);
      }
    } catch (e: any) {
      message.error("获取通知失败");
    } finally {
      setLoading(false);
    }
  };

  // 获取未读通知数量
  const fetchUnreadCount = async () => {
    try {
      const res = await listMyNotificationVOByPageUsingPost({
        current: 1,
        pageSize: 1,
        status: 0, // 未读
      });
      if (res.data) {
        const data = res.data as API.PageNotificationVO_;
        setUnreadCount(data.total || 0);
      }
    } catch (e) {
      console.error("获取未读通知数量失败", e);
    }
  };

  // 轮询获取未读数量
  useEffect(() => {
    fetchUnreadCount();
    const timer = setInterval(fetchUnreadCount, 60000);
    return () => clearInterval(timer);
  }, []);

  // 全部标记已读
  const handleReadAll = async () => {
    try {
      const res = await readAllNotificationUsingPost();
      if (res.data) {
        message.success("全部标记为已读");
        fetchNotifications();
        fetchUnreadCount();
      }
    } catch (e) {
      message.error("操作失败");
    }
  };

  // 标记单条已读并跳转
  const handleRead = async (item: API.NotificationVO) => {
    const { id } = item;
    if (!id) return;
    const targetUrl = getNotificationTargetUrl(item);
    try {
      const res = await readNotificationUsingPost({ id });
      if (res.data) {
        fetchNotifications();
        fetchUnreadCount();
      }
    } catch (e) {
      message.error("已为你打开对应内容，通知状态稍后会自动同步");
    }
    router.push(targetUrl);
  };

  const popoverContent = (
    <div style={{ width: 320 }}>
      <div 
        style={{ 
          display: "flex", 
          justifyContent: "space-between", 
          alignItems: "center", 
          marginBottom: 8, 
          paddingBottom: 8, 
          borderBottom: "1px solid #f0f0f0" 
        }}
      >
        <Text strong style={{ fontSize: 16 }}>我的通知</Text>
        {unreadCount > 0 && (
          <Button type="link" size="small" onClick={handleReadAll} style={{ padding: 0 }}>
            全部标记已读
          </Button>
        )}
      </div>
      <List
        loading={loading}
        itemLayout="horizontal"
        dataSource={notifications}
        locale={{ 
          emptyText: <Empty description="暂无通知" image={Empty.PRESENTED_IMAGE_SIMPLE} /> 
        }}
        renderItem={(item) => (
          <List.Item
            style={{ 
              cursor: "pointer", 
              padding: "12px 0",
              opacity: item.status === 1 ? 0.6 : 1,
              transition: "background 0.3s"
            }}
            onMouseEnter={(e) => (e.currentTarget.style.backgroundColor = "#fafafa")}
            onMouseLeave={(e) => (e.currentTarget.style.backgroundColor = "transparent")}
            onClick={() => handleRead(item)}
          >
            <List.Item.Meta
              title={
                <Space align="start">
                  {!item.status && <Badge dot color="red" offset={[2, 4]} />}
                  <Text strong={!item.status} style={{ fontSize: 14 }}>{item.title}</Text>
                </Space>
              }
              description={
                <div style={{ paddingLeft: item.status ? 0 : 12 }}>
                  <div style={{ color: "rgba(0, 0, 0, 0.65)", marginBottom: 4, fontSize: 13 }}>
                    {item.content}
                  </div>
                  <Text type="secondary" style={{ fontSize: 12 }}>
                    {dayjs(item.createTime).format("YYYY-MM-DD HH:mm")}
                  </Text>
                </div>
              }
            />
          </List.Item>
        )}
      />
      {notifications.length > 0 && (
        <div 
          style={{ 
            textAlign: "center", 
            marginTop: 4, 
            borderTop: "1px solid #f0f0f0", 
            paddingTop: 8 
          }}
        >
          <Button type="link" size="small" style={{ fontSize: 13 }} onClick={() => router.push("/user/notifications")}>查看所有通知</Button>
        </div>
      )}
    </div>
  );

  return (
    <Popover
      content={popoverContent}
      trigger="click"
      placement="bottomRight"
      onOpenChange={(open) => open && fetchNotifications()}
      overlayStyle={{ paddingTop: 12 }}
    >
      <div className="relative cursor-pointer group">
        <Badge count={unreadCount} size="small" offset={[-2, 6]}>
          <div className="h-9 w-9 rounded-full flex items-center justify-center hover:bg-muted transition-colors text-muted-foreground group-hover:text-foreground">
            <Bell className="h-5 w-5" />
          </div>
        </Badge>
      </div>
    </Popover>
  );
};

export default NotificationPopover;
