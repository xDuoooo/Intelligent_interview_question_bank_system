import Image from "next/image";
import { cn, validateImageSrc } from "@/lib/utils";

interface Props {
  src?: string | null;
  name?: string;
  size?: number;
  className?: string;
}

/**
 * 统一用户头像组件
 */
export default function UserAvatar({ src, name, size = 40, className }: Props) {
  const validSrc = validateImageSrc(src || undefined, "");

  if (validSrc) {
    return (
      <div
        style={{ width: size, height: size }}
        className={cn(
          "shrink-0 overflow-hidden rounded-full ring-2 ring-white shadow-sm",
          className,
        )}
      >
        <Image
          src={validSrc}
          width={size}
          height={size}
          alt={name || "用户头像"}
          className="h-full w-full object-cover"
        />
      </div>
    );
  }

  return (
    <div
      style={{ width: size, height: size }}
      className={cn(
        "relative flex shrink-0 items-center justify-center overflow-hidden rounded-full bg-white ring-2 ring-white shadow-sm",
        className,
      )}
    >
      <div className="absolute inset-0 bg-gradient-to-br from-primary/6 to-blue-100/70" />
      <div
        className="relative flex items-center justify-center rounded-full bg-white/90 shadow-inner"
        style={{ width: size * 0.72, height: size * 0.72 }}
      >
        <Image
          src="/assets/logo.png"
          width={Math.round(size * 0.42)}
          height={Math.round(size * 0.42)}
          alt={name || "默认头像"}
          className="object-contain"
        />
      </div>
    </div>
  );
}
