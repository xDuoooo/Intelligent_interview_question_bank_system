import React from "react";
import { Globe, Mail, Bell, Sparkles } from "lucide-react";

export default function GlobalFooter() {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="w-full bg-slate-50/80 border-t py-14 px-4 sm:px-6 lg:px-8 mt-auto backdrop-blur-sm">
      <div className="container mx-auto max-w-7xl">
        <div className="grid grid-cols-1 md:grid-cols-4 lg:grid-cols-5 gap-12 lg:gap-8">
          <div className="col-span-1 md:col-span-1 lg:col-span-2">
             <div className="flex items-center gap-2 mb-6">
              <div className="h-9 w-9 rounded-xl bg-primary/10 flex items-center justify-center p-1.5 shadow-sm border border-primary/10">
                <Sparkles className="h-5 w-5 text-primary" />
              </div>
              <span className="text-2xl font-black tracking-tighter bg-gradient-to-br from-slate-900 to-slate-600 bg-clip-text text-transparent">
                智面 <span className="text-primary font-medium tracking-normal text-sm ml-1 opacity-80">IntelliFace</span>
              </span>
            </div>
            <p className="text-sm text-slate-500 leading-relaxed font-medium max-w-xs mb-6">
              专业的智能面试题库系统，提供全方位面试官视角与深度技术解析，助您在求职道路上稳操胜券。
            </p>
            <div className="flex items-center gap-3">
               <div className="h-1.5 w-1.5 rounded-full bg-green-500 animate-pulse" />
               <span className="text-[11px] font-bold text-slate-400 uppercase tracking-widest">System Status: Operational</span>
            </div>
          </div>

          <div className="grid grid-cols-2 col-span-1 md:col-span-2 gap-8">
            <div className="space-y-5">
              <h4 className="text-xs font-black uppercase tracking-[0.2em] text-slate-900/40">核心产品</h4>
              <ul className="space-y-3">
                <li><a href="/banks" className="text-sm font-semibold text-slate-600 hover:text-primary transition-all hover:translate-x-1 inline-block">热门题库</a></li>
                <li><a href="/questions" className="text-sm font-semibold text-slate-600 hover:text-primary transition-all hover:translate-x-1 inline-block">精选试题</a></li>
                <li><a href="/mockInterview" className="text-sm font-semibold text-slate-600 hover:text-primary transition-all hover:translate-x-1 inline-block">AI 模拟面试</a></li>
                <li><a href="/roadmap" className="text-sm font-semibold text-slate-600 hover:text-primary transition-all hover:translate-x-1 inline-block">进阶路线图</a></li>
              </ul>
            </div>
            <div className="space-y-5">
              <h4 className="text-xs font-black uppercase tracking-[0.2em] text-slate-900/40">支持中心</h4>
              <ul className="space-y-3">
                <li><a href="/ai" className="text-sm font-semibold text-slate-600 hover:text-primary transition-all hover:translate-x-1 inline-block">智能求职助手</a></li>
                <li><a href="/community" className="text-sm font-semibold text-slate-600 hover:text-primary transition-all hover:translate-x-1 inline-block">开发者社区</a></li>
                <li><a href="/contact" className="text-sm font-semibold text-slate-600 hover:text-primary transition-all hover:translate-x-1 inline-block">商务合作</a></li>
                <li><a href="/privacy" className="text-sm font-semibold text-slate-600 hover:text-primary transition-all hover:translate-x-1 inline-block">隐私与条款</a></li>
              </ul>
            </div>
          </div>

          <div className="space-y-6">
            <h4 className="text-xs font-black uppercase tracking-[0.2em] text-slate-900/40">移动端访问</h4>
            <div className="bg-white p-2 rounded-2xl border border-slate-200 shadow-sm inline-block group hover:shadow-md transition-shadow">
               <div className="h-24 w-24 bg-slate-50 rounded-xl flex items-center justify-center border border-dashed border-slate-200">
                  <span className="text-[10px] text-slate-400 font-bold group-hover:text-primary transition-colors">小程序 / 公众号</span>
               </div>
            </div>
            <p className="text-[11px] text-slate-400 font-medium leading-normal">
              扫描上方二维码<br />即可通过移动端开启高效学习。
            </p>
          </div>
        </div>

        <div className="mt-16 pt-8 border-t border-slate-200 flex flex-col md:flex-row justify-between items-center gap-6">
          <div className="flex flex-col md:flex-row items-center gap-4 text-center md:text-left">
            <p className="text-xs font-bold text-slate-400">
              © {currentYear} 智面 · 智能面试题库系统
            </p>
            <div className="hidden md:block h-4 w-px bg-slate-200" />
            <span className="text-[10px] font-medium text-slate-400 px-3 py-1 rounded-full bg-slate-100 italic">京ICP备XXXXXXXX号-1</span>
          </div>
          
          <div className="flex items-center gap-4">
             <a href="https://github.com/xduo" target="_blank" className="flex items-center gap-2 text-xs font-bold text-slate-500 hover:text-primary transition-all bg-white border border-slate-200 px-4 py-2 rounded-xl shadow-sm hover:shadow-md active:scale-95">
               <Globe className="h-3.5 w-3.5" />
               View GitHub
             </a>
             <div className="flex items-center gap-2">
               <a href="#" className="h-9 w-9 rounded-xl bg-white border border-slate-200 flex items-center justify-center text-slate-400 hover:text-primary hover:border-primary transition-all shadow-sm"><Mail className="h-4.5 w-4.5" /></a>
               <a href="#" className="h-9 w-9 rounded-xl bg-white border border-slate-200 flex items-center justify-center text-slate-400 hover:text-primary hover:border-primary transition-all shadow-sm"><Bell className="h-4.5 w-4.5" /></a>
             </div>
          </div>
        </div>
      </div>
    </footer>
  );
}
