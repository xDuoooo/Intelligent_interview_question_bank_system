"use client";
import React, { useState } from "react";
import { 
  Card, 
  Form, 
  Input, 
  Switch, 
  Button, 
  Typography, 
  Space, 
  Divider, 
  message,
  Tabs,
  Badge,
  Alert
} from "antd";
import { 
  Settings as SettingsIcon, 
  Globe, 
  ShieldCheck, 
  Bell, 
  Save,
  Monitor,
  Lock,
  Zap
} from "lucide-react";

const { Title, Text, Paragraph } = Typography;

/**
 * 全局系统设置页面
 */
export default function AdminSettingsPage() {
  const [loading, setLoading] = useState(false);

  const onFinish = (values: any) => {
    setLoading(true);
    // 模拟保存
    setTimeout(() => {
      setLoading(false);
      message.success("设置已更新并实时生效");
    }, 1000);
  };

  const basicSettings = (
    <div className="space-y-6 animate-in slide-in-from-left-4 duration-500">
      <Form.Item label="站点名称" name="siteName" initialValue="Intelliface 智面">
        <Input size="large" className="rounded-xl bg-slate-50 border-slate-100" />
      </Form.Item>
      <Form.Item label="SEO 关键词" name="seoKeywords" initialValue="面试, 刷题, Java, 互联网">
        <Input size="large" className="rounded-xl bg-slate-50 border-slate-100" />
      </Form.Item>
      <Form.Item label="系统公告" name="announcement" initialValue="欢迎来到智面 1.0 版本，体验 AI 智能面经！">
        <Input.TextArea rows={4} className="rounded-xl bg-slate-50 border-slate-100" />
      </Form.Item>
    </div>
  );

  const securitySettings = (
    <div className="space-y-8 animate-in slide-in-from-left-4 duration-500 pt-2">
      <div className="flex items-center justify-between">
         <div>
            <Text className="font-bold text-slate-800 block text-lg">开放注册</Text>
            <Text type="secondary">允许新用户自行注册账号</Text>
         </div>
         <Form.Item name="allowRegister" valuePropName="checked" initialValue={true} noStyle>
            <Switch className="bg-slate-200" />
         </Form.Item>
      </div>
      <Divider className="my-0 border-slate-100" />
      <div className="flex items-center justify-between">
         <div>
            <Text className="font-bold text-slate-800 block text-lg">强制图形验证码</Text>
            <Text type="secondary">登录与注册时必须输入验证码（防刷）</Text>
         </div>
         <Form.Item name="requireCaptcha" valuePropName="checked" initialValue={true} noStyle>
            <Switch className="bg-slate-200" />
         </Form.Item>
      </div>
      <Divider className="my-0 border-slate-100" />
      <div className="flex items-center justify-between">
         <div className="flex items-center gap-3">
            <div className="bg-amber-100 p-2 rounded-xl"><ShieldCheck className="h-5 w-5 text-amber-600" /></div>
            <div>
               <Text className="font-bold text-slate-800 block text-lg">系统维护模式</Text>
               <Text type="secondary">开启后仅管理员可登录，普通用户将看到维护提示</Text>
            </div>
         </div>
         <Form.Item name="maintenanceMode" valuePropName="checked" initialValue={false} noStyle>
            <Switch className="bg-slate-200" />
         </Form.Item>
      </div>
    </div>
  );

  return (
    <div className="max-w-5xl mx-auto space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-700">
      {/* Hero Header */}
      <section className="bg-white/70 backdrop-blur-xl rounded-[3rem] p-10 sm:p-14 border border-white shadow-2xl shadow-slate-200/50 relative overflow-hidden">
        <div className="absolute top-0 right-0 p-12 opacity-5 rotate-12">
           <Zap className="h-40 w-40 text-slate-900" />
        </div>
        <div className="relative z-10 space-y-4">
           <div className="inline-flex items-center gap-2 bg-primary/10 text-primary px-4 py-2 rounded-full font-black text-xs uppercase tracking-widest">
              <SettingsIcon className="h-3 w-3" />
              Dynamic Control Center
           </div>
           <h1 className="text-3xl sm:text-4xl font-black text-slate-900 tracking-tight">全局动态设置</h1>
           <p className="text-slate-500 font-medium max-w-xl text-lg">
             无需重启服务器即可实时修改平台核心参数，灵活、高效应对各种业务场景。
           </p>
        </div>
      </section>

      <Form layout="vertical" onFinish={onFinish}>
        <Card className="rounded-[3rem] border-slate-100 shadow-xl shadow-slate-200/50 overflow-hidden">
          <Tabs 
            className="admin-settings-tabs"
            defaultActiveKey="1"
            tabBarExtraContent={
              <Button 
                type="primary" 
                htmlType="submit" 
                loading={loading}
                className="h-12 px-8 rounded-xl font-black bg-primary border-none shadow-lg shadow-primary/25 flex items-center gap-2"
              >
                <Save className="h-4 w-4" /> 保存所有更改
              </Button>
            }
            items={[
              {
                key: '1',
                label: <span className="flex items-center gap-2 px-4 py-2 font-bold"><Monitor className="h-4 w-4" /> 基础配置</span>,
                children: <div className="p-8 pb-12">{basicSettings}</div>,
              },
              {
                key: '2',
                label: <span className="flex items-center gap-2 px-4 py-2 font-bold"><Lock className="h-4 w-4" /> 安全策略</span>,
                children: <div className="p-8 pb-12">{securitySettings}</div>,
              },
              {
                key: '3',
                label: <span className="flex items-center gap-2 px-4 py-2 font-bold"><Bell className="h-4 w-4" /> 消息推送</span>,
                children: <div className="p-8 pb-12"><Alert message="通知推送系统正在联调中，暂不开放配置。" type="info" showIcon className="rounded-xl py-6" /></div>,
              },
            ]}
          />
        </Card>
      </Form>
    </div>
  );
}
