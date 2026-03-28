import React from "react";
import Image from "next/image";
import Link from "next/link";
import { Globe, Mail, Bell, Sparkles } from "lucide-react";
import { APP_CONFIG, FOOTER_LINK_GROUPS, type AppLink } from "@/config/appConfig";

function FooterLink({ item }: { item: AppLink }) {
  const className =
    "text-sm font-semibold text-slate-600 hover:text-primary transition-all hover:translate-x-1 inline-block";

  if (item.external) {
    return (
      <a href={item.href} target="_blank" rel="noreferrer" className={className}>
        {item.label}
      </a>
    );
  }

  return (
    <Link href={item.href} className={className}>
      {item.label}
    </Link>
  );
}

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
                {APP_CONFIG.brand.name} <span className="text-primary font-medium tracking-normal text-sm ml-1 opacity-80">{APP_CONFIG.brand.englishName}</span>
              </span>
            </div>
            <p className="text-sm text-slate-500 leading-relaxed font-medium max-w-xs mb-6">
              {APP_CONFIG.brand.shortDescription}
            </p>
            <div className="flex items-center gap-3">
              <div className="h-1.5 w-1.5 rounded-full bg-green-500 animate-pulse" />
              <span className="text-[11px] font-bold text-slate-400 uppercase tracking-widest">{APP_CONFIG.footer.statusText}</span>
            </div>
          </div>

          <div className="grid grid-cols-2 col-span-1 md:col-span-2 gap-8">
            {FOOTER_LINK_GROUPS.map((group) => (
              <div key={group.title} className="space-y-5">
                <h4 className="text-xs font-black uppercase tracking-[0.2em] text-slate-900/40">{group.title}</h4>
                <ul className="space-y-3">
                  {group.links.map((item) => (
                    <li key={item.href}>
                      <FooterLink item={item} />
                    </li>
                  ))}
                </ul>
              </div>
            ))}
          </div>

          <div className="space-y-6">
            <h4 className="text-xs font-black uppercase tracking-[0.2em] text-slate-900/40">{APP_CONFIG.footer.notesTitle}</h4>
              <div className="h-28 w-28 bg-white rounded-xl flex items-center justify-center overflow-hidden border border-slate-100 p-1">
                <Image
                  src="/assets/go-notes-qr.png"
                  alt="My Notes QR"
                  width={112}
                  height={112}
                  className="object-contain w-full h-full"
                />
              </div>
            <p className="text-[11px] text-slate-400 font-medium leading-relaxed">
              {APP_CONFIG.footer.notesDescription.map((line) => (
                <React.Fragment key={line}>
                  {line}
                  <br />
                </React.Fragment>
              ))}
            </p>
          </div>
        </div>

        <div className="mt-16 pt-8 border-t border-slate-200 flex flex-col md:flex-row justify-between items-center gap-6">
          <div className="flex flex-col md:flex-row items-center gap-4 text-center md:text-left">
            <p className="text-xs font-bold text-slate-400">
              © {currentYear} {APP_CONFIG.brand.name} · {APP_CONFIG.brand.systemName}
            </p>
            <div className="hidden md:block h-4 w-px bg-slate-200" />
            <span className="text-[10px] font-medium text-slate-400 px-3 py-1 rounded-full bg-slate-100 italic">{APP_CONFIG.footer.icp}</span>
          </div>

          <div className="flex items-center gap-4">
            <a href={APP_CONFIG.footer.githubUrl} target="_blank" rel="noreferrer" className="flex items-center gap-2 text-xs font-bold text-slate-500 hover:text-primary transition-all bg-white border border-slate-200 px-4 py-2 rounded-xl shadow-sm hover:shadow-md active:scale-95">
              <Globe className="h-3.5 w-3.5" />
              {APP_CONFIG.footer.githubLabel}
            </a>
            <div className="flex items-center gap-2">
              <Link href="/user/login" className="h-9 w-9 rounded-xl bg-white border border-slate-200 flex items-center justify-center text-slate-400 hover:text-primary hover:border-primary transition-all shadow-sm" aria-label="登录入口">
                <Mail className="h-4.5 w-4.5" />
              </Link>
              <Link href="/user/notifications" className="h-9 w-9 rounded-xl bg-white border border-slate-200 flex items-center justify-center text-slate-400 hover:text-primary hover:border-primary transition-all shadow-sm" aria-label="消息中心">
                <Bell className="h-4.5 w-4.5" />
              </Link>
            </div>
          </div>
        </div>
      </div>
    </footer>
  );
}
