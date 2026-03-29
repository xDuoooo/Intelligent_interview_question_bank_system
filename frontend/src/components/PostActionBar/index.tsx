"use client";

import React, { useState } from "react";
import { useSelector } from "react-redux";
import { message } from "antd";
import { Heart, ThumbsUp } from "lucide-react";
import { RootState } from "@/stores";
import { doThumbUsingPost } from "@/api/postThumbController";
import { doPostFavourUsingPost } from "@/api/postFavourController";

interface Props {
  postId: number;
  initialThumbNum?: number;
  initialFavourNum?: number;
  initialHasThumb?: boolean;
  initialHasFavour?: boolean;
}

export default function PostActionBar({
  postId,
  initialThumbNum = 0,
  initialFavourNum = 0,
  initialHasThumb = false,
  initialHasFavour = false,
}: Props) {
  const loginUser = useSelector((state: RootState) => state.loginUser);
  const [thumbNum, setThumbNum] = useState(initialThumbNum);
  const [favourNum, setFavourNum] = useState(initialFavourNum);
  const [hasThumb, setHasThumb] = useState(initialHasThumb);
  const [hasFavour, setHasFavour] = useState(initialHasFavour);
  const [thumbLoading, setThumbLoading] = useState(false);
  const [favourLoading, setFavourLoading] = useState(false);

  const ensureLogin = () => {
    if (!loginUser?.id) {
      message.info("登录后才可以点赞或收藏帖子");
      return false;
    }
    return true;
  };

  const handleThumb = async () => {
    if (!ensureLogin() || thumbLoading) {
      return;
    }
    setThumbLoading(true);
    try {
      const res = await doThumbUsingPost({ postId });
      const delta = Number(res.data || 0);
      if (delta !== 0) {
        setThumbNum((prev) => Math.max(0, prev + delta));
        setHasThumb(delta > 0);
      }
    } catch (error: any) {
      message.error(error?.message || "点赞失败");
    } finally {
      setThumbLoading(false);
    }
  };

  const handleFavour = async () => {
    if (!ensureLogin() || favourLoading) {
      return;
    }
    setFavourLoading(true);
    try {
      const res = await doPostFavourUsingPost({ postId });
      const delta = Number(res.data || 0);
      if (delta !== 0) {
        setFavourNum((prev) => Math.max(0, prev + delta));
        setHasFavour(delta > 0);
      }
    } catch (error: any) {
      message.error(error?.message || "收藏失败");
    } finally {
      setFavourLoading(false);
    }
  };

  return (
    <div className="flex flex-wrap items-center gap-3">
      <button
        onClick={handleThumb}
        disabled={thumbLoading}
        className={`inline-flex items-center gap-2 rounded-2xl border px-4 py-2 text-sm font-black transition-all ${
          hasThumb
            ? "border-blue-200 bg-blue-50 text-blue-600"
            : "border-slate-200 bg-white text-slate-500 hover:border-blue-200 hover:text-blue-600"
        }`}
      >
        <ThumbsUp className="h-4 w-4" />
        点赞 {thumbNum}
      </button>
      <button
        onClick={handleFavour}
        disabled={favourLoading}
        className={`inline-flex items-center gap-2 rounded-2xl border px-4 py-2 text-sm font-black transition-all ${
          hasFavour
            ? "border-rose-200 bg-rose-50 text-rose-600"
            : "border-slate-200 bg-white text-slate-500 hover:border-rose-200 hover:text-rose-600"
        }`}
      >
        <Heart className="h-4 w-4" />
        收藏 {favourNum}
      </button>
    </div>
  );
}
