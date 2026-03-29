"use client";

import { useEffect, useState } from "react";
import { getMyPostVoByIdUsingGet, getPostVoByIdUsingGet, listRelatedPostUsingGet } from "@/api/postController";
import PostDetailContent from "@/components/PostDetailContent";

interface Props {
  postId: string | number;
  initialPost?: API.PostVO;
  initialRelatedPostList?: API.PostVO[];
}

export default function PostDetailClient({
  postId,
  initialPost,
  initialRelatedPostList = [],
}: Props) {
  const [post, setPost] = useState<API.PostVO | undefined>(initialPost);
  const [relatedPostList, setRelatedPostList] = useState<API.PostVO[]>(initialRelatedPostList);
  const [loading, setLoading] = useState(!initialPost);

  useEffect(() => {
    if (initialPost?.id) {
      return;
    }
    let mounted = true;
    setLoading(true);
    Promise.allSettled([getPostVoByIdUsingGet({ id: postId }), listRelatedPostUsingGet({ postId, size: 4 })])
      .then(async ([postResult, relatedResult]) => {
        if (!mounted) {
          return;
        }
        if (postResult.status === "fulfilled") {
          setPost(postResult.value.data);
        } else {
          try {
            const myPostResult = await getMyPostVoByIdUsingGet({ id: postId });
            if (mounted) {
              setPost(myPostResult.data);
            }
          } catch {
            if (mounted) {
              setPost(undefined);
            }
          }
        }
        if (relatedResult.status === "fulfilled") {
          setRelatedPostList(relatedResult.value.data || []);
        }
      })
      .finally(() => {
        if (mounted) {
          setLoading(false);
        }
      });
    return () => {
      mounted = false;
    };
  }, [initialPost, postId]);

  if (post?.id) {
    return <PostDetailContent post={post} relatedPostList={relatedPostList} />;
  }

  return (
    <div className="max-width-content rounded-[2rem] border border-slate-100 bg-white px-8 py-16 text-center text-slate-400 shadow-xl shadow-slate-200/30">
      {loading ? "正在加载帖子内容..." : "这篇帖子不存在，或者暂时无法访问。"}
    </div>
  );
}
