import { Metadata } from "next";
import BasicLayout from "@/layouts/BasicLayout";
import React from "react";
import AccessLayout from "@/access/AccessLayout";
import ClientLayout from "./ClientLayout";
import { APP_CONFIG } from "@/config/appConfig";
import "./globals.css";

export const metadata: Metadata = {
  title: APP_CONFIG.brand.fullTitle,
  description: APP_CONFIG.brand.description,
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
