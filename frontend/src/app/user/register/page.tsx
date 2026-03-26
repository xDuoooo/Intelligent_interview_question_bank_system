"use client";

import React, { useState } from "react";
import Image from "next/image";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { userRegisterUsingPost } from "@/api/userController";
import { message } from "antd";
import { cn } from "@/lib/utils";
import { User, Lock, ArrowRight, Loader2, ShieldCheck, Globe, Mail } from "lucide-react";

export default function UserRegisterPage() {
  const [formData, setFormData] = useState<API.UserRegisterRequest>({
    userAccount: "",
    userPassword: "",
    checkPassword: "",
  });
  const [loading, setLoading] = useState(false);
  const router = useRouter();

  const doSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const { userAccount, userPassword, checkPassword } = formData;
    if (!userAccount || !userPassword || !checkPassword) {
      message.warning("请填写所有字段");
      return;
    }
    if (userPassword !== checkPassword) {
      message.error("两次输入的密码不一致");
      return;
    }

    setLoading(true);
    try {
      const res = await userRegisterUsingPost(formData);
      if (res.data) {
        message.success("注册成功，请重新登录");
        router.replace("/user/login");
      }
    } catch (e) {
      message.error("注册失败，请检查账号格式");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-[calc(100vh-64px-160px)] items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="relative w-full max-w-md">
        {/* Background Decorative Elements */}
        <div className="absolute -top-12 -left-12 h-64 w-64 rounded-full bg-primary/10 blur-3xl animate-pulse" />
        <div className="absolute -bottom-12 -right-12 h-64 w-64 rounded-full bg-blue-500/10 blur-3xl animate-pulse delay-700" />

        {/* Register Card */}
        <div className="relative overflow-hidden rounded-[2.5rem] border bg-white/60 backdrop-blur-2xl shadow-2xl p-8 sm:p-10 transition-all hover:shadow-primary/5">
          {/* Header */}
          <div className="flex flex-col items-center text-center space-y-4 mb-10">
            <div className="relative h-20 w-20 rounded-3xl bg-primary/10 flex items-center justify-center p-4 shadow-inner group">
               <Image
                src="/assets/logo.png"
                height={64}
                width={64}
                alt="Logo"
                className="object-contain group-hover:scale-110 transition-transform duration-500"
              />
            </div>
            <div className="space-y-1">
              <h1 className="text-3xl font-black tracking-tight text-foreground">加入智面</h1>
              <p className="text-sm font-medium text-muted-foreground">
                开启您的智能面试刷题之旅
              </p>
            </div>
          </div>

          <form onSubmit={doSubmit} className="space-y-5">
            <div className="space-y-4">
              <div className="space-y-2">
                <label className="text-sm font-bold text-foreground/80 ml-1">账号</label>
                <div className="relative group">
                  <User className="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-muted-foreground group-focus-within:text-primary transition-colors" />
                  <input
                    type="text"
                    required
                    placeholder="请设置用户账号 (至少 4 位)"
                    className="w-full h-12 pl-12 pr-4 rounded-2xl bg-slate-100/50 border-transparent border-2 focus:border-primary focus:bg-white outline-none transition-all font-medium text-sm"
                    value={formData.userAccount}
                    onChange={(e) => setFormData({ ...formData, userAccount: e.target.value })}
                  />
                </div>
              </div>

              <div className="space-y-2">
                <label className="text-sm font-bold text-foreground/80 ml-1">密码</label>
                <div className="relative group">
                  <Lock className="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-muted-foreground group-focus-within:text-primary transition-colors" />
                  <input
                    type="password"
                    required
                    placeholder="请设置登录密码 (至少 8 位)"
                    className="w-full h-12 pl-12 pr-4 rounded-2xl bg-slate-100/50 border-transparent border-2 focus:border-primary focus:bg-white outline-none transition-all font-medium text-sm"
                    value={formData.userPassword}
                    onChange={(e) => setFormData({ ...formData, userPassword: e.target.value })}
                  />
                </div>
              </div>

              <div className="space-y-2">
                <label className="text-sm font-bold text-foreground/80 ml-1">确认密码</label>
                <div className="relative group">
                  <ShieldCheck className="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-muted-foreground group-focus-within:text-primary transition-colors" />
                  <input
                    type="password"
                    required
                    placeholder="请再次输入密码"
                    className="w-full h-12 pl-12 pr-4 rounded-2xl bg-slate-100/50 border-transparent border-2 focus:border-primary focus:bg-white outline-none transition-all font-medium text-sm"
                    value={formData.checkPassword}
                    onChange={(e) => setFormData({ ...formData, checkPassword: e.target.value })}
                  />
                </div>
              </div>
            </div>

            <button
              disabled={loading}
              className="group relative w-full h-14 rounded-2xl bg-primary text-primary-foreground font-black text-lg shadow-lg shadow-primary/30 hover:shadow-primary/40 active:scale-95 transition-all disabled:opacity-70 flex items-center justify-center gap-2 mt-4"
            >
              {loading ? (
                <Loader2 className="h-6 w-6 animate-spin" />
              ) : (
                <>
                  立即注册
                  <ArrowRight className="h-5 w-5 group-hover:translate-x-1 transition-transform" />
                </>
              )}
            </button>
          </form>

          {/* Divider */}
          <div className="relative my-8">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-slate-200"></div>
            </div>
            <div className="relative flex justify-center text-xs uppercase tracking-widest font-black">
              <span className="bg-transparent px-4 text-muted-foreground">合作交流</span>
            </div>
          </div>

          {/* Icon Groups */}
           <div className="flex justify-center gap-6 text-muted-foreground">
             <Globe className="h-6 w-6 hover:text-primary transition-colors cursor-pointer" />
             <Mail className="h-6 w-6 hover:text-primary transition-colors cursor-pointer" />
          </div>

          <div className="mt-8 text-center border-t pt-6">
            <p className="text-sm font-bold text-muted-foreground">
              已有账号?{" "}
              <Link href="/user/login" className="text-primary hover:underline underline-offset-4">
                立即登录
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

