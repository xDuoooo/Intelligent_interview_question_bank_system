export type AppLink = {
  label: string;
  href: string;
  external?: boolean;
};

export type AppLinkGroup = {
  title: string;
  links: AppLink[];
};

export const APP_CONFIG = {
  brand: {
    name: "智面",
    englishName: "IntelliFace",
    displayName: "智面 IntelliFace",
    fullTitle: "智面 (IntelliFace) - 智能面试题库系统",
    systemName: "智能面试题库系统",
    description: "全方位面试官视角，深度技术解析，助你锁定大厂名额。",
    shortDescription:
      "专业的智能面试题库系统，提供全方位面试官视角与深度技术解析，助您在求职道路上稳操胜券。",
  },
  home: {
    heroBadge: "2026 真题库已同步更新",
    heroDescription:
      "全方位真题解析，沉浸式刷题体验。助你精准攻克面试难题，从此大厂 Offer 触手可及。",
  },
  auth: {
    loginActionText: "进入智面",
    firstLoginHint: "首次使用验证码登录将自动创建账号",
    socialLoginTitle: "第三方登录",
  },
  footer: {
    statusText: "System Status: Operational",
    notesTitle: "我的学习笔记",
    notesDescription: ["公众号", "扫描上方二维码，", "即可前往查看。"],
    githubUrl: "https://github.com/xDuoooo",
    githubLabel: "View GitHub",
    icp: "京ICP备XXXXXXXX号-1",
  },
  adminDefaults: {
    siteName: "IntelliFace 智面",
    seoKeywords: "面试, 刷题, Java, 互联网",
    announcement: "欢迎来到智面 1.0 版本，体验 AI 智能面经！",
  },
} as const;

export const FOOTER_LINK_GROUPS: AppLinkGroup[] = [
  {
    title: "核心产品",
    links: [
      { label: "热门题库", href: "/banks" },
      { label: "精选试题", href: "/questions" },
      { label: "经验社区", href: "/posts" },
      { label: "AI 模拟面试", href: "/mockInterview" },
      { label: "个人成长中心", href: "/user/center" },
    ],
  },
  {
    title: "快速入口",
    links: [
      { label: "登录入口", href: "/user/login" },
      { label: "我的通知", href: "/user/notifications" },
      { label: "发起模拟面试", href: "/mockInterview/add" },
    ],
  },
];
