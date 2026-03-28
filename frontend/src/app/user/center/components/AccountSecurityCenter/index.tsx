"use client";
import React, { useEffect, useMemo, useState } from "react";
import { Button, Input, List, Modal, Tag, message, Typography } from "antd";
import { 
  ShieldCheck, 
  Key, 
  Phone, 
  Mail, 
  Unlink,
  RefreshCw
} from "lucide-react";
import Image from "next/image";
import { 
  bindEmailUsingPost,
  bindPhoneUsingPost,
  getLoginUserUsingGet,
  sendVerificationCodeUsingPost,
  unbindEmailUsingPost,
  unbindGithubUsingPost, 
  unbindGiteeUsingPost, 
  unbindGoogleUsingPost,
  unbindPhoneUsingPost,
  updateMyUserUsingPost,
} from "@/api/userController";
import { useDispatch } from "react-redux";
import { AppDispatch } from "@/stores";
import { setLoginUser } from "@/stores/loginUser";
import { getSocialAuthProviderLabel, getSocialAuthUrl } from "@/config/auth";
import PasswordChangeForm from "../PasswordChangeForm";
import request from "@/libs/request";

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
  const [bindModalVisible, setBindModalVisible] = useState(false);
  const [accountModalVisible, setAccountModalVisible] = useState(false);
  const [bindType, setBindType] = useState<"phone" | "email" | null>(null);
  const [bindTarget, setBindTarget] = useState("");
  const [bindCode, setBindCode] = useState("");
  const [accountValue, setAccountValue] = useState("");
  const [captchaInput, setCaptchaInput] = useState("");
  const [captchaData, setCaptchaData] = useState<{ image: string; uuid: string } | null>(null);
  const [sendCodeLoading, setSendCodeLoading] = useState(false);
  const [bindLoading, setBindLoading] = useState(false);
  const [accountLoading, setAccountLoading] = useState(false);
  const [unbindLoadingType, setUnbindLoadingType] = useState<string | null>(null);
  const [countdown, setCountdown] = useState(0);
  const hasPasswordConfigured = Number(user.passwordConfigured || 0) === 1;

  // 脱敏显示
  const maskPhone = (phone?: string) => phone ? phone.replace(/(\d{3})\d{4}(\d{4})/, "$1****$2") : "未绑定";
  const maskEmail = (email?: string) => email ? email.replace(/(.{2}).+(.{2}@.+)/, "$1****$2") : "未绑定";

  const bindConfig = useMemo(() => {
    if (bindType === "phone") {
      return {
        label: "手机号",
        type: 2,
        placeholder: "请输入 11 位手机号",
        actionText: user.phone ? "更换手机号" : "绑定手机号",
        pattern: /^1[3-9]\d{9}$/,
        invalidMessage: "请输入有效的 11 位手机号",
      };
    }
    return {
      label: "邮箱",
      type: 1,
      placeholder: "请输入常用邮箱",
      actionText: user.email ? "更换邮箱" : "绑定邮箱",
      pattern: /^[\w.+-]+@[\w-]+\.[\w.]+$/,
      invalidMessage: "请输入有效的邮箱地址",
    };
  }, [bindType, user.email, user.phone]);

  const refreshLoginUser = async () => {
    const res = await getLoginUserUsingGet();
    if (res.data) {
      const latestUser = res.data as API.LoginUserVO;
      dispatch(setLoginUser(latestUser));
      return latestUser;
    }
    return undefined;
  };

  const refreshCaptcha = async () => {
    try {
      const res: any = await request.get("/api/captcha/get");
      if (res.code === 0) {
        setCaptchaData(res.data);
      }
    } catch (error) {
      console.error("获取图形验证码失败", error);
      message.error("获取图形验证码失败，请稍后再试");
    }
  };

  useEffect(() => {
    if (!bindModalVisible) {
      return;
    }
    void refreshCaptcha();
    setBindCode("");
    setCaptchaInput("");
  }, [bindModalVisible, bindType]);

  useEffect(() => {
    if (countdown <= 0) {
      return;
    }
    const timer = window.setTimeout(() => {
      setCountdown((current) => current - 1);
    }, 1000);
    return () => window.clearTimeout(timer);
  }, [countdown]);

  const openBindModal = (type: "phone" | "email") => {
    setBindType(type);
    setBindTarget(type === "phone" ? user.phone || "" : user.email || "");
    setBindCode("");
    setCaptchaInput("");
    setCountdown(0);
    setBindModalVisible(true);
  };

  const openAccountModal = () => {
    setAccountValue(user.userAccount || "");
    setAccountModalVisible(true);
  };

  const handleSendCode = async () => {
    const target = bindTarget.trim();
    if (!bindType) {
      return;
    }
    if (!target) {
      message.warning(`请先输入${bindConfig.label}`);
      return;
    }
    if (!bindConfig.pattern.test(target)) {
      message.warning(bindConfig.invalidMessage);
      return;
    }
    if (!captchaInput.trim()) {
      message.warning("请先输入图形验证码");
      return;
    }
    setSendCodeLoading(true);
    try {
      await sendVerificationCodeUsingPost({
        target,
        type: bindConfig.type,
        captcha: captchaInput.trim(),
        captchaUuid: captchaData?.uuid,
      });
      message.success("验证码已发送，请注意查收");
      setCountdown(60);
      setCaptchaInput("");
      await refreshCaptcha();
    } catch (error: any) {
      message.error(error?.message || "发送验证码失败");
      setCaptchaInput("");
      await refreshCaptcha();
    } finally {
      setSendCodeLoading(false);
    }
  };

  const handleBindSubmit = async () => {
    const target = bindTarget.trim();
    const code = bindCode.trim();
    if (!bindType) {
      return;
    }
    if (!target) {
      message.warning(`请先输入${bindConfig.label}`);
      return;
    }
    if (!bindConfig.pattern.test(target)) {
      message.warning(bindConfig.invalidMessage);
      return;
    }
    if (!code) {
      message.warning("请输入验证码");
      return;
    }
    setBindLoading(true);
    try {
      if (bindType === "phone") {
        await bindPhoneUsingPost({ target, code });
      } else {
        await bindEmailUsingPost({ target, code });
      }
      await refreshLoginUser();
      message.success(`${bindConfig.actionText}成功`);
      setBindModalVisible(false);
      setBindCode("");
      setCaptchaInput("");
    } catch (error: any) {
      message.error(error?.message || `${bindConfig.actionText}失败`);
    } finally {
      setBindLoading(false);
    }
  };

  const handleUnbindContact = async (type: "phone" | "email") => {
    const label = type === "phone" ? "手机号" : "邮箱";
    Modal.confirm({
      title: `确认解绑${label}`,
      content: `解绑后你将不能再通过${label}验证码登录，请确认当前账号还保留至少一种其他登录方式。`,
      okButtonProps: { danger: true, loading: unbindLoadingType === type },
      onOk: async () => {
        setUnbindLoadingType(type);
        try {
          if (type === "phone") {
            await unbindPhoneUsingPost();
          } else {
            await unbindEmailUsingPost();
          }
          await refreshLoginUser();
          message.success(`${label}解绑成功`);
        } catch (error: any) {
          message.error(error?.message || `${label}解绑失败`);
        } finally {
          setUnbindLoadingType(null);
        }
      },
    });
  };

  const handleUpdateAccount = async () => {
    const nextAccount = accountValue.trim();
    if (!nextAccount) {
      message.warning("请输入登录账号");
      return;
    }
    if (nextAccount.length < 4 || nextAccount.length > 20) {
      message.warning("账号长度需在 4 到 20 个字符之间");
      return;
    }
    if (!/^[A-Za-z0-9_]+$/.test(nextAccount)) {
      message.warning("账号仅支持字母、数字和下划线");
      return;
    }
    setAccountLoading(true);
    try {
      await updateMyUserUsingPost({ userAccount: nextAccount });
      await refreshLoginUser();
      setAccountModalVisible(false);
      message.success("登录账号修改成功");
    } catch (error: any) {
      message.error(error?.message || "登录账号修改失败");
    } finally {
      setAccountLoading(false);
    }
  };

  // 解绑逻辑
  const handleUnbind = async (type: "github" | "gitee" | "google") => {
    const providerLabel = getSocialAuthProviderLabel(type);
    Modal.confirm({
      title: "确认解绑",
      content: `确定要解绑您的 ${providerLabel} 账号吗？解绑后将无法通过该方式登录。`,
      okButtonProps: { danger: true, loading: unbindLoadingType === type },
      onOk: async () => {
        setUnbindLoadingType(type);
        try {
          if (type === "github") await unbindGithubUsingPost();
          if (type === "gitee") await unbindGiteeUsingPost();
          if (type === "google") await unbindGoogleUsingPost();
          const latestUser = await refreshLoginUser();
          const onlyPasswordLoginLeft =
            Number(latestUser?.passwordConfigured || 0) === 1 &&
            !latestUser?.phone &&
            !latestUser?.email &&
            !latestUser?.githubId &&
            !latestUser?.giteeId &&
            !latestUser?.googleId;
          if (onlyPasswordLoginLeft && latestUser?.userAccount) {
            message.success(`解绑成功，后续请使用账号 ${latestUser.userAccount} 和密码登录`);
          } else {
            message.success("解绑成功");
          }
        } catch (error: any) {
          message.error("解绑失败：" + (error?.message || "请稍后重试"));
        } finally {
          setUnbindLoadingType(null);
        }
      },
    });
  };

  const securityItems = [
    {
      key: "account",
      title: "登录账号",
      description: user.userAccount
        ? `当前登录账号：${user.userAccount}`
        : "当前账号暂无可展示的登录账号",
      status: user.userAccount
        ? <Tag color={hasPasswordConfigured ? "processing" : "default"}>{hasPasswordConfigured ? "可用于密码登录" : "已生成账号"}</Tag>
        : <Tag>未生成</Tag>,
      icon: <Key size={20} className="text-slate-500" />,
      action: user.userAccount ? (
        <div className="flex items-center gap-2">
          <Button type="link" onClick={openAccountModal}>修改账号</Button>
          <Text
            copyable={{ text: user.userAccount, tooltips: ["复制账号", "已复制"] }}
            className="text-sm text-primary"
          >
            复制账号
          </Text>
        </div>
      ) : (
        <Button type="link" onClick={openAccountModal}>设置账号</Button>
      ),
    },
    {
      key: "password",
      title: "登录密码",
      description: hasPasswordConfigured
        ? "定期更换密码可以提高账号安全性"
        : "当前账号尚未设置独立密码，建议先设置密码再解绑第三方账号",
      status: hasPasswordConfigured ? <Tag color="success">已设置</Tag> : <Tag color="warning">未设置</Tag>,
      icon: <Key size={20} className="text-blue-500" />,
      action: <Button type="link" onClick={() => setPasswordModalVisible(true)}>{hasPasswordConfigured ? "修改密码" : "设置密码"}</Button>
    },
    {
      key: "phone",
      title: "手机绑定",
      description: `当前绑定：${maskPhone(user.phone)}`,
      status: user.phone ? <Tag color="success">已绑定</Tag> : <Tag>未绑定</Tag>,
      icon: <Phone size={20} className="text-green-500" />,
      action: user.phone ? (
        <div className="flex items-center gap-2">
          <Button type="link" onClick={() => openBindModal("phone")}>更换手机</Button>
          <Button type="text" danger icon={<Unlink size={16} />} onClick={() => handleUnbindContact("phone")} />
        </div>
      ) : (
        <Button type="link" onClick={() => openBindModal("phone")}>立即绑定</Button>
      )
    },
    {
      key: "email",
      title: "邮箱绑定",
      description: `当前绑定：${maskEmail(user.email)}`,
      status: user.email ? <Tag color="success">已绑定</Tag> : <Tag>未绑定</Tag>,
      icon: <Mail size={20} className="text-orange-500" />,
      action: user.email ? (
        <div className="flex items-center gap-2">
          <Button type="link" onClick={() => openBindModal("email")}>更换邮箱</Button>
          <Button type="text" danger icon={<Unlink size={16} />} onClick={() => handleUnbindContact("email")} />
        </div>
      ) : (
        <Button type="link" onClick={() => openBindModal("email")}>立即绑定</Button>
      )
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
        <Button type="link" href={getSocialAuthUrl("github", "bind")}>立即关联</Button>
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
        <Button type="link" href={getSocialAuthUrl("gitee", "bind")}>立即关联</Button>
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
        <Button type="link" href={getSocialAuthUrl("google", "bind")}>立即关联</Button>
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
        title="修改登录账号"
        open={accountModalVisible}
        onCancel={() => setAccountModalVisible(false)}
        onOk={handleUpdateAccount}
        okText="保存账号"
        cancelText="取消"
        confirmLoading={accountLoading}
        destroyOnClose
        centered
      >
        <div className="space-y-4 pt-4">
          <div className="rounded-2xl bg-slate-50 px-4 py-3 text-sm text-slate-500">
            登录账号用于账号密码登录。建议改成你容易记住的名称，支持字母、数字和下划线。
          </div>
          <Input
            value={accountValue}
            placeholder="例如：xduo_java"
            maxLength={20}
            onChange={(e) => setAccountValue(e.target.value)}
          />
        </div>
      </Modal>

      <Modal
        title={hasPasswordConfigured ? "修改登录密码" : "设置登录密码"}
        open={passwordModalVisible}
        onCancel={() => setPasswordModalVisible(false)}
        footer={null}
        destroyOnClose
        centered
      >
        <div className="pt-4">
          <PasswordChangeForm
            passwordConfigured={hasPasswordConfigured}
            onSuccess={() => setPasswordModalVisible(false)}
          />
        </div>
      </Modal>

      <Modal
        title={bindConfig.actionText}
        open={bindModalVisible}
        onCancel={() => setBindModalVisible(false)}
        onOk={handleBindSubmit}
        okText={bindConfig.actionText}
        cancelText="取消"
        confirmLoading={bindLoading}
        destroyOnClose
        centered
      >
        <div className="space-y-4 pt-4">
          <div className="space-y-2">
            <Text strong>{bindConfig.label}</Text>
            <Input
              value={bindTarget}
              placeholder={bindConfig.placeholder}
              onChange={(e) => setBindTarget(e.target.value)}
            />
          </div>

          <div className="space-y-2">
            <Text strong>图形验证码</Text>
            <div className="flex items-center gap-3">
              <Input
                value={captchaInput}
                placeholder="请输入图形验证码"
                onChange={(e) => setCaptchaInput(e.target.value)}
              />
              {captchaData ? (
                <Image
                  src={captchaData.image}
                  alt="captcha"
                  width={130}
                  height={48}
                  className="h-12 w-[130px] cursor-pointer rounded-lg border border-slate-200 bg-white object-cover"
                  onClick={() => void refreshCaptcha()}
                />
              ) : (
                <Button icon={<RefreshCw size={14} />} onClick={() => void refreshCaptcha()}>
                  刷新
                </Button>
              )}
            </div>
          </div>

          <div className="space-y-2">
            <Text strong>短信 / 邮件验证码</Text>
            <div className="flex items-center gap-3">
              <Input
                value={bindCode}
                placeholder="请输入 6 位验证码"
                onChange={(e) => setBindCode(e.target.value)}
              />
              <Button loading={sendCodeLoading} disabled={countdown > 0} onClick={handleSendCode}>
                {countdown > 0 ? `${countdown}s 后重试` : "发送验证码"}
              </Button>
            </div>
          </div>

          <Text type="secondary" className="text-xs">
            绑定后可作为登录方式与安全找回方式使用。
          </Text>
        </div>
      </Modal>
    </div>
  );
};

export default AccountSecurityCenter;
