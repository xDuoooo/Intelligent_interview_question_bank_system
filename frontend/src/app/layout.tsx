import { Metadata } from "next";
import BasicLayout from "@/layouts/BasicLayout";
import React from "react";
import AccessLayout from "@/access/AccessLayout";
import ClientLayout from "./ClientLayout";
import "./globals.css";

export const metadata: Metadata = {
  title: "智面 (IntelliFace) - 智能面试题库系统",
  description: "全方位面试官视角，深度技术解析，助你锁定大厂名额。",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="zh">
      <body>
        <ClientLayout>
          <BasicLayout>
            <AccessLayout>{children}</AccessLayout>
          </BasicLayout>
        </ClientLayout>
      </body>
    </html>
  );
}
