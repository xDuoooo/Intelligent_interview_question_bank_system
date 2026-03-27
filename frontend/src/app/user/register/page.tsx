"use client";

import React, { useState } from "react";
import Image from "next/image";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useDispatch, useSelector } from "react-redux";
import { AppDispatch, RootState } from "@/stores";
import { setLoginUser } from "@/stores/loginUser";
import { useEffect } from "react";
import { 
  userRegisterUsingPost, 
  getLoginUserUsingGet 
} from "@/api/userController";
import { message } from "antd";
import { User, Lock, ArrowRight, Loader2, ShieldCheck } from "lucide-react";

export default function UserRegisterPage() {
  const [formData, setFormData] = useState<API.UserRegisterRequest>({
    userAccount: "",
    userPassword: "",
    checkPassword: "",
  });
  const [loading, setLoading] = useState(false);
  const router = useRouter();
  const dispatch = useDispatch<AppDispatch>();
  const loginUser = useSelector((state: RootState) => state.loginUser);

  // 已登录用户自动重定向
  useEffect(() => {
    if (loginUser && loginUser.id) {
      router.replace("/");
    }
  }, [loginUser, router]);

  const doSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const { userAccount, userPassword, checkPassword } = formData;
    
    // 前端基本校验
    if (!userAccount || userAccount.length < 4) {
      message.warning("账号长度不能少于 4 位");
      return;
    }
    if (!userPassword || userPassword.length < 8) {
      message.warning("密码长度不能少于 8 位");
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
        message.success("注册成功，正在为您登录...");
        // 注册成功后尝试获取登录态
        const userRes = await getLoginUserUsingGet();
        if (userRes.data) {
          dispatch(setLoginUser(userRes.data as API.LoginUserVO));
        }
        router.replace("/");
      }
    } catch (e: any) {
      message.error(e.message || "注册失败，请检查账号是否已存在");
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
        <div className="relative overflow-hidden rounded-[2.5rem] border bg-white/60 backdrop-blur-2xl shadow-2xl p-8 sm:p-10">
          {/* Header */}
          <div className="flex flex-col items-center text-center space-y-4 mb-10">
            <div className="relative h-20 w-20 rounded-3xl overflow-hidden shadow-xl ring-4 ring-slate-50 group transform hover:scale-105 transition-transform duration-500">
               <div className="absolute inset-0 bg-white flex items-center justify-center p-3">
                 <Image
                  src="/assets/logo.png"
                  height={64}
                  width={64}
                  alt="Logo"
                  className="object-contain"
                />
              </div>
            </div>
            <div className="space-y-1">
              <h1 className="text-2xl font-black tracking-tight text-foreground">加入智面</h1>
              <p className="text-xs font-bold text-muted-foreground uppercase tracking-widest">
                START YOUR JOURNEY
              </p>
            </div>
          </div>

          <form onSubmit={doSubmit} className="space-y-5">
            <div className="space-y-4">
              <div className="space-y-2">
                <label className="text-xs font-bold text-muted-foreground ml-1 uppercase tracking-wider">账号</label>
                <div className="relative group">
                  <User className="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-slate-400 group-focus-within:text-primary transition-colors" />
                  <input
                    type="text"
                    required
                    placeholder="至少 4 位"
                    className="w-full h-12 pl-12 pr-4 rounded-2xl bg-slate-100/50 border-transparent border-2 focus:border-primary focus:bg-white outline-none transition-all font-medium text-sm"
                    value={formData.userAccount}
                    onChange={(e) => setFormData({ ...formData, userAccount: e.target.value })}
                  />
                </div>
              </div>

              <div className="space-y-2">
                <label className="text-xs font-bold text-muted-foreground ml-1 uppercase tracking-wider">密码</label>
                <div className="relative group">
                  <Lock className="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-slate-400 group-focus-within:text-primary transition-colors" />
                  <input
                    type="password"
                    required
                    placeholder="至少 8 位"
                    className="w-full h-12 pl-12 pr-4 rounded-2xl bg-slate-100/50 border-transparent border-2 focus:border-primary focus:bg-white outline-none transition-all font-medium text-sm"
                    value={formData.userPassword}
                    onChange={(e) => setFormData({ ...formData, userPassword: e.target.value })}
                  />
                </div>
              </div>

              <div className="space-y-2">
                <label className="text-xs font-bold text-muted-foreground ml-1 uppercase tracking-wider">确认密码</label>
                <div className="relative group">
                  <ShieldCheck className="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-slate-400 group-focus-within:text-primary transition-colors" />
                  <input
                    type="password"
                    required
                    placeholder="请再次输入"
                    className="w-full h-12 pl-12 pr-4 rounded-2xl bg-slate-100/50 border-transparent border-2 focus:border-primary focus:bg-white outline-none transition-all font-medium text-sm"
                    value={formData.checkPassword}
                    onChange={(e) => setFormData({ ...formData, checkPassword: e.target.value })}
                  />
                </div>
              </div>
            </div>

            <button
              disabled={loading}
              className="group relative w-full h-14 rounded-2xl bg-primary text-white font-black text-lg shadow-lg shadow-primary/30 hover:shadow-primary/40 active:scale-95 transition-all disabled:opacity-70 flex items-center justify-center gap-2 mt-4"
            >
              {loading ? (
                <Loader2 className="h-6 w-6 animate-spin" />
              ) : (
                <>
                  完成注册
                  <ArrowRight className="h-5 w-5 group-hover:translate-x-1 transition-transform" />
                </>
              )}
            </button>
          </form>

          <div className="mt-8 text-center border-t border-slate-100 pt-6">
            <p className="text-sm font-bold text-slate-400">
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
