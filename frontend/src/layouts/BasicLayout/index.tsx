"use client";

import React from "react";
import GlobalHeader from "@/components/GlobalHeader";
import GlobalFooter from "@/components/GlobalFooter";
import "./index.css";

interface Props {
  children: React.ReactNode;
}

/**
 * 全局通用布局
 * @param children
 * @constructor
 */
export default function BasicLayout({ children }: Props) {
  return (
    <div id="basicLayout" className="min-h-screen flex flex-col bg-slate-50/50">
      <GlobalHeader />
      <main className="flex-1 w-full mx-auto max-w-7xl 2xl:max-w-[1600px] px-4 sm:px-6 lg:px-8 py-8">
        <div className="animate-in fade-in slide-in-from-bottom-4 duration-500">
          {children}
        </div>
      </main>
      <GlobalFooter />
    </div>
  );
}

