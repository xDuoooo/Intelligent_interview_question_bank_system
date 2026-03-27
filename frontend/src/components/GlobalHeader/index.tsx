"use client";

import React, { useState } from "react";
import Link from "next/link";
import Image from "next/image";
import { usePathname, useRouter } from "next/navigation";
import { useDispatch, useSelector } from "react-redux";
import { AppDispatch, RootState } from "@/stores";
import { setLoginUser } from "@/stores/loginUser";
import { userLogoutUsingPost } from "@/api/userController";
import { DEFAULT_USER } from "@/constants/user";
import ACCESS_ENUM from "@/access/accessEnum";
import getAccessibleMenus from "@/access/menuAccess";
import { menus } from "../../../config/menu";
import { cn } from "@/lib/utils";
import { Search, Menu, X, LogOut, User, Settings, Crown, ChevronDown } from "lucide-react";
import NotificationPopover from "../NotificationPopover";

export default function GlobalHeader() {
  const pathname = usePathname();
  const router = useRouter();
  const dispatch = useDispatch<AppDispatch>();
  const loginUser = useSelector((state: RootState) => state.loginUser);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isUserDropdownOpen, setIsUserDropdownOpen] = useState(false);

  // 获取可访问的菜单
  const accessibleMenus = getAccessibleMenus(loginUser, menus);

  const handleLogout = async () => {
    try {
      await userLogoutUsingPost();
      dispatch(setLoginUser(DEFAULT_USER));
      router.push("/user/login");
    } catch (e) {
      console.error("Logout failed", e);
    }
  };

  return (
    <header className="sticky top-0 z-50 w-full border-b bg-background/80 backdrop-blur-md transition-all">
      <div className="container mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex h-16 items-center justify-between gap-4">
          {/* Logo */}
          <Link href="/" className="flex items-center gap-2 shrink-0 group">
            <div className="relative h-10 w-10 overflow-hidden rounded-xl shadow-md ring-2 ring-slate-50 transform group-hover:scale-105 transition-transform duration-300">
               <div className="absolute inset-0 bg-white flex items-center justify-center p-1.5">
                 <Image
                  src="/assets/logo.png"
                  height={28}
                  width={28}
                  alt="智面"
                  className="object-contain transition-transform duration-500 group-hover:scale-110"
                />
              </div>
            </div>
            <span className="hidden sm:inline-block text-xl font-black tracking-tight bg-gradient-to-r from-primary to-blue-600 bg-clip-text text-transparent">
              智面
            </span>
          </Link>

          {/* Nav Links - Desktop */}
          <nav className="hidden md:flex items-center gap-1">
            {accessibleMenus.map((menu) => (
              <Link
                key={menu.path}
                href={menu.path || "#"}
                target={menu.target}
                className={cn(
                  "px-4 py-2 text-sm font-semibold rounded-full transition-all flex items-center gap-2",
                  pathname === menu.path
                    ? "text-primary bg-primary/10"
                    : "text-muted-foreground hover:text-foreground hover:bg-muted"
                )}
              >
                {menu.icon && <span className="text-current scale-90">{menu.icon}</span>}
                {menu.name}
              </Link>
            ))}
          </nav>

          {/* Right Actions */}
          <div className="flex items-center gap-2 sm:gap-4 ml-auto">
            {/* Search - Desktop */}
            <div className="hidden lg:flex relative group items-center">
              <Search className="absolute left-3 h-4 w-4 text-muted-foreground group-focus-within:text-primary transition-colors" />
              <input
                type="text"
                placeholder="搜索题目..."
                className="h-10 w-48 xl:w-64 rounded-full bg-muted/50 pl-10 pr-4 text-sm outline-none ring-primary/20 transition-all focus:ring-2 focus:w-72 xl:focus:w-80"
                onKeyDown={(e) => {
                  if (e.key === "Enter") {
                    router.push(`/questions?q=${(e.target as HTMLInputElement).value}`);
                  }
                }}
              />
            </div>

            {/* Notification */}
            {loginUser.id && (
              <NotificationPopover />
            )}

            {/* User Profile */}
            {loginUser.id ? (
              <div className="relative">
                <button
                  onClick={() => setIsUserDropdownOpen(!isUserDropdownOpen)}
                  onBlur={() => setTimeout(() => setIsUserDropdownOpen(false), 200)}
                  className="flex items-center gap-2 p-1 pl-2 hover:bg-muted rounded-full transition-all border border-transparent hover:border-border group"
                >
                   <span className="hidden lg:inline-block text-sm font-semibold max-w-[100px] truncate text-muted-foreground group-hover:text-foreground transition-colors">
                    {loginUser.userName || "未命名"}
                  </span>
                  <div className="relative h-8 w-8 rounded-full overflow-hidden ring-2 ring-primary/10 group-hover:ring-primary/30 transition-all shadow-sm">
                    <Image
                      src={loginUser.userAvatar || "/assets/logo.png"}
                      fill
                      alt="Avatar"
                      className="object-cover"
                    />
                  </div>
                  <ChevronDown className={cn("h-4 w-4 text-muted-foreground transition-transform duration-200", isUserDropdownOpen && "rotate-180")} />
                </button>

                {/* Dropdown Menu */}
                {isUserDropdownOpen && (
                  <div className="absolute right-0 mt-2 w-56 rounded-2xl border bg-popover p-2 shadow-xl ring-1 ring-foreground/5 animate-in fade-in zoom-in-95 duration-150 origin-top-right">
                    <div className="px-3 py-2 border-b mb-1">
                      <p className="text-xs font-bold text-muted-foreground uppercase tracking-widest">我的账号</p>
                    </div>
                    <Link href="/user/center" className="flex items-center gap-2 px-3 py-2.5 text-sm font-medium rounded-xl hover:bg-muted transition-colors">
                      <User className="h-4 w-4" /> 个人中心
                    </Link>
                    <button className="w-full flex items-center gap-2 px-3 py-2.5 text-sm font-medium rounded-xl hover:bg-muted transition-colors">
                      < Crown className="h-4 w-4 text-yellow-500" /> 会员权益
                    </button>
                    <button className="w-full flex items-center gap-2 px-3 py-2.5 text-sm font-medium rounded-xl hover:bg-muted transition-colors">
                      <Settings className="h-4 w-4" /> 账号设置
                    </button>
                    {loginUser.userRole === ACCESS_ENUM.ADMIN && (
                      <Link href="/admin" className="flex items-center gap-2 px-3 py-2.5 text-sm font-medium rounded-xl hover:bg-muted text-primary transition-colors">
                        <Crown className="h-4 w-4" /> 后台管理
                      </Link>
                    )}
                    <div className="border-t my-1" />
                    <button
                      onClick={handleLogout}
                      className="w-full flex items-center gap-2 px-3 py-2.5 text-sm font-bold rounded-xl hover:bg-red-50 text-red-500 transition-colors"
                    >
                      <LogOut className="h-4 w-4" /> 退出登录
                    </button>
                  </div>
                )}
              </div>
            ) : (
              <Link
                href="/user/login"
                className="h-10 px-6 rounded-full bg-primary text-primary-foreground font-semibold flex items-center justify-center transition-all shadow-lg shadow-primary/25 hover:scale-105 active:scale-95 tracking-wider"
              >
                登录
              </Link>
            )}

            {/* Mobile Menu Toggle */}
            <button
              onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
              className="md:hidden h-9 w-9 rounded-full flex items-center justify-center hover:bg-muted transition-colors"
            >
              {isMobileMenuOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile Nav Overlay */}
      {isMobileMenuOpen && (
        <div className="md:hidden border-t bg-background animate-in slide-in-from-top duration-300">
          <div className="container mx-auto px-4 py-6 space-y-4">
            {/* Mobile Search */}
            <div className="relative group items-center">
              <Search className="absolute left-3 h-4 w-4 text-muted-foreground" />
              <input
                type="text"
                placeholder="搜索题目..."
                className="h-11 w-full rounded-xl bg-muted/50 pl-10 pr-4 text-sm outline-none border-none"
              />
            </div>
            
            <nav className="flex flex-col gap-1">
              {accessibleMenus.map((menu) => (
                <Link
                  key={menu.path}
                  href={menu.path || "#"}
                  onClick={() => setIsMobileMenuOpen(false)}
                  className={cn(
                    "flex items-center gap-3 px-4 py-3 rounded-xl text-base font-bold transition-all",
                    pathname === menu.path
                      ? "bg-primary/10 text-primary"
                      : "hover:bg-muted text-muted-foreground hover:text-foreground"
                  )}
                >
                  {menu.name}
                </Link>
              ))}
            </nav>
          </div>
        </div>
      )}
    </header>
  );
}
