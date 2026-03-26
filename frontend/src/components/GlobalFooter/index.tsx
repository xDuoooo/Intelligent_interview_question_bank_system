import React from "react";
import { Globe, Mail, Bell, Sparkles } from "lucide-react";

export default function GlobalFooter() {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="w-full bg-slate-50 border-t py-12 px-4 sm:px-6 lg:px-8 mt-auto">
      <div className="container mx-auto max-w-7xl">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-10">
          <div className="col-span-1 md:col-span-1 border-b md:border-b-0 pb-6 md:pb-0">
             <div className="flex items-center gap-2 mb-4">
              <div className="h-8 w-8 rounded-lg bg-primary/10 flex items-center justify-center p-1 shadow-inner">
                <Sparkles className="h-5 w-5 text-primary" />
              </div>
              <span className="text-xl font-black tracking-tight bg-gradient-to-r from-primary to-blue-600 bg-clip-text text-transparent">
                智面
              </span>
            </div>
            <p className="text-sm text-muted-foreground leading-relaxed italic pr-4">
              全方位面试官视角，深度技术解析，助你锁定大厂名额。
            </p>
          </div>

          <div className="grid grid-cols-2 col-span-1 md:col-span-2 gap-8">
            <div className="space-y-4">
              <h4 className="text-sm font-bold uppercase tracking-widest text-foreground">快速链接</h4>
              <ul className="space-y-2">
                <li><a href="/banks" className="text-sm text-muted-foreground hover:text-primary transition-colors">热门题库</a></li>
                <li><a href="/questions" className="text-sm text-muted-foreground hover:text-primary transition-colors">题目列表</a></li>
                <li><a href="/mockInterview" className="text-sm text-muted-foreground hover:text-primary transition-colors">模拟面试</a></li>
                <li><a href="/roadmap" className="text-sm text-muted-foreground hover:text-primary transition-colors">学习路线</a></li>
              </ul>
            </div>
            <div className="space-y-4">
              <h4 className="text-sm font-bold uppercase tracking-widest text-foreground">关于我们</h4>
              <ul className="space-y-2">
                <li><a href="/about" className="text-sm text-muted-foreground hover:text-primary transition-colors">平台简介</a></li>
                <li><a href="/contact" className="text-sm text-muted-foreground hover:text-primary transition-colors">联系合作</a></li>
                <li><a href="/privacy" className="text-sm text-muted-foreground hover:text-primary transition-colors">隐私政策</a></li>
              </ul>
            </div>
          </div>

          <div className="space-y-6">
            <h4 className="text-sm font-bold uppercase tracking-widest text-foreground">关注我们</h4>
            <div className="flex items-center gap-4">
              <a href="#" className="h-10 w-10 rounded-full bg-white border flex items-center justify-center hover:text-primary hover:border-primary transition-all shadow-sm"><Bell className="h-5 w-5" /></a>
              <a href="#" className="h-10 w-10 rounded-full bg-white border flex items-center justify-center hover:text-primary hover:border-primary transition-all shadow-sm"><Mail className="h-5 w-5" /></a>
            </div>
          </div>
        </div>

        <div className="mt-12 pt-8 border-t flex flex-col md:flex-row justify-between items-center gap-4">
          <p className="text-xs text-muted-foreground font-medium">
            © {currentYear} 智面 · 智能面试刷题平台
          </p>
          <div className="flex items-center gap-6">
             <span className="text-[10px] text-muted-foreground px-2 py-0.5 rounded border border-muted-foreground/20">京ICP备XXXXXXXX号-1</span>
          </div>
        </div>
      </div>
    </footer>
  );
}
