"use client";
import {Avatar, Card, Col, Row, Segmented, Tag} from "antd";
import {useSelector} from "react-redux";
import {RootState} from "@/stores";
import Title from "antd/es/typography/Title";
import Paragraph from "antd/es/typography/Paragraph";
import {useState} from "react";
import CalendarChart from "@/app/user/center/components/CalendarChart";
import "./index.css";
import UserInfo from "@/app/user/center/components/UserInfo";
import UserInfoEditForm from "@/app/user/center/components/UserInfoEditForm";
import {USER_ROLE_ENUM, USER_ROLE_TEXT_MAP} from "@/constants/user";
import dayjs from "dayjs";

import PasswordChangeForm from "@/app/user/center/components/PasswordChangeForm";
import LearningDataDashboard from "@/app/user/center/components/LearningDataDashboard";
import MyFavourList from "@/app/user/center/components/MyFavourList";
import LearningHistoryList from "@/app/user/center/components/LearningHistoryList";

/**
 * 用户中心页面
 * @constructor
 */
export default function UserCenterPage() {
  // 获取登录用户信息
  const loginUser = useSelector((state: RootState) => state.loginUser);
  // 便于复用，新起一个变量
  const user = loginUser;
  // 控制菜单栏的 Tab 高亮
  const [activeTabKey, setActiveTabKey] = useState<string>("info");
  // 控制内容切换（Segmented）
  const [innerState, setInnerState] = useState<string>("查看信息");

  // 当主 Tab 切换时，重置子状态
  const onTabChange = (key: string) => {
    setActiveTabKey(key);
    if (key === "info") setInnerState("查看信息");
    if (key === "security") setInnerState("修改密码");
    if (key === "record") setInnerState("成就看板");
  };

  return (
    <div id="userCenterPage" className="max-width-content">
      <Row gutter={[16, 16]}>
        <Col xs={24} md={6}>
          <Card style={{ textAlign: "center" }}>
            <Avatar src={user.userAvatar} size={72} />
            <div style={{ marginBottom: 16 }} />
            <Card.Meta
              title={
                <Title level={4} style={{ marginBottom: 0 }}>
                  {user.userName}
                </Title>
              }
              description={
                <Paragraph type="secondary">{user.userProfile}</Paragraph>
              }
            />
            <Tag
              color={user.userRole === USER_ROLE_ENUM.ADMIN ? "gold" : "grey"}
            >
              {user.userRole
                ? (USER_ROLE_TEXT_MAP as Record<string, string>)[user.userRole]
                : "未知角色"}
            </Tag>
            <Paragraph type="secondary" style={{ marginTop: 8 }}>
              注册日期：{dayjs(user.createTime).format("YYYY-MM-DD")}
            </Paragraph>
            <Paragraph type="secondary" style={{ marginTop: 8 }} copyable={{
              text: String(user.id)
            }}>
              我的 id：{user.id}
            </Paragraph>
          </Card>
        </Col>
        <Col xs={24} md={18}>
          <Card
            tabList={[
              {
                key: "info",
                label: "我的信息",
              },
              {
                key: "record",
                label: "刷题记录",
              },
              {
                key: "security",
                label: "账号安全",
              },
              {
                key: "others",
                label: "其他内容",
              },
            ]}
            activeTabKey={activeTabKey}
            onTabChange={onTabChange}
          >
            {activeTabKey === "info" && (
              <>
                <Segmented<string>
                  options={["查看信息", "修改信息"]}
                  value={innerState}
                  onChange={setInnerState}
                  style={{ marginBottom: 20 }}
                />
                {innerState === "查看信息" && <UserInfo user={user} />}
                {innerState === "修改信息" && (
                  <UserInfoEditForm user={user} />
                )}
              </>
            )}
            {activeTabKey === "security" && (
              <>
                <Segmented<string>
                  options={["修改密码"]}
                  value={innerState}
                  onChange={setInnerState}
                  style={{ marginBottom: 20 }}
                />
                {innerState === "修改密码" && <PasswordChangeForm />}
              </>
            )}
            {activeTabKey === "record" && (
              <>
                <Segmented<string>
                  options={["成就看板", "收藏题目", "学习轨迹"]}
                  value={innerState}
                  onChange={setInnerState}
                  style={{ marginBottom: 20 }}
                />
                {innerState === "成就看板" && (
                  <>
                    <LearningDataDashboard />
                    <Title level={5}>刷题热力图</Title>
                    <CalendarChart />
                  </>
                )}
                {innerState === "收藏题目" && <MyFavourList />}
                {innerState === "学习轨迹" && <LearningHistoryList />}
              </>
            )}
            {activeTabKey === "others" && <>这里暂无内容，敬请期待</>}
          </Card>
        </Col>
      </Row>
    </div>
  );
}
