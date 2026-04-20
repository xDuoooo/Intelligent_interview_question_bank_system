"use client";

import React, { useEffect, useMemo, useRef, useState } from "react";
import Image from "next/image";
import { Lock, Mail, Phone, QrCode, ShieldCheck, User } from "lucide-react";
import { Alert, message } from "antd";
import { useRouter } from "next/navigation";
import { useDispatch, useSelector } from "react-redux";
import { AppDispatch, RootState } from "@/stores";
import { setLoginUser } from "@/stores/loginUser";
import request from "@/libs/request";
import { APP_CONFIG } from "@/config/appConfig";
import { getSocialAuthUrl, SOCIAL_AUTH_PROVIDERS } from "@/config/auth";
import { getPublicSystemConfigUsingGet } from "@/api/systemConfigController";
import {
  createWxMpLoginTicketUsingPost,
  getWxMpLoginStatusUsingGet,
  loginByWxMpCodeUsingPost,
  type WxMpLoginStatusVO,
  type WxMpLoginTicketVO,
} from "@/api/wxMpController";
import {
  getLoginUserUsingGet,
  sendVerificationCodeUsingPost,
  userCodeLoginUsingPost,
  userLoginUsingPost,
} from "@/api/userController";

type LoginType = "password" | "code" | "wxMp";

const DEFAULT_REDIRECT_PATH = "/";

const getSafeRedirectPath = (redirect: string | null) => {
  if (!redirect || typeof window === "undefined") {
    return DEFAULT_REDIRECT_PATH;
  }
  try {
    const url = new URL(redirect, window.location.origin);
    if (url.origin !== window.location.origin) {
      return DEFAULT_REDIRECT_PATH;
    }
    const path = `${url.pathname}${url.search}${url.hash}`;
    if (!path || path.startsWith("/user/login")) {
      return DEFAULT_REDIRECT_PATH;
    }
    return path;
  } catch {
    return DEFAULT_REDIRECT_PATH;
  }
};

const UserLoginPage: React.FC = () => {
  const [loginType, setLoginType] = useState<LoginType>("code");
  const [loginLoading, setLoginLoading] = useState(false);
  const [redirectPath, setRedirectPath] = useState(DEFAULT_REDIRECT_PATH);
  const router = useRouter();
  const dispatch = useDispatch<AppDispatch>();
  const loginUser = useSelector((state: RootState) => state.loginUser);

  useEffect(() => {
    if (loginUser && loginUser.id) {
      const searchParams = new URLSearchParams(window.location.search);
      router.replace(getSafeRedirectPath(searchParams.get("redirect")));
    }
  }, [loginUser, router]);

  const [passwordData, setPasswordData] = useState({
    userAccount: "",
    userPassword: "",
  });
  const [target, setTarget] = useState("");
  const [code, setCode] = useState("");
  const [wxMpCode, setWxMpCode] = useState("");
  const [wxMpTicketInfo, setWxMpTicketInfo] = useState<WxMpLoginTicketVO | null>(null);
  const [wxMpStatus, setWxMpStatus] = useState<WxMpLoginStatusVO | null>(null);
  const [wxMpTicketLoading, setWxMpTicketLoading] = useState(false);

  const inputType = useMemo(() => {
    if (target.includes("@")) return 1;
    if (/^\d{11}$/.test(target)) return 2;
    return 0;
  }, [target]);

  const [count, setCount] = useState(0);
  const [captchaData, setCaptchaData] = useState<{ image: string; uuid: string } | null>(null);
  const [captchaInput, setCaptchaInput] = useState("");
  const [systemConfig, setSystemConfig] = useState<API.SystemConfigVO | null>(null);
  const hasHandledQueryFeedback = useRef(false);

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
    const loadPublicSystemConfig = async () => {
      try {
        const res = await getPublicSystemConfigUsingGet();
        setSystemConfig(res.data ?? null);
      } catch (e) {
        console.error("获取公开系统配置失败", e);
      }
    };
    loadPublicSystemConfig();
  }, []);

  const requireCaptcha = systemConfig?.requireCaptcha ?? true;
  const siteName = systemConfig?.siteName || APP_CONFIG.brand.displayName;
  const announcement = systemConfig?.announcement?.trim();
  const firstLoginHint =
    systemConfig?.allowRegister === false
      ? "当前未开放新账号注册，仅支持已绑定邮箱或手机号的账号使用验证码登录"
      : APP_CONFIG.auth.firstLoginHint;
  const wxMpExpireAt = wxMpStatus?.expireAt ?? wxMpTicketInfo?.expireAt;
  const wxMpExpireText = wxMpExpireAt
    ? new Date(wxMpExpireAt).toLocaleTimeString("zh-CN", {
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit",
      })
    : null;

  useEffect(() => {
    if (requireCaptcha) {
      refreshCaptcha();
    } else {
      setCaptchaData(null);
      setCaptchaInput("");
    }
  }, [requireCaptcha]);

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
    setRedirectPath(getSafeRedirectPath(searchParams.get("redirect")));

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
    if (nextLoginType === "password" || nextLoginType === "wxMp") {
      setLoginType(nextLoginType);
    }
    if (error) {
      message.error(error);
    } else if (msg) {
      message.success(msg);
    }
    const nextSearchParams = new URLSearchParams(window.location.search);
    nextSearchParams.delete("error");
    nextSearchParams.delete("msg");
    nextSearchParams.delete("account");
    nextSearchParams.delete("loginType");
    const nextQuery = nextSearchParams.toString();
    router.replace(nextQuery ? `${window.location.pathname}?${nextQuery}` : window.location.pathname);
  }, [router]);

  useEffect(() => {
    let timer: number | undefined;
    if (count > 0) {
      timer = window.setInterval(() => {
        setCount((c) => c - 1);
      }, 1000);
    }
    return () => {
      if (timer) {
        window.clearInterval(timer);
      }
    };
  }, [count]);

  const createWxMpTicket = async (silent = false) => {
    setWxMpTicketLoading(true);
    try {
      const res = await createWxMpLoginTicketUsingPost();
      setWxMpTicketInfo(res.data ?? null);
      setWxMpStatus({
        status: "pending",
        codeSent: false,
        message: "请在微信中发送页面展示的登录口令，系统会把 6 位验证码回复到公众号会话里。",
        expireAt: res.data?.expireAt,
      });
      setWxMpCode("");
    } catch (e: any) {
      setWxMpTicketInfo(null);
      setWxMpStatus({
        status: "expired",
        codeSent: false,
        message: e.message || "公众号验证码登录暂时不可用，请稍后再试",
      });
      if (!silent) {
        message.error(e.message || "公众号验证码登录暂时不可用，请稍后再试");
      }
    } finally {
      setWxMpTicketLoading(false);
    }
  };

  useEffect(() => {
    if (loginType !== "wxMp" || wxMpTicketInfo || wxMpTicketLoading || wxMpStatus) {
      return;
    }
    void createWxMpTicket(true);
  }, [loginType, wxMpTicketInfo, wxMpTicketLoading, wxMpStatus]);

  useEffect(() => {
    if (loginType !== "wxMp" || !wxMpTicketInfo?.ticket) {
      return;
    }
    let cancelled = false;
    const syncWxMpStatus = async () => {
      try {
        const res = await getWxMpLoginStatusUsingGet();
        if (!cancelled) {
          setWxMpStatus(res.data ?? null);
        }
      } catch (e: any) {
        if (!cancelled) {
          setWxMpStatus((prev) => ({
            status: prev?.status ?? "pending",
            codeSent: prev?.codeSent ?? false,
            message: e.message || prev?.message || "登录状态同步失败，请稍后重试",
            expireAt: prev?.expireAt ?? wxMpTicketInfo.expireAt,
          }));
        }
      }
    };
    void syncWxMpStatus();
    const timer = window.setInterval(() => {
      void syncWxMpStatus();
    }, 3000);
    return () => {
      cancelled = true;
      window.clearInterval(timer);
    };
  }, [loginType, wxMpTicketInfo?.ticket, wxMpTicketInfo?.expireAt]);

  const sendCode = async () => {
    if (!target) {
      message.warning("请输入邮箱或手机号");
      return;
    }
    if (inputType === 0) {
      message.warning("请输入有效的邮箱地址或 11 位手机号");
      return;
    }
    if (requireCaptcha && !captchaInput) {
      message.warning("请先输入图形验证码");
      return;
    }
    try {
      const res = await sendVerificationCodeUsingPost({
        target,
        type: inputType,
        captcha: requireCaptcha ? captchaInput : undefined,
        captchaUuid: requireCaptcha ? captchaData?.uuid : undefined,
      });
      if (res.data) {
        message.success("验证码已发送");
        setCount(60);
        if (requireCaptcha) {
          refreshCaptcha();
        }
        setCaptchaInput("");
      }
    } catch (e: any) {
      message.error(e.message || "验证码发送失败，请稍后重试");
      if (requireCaptcha) {
        refreshCaptcha();
      }
      setCaptchaInput("");
    }
  };

  const doSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoginLoading(true);

    try {
      let res;
      if (loginType === "password") {
        if (requireCaptcha && !captchaInput) {
          message.warning("请先输入图形验证码");
          setLoginLoading(false);
          return;
        }
        res = await userLoginUsingPost({
          ...passwordData,
          captcha: requireCaptcha ? captchaInput : undefined,
          captchaUuid: requireCaptcha ? captchaData?.uuid : undefined,
        });
      } else if (loginType === "code") {
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
      } else {
        if (!wxMpCode) {
          message.warning("请输入公众号会话里收到的 6 位验证码");
          setLoginLoading(false);
          return;
        }
        res = await loginByWxMpCodeUsingPost({
          code: wxMpCode,
        });
      }

      if (res.data) {
        message.success("欢迎回来！");
        const userRes = await getLoginUserUsingGet();
        if (userRes.data) {
          dispatch(setLoginUser(userRes.data as API.LoginUserVO));
        }
        router.replace(redirectPath);
      }
    } catch (e: any) {
      message.error(e.message || "认证失败，请检查输入项");
      if (requireCaptcha && loginType === "password") {
        refreshCaptcha();
        setCaptchaInput("");
      }
    } finally {
      setLoginLoading(false);
    }
  };

  return (
    <div className="flex min-h-[calc(100vh-64px-160px)] items-center justify-center px-4 py-12 sm:px-6 lg:px-8">
      <div className="relative w-full max-w-md">
        <div className="absolute -left-12 -top-12 h-64 w-64 animate-pulse rounded-full bg-primary/10 blur-3xl" />
        <div className="absolute -bottom-12 -right-12 h-64 w-64 animate-pulse rounded-full bg-blue-500/10 blur-3xl delay-700" />

        <div className="relative overflow-hidden rounded-[2.5rem] border bg-white/60 p-8 shadow-2xl backdrop-blur-2xl sm:p-10">
          <div className="mb-8 flex flex-col items-center space-y-4 text-center">
            <div className="relative h-20 w-20 overflow-hidden rounded-3xl shadow-lg ring-4 ring-slate-50">
              <div className="absolute inset-0 flex items-center justify-center bg-white p-3">
                <Image
                  src="/assets/logo.png"
                  height={64}
                  width={64}
                  alt="Logo"
                  className="object-contain"
                />
              </div>
            </div>
            <h1 className="text-2xl font-black text-foreground">{siteName}</h1>
          </div>

          {(systemConfig?.maintenanceMode || announcement) && (
            <div className="mb-6 space-y-3">
              {systemConfig?.maintenanceMode && (
                <Alert
                  showIcon
                  type="warning"
                  className="rounded-2xl"
                  message="系统维护中"
                  description="当前仅管理员账号可以登录，普通用户请稍后再试。"
                />
              )}
              {announcement && (
                <Alert
                  showIcon
                  type="info"
                  className="rounded-2xl"
                  message="系统公告"
                  description={announcement}
                />
              )}
            </div>
          )}

          <div className="mb-8 grid grid-cols-3 gap-1.5 rounded-2xl bg-slate-100/80 p-1.5">
            <button
              type="button"
              onClick={() => setLoginType("code")}
              className={`rounded-xl py-2.5 text-xs font-bold transition-all sm:text-sm ${
                loginType === "code"
                  ? "bg-white text-primary shadow-sm"
                  : "text-slate-500 hover:text-slate-700"
              }`}
            >
              验证码登录
            </button>
            <button
              type="button"
              onClick={() => setLoginType("password")}
              className={`rounded-xl py-2.5 text-xs font-bold transition-all sm:text-sm ${
                loginType === "password"
                  ? "bg-white text-primary shadow-sm"
                  : "text-slate-500 hover:text-slate-700"
              }`}
            >
              账号密码
            </button>
            <button
              type="button"
              onClick={() => setLoginType("wxMp")}
              className={`rounded-xl py-2.5 text-xs font-bold transition-all sm:text-sm ${
                loginType === "wxMp"
                  ? "bg-white text-primary shadow-sm"
                  : "text-slate-500 hover:text-slate-700"
              }`}
            >
              公众号验证码
            </button>
          </div>

          <form onSubmit={doSubmit} className="space-y-5">
            {loginType === "password" ? (
              <>
                <div className="space-y-2">
                  <label className="ml-1 text-xs font-bold uppercase tracking-wider text-muted-foreground">
                    账号
                  </label>
                  <div className="relative">
                    <User className="absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-slate-400" />
                    <input
                      type="text"
                      className="h-12 w-full rounded-2xl border-2 border-transparent bg-slate-100/50 pl-12 pr-4 font-medium outline-none transition-all focus:border-primary focus:bg-white"
                      placeholder="账号 / 手机号 / 邮箱"
                      value={passwordData.userAccount}
                      onChange={(e) =>
                        setPasswordData({
                          ...passwordData,
                          userAccount: e.target.value,
                        })
                      }
                    />
                  </div>
                </div>
                <div className="space-y-2">
                  <label className="ml-1 text-xs font-bold uppercase tracking-wider text-muted-foreground">
                    密码
                  </label>
                  <div className="relative">
                    <Lock className="absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-slate-400" />
                    <input
                      type="password"
                      className="h-12 w-full rounded-2xl border-2 border-transparent bg-slate-100/50 pl-12 pr-4 font-medium outline-none transition-all focus:border-primary focus:bg-white"
                      placeholder="您的登录密码"
                      value={passwordData.userPassword}
                      onChange={(e) =>
                        setPasswordData({
                          ...passwordData,
                          userPassword: e.target.value,
                        })
                      }
                    />
                  </div>
                </div>

                {requireCaptcha && (
                  <div className="space-y-2">
                    <label className="ml-1 text-xs font-bold uppercase tracking-wider text-muted-foreground">
                      图形验证码
                    </label>
                    <div className="flex flex-col gap-3 sm:flex-row">
                      <div className="relative min-w-0 flex-1">
                        <ShieldCheck className="absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-slate-400" />
                        <input
                          type="text"
                          className="h-12 w-full rounded-2xl border-2 border-transparent bg-slate-100/50 pl-12 pr-4 font-medium outline-none transition-all focus:border-primary focus:bg-white"
                          placeholder="请输入右侧验证码"
                          value={captchaInput}
                          onChange={(e) => setCaptchaInput(e.target.value)}
                        />
                      </div>
                      <div
                        className="flex h-12 w-full cursor-pointer items-center justify-center overflow-hidden rounded-2xl border-2 border-slate-100 bg-white p-1 transition-all hover:border-primary sm:w-28"
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
                            className="h-full w-full rounded-xl object-cover"
                          />
                        ) : (
                          <div className="h-full w-full animate-pulse rounded-xl bg-slate-100" />
                        )}
                      </div>
                    </div>
                  </div>
                )}
              </>
            ) : loginType === "code" ? (
              <>
                <div className="space-y-2">
                  <label className="ml-1 text-xs font-bold uppercase tracking-wider text-muted-foreground">
                    手机号或邮箱
                  </label>
                  <div className="relative transition-all">
                    {inputType === 1 ? (
                      <Mail className="absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 animate-in zoom-in-50 text-primary duration-300" />
                    ) : inputType === 2 ? (
                      <Phone className="absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 animate-in zoom-in-50 text-primary duration-300" />
                    ) : (
                      <User className="absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-slate-400" />
                    )}
                    <input
                      type="text"
                      className="h-12 w-full rounded-2xl border-2 border-transparent bg-slate-100/50 pl-12 pr-4 font-medium outline-none transition-all focus:border-primary focus:bg-white"
                      placeholder="输入手机号或邮箱"
                      value={target}
                      onChange={(e) => setTarget(e.target.value.trim())}
                    />
                  </div>
                </div>

                {requireCaptcha && (
                  <div className="space-y-2">
                    <label className="ml-1 text-xs font-bold uppercase tracking-wider text-muted-foreground">
                      图形验证码
                    </label>
                    <div className="flex flex-col gap-3 sm:flex-row">
                      <div className="relative min-w-0 flex-1">
                        <ShieldCheck className="absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-slate-400" />
                        <input
                          type="text"
                          className="h-12 w-full rounded-2xl border-2 border-transparent bg-slate-100/50 pl-12 pr-4 font-medium outline-none transition-all focus:border-primary focus:bg-white"
                          placeholder="请输入右侧验证码"
                          value={captchaInput}
                          onChange={(e) => setCaptchaInput(e.target.value)}
                        />
                      </div>
                      <div
                        className="flex h-12 w-full cursor-pointer items-center justify-center overflow-hidden rounded-2xl border-2 border-slate-100 bg-white p-1 transition-all hover:border-primary sm:w-28"
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
                            className="h-full w-full rounded-xl object-cover"
                          />
                        ) : (
                          <div className="h-full w-full animate-pulse rounded-xl bg-slate-100" />
                        )}
                      </div>
                    </div>
                  </div>
                )}

                <div className="space-y-2">
                  <label className="ml-1 text-xs font-bold uppercase tracking-wider text-muted-foreground">
                    验证码
                  </label>
                  <div className="flex flex-col gap-3 sm:flex-row">
                    <div className="relative min-w-0 flex-1">
                      <Lock className="absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-slate-400" />
                      <input
                        type="text"
                        className="h-12 w-full rounded-2xl border-2 border-transparent bg-slate-100/50 pl-12 pr-4 font-medium outline-none transition-all focus:border-primary focus:bg-white"
                        placeholder="6 位数字"
                        value={code}
                        onChange={(e) => setCode(e.target.value)}
                      />
                    </div>
                    <button
                      type="button"
                      disabled={count > 0}
                      onClick={sendCode}
                      className="h-12 w-full whitespace-nowrap rounded-2xl bg-primary px-6 text-sm font-bold text-white shadow-lg shadow-primary/20 transition-all hover:brightness-110 active:scale-95 disabled:grayscale disabled:opacity-50 sm:w-auto"
                    >
                      {count > 0 ? `${count}s` : "获取验证码"}
                    </button>
                  </div>
                </div>
              </>
            ) : (
              <>
                <div className="rounded-3xl border border-emerald-100 bg-emerald-50/70 p-4">
                  <div className="flex items-start gap-4">
                    <div className="flex h-14 w-14 shrink-0 items-center justify-center rounded-2xl bg-white text-emerald-600 shadow-sm">
                      <QrCode className="h-7 w-7" />
                    </div>
                    <div className="space-y-1 text-sm text-slate-600">
                      <div className="font-bold text-slate-900">通过公众号验证码登录</div>
                      <div>① 扫描下方二维码，关注公众号</div>
                      <div>② 在公众号对话框中发送「<span className="font-bold text-emerald-700">{wxMpTicketInfo?.keyword || "登录"}</span>」</div>
                      <div>③ 把公众号回复的 <span className="font-bold">6 位数字</span>填入下方输入框</div>
                    </div>
                  </div>
                </div>

                <div className="flex flex-col items-center gap-4 rounded-3xl border border-slate-100 bg-slate-50/60 p-5 sm:flex-row sm:items-center">
                  <div className="flex h-44 w-44 shrink-0 items-center justify-center rounded-3xl border border-dashed border-slate-200 bg-white p-3">
                    {wxMpTicketInfo?.qrImageUrl ? (
                      // eslint-disable-next-line @next/next/no-img-element
                      <img
                        src={wxMpTicketInfo.qrImageUrl}
                        alt="公众号二维码"
                        className="h-full w-full rounded-2xl object-contain"
                      />
                    ) : (
                      <div className="flex flex-col items-center justify-center gap-2 text-center text-xs text-slate-400">
                        <QrCode className="h-10 w-10" />
                        <span>暂未配置二维码</span>
                      </div>
                    )}
                  </div>
                  <div className="text-center sm:text-left space-y-1">
                    <div className="text-xs font-bold uppercase tracking-wider text-slate-400">关注公众号</div>
                    <div className="text-lg font-black text-slate-900">{wxMpTicketInfo?.accountName || "公众号"}</div>
                    <div className="mt-2 text-sm text-slate-500">
                      发送「<span className="font-bold text-primary">{wxMpTicketInfo?.keyword || "登录"}</span>」
                      即可获得登录验证码
                    </div>
                  </div>
                </div>

                <div className="space-y-2">
                  <label className="ml-1 text-xs font-bold uppercase tracking-wider text-muted-foreground">
                    公众号验证码
                  </label>
                  <div className="relative">
                    <Lock className="absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-slate-400" />
                    <input
                      type="text"
                      inputMode="numeric"
                      className="h-12 w-full rounded-2xl border-2 border-transparent bg-slate-100/50 pl-12 pr-4 font-medium outline-none transition-all focus:border-primary focus:bg-white"
                      placeholder="请输入公众号回复的 6 位验证码"
                      value={wxMpCode}
                      onChange={(e) =>
                        setWxMpCode(e.target.value.replace(/\D/g, "").slice(0, 6))
                      }
                    />
                  </div>
                </div>
              </>
            )}

            <button
              type="submit"
              disabled={loginLoading}
              className="mt-4 flex h-14 w-full items-center justify-center rounded-2xl bg-primary text-lg font-black text-white shadow-xl shadow-primary/25 transition-all hover:-translate-y-0.5 hover:shadow-primary/40 active:translate-y-0 disabled:opacity-70"
            >
              {loginLoading ? (
                <div className="h-6 w-6 animate-spin rounded-full border-4 border-white/30 border-t-white" />
              ) : (
                APP_CONFIG.auth.loginActionText
              )}
            </button>
          </form>

          <div className="mt-8 text-center text-xs font-medium text-slate-400">
            {loginType === "wxMp"
              ? "首次通过公众号登录时，系统会自动为你创建一个可继续补充邮箱、手机号和密码的账号。"
              : firstLoginHint}
          </div>

          <div className="relative my-6">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-slate-100"></div>
            </div>
            <div className="relative flex justify-center text-[10px] font-bold uppercase tracking-widest">
              <span className="bg-white px-4 text-slate-300">
                {APP_CONFIG.auth.socialLoginTitle}
              </span>
            </div>
          </div>

          <div className="grid grid-cols-3 gap-3">
            {SOCIAL_AUTH_PROVIDERS.map((social) => (
              <button
                key={social.key}
                type="button"
                onClick={() => {
                  window.location.href = getSocialAuthUrl(social.key);
                }}
                className="group flex h-16 flex-col items-center justify-center gap-1 rounded-2xl border border-slate-50 bg-slate-50/30 transition-all hover:border-slate-200 hover:bg-white hover:shadow-sm"
              >
                <div className="transition-transform group-hover:scale-110">
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
