"use client";
import React from "react";
import { usePathname } from "next/navigation";
import { useSelector } from "react-redux";
import { RootState } from "@/stores";
import { findAllMenuItemByPath } from "../../config/menu";
import ACCESS_ENUM from "@/access/accessEnum";
import checkAccess from "@/access/checkAccess";
import Forbidden from "@/app/forbidden";
import { useAuthInitialized } from "@/contexts/AuthInitContext";

/**
 * 统一权限校验拦截器
 * @param children
 * @constructor
 */
const AccessLayout: React.FC<
  Readonly<{
    children: React.ReactNode;
  }>
> = ({ children }) => {
  const pathname = usePathname() ?? "/";
  const authInitialized = useAuthInitialized();
  // 当前登录用户
  const loginUser = useSelector((state: RootState) => state.loginUser);
  // 获取当前路径需要的权限
  const menu = findAllMenuItemByPath(pathname);
  const needAccess = menu?.access ?? ACCESS_ENUM.NOT_LOGIN;
  if (!authInitialized && needAccess !== ACCESS_ENUM.NOT_LOGIN) {
    return (
      <div className="max-width-content py-20 text-center text-slate-400">
        正在校验登录状态...
      </div>
    );
  }
  // 校验权限
  const canAccess = checkAccess(loginUser, needAccess);
  if (!canAccess) {
    return <Forbidden />;
  }
  return children;
};

export default AccessLayout;
