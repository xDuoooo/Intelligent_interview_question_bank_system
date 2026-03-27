import { Button, Form, Input, message, Divider, Space, Modal, Typography } from "antd";
import { 
  updateMyUserUsingPost, 
  sendVerificationCodeUsingPost,
  bindPhoneUsingPost,
  bindEmailUsingPost
} from "@/api/userController";
import React, { useState, useEffect } from "react";
import { useDispatch } from "react-redux";
import { AppDispatch } from "@/stores";
import { setLoginUser } from "@/stores/loginUser";
import { User, Mail, Phone, FileText, Share2, ShieldCheck, CheckCircle2, ChevronRight, Plus, ArrowRight } from "lucide-react";
import Image from "next/image";

const { Text } = Typography;

interface Props {
  user: API.LoginUserVO;
}

/**
 * 用户信息编辑表单（增强安全版）
 */
const UserInfoEditForm = (props: Props) => {
  const dispatch = useDispatch<AppDispatch>();
  const [loading, setLoading] = useState(false);
  const [form] = Form.useForm();
  const { user } = props;

  // 绑定弹窗控制
  const [bindVisible, setBindVisible] = useState(false);
  const [bindType, setBindType] = useState<"phone" | "email">("phone");
  const [bindTarget, setBindTarget] = useState("");
  const [bindCode, setBindCode] = useState("");
  const [countdown, setCountdown] = useState(0);
  const [bindLoading, setBindLoading] = useState(false);

  useEffect(() => {
    form.setFieldsValue(user);
  }, [user, form]);

  // 倒计时逻辑
  useEffect(() => {
    let timer: any;
    if (countdown > 0) {
      timer = setTimeout(() => setCountdown(countdown - 1), 1000);
    }
    return () => clearTimeout(timer);
  }, [countdown]);

  /**
   * 发送绑定验证码
   */
  const handleSendCode = async () => {
    if (!bindTarget) {
      message.warning(`请输入新的${bindType === "phone" ? "手机号" : "邮箱"}`);
      return;
    }
    try {
      await sendVerificationCodeUsingPost({
        target: bindTarget,
        type: bindType === "email" ? 1 : 2,
      });
      message.success("验证码已发送");
      setCountdown(60);
    } catch (error: any) {
      message.error("发送失败：" + error.message);
    }
  };

  /**
   * 执行绑定操作
   */
  const doBind = async () => {
    if (!bindTarget || !bindCode) {
      message.warning("请补全信息");
      return;
    }
    setBindLoading(true);
    try {
      if (bindType === "phone") {
        await bindPhoneUsingPost({ target: bindTarget, code: bindCode });
      } else {
        await bindEmailUsingPost({ target: bindTarget, code: bindCode });
      }
      message.success("绑定成功");
      setBindVisible(false);
      // 局部刷新全局状态
      const newUser = { ...user, [bindType]: bindTarget };
      dispatch(setLoginUser(newUser));
      form.setFieldValue(bindType, bindTarget);
    } catch (error: any) {
      message.error("绑定失败：" + error.message);
    } finally {
      setBindLoading(false);
    }
  };

  /**
   * 提交基础信息修改
   */
  const doSubmit = async (values: API.UserUpdateMyRequest) => {
    setLoading(true);
    const hide = message.loading("正在保存更改...");
    try {
      // 后端 updateMyUser 已屏蔽敏感字段，这里仅传递允许修改的字段
      await updateMyUserUsingPost({
        userName: values.userName,
        userAvatar: values.userAvatar,
        userProfile: values.userProfile,
      });
      message.success("资料更新成功");
      dispatch(setLoginUser({ ...user, ...values }));
    } catch (error: any) {
      message.error("更新失败：" + error.message);
    } finally {
      hide();
      setLoading(false);
    }
  };

  const openBindModal = (type: "phone" | "email") => {
    setBindType(type);
    setBindTarget("");
    setBindCode("");
    setCountdown(0);
    setBindVisible(true);
  };

  return (
    <div className="user-info-edit-container max-w-2xl px-2">
      <Form
        form={form}
        layout="vertical"
        onFinish={doSubmit}
        className="space-y-8"
      >
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          <Form.Item
            label={<span className="font-bold text-slate-700 flex items-center gap-2 text-sm"><User size={16} className="text-primary"/> 昵称</span>}
            name="userName"
            rules={[{ required: true, message: '请输入昵称' }]}
          >
            <Input 
              placeholder="您的公开显示名称" 
              className="h-12 rounded-2xl bg-slate-50 border-slate-100 hover:border-primary focus:border-primary transition-all shadow-sm"
            />
          </Form.Item>

          <Form.Item
            label={<span className="font-bold text-slate-700 flex items-center gap-2 text-sm"><FileText size={16} className="text-primary"/> 个人简介</span>}
            name="userProfile"
          >
            <Input 
              placeholder="介绍一下你自己..." 
              className="h-12 rounded-2xl bg-slate-50 border-slate-100 hover:border-primary focus:border-primary transition-all shadow-sm"
            />
          </Form.Item>
        </div>

        <Divider orientation="left">
          <span className="text-slate-400 text-[10px] font-black uppercase tracking-[0.2em] flex items-center gap-2">
            <ShieldCheck size={14}/> 账号安全绑定
          </span>
        </Divider>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* 手机号展示与修改 */}
          <div className="p-5 rounded-[2rem] bg-slate-50/50 border border-slate-100 flex items-center justify-between group hover:bg-white hover:shadow-xl hover:shadow-slate-200/50 transition-all duration-500">
            <div className="flex items-center gap-4">
              <div className="p-3 rounded-2xl bg-white shadow-sm text-blue-500 group-hover:scale-110 transition-transform">
                <Phone size={20} />
              </div>
              <div className="flex flex-col items-center flex-1">
                <div className="text-[10px] font-black text-slate-400 uppercase tracking-[0.1em] mb-0.5">手机号码</div>
                <div className={`text-sm font-bold ${user.phone ? 'text-slate-800' : 'text-slate-300'}`}>
                  {user.phone || "暂未绑定"}
                </div>
              </div>
            </div>
            <div 
              onClick={() => openBindModal("phone")}
              className={`group/btn h-9 px-4 rounded-xl flex items-center justify-center gap-2 cursor-pointer transition-all duration-200 ${
                user.phone 
                ? "bg-slate-100 text-slate-500 hover:bg-slate-200" 
                : "bg-blue-600 text-white hover:bg-blue-700 shadow-sm shadow-blue-200 font-bold"
              }`}
            >
              <span className="text-[11px] uppercase tracking-wide">
                {user.phone ? "更换" : "去绑定"}
              </span>
              <ChevronRight size={14} className={user.phone ? "opacity-30" : "opacity-90"} />
            </div>
          </div>

          {/* 邮箱展示与修改 */}
          <div className="p-5 rounded-[2rem] bg-slate-50/50 border border-slate-100 flex items-center justify-between group hover:bg-white hover:shadow-xl hover:shadow-slate-200/50 transition-all duration-500">
            <div className="flex items-center gap-4">
              <div className="p-3 rounded-2xl bg-white shadow-sm text-orange-500 group-hover:scale-110 transition-transform">
                <Mail size={20} />
              </div>
              <div className="flex flex-col items-center flex-1">
                <div className="text-[10px] font-black text-slate-400 uppercase tracking-[0.1em] mb-0.5">电子邮箱</div>
                <div className={`text-sm font-bold ${user.email ? 'text-slate-800' : 'text-slate-300'}`}>
                  {user.email ? (
                    <span className="truncate max-w-[120px] inline-block">{user.email}</span>
                  ) : (
                    "暂未绑定"
                  )}
                </div>
              </div>
            </div>
            <div 
              onClick={() => openBindModal("email")}
              className={`group/btn h-9 px-4 rounded-xl flex items-center justify-center gap-2 cursor-pointer transition-all duration-200 ${
                user.email 
                ? "bg-slate-100 text-slate-500 hover:bg-slate-200" 
                : "bg-orange-500 text-white hover:bg-orange-600 shadow-sm shadow-orange-200 font-bold"
              }`}
            >
              <span className="text-[11px] uppercase tracking-wide">
                {user.email ? "更换" : "去绑定"}
              </span>
              <ChevronRight size={14} className={user.email ? "opacity-30" : "opacity-90"} />
            </div>
          </div>
        </div>

        <Divider orientation="left">
          <span className="text-slate-400 text-[10px] font-black uppercase tracking-[0.2em] flex items-center gap-2">
            <Share2 size={14}/> 社交账号关联 (只读展示)
          </span>
        </Divider>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {[
            { key: "githubId", label: "GitHub", icon: "/assets/github-logo.png" },
            { key: "giteeId", label: "Gitee", icon: "/assets/gitee-logo.png" },
            { key: "googleId", label: "Google", icon: "/assets/google-logo.png" },
          ].map((item) => (
            <div key={item.key} className="p-4 rounded-2xl bg-slate-50 border border-slate-100 flex items-center gap-3 opacity-80 cursor-not-allowed">
              <Image src={item.icon} width={20} height={20} alt={item.label} />
              <div className="flex-1 min-w-0">
                <div className="text-[9px] font-bold text-slate-400 uppercase">{item.label}</div>
                <div className="text-xs font-semibold text-slate-500 truncate">{(user as any)[item.key] || "未关联"}</div>
              </div>
            </div>
          ))}
        </div>

        <div className="pt-8 flex justify-center">
          <Button 
            type="primary" 
            htmlType="submit" 
            loading={loading}
            className="h-12 px-12 rounded-xl bg-slate-900 hover:bg-black text-white border-none font-bold text-sm flex items-center gap-2 shadow-lg shadow-slate-200 transition-all hover:-translate-y-0.5"
          >
            {loading ? "更新中..." : "保存更改"}
            {!loading && <ArrowRight size={16} />}
          </Button>
        </div>
      </Form>

      {/* 绑定弹窗 */}
      <Modal
        title={null}
        open={bindVisible}
        onCancel={() => setBindVisible(false)}
        footer={null}
        centered
        width={400}
        bodyStyle={{ padding: '32px' }}
        className="bind-modal"
      >
        <div className="text-center mb-8">
          <div className={`mx-auto w-16 h-16 rounded-3xl flex items-center justify-center mb-4 shadow-lg ${bindType === 'phone' ? 'bg-blue-50 text-blue-600' : 'bg-orange-50 text-orange-600'}`}>
            {bindType === 'phone' ? <Phone size={32} /> : <Mail size={32} />}
          </div>
          <h2 className="text-xl font-black text-slate-800">安全修改 {bindType === 'phone' ? '手机号' : '邮箱'}</h2>
          <p className="text-xs text-slate-400 font-bold mt-2 uppercase tracking-widest">需要验证您的所有权</p>
        </div>

        <div className="space-y-4">
          <div>
            <label className="text-xs font-black text-slate-500 mb-1.5 block uppercase ml-1">新的 {bindType === 'phone' ? '号码' : '地址'}</label>
            <Input 
              prefix={bindType === 'phone' ? <Phone size={16} /> : <Mail size={16} />}
              placeholder={`请输入新的${bindType === 'phone' ? '手机号' : '邮箱'}`}
              value={bindTarget}
              onChange={(e) => setBindTarget(e.target.value)}
              className="h-12 rounded-2xl bg-slate-50 border-transparent focus:bg-white focus:border-primary transition-all font-bold"
            />
          </div>

          <div>
            <label className="text-xs font-black text-slate-500 mb-1.5 block uppercase ml-1">六位验证码</label>
            <div className="flex gap-2">
              <Input 
                prefix={<ShieldCheck size={16} />}
                placeholder="数字代码"
                value={bindCode}
                onChange={(e) => setBindCode(e.target.value)}
                className="h-12 rounded-2xl bg-slate-50 border-transparent focus:bg-white focus:border-primary transition-all font-bold flex-1"
              />
              <Button 
                onClick={handleSendCode}
                disabled={countdown > 0}
                className="h-12 px-4 rounded-xl font-bold bg-slate-800 text-white border-none hover:brightness-110 disabled:grayscale disabled:opacity-50"
              >
                {countdown > 0 ? `${countdown}s` : "获取"}
              </Button>
            </div>
          </div>

          <Button 
            type="primary" 
            block
            loading={bindLoading}
            onClick={doBind}
            className="h-14 rounded-2xl bg-primary text-white font-black text-base shadow-lg shadow-primary/20 mt-6"
          >
            确认绑定
          </Button>
        </div>
      </Modal>
    </div>
  );
};

export default UserInfoEditForm;