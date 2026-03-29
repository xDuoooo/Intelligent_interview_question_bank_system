import { headers } from "next/headers";
import { getPostVoByIdUsingGet, listRelatedPostUsingGet } from "@/api/postController";
import PostDetailClient from "./PostDetailClient";

export const dynamic = "force-dynamic";

export default async function PostDetailPage({ params }: { params: { postId: string } }) {
  const cookie = headers().get("cookie") || "";
  const postId = Number(params.postId);
  let post: API.PostVO | undefined;
  let relatedPostList: API.PostVO[] = [];

  try {
    const [postRes, relatedRes] = await Promise.all([
      getPostVoByIdUsingGet(
        { id: postId },
        { headers: { cookie } },
      ),
      listRelatedPostUsingGet(
        { postId, size: 4 },
        { headers: { cookie } },
      ),
    ]);
    post = postRes.data;
    relatedPostList = relatedRes.data || [];
  } catch (error) {
    console.error("获取帖子详情失败", error);
  }

  return <PostDetailClient postId={postId} initialPost={post} initialRelatedPostList={relatedPostList} />;
}
