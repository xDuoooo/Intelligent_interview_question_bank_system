"use client";
import React, { ReactNode } from "react";
import { useSelector } from "react-redux";
import { RootState } from "@/stores";
import { 
  BarChart3, 
  Users, 
  BookOpen, 
  Database, 
  ChevronRight, 
  ArrowLeft,
  ShieldCheck,
  ShieldAlert,
  LayoutDashboard,
  Wand2,
  Activity,
  Settings,
  MessageSquareText,
  BrainCircuit,
  BellRing,
} from "lucide-react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { cn } from "@/lib/utils";
import { Result, Button } from "antd";

interface Props {
  children: ReactNode;
}

/**
 * 管理员后台统一布局
 */
export default function AdminLayout({ children }: Props) {
  const loginUser = useSelector((state: RootState) => state.loginUser);
  const pathname = usePathname() || "";

  // 权限校验
  if (loginUser.userRole !== "admin") {
    return (
      <div className="min-h-[80vh] flex items-center justify-center">
        <Result
          status="403"
          title="403"
          subTitle="抱歉，您没有权限访问该页面。"
          extra={
            <Link href="/">
              <Button type="primary" shape="round" size="large">返回首页</Button>
            </Link>
          }
        />
      </div>
    );
  }

  const menuItems = [
    { label: "仪表盘", icon: LayoutDashboard, href: "/admin" },
    { label: "用户管理", icon: Users, href: "/admin/user" },
    { label: "题库管理", icon: BookOpen, href: "/admin/bank" },
    { label: "题目管理", icon: Database, href: "/admin/question" },
    { label: "社区管理", icon: MessageSquareText, href: "/admin/post" },
    { label: "通知管理", icon: BellRing, href: "/admin/notification" },
    { label: "风控面板", icon: ShieldAlert, href: "/admin/security" },
    { label: "面试管理", icon: BrainCircuit, href: "/admin/mockInterview" },
    { label: "AI 智能增题", icon: Wand2, href: "/admin/question/ai" },
    { label: "审计日志", icon: Activity, href: "/admin/logs" },
    { label: "全局设置", icon: Settings, href: "/admin/settings" },
  ];

  const activeMenuHref =
    menuItems
      .filter((item) => pathname === item.href || pathname.startsWith(`${item.href}/`))
      .sort((a, b) => b.href.length - a.href.length)[0]?.href ?? "";

  return (
    <div id="adminLayout" className="flex flex-col lg:flex-row gap-8 pb-20">
      {/* Admin Sidebar Navigation */}
      <aside className="lg:w-72 shrink-0">
        <div className="sticky top-24 space-y-6">
          {/* Back Home Button */}
          <Link 
            href="/"
            className="group flex items-center gap-2 text-sm font-bold text-muted-foreground hover:text-primary transition-colors px-2"
          >
            <ArrowLeft className="h-4 w-4 group-hover:-translate-x-1 transition-transform" />
            返回门户首页
          </Link>

          {/* Sidebar Card */}
          <div className="bg-white/70 backdrop-blur-xl rounded-[2.5rem] border border-slate-200/60 shadow-2xl shadow-slate-200/50 p-4 overflow-hidden">
            <div className="px-6 py-6 border-b border-slate-100 flex items-center gap-3">
              <div className="bg-primary/20 p-2.5 rounded-2xl">
                <ShieldCheck className="h-6 w-6 text-primary" />
              </div>
              <div className="flex flex-col">
                <span className="text-xs font-black text-primary uppercase tracking-widest">Admin</span>
                <span className="text-lg font-black text-slate-800">管理中台</span>
              </div>
            </div>

            <nav className="mt-6 space-y-2">
              {menuItems.map((item) => {
                const isActive = activeMenuHref === item.href;
                return (
                  <Link
                    key={item.href}
                    href={item.href}
                    className={cn(
                      "flex items-center justify-between px-6 py-4 rounded-2xl transition-all duration-300 group",
                      isActive 
                        ? "bg-primary text-primary-foreground shadow-lg shadow-primary/25" 
                        : "text-slate-500 hover:bg-slate-50 hover:text-slate-900"
                    )}
                  >
                    <div className="flex items-center gap-3">
                      <item.icon className={cn("h-5 w-5", isActive ? "" : "opacity-50 group-hover:opacity-100")} />
                      <span className="font-bold">{item.label}</span>
                    </div>
                    <ChevronRight className={cn(
                      "h-4 w-4 transition-transform", 
                      isActive ? "opacity-100" : "opacity-0 group-hover:opacity-40 group-hover:translate-x-1"
                    )} />
                  </Link>
                );
              })}
            </nav>

            <div className="mt-10 px-6 py-8 bg-slate-50/50 rounded-[2rem] border border-slate-100">
               <div className="flex items-center gap-2 mb-2">
                  <BarChart3 className="h-4 w-4 text-emerald-400" />
                  <span className="text-[10px] font-black text-slate-500 uppercase">Status</span>
               </div>
               <div className="text-xs text-slate-400 leading-relaxed font-medium">
                  当前环境：开发模式<br/>
                  最后同步：刚刚
               </div>
            </div>
          </div>
        </div>
      </aside>

      {/* Main Admin Content */}
      <main className="flex-1 min-w-0">
        {children}
      </main>
    </div>
  );
}
