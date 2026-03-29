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
  const initials = (name || "?").charAt(0).toUpperCase();
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
      style={{ width: size, height: size, fontSize: size * 0.4 }}
      className={cn(
        "flex shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-primary/80 to-blue-500 font-black text-white ring-2 ring-white shadow-sm",
        className,
      )}
    >
      {initials}
    </div>
  );
}
