"use client";

import React, { useState } from "react";
import Image from "next/image";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useDispatch } from "react-redux";
import { AppDispatch } from "@/stores";
import { setLoginUser } from "@/stores/loginUser";
import { userLoginUsingPost } from "@/api/userController";
import { message } from "antd";
import { cn } from "@/lib/utils";
import { User, Lock, ArrowRight, Loader2, Globe, Mail, MessageSquare } from "lucide-react";
import WxLoginModal from "@/components/WxLoginModal";

export default function UserLoginPage() {
  const [formData, setFormData] = useState<API.UserLoginRequest>({
    userAccount: "",
    userPassword: "",
  });
  const [loginLoading, setLoginLoading] = useState(false);
  const [wxModalVisible, setWxModalVisible] = useState(false);
  const dispatch = useDispatch<AppDispatch>();
  const router = useRouter();

  const doSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.userAccount || !formData.userPassword) {
      message.warning("请填写账号和密码");
      return;
    }
    setLoginLoading(true);
    try {
      const res = await userLoginUsingPost(formData);
      if (res.data) {
        message.success("欢迎回来！");
        dispatch(setLoginUser(res.data as API.LoginUserVO));
        router.replace("/");
      }
    } catch (e) {
      message.error("登录失败，请检查账号密码");
    } finally {
      setLoginLoading(false);
    }
  };

  return (
    <div className="flex min-h-[calc(100vh-64px-160px)] items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="relative w-full max-w-md">
        {/* Background Decorative Elements */}
        <div className="absolute -top-12 -left-12 h-64 w-64 rounded-full bg-primary/10 blur-3xl animate-pulse" />
        <div className="absolute -bottom-12 -right-12 h-64 w-64 rounded-full bg-blue-500/10 blur-3xl animate-pulse delay-700" />

        {/* Login Card */}
        <div className="relative overflow-hidden rounded-[2.5rem] border bg-white/60 backdrop-blur-2xl shadow-2xl p-8 sm:p-10 transition-all hover:shadow-primary/5">
          {/* Header */}
          <div className="flex flex-col items-center text-center space-y-4 mb-10">
            <div className="relative h-24 w-24 rounded-3xl overflow-hidden shadow-xl ring-4 ring-slate-50 group transform hover:scale-105 transition-transform duration-500">
               <div className="absolute inset-0 bg-white flex items-center justify-center p-4">
                 <Image
                  src="/assets/logo.png"
                  height={64}
                  width={64}
                  alt="Logo"
                  className="object-contain group-hover:scale-110 transition-transform duration-500"
                />
              </div>
            </div>
            <div className="space-y-1">
              <h1 className="text-3xl font-black tracking-tight text-foreground">欢迎回来</h1>
              <p className="text-sm font-medium text-muted-foreground">
                使用您的账号登录智面刷题平台
              </p>
            </div>
          </div>

          <form onSubmit={doSubmit} className="space-y-6">
            <div className="space-y-4">
              <div className="space-y-2">
                <label className="text-sm font-bold text-foreground/80 ml-1">账号</label>
                <div className="relative group">
                  <User className="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-muted-foreground group-focus-within:text-primary transition-colors" />
                  <input
                    type="text"
                    required
                    placeholder="请输入用户账号"
                    className="w-full h-14 pl-12 pr-4 rounded-2xl bg-slate-100/50 border-transparent border-2 focus:border-primary focus:bg-white outline-none transition-all font-medium"
                    value={formData.userAccount}
                    onChange={(e) => setFormData({ ...formData, userAccount: e.target.value })}
                  />
                </div>
              </div>

              <div className="space-y-2">
                <div className="flex justify-between items-center ml-1">
                  <label className="text-sm font-bold text-foreground/80">密码</label>
                  <Link href="#" className="text-xs font-bold text-primary hover:underline">忘记密码?</Link>
                </div>
                <div className="relative group">
                  <Lock className="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-muted-foreground group-focus-within:text-primary transition-colors" />
                  <input
                    type="password"
                    required
                    placeholder="请输入密码"
                    className="w-full h-14 pl-12 pr-4 rounded-2xl bg-slate-100/50 border-transparent border-2 focus:border-primary focus:bg-white outline-none transition-all font-medium"
                    value={formData.userPassword}
                    onChange={(e) => setFormData({ ...formData, userPassword: e.target.value })}
                  />
                </div>
                <p className="text-[10px] text-slate-400 font-bold ml-1 flex items-center gap-1">
                  <span className="h-1.5 w-1.5 rounded-full bg-primary animate-pulse" />
                  新用户输入账号密码后将自动为您注册
                </p>
              </div>
            </div>

            <button
              disabled={loginLoading}
              className="group relative w-full h-14 rounded-2xl bg-primary text-primary-foreground font-black text-lg shadow-lg shadow-primary/30 hover:shadow-primary/40 active:scale-95 transition-all disabled:opacity-70 flex items-center justify-center gap-2"
            >
              {loginLoading ? (
                <Loader2 className="h-6 w-6 animate-spin" />
              ) : (
                <>
                  立即进入
                  <ArrowRight className="h-5 w-5 group-hover:translate-x-1 transition-transform" />
                </>
              )}
            </button>
          </form>

          {/* Divider */}
          <div className="relative my-10">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-slate-200"></div>
            </div>
            <div className="relative flex justify-center text-xs uppercase tracking-widest font-black">
              <span className="bg-transparent px-4 text-muted-foreground">合作平台登录</span>
            </div>
          </div>

          {/* Social Login */}
          <div className="flex flex-col gap-3">
             <div className="grid grid-cols-2 gap-3">
              <button 
                type="button"
                className="flex h-12 items-center justify-center rounded-2xl border bg-white hover:bg-slate-50 transition-all gap-2 font-bold shadow-sm"
              >
                <Globe className="h-5 w-5 text-slate-700" /> GitHub
              </button>
              <button 
                type="button"
                className="flex h-12 items-center justify-center rounded-2xl border bg-white hover:bg-slate-50 transition-all gap-2 font-bold shadow-sm"
              >
                <MessageSquare className="h-5 w-5 text-red-500" /> Gitee
              </button>
             </div>
            <button 
              type="button"
              className="flex h-12 items-center justify-center rounded-2xl border bg-white hover:bg-slate-50 transition-all gap-2 font-bold shadow-sm w-full"
            >
              <Mail className="h-5 w-5 text-blue-500" /> Google 账号登录
            </button>
          </div>

          <div className="mt-8 text-center">
            <p className="text-sm font-bold text-muted-foreground">
              还没有账号?{" "}
              <Link href="/user/register" className="text-primary hover:underline underline-offset-4">
                立即注册
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

