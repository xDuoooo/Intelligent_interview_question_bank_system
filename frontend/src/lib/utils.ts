import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

/**
 * 校验图片路径是否合法 (Next.js Image 要求)
 * @param src
 * @param fallback
 */
export function validateImageSrc(src?: string, fallback: string = "/assets/logo.png") {
  if (!src) return fallback;
  // Next.js Image 要求相对路径必须以 / 开头，或为绝对路径
  if (src.startsWith("/") || src.startsWith("http://") || src.startsWith("https://")) {
    return src;
  }
  return fallback;
}

function parseDateValue(value?: string | number | Date | null) {
  if (!value) {
    return null;
  }
  const date = value instanceof Date ? value : new Date(value);
  return Number.isNaN(date.getTime()) ? null : date;
}

/**
 * 统一格式化日期时间
 */
export function formatDateTime(value?: string | number | Date | null, fallback = "-") {
  const date = parseDateValue(value);
  if (!date) {
    return fallback;
  }
  return new Intl.DateTimeFormat("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  })
    .format(date)
    .replace(/\//g, "-");
}

/**
 * 统一格式化日期
 */
export function formatDate(value?: string | number | Date | null, fallback = "-") {
  const date = parseDateValue(value);
  if (!date) {
    return fallback;
  }
  return new Intl.DateTimeFormat("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  })
    .format(date)
    .replace(/\//g, "-");
}
