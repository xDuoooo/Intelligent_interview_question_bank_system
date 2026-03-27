"use client";
import React, { useState } from "react";
import { Button, List, Modal, Tag, message, Typography } from "antd";
import { 
  ShieldCheck, 
  Key, 
  Phone, 
  Mail, 
  Cloud, 
  ArrowRight,
  Link as LinkIcon,
  Unlink,
  Globe,
  Share2
} from "lucide-react";
import Image from "next/image";
import { 
  unbindGithubUsingPost, 
  unbindGiteeUsingPost, 
  unbindGoogleUsingPost 
} from "@/api/userController";
import { useDispatch } from "react-redux";
import { AppDispatch } from "@/stores";
import { setLoginUser } from "@/stores/loginUser";
import PasswordChangeForm from "../PasswordChangeForm";

const { Text } = Typography;

interface Props {
  user: API.LoginUserVO;
}

/**
 * 账号安全中心组件
 */
const AccountSecurityCenter: React.FC<Props> = ({ user }) => {
  const dispatch = useDispatch<AppDispatch>();
  const [passwordModalVisible, setPasswordModalVisible] = useState(false);

  // 脱敏显示
  const maskPhone = (phone?: string) => phone ? phone.replace(/(\d{3})\d{4}(\d{4})/, "$1****$2") : "未绑定";
  const maskEmail = (email?: string) => email ? email.replace(/(.{2}).+(.{2}@.+)/, "$1****$2") : "未绑定";

  // 解绑逻辑
  const handleUnbind = async (type: "github" | "gitee" | "google") => {
    Modal.confirm({
      title: "确认解绑",
      content: `确定要解绑您的 ${type.toUpperCase()} 账号吗？解绑后将无法通过该方式登录。`,
      onOk: async () => {
        try {
          if (type === "github") await unbindGithubUsingPost();
          if (type === "gitee") await unbindGiteeUsingPost();
          if (type === "google") await unbindGoogleUsingPost();
          message.success("解绑成功");
          // 更新全局状态
          const newUser = { ...user, [`${type}Id`]: "" };
          dispatch(setLoginUser(newUser));
        } catch (error: any) {
          message.error("解绑失败：" + error.message);
        }
      },
    });
  };

  const securityItems = [
    {
      key: "password",
      title: "登录密码",
      description: "定期更换密码可以提高账号安全性",
      status: <Tag color="success">已设置</Tag>,
      icon: <Key size={20} className="text-blue-500" />,
      action: <Button type="link" onClick={() => setPasswordModalVisible(true)}>修改密码</Button>
    },
    {
      key: "phone",
      title: "手机绑定",
      description: `当前绑定：${maskPhone(user.phone)}`,
      status: user.phone ? <Tag color="success">已绑定</Tag> : <Tag>未绑定</Tag>,
      icon: <Phone size={20} className="text-green-500" />,
      action: <Button type="link" disabled>{user.phone ? "更换手机" : "立即绑定"}</Button>
    },
    {
      key: "email",
      title: "邮箱绑定",
      description: `当前绑定：${maskEmail(user.email)}`,
      status: user.email ? <Tag color="success">已绑定</Tag> : <Tag>未绑定</Tag>,
      icon: <Mail size={20} className="text-orange-500" />,
      action: <Button type="link" disabled>{user.email ? "更换邮箱" : "立即绑定"}</Button>
    },
    {
      key: "github",
      title: "GitHub 账号",
      description: user.githubId ? "已关联 GitHub 账号" : "关联后支持快捷登录",
      status: user.githubId ? <Tag color="success">已关联</Tag> : <Tag>未关联</Tag>,
      icon: <Image src="/assets/github-logo.png" width={20} height={20} alt="GitHub" />,
      action: user.githubId ? (
        <Button title="解绑" type="text" danger icon={<Unlink size={16}/>} onClick={() => handleUnbind("github")} />
      ) : (
        <Button type="link" href={`http://localhost:8101/api/user/login/github?action=bind`}>立即关联</Button>
      )
    },
    {
      key: "gitee",
      title: "Gitee 账号",
      description: user.giteeId ? "已关联 Gitee 账号" : "关联后支持快捷登录",
      status: user.giteeId ? <Tag color="success">已关联</Tag> : <Tag>未关联</Tag>,
      icon: <Image src="/assets/gitee-logo.png" width={20} height={20} alt="Gitee" />,
      action: user.giteeId ? (
        <Button title="解绑" type="text" danger icon={<Unlink size={16}/>} onClick={() => handleUnbind("gitee")} />
      ) : (
        <Button type="link" href={`http://localhost:8101/api/user/login/gitee?action=bind`}>立即关联</Button>
      )
    },
    {
      key: "google",
      title: "Google 账号",
      description: user.googleId ? "已关联 Google 账号" : "关联后支持快捷登录",
      status: user.googleId ? <Tag color="success">已关联</Tag> : <Tag>未关联</Tag>,
      icon: <Image src="/assets/google-logo.png" width={20} height={20} alt="Google" />,
      action: user.googleId ? (
        <Button title="解绑" type="text" danger icon={<Unlink size={16}/>} onClick={() => handleUnbind("google")} />
      ) : (
        <Button type="link" href={`http://localhost:8101/api/user/login/google?action=bind`}>立即关联</Button>
      )
    }
  ];

  return (
    <div className="account-security-center">
      <div className="flex items-center gap-3 mb-8 px-2">
        <div className="p-3 bg-blue-50 rounded-2xl">
          <ShieldCheck size={28} className="text-primary" />
        </div>
        <div>
          <Typography.Title level={4} style={{ margin: 0 }}>账号安全中心</Typography.Title>
          <Text type="secondary" className="text-sm">管理您的账号安全与第三方服务关联</Text>
        </div>
      </div>

      <List
        className="bg-white rounded-2xl border-none"
        itemLayout="horizontal"
        dataSource={securityItems}
        renderItem={(item) => (
          <List.Item
            className="px-4 py-6 hover:bg-slate-50/50 transition-colors border-b border-slate-100 last:border-b-0"
            actions={[item.action]}
          >
            <List.Item.Meta
              avatar={<div className="p-2.5 bg-white rounded-xl shadow-sm border border-slate-100">{item.icon}</div>}
              title={<span className="font-semibold text-slate-800 flex items-center gap-3">{item.title} {item.status}</span>}
              description={item.description}
            />
          </List.Item>
        )}
      />

      <Modal
        title="修改登录密码"
        open={passwordModalVisible}
        onCancel={() => setPasswordModalVisible(false)}
        footer={null}
        destroyOnClose
        centered
      >
        <div className="pt-4">
          <PasswordChangeForm />
        </div>
      </Modal>
    </div>
  );
};

export default AccountSecurityCenter;
