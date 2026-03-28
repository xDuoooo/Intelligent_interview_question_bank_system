"use client";

import React, { useEffect, useState, useMemo, useRef } from "react";
import Image from "next/image";
import { Lock, Mail, Phone, User, ShieldCheck } from "lucide-react";
import { message } from "antd";
import { useRouter } from "next/navigation";
import { useDispatch, useSelector } from "react-redux";
import { AppDispatch, RootState } from "@/stores";
import { setLoginUser } from "@/stores/loginUser";
import request from "@/libs/request";
import { APP_CONFIG } from "@/config/appConfig";
import { getSocialAuthUrl, SOCIAL_AUTH_PROVIDERS } from "@/config/auth";
import {
  getLoginUserUsingGet,
  sendVerificationCodeUsingPost,
  userCodeLoginUsingPost,
  userLoginUsingPost,
} from "@/api/userController";

type LoginType = "password" | "code";

const UserLoginPage: React.FC = () => {
  const [loginType, setLoginType] = useState<LoginType>("code");
  const [loginLoading, setLoginLoading] = useState(false);
  const router = useRouter();
  const dispatch = useDispatch<AppDispatch>();
  const loginUser = useSelector((state: RootState) => state.loginUser);

  // 已登录用户自动重定向
  useEffect(() => {
    if (loginUser && loginUser.id) {
      router.replace("/");
    }
  }, [loginUser, router]);

  // 账号密码登录数据
  const [passwordData, setPasswordData] = useState({
    userAccount: "",
    userPassword: "",
  });

  // 验证码登录数据
  const [target, setTarget] = useState("");
  const [code, setCode] = useState("");

  // 自动检测输入类型 (1-邮件, 2-手机)
  const inputType = useMemo(() => {
    if (target.includes("@")) return 1;
    if (/^\d{11}$/.test(target)) return 2;
    return 0; // 未识别
  }, [target]);

  const [count, setCount] = useState(0);
  const [captchaData, setCaptchaData] = useState<{ image: string; uuid: string } | null>(null);
  const [captchaInput, setCaptchaInput] = useState("");
  const hasHandledQueryFeedback = useRef(false);

  // 获取图形验证码
  const refreshCaptcha = async () => {
    try {
      const res: any = await request.get("/api/captcha/get");
      if (res.code === 0) {
        setCaptchaData(res.data);
      }
    } catch (e) {
      console.error("获取图形验证码失败", e);
    }
  };

  useEffect(() => {
    refreshCaptcha();
  }, []);

  useEffect(() => {
    if (typeof window === "undefined") {
      return;
    }
    if (hasHandledQueryFeedback.current) {
      return;
    }
    const searchParams = new URLSearchParams(window.location.search);
    const error = searchParams.get("error");
    const msg = searchParams.get("msg");
    const account = searchParams.get("account");
    const nextLoginType = searchParams.get("loginType");

    if (!error && !msg && !account && !nextLoginType) {
      return;
    }
    hasHandledQueryFeedback.current = true;

    if (account) {
      setPasswordData((prev) => ({
        ...prev,
        userAccount: account,
      }));
    }
    if (nextLoginType === "password") {
      setLoginType("password");
    }
    if (error) {
      message.error(error);
    } else if (msg) {
      message.success(msg);
    }
    router.replace(window.location.pathname);
  }, [router]);

  // 定时器处理
  useEffect(() => {
    let timer: any;
    if (count > 0) {
      timer = setInterval(() => {
        setCount((c) => c - 1);
      }, 1000);
    }
    return () => clearInterval(timer);
  }, [count]);

  // 发送验证码
  const sendCode = async () => {
    if (!target) {
      message.warning("请输入邮箱或手机号");
      return;
    }
    if (inputType === 0) {
      message.warning("请输入有效的邮箱地址或 11 位手机号");
      return;
    }
    if (!captchaInput) {
      message.warning("请先输入图形验证码");
      return;
    }
    try {
      const res = await sendVerificationCodeUsingPost({
        target,
        type: inputType,
        captcha: captchaInput,
        captchaUuid: captchaData?.uuid,
      });
      if (res.data) {
        message.success("验证码已发送");
        setCount(60);
        refreshCaptcha();
        setCaptchaInput("");
      }
    } catch (e: any) {
      message.error(e.message || "验证码发送失败，请稍后重试");
      refreshCaptcha();
      setCaptchaInput("");
    }
  };

  const doSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoginLoading(true);

    try {
      let res;
      if (loginType === "password") {
        res = await userLoginUsingPost(passwordData);
      } else {
        if (inputType === 0) {
          message.error("请输入有效的手机号或邮箱");
          setLoginLoading(false);
          return;
        }
        res = await userCodeLoginUsingPost({
          target,
          code,
          type: inputType,
        });
      }

      if (res.data) {
        message.success("欢迎回来！");
        const userRes = await getLoginUserUsingGet();
        if (userRes.data) {
          dispatch(setLoginUser(userRes.data as API.LoginUserVO));
        }
        router.replace("/");
      }
    } catch (e: any) {
      message.error(e.message || "认证失败，请检查输入项");
    } finally {
      setLoginLoading(false);
    }
  };

  return (
    <div className="flex min-h-[calc(100vh-64px-160px)] items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="relative w-full max-w-md">
        <div className="absolute -top-12 -left-12 h-64 w-64 rounded-full bg-primary/10 blur-3xl animate-pulse" />
        <div className="absolute -bottom-12 -right-12 h-64 w-64 rounded-full bg-blue-500/10 blur-3xl animate-pulse delay-700" />

        <div className="relative overflow-hidden rounded-[2.5rem] border bg-white/60 backdrop-blur-2xl shadow-2xl p-8 sm:p-10">
          <div className="flex flex-col items-center text-center space-y-4 mb-8">
            <div className="relative h-20 w-20 rounded-3xl overflow-hidden shadow-lg ring-4 ring-slate-50">
              <div className="absolute inset-0 bg-white flex items-center justify-center p-3">
                <Image src="/assets/logo.png" height={64} width={64} alt="Logo" className="object-contain" />
              </div>
            </div>
            <h1 className="text-2xl font-black text-foreground">{APP_CONFIG.brand.displayName}</h1>
          </div>

          <div className="flex p-1.5 bg-slate-100/80 rounded-2xl mb-8">
            <button
              onClick={() => setLoginType("code")}
              className={`flex-1 py-2.5 text-sm font-bold rounded-xl transition-all ${
                loginType === "code" ? "bg-white text-primary shadow-sm" : "text-slate-500 hover:text-slate-700"
              }`}
            >
              验证码登录
            </button>
            <button
              onClick={() => setLoginType("password")}
              className={`flex-1 py-2.5 text-sm font-bold rounded-xl transition-all ${
                loginType === "password" ? "bg-white text-primary shadow-sm" : "text-slate-500 hover:text-slate-700"
              }`}
            >
              账号密码
            </button>
          </div>

          <form onSubmit={doSubmit} className="space-y-5">
            {loginType === "password" ? (
              <>
                <div className="space-y-2">
                  <label className="text-xs font-bold text-muted-foreground ml-1 uppercase tracking-wider">账号</label>
                  <div className="relative">
                    <User className="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-slate-400" />
                    <input
                      type="text"
                      className="w-full h-12 pl-12 pr-4 rounded-2xl bg-slate-100/50 border-2 border-transparent focus:border-primary focus:bg-white outline-none transition-all font-medium"
                      placeholder="账号 / 手机号 / 邮箱"
                      value={passwordData.userAccount}
                      onChange={(e) => setPasswordData({ ...passwordData, userAccount: e.target.value })}
                    />
                  </div>
                </div>
                <div className="space-y-2">
                  <label className="text-xs font-bold text-muted-foreground ml-1 uppercase tracking-wider">密码</label>
                  <div className="relative">
                    <Lock className="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-slate-400" />
                    <input
                      type="password"
                      className="w-full h-12 pl-12 pr-4 rounded-2xl bg-slate-100/50 border-2 border-transparent focus:border-primary focus:bg-white outline-none transition-all font-medium"
                      placeholder="您的登录密码"
                      value={passwordData.userPassword}
                      onChange={(e) => setPasswordData({ ...passwordData, userPassword: e.target.value })}
                    />
                  </div>
                </div>
              </>
            ) : (
              <>
                <div className="space-y-2">
                  <label className="text-xs font-bold text-muted-foreground ml-1 uppercase tracking-wider">手机号或邮箱</label>
                  <div className="relative transition-all">
                    {inputType === 1 ? (
                      <Mail className="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-primary animate-in zoom-in-50 duration-300" />
                    ) : inputType === 2 ? (
                      <Phone className="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-primary animate-in zoom-in-50 duration-300" />
                    ) : (
                      <User className="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-slate-400" />
                    )}
                    <input
                      type="text"
                      className="w-full h-12 pl-12 pr-4 rounded-2xl bg-slate-100/50 border-2 border-transparent focus:border-primary focus:bg-white outline-none transition-all font-medium"
                      placeholder="输入手机号或邮箱"
                      value={target}
                      onChange={(e) => setTarget(e.target.value.trim())}
                    />
                  </div>
                </div>

                <div className="space-y-2">
                  <label className="text-xs font-bold text-muted-foreground ml-1 uppercase tracking-wider">图形验证码</label>
                  <div className="flex gap-3">
                    <div className="relative flex-1">
                      <ShieldCheck className="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-slate-400" />
                      <input
                        type="text"
                        className="w-full h-12 pl-12 pr-4 rounded-2xl bg-slate-100/50 border-2 border-transparent focus:border-primary focus:bg-white outline-none transition-all font-medium"
                        placeholder="请输入右侧验证码"
                        value={captchaInput}
                        onChange={(e) => setCaptchaInput(e.target.value)}
                      />
                    </div>
                    <div 
                      className="h-12 w-28 rounded-2xl border-2 border-slate-100 overflow-hidden cursor-pointer hover:border-primary transition-all bg-white flex items-center justify-center p-1"
                      onClick={refreshCaptcha}
                      title="点击刷新"
                    >
                      {captchaData ? (
                        <Image
                          src={captchaData.image}
                          alt="captcha"
                          width={112}
                          height={48}
                          unoptimized
                          className="h-full w-full object-cover rounded-xl"
                        />
                      ) : (
                        <div className="animate-pulse h-full w-full bg-slate-100 rounded-xl" />
                      )}
                    </div>
                  </div>
                </div>

                <div className="space-y-2">
                  <label className="text-xs font-bold text-muted-foreground ml-1 uppercase tracking-wider">验证码</label>
                  <div className="flex gap-3">
                    <div className="relative flex-1">
                      <Lock className="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-slate-400" />
                      <input
                        type="text"
                        className="w-full h-12 pl-12 pr-4 rounded-2xl bg-slate-100/50 border-2 border-transparent focus:border-primary focus:bg-white outline-none transition-all font-medium"
                        placeholder="6 位数字"
                        value={code}
                        onChange={(e) => setCode(e.target.value)}
                      />
                    </div>
                    <button
                      type="button"
                      disabled={count > 0}
                      onClick={sendCode}
                      className="h-12 px-6 rounded-2xl bg-primary text-white font-bold text-sm shadow-lg shadow-primary/20 hover:brightness-110 active:scale-95 disabled:grayscale disabled:opacity-50 transition-all whitespace-nowrap"
                    >
                      {count > 0 ? `${count}s` : "获取验证码"}
                    </button>
                  </div>
                </div>
              </>
            )}

            <button
              type="submit"
              disabled={loginLoading}
              className="w-full h-14 mt-4 rounded-2xl bg-primary text-white font-black text-lg shadow-xl shadow-primary/25 hover:shadow-primary/40 hover:-translate-y-0.5 active:translate-y-0 transition-all flex items-center justify-center disabled:opacity-70"
            >
              {loginLoading ? <div className="h-6 w-6 border-4 border-white/30 border-t-white rounded-full animate-spin" /> : APP_CONFIG.auth.loginActionText}
            </button>
          </form>

          <div className="mt-8 text-center text-slate-400 text-xs font-medium">
            {APP_CONFIG.auth.firstLoginHint}
          </div>

          <div className="relative my-6">
            <div className="absolute inset-0 flex items-center"><div className="w-full border-t border-slate-100"></div></div>
            <div className="relative flex justify-center text-[10px] uppercase tracking-widest font-bold"><span className="bg-white px-4 text-slate-300">{APP_CONFIG.auth.socialLoginTitle}</span></div>
          </div>

          <div className="grid grid-cols-3 gap-3">
            {SOCIAL_AUTH_PROVIDERS.map((social) => (
              <button 
                key={social.key}
                type="button"
                onClick={() => {
                  window.location.href = getSocialAuthUrl(social.key);
                }}
                className="flex flex-col items-center justify-center h-16 rounded-2xl border border-slate-50 bg-slate-50/30 hover:bg-white hover:border-slate-200 hover:shadow-sm transition-all gap-1 group"
              >
                <div className="group-hover:scale-110 transition-transform">
                  <Image src={social.iconSrc} width={22} height={22} alt={social.label} />
                </div>
                <span className="text-[10px] font-bold text-slate-400">{social.label}</span>
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default UserLoginPage;
