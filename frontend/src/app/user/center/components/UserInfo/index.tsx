import React from "react";
import { List, Space, Tooltip, Divider } from "antd";
import { 
  Phone, 
  Mail, 
  Globe, 
  CheckCircle2, 
  XCircle, 
  User as UserIcon,
  MessageCircle
} from "lucide-react";
import Image from "next/image";
import "./index.css";

interface Props {
  user: API.LoginUserVO;
}

/**
 * 用户个人资料组件
 */
const UserInfo = (props: Props) => {
  const { user } = props;

  // 社交账号绑定状态配置
  const socialAccounts = [
    {
      key: "github",
      name: "GitHub",
      icon: <Image src="/assets/github-logo.png" width={20} height={20} alt="GitHub" />,
      isBound: !!user.githubId,
      label: user.githubId || "未绑定",
    },
    {
      key: "gitee",
      name: "Gitee",
      icon: <Image src="/assets/gitee-logo.png" width={20} height={20} alt="Gitee" />,
      isBound: !!user.giteeId,
      label: user.giteeId || "未绑定",
    },
    {
      key: "google",
      name: "Google",
      icon: <Image src="/assets/google-logo.png" width={20} height={20} alt="Google" />,
      isBound: !!user.googleId,
      label: user.googleId || "未绑定",
    },
  ];

  return (
    <div className="user-info-container">
      <div className="info-section mb-8">
        <h3 className="flex items-center gap-2 text-lg font-bold mb-4">
          <UserIcon size={20} className="text-primary" />
          基础信息
        </h3>
        <List
          grid={{ gutter: 16, xs: 1, sm: 2 }}
          dataSource={[
            {
              label: "绑定手机",
              value: user.phone || "暂未绑定",
              icon: <Phone size={18} className="text-blue-500" />,
              isSet: !!user.phone
            },
            {
              label: "绑定邮箱",
              value: user.email || "暂未绑定",
              icon: <Mail size={18} className="text-orange-500" />,
              isSet: !!user.email
            }
          ]}
          renderItem={(item) => (
            <List.Item>
              <div className="flex items-center p-4 rounded-2xl bg-slate-50/50 border border-slate-100/50 hover:bg-white hover:shadow-sm transition-all group">
                <div className="mr-4 p-2.5 rounded-xl bg-white shadow-sm group-hover:scale-110 transition-transform">
                  {item.icon}
                </div>
                <div className="flex flex-col items-center flex-1">
                  <div className="text-[10px] font-black text-slate-400 uppercase tracking-[0.1em] mb-0.5">{item.label}</div>
                  <div className={`text-sm font-bold ${item.isSet ? 'text-slate-800' : 'text-slate-300'}`}>
                    {item.value}
                  </div>
                </div>
              </div>
            </List.Item>
          )}
        />
      </div>

      <Divider className="my-8" />

      <div className="social-section">
        <h3 className="flex items-center gap-2 text-lg font-bold mb-6">
          <Globe size={20} className="text-primary" />
          社交账号绑定
        </h3>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          {socialAccounts.map((account) => (
            <div 
              key={account.key}
              className="relative flex flex-col items-center p-6 rounded-3xl border-2 border-slate-50 hover:border-primary/20 hover:bg-primary/[0.02] transition-all group overflow-hidden"
            >
              {/* Background Decoration */}
              <div className={`absolute -right-4 -bottom-4 opacity-5 group-hover:opacity-10 transition-opacity transform group-hover:scale-110`}>
                {account.icon}
              </div>

              <div className="mb-4 p-3 rounded-2xl bg-white shadow-md ring-4 ring-slate-50/50 group-hover:rotate-6 transition-transform">
                {account.icon}
              </div>
              
              <div className="text-sm font-black text-slate-700 mb-1">{account.name}</div>
              
              <div className="flex items-center gap-1.5">
                {account.isBound ? (
                  <>
                    <CheckCircle2 size={14} className="text-green-500" />
                    <span className="text-xs font-bold text-green-600">已绑定</span>
                  </>
                ) : (
                  <>
                    <XCircle size={14} className="text-slate-300" />
                    <span className="text-xs font-bold text-slate-400">未启用</span>
                  </>
                )}
              </div>

              {account.isBound && (
                <div className="mt-3 px-3 py-1 rounded-full bg-slate-50 border border-slate-100/50 text-[9px] font-mono text-slate-400 max-w-full truncate">
                  UID: {account.label}
                </div>
              )}
            </div>
          ))}
        </div>
      </div>

      <div className="mt-10 p-6 rounded-3xl bg-blue-50/50 border border-blue-100 flex items-start gap-4">
        <div className="p-2 bg-blue-100 rounded-xl text-blue-600">
          <MessageCircle size={20} />
        </div>
        <div>
          <h4 className="text-sm font-bold text-blue-900 mb-1">绑定提示</h4>
          <p className="text-xs text-blue-700/80 leading-relaxed">
            绑定社交账号后，您可以直接通过对应的第三方平台一键登录系统。目前社交登录功能正在内测中，如有疑问请联系管理员。
          </p>
        </div>
      </div>
    </div>
  );
};

export default UserInfo;