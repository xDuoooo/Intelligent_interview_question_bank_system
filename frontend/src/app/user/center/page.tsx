"use client";
import React, { Suspense, useEffect, useRef, useState } from "react";
import { Avatar, Card, Col, Row, Tag, Button, Typography, Modal, message, Popover } from "antd";
import { useSelector, useDispatch } from "react-redux";
import { RootState } from "@/stores";
import { useSearchParams, useRouter } from "next/navigation";
import Image from "next/image";
import dayjs from "dayjs";
import {
  Edit,
  LayoutDashboard,
  History,
  Heart,
  ShieldCheck,
  Calendar,
  BookOpen,
  Flame,
  User as UserIcon
} from "lucide-react";

import { getMyQuestionStatsUsingGet } from "@/api/userQuestionHistoryController";
import { getLoginUserUsingGet } from "@/api/userController";
import { setLoginUser } from "@/stores/loginUser";
import { USER_ROLE_ENUM, USER_ROLE_TEXT_MAP } from "@/constants/user";

import CalendarChart from "@/app/user/center/components/CalendarChart";
import UserInfoEditForm from "@/app/user/center/components/UserInfoEditForm";
import AccountSecurityCenter from "@/app/user/center/components/AccountSecurityCenter";
import LearningDataDashboard from "@/app/user/center/components/LearningDataDashboard";
import MyFavourList from "@/app/user/center/components/MyFavourList";
import LearningHistoryList from "@/app/user/center/components/LearningHistoryList";
import ResumeRecommendPanel from "@/app/user/center/components/ResumeRecommendPanel";
import "./index.css";

const { Title, Paragraph, Text } = Typography;

/**
 * 用户中心页面
 */
function UserCenterContent() {
  const loginUser = useSelector((state: RootState) => state.loginUser);
  const user = loginUser;
  const [activeTabKey, setActiveTabKey] = useState<string>("overview");
  const [isEditModalVisible, setIsEditModalVisible] = useState(false);
  const [stats, setStats] = useState<any>({});

  const searchParams = useSearchParams();
  const router = useRouter();
  const hasShownMessage = useRef(false);

  // 获取统计数据用于侧边栏快查
  const fetchStats = async () => {
    try {
      const res = await getMyQuestionStatsUsingGet();
      setStats(res.data || {});
    } catch (error) { }
  };

  useEffect(() => {
    fetchStats();
  }, [user]);

  // 处理来自三方跳转的提示消息
  useEffect(() => {
    const error = searchParams.get("error");
    const msg = searchParams.get("msg");
    if ((error || msg) && !hasShownMessage.current) {
      if (error) message.error(error);
      if (msg) message.success(msg);
      hasShownMessage.current = true;
      setActiveTabKey("security");
      router.replace(window.location.pathname);
    }
  }, [searchParams, router]);

  const onTabChange = (key: string) => {
    setActiveTabKey(key);
  };

  return (
    <div id="userCenterPage" className="max-width-content">
      <Row gutter={[24, 24]}>
        {/* 左侧华丽版侧边栏 */}
        <Col xs={24} md={7}>
          <Card
            className="user-profile-card overflow-hidden"
          >
            {/* 用户核心信息 */}
            <div className="px-6 py-8 relative">
              <div className="relative mb-6 inline-block">
                <Avatar
                  src={user.userAvatar}
                  size={96}
                  className="shadow-xl ring-1 ring-slate-100"
                />
                {user.userRole === USER_ROLE_ENUM.ADMIN && (
                  <div className="absolute -bottom-1 -right-1 bg-amber-400 p-1.5 rounded-full border-2 border-white shadow-sm">
                    <ShieldCheck size={14} className="text-white" />
                  </div>
                )}
              </div>

              <div className="flex items-center justify-between mb-2">
                <Title level={4} style={{ margin: 0 }}>{user.userName}</Title>
                <Button
                  icon={<Edit size={14} />}
                  size="small"
                  className="rounded-lg border-slate-200 text-slate-500 hover:text-primary"
                  onClick={() => setIsEditModalVisible(true)}
                >
                  编辑资料
                </Button>
              </div>

              <Paragraph type="secondary" className="text-sm line-clamp-2 min-h-[40px] mb-4">
                {user.userProfile || "这个用户很懒，什么都没有写~"}
              </Paragraph>

              <div className="flex flex-wrap gap-2 mb-6">
                <Tag color={user.userRole === USER_ROLE_ENUM.ADMIN ? "gold" : "blue"} className="rounded-full px-3 m-0">
                  {user.userRole ? (USER_ROLE_TEXT_MAP as Record<string, string>)[user.userRole] : "未知角色"}
                </Tag>
                <Tag className="rounded-full px-3 m-0 bg-slate-50 border-slate-100">
                  ID: {user.id}
                </Tag>
                {user.city ? (
                  <Tag className="rounded-full px-3 m-0 bg-emerald-50 border-emerald-100 text-emerald-700">
                    城市: {user.city}
                  </Tag>
                ) : null}
              </div>

              {/* 侧边栏快查数据 */}
              <div className="grid grid-cols-2 gap-3 pt-6 border-t border-slate-100">
                <div className="p-3 rounded-2xl bg-blue-50/50 border border-blue-100/30">
                  <div className="text-[10px] uppercase font-black text-blue-400 flex items-center gap-1.5 mb-1">
                    <BookOpen size={12} /> Total Solved
                  </div>
                  <div className="text-xl font-black text-blue-700">{stats.totalCount || 0}</div>
                </div>
                <div className="p-3 rounded-2xl bg-orange-50/50 border border-orange-100/30">
                  <div className="text-[10px] uppercase font-black text-orange-400 flex items-center gap-1.5 mb-1">
                    <Flame size={12} /> Mastered
                  </div>
                  <div className="text-xl font-black text-orange-700">{stats.masteredCount || 0}</div>
                </div>
              </div>

              <Paragraph type="secondary" className="text-[11px] mt-6 text-center opacity-40">
                Member since {user.createTime ? dayjs(user.createTime).format("YYYY-MM-DD") : "Today"}
              </Paragraph>
            </div>
          </Card>
        </Col>

        {/* 右侧核心内容区 */}
        <Col xs={24} md={17}>
          <Card
            className="user-tabs-card min-h-[600px]"
            tabList={[
              { key: "overview", label: <span className="flex items-center gap-2"><LayoutDashboard size={16} />个人概览</span> },
              { key: "record", label: <span className="flex items-center gap-2"><Calendar size={16} />成就看板</span> },
              { key: "security", label: <span className="flex items-center gap-2"><ShieldCheck size={16} />账号安全</span> },
              { key: "favour", label: <span className="flex items-center gap-2"><Heart size={16} />收藏题目</span> },
              { key: "history", label: <span className="flex items-center gap-2"><History size={16} />刷题轨迹</span> },
            ]}
            activeTabKey={activeTabKey}
            onTabChange={onTabChange}
          >
            {activeTabKey === "overview" && (
              <div className="fade-in animate-in slide-in-from-bottom-2 duration-500">
                <LearningDataDashboard />
                <div className="mb-8">
                  <ResumeRecommendPanel />
                </div>
                <Title level={5} className="flex items-center gap-2 mb-6">
                  <Calendar size={18} className="text-primary" /> 刷题热力图
                </Title>
                <div className="bg-slate-50/50 p-6 rounded-3xl border border-dashed border-slate-200 mb-8">
                  <CalendarChart />
                </div>
                <Title level={5} className="flex items-center gap-2 mb-6">
                  <History size={18} className="text-primary" /> 最近刷题
                </Title>
                <LearningHistoryList limit={5} />
              </div>
            )}
            {activeTabKey === "security" && (
              <div className="fade-in animate-in slide-in-from-bottom-2 duration-500">
                <AccountSecurityCenter user={user} />
              </div>
            )}
            {activeTabKey === "record" && <LearningDataDashboard />}
            {activeTabKey === "favour" && <MyFavourList />}
            {activeTabKey === "history" && <LearningHistoryList />}
          </Card>
        </Col>
      </Row>

      {/* 编辑个人资料弹窗 */}
      <Modal
        title={null}
        open={isEditModalVisible}
        onCancel={() => setIsEditModalVisible(false)}
        footer={null}
        width={720}
        destroyOnClose
        centered
        className="profile-edit-modal"
      >
        <div className="pt-4">
          <div className="flex items-center gap-3 mb-8 px-2 border-b border-slate-100 pb-4">
            <div className="p-3 bg-blue-50 rounded-2xl">
              <UserIcon size={28} className="text-primary" />
            </div>
            <div>
              <Typography.Title level={4} style={{ margin: 0 }}>完善您的资料</Typography.Title>
              <Text type="secondary" className="text-sm">打造一个个性化的个人主页</Text>
            </div>
          </div>
          <UserInfoEditForm user={user} onSuccess={() => setIsEditModalVisible(false)} />
        </div>
      </Modal>
    </div>
  );
}

export default function UserCenterPage() {
  return (
    <Suspense fallback={<div className="max-width-content py-16 text-center text-slate-400">正在加载用户中心...</div>}>
      <UserCenterContent />
    </Suspense>
  );
}
