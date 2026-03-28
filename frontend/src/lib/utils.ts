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
