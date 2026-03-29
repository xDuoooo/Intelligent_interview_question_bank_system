import { headers } from "next/headers";
import Link from "next/link";
import { getPostVoByIdUsingGet, listRelatedPostUsingGet } from "@/api/postController";
import MdViewer from "@/components/MdViewer";
import PostActionBar from "@/components/PostActionBar";
import PostList from "@/components/PostList";
import UserAvatar from "@/components/UserAvatar";
import UserProfileHoverCard from "@/components/UserProfileHoverCard";
import { CalendarClock, Heart, ThumbsUp } from "lucide-react";
import { Tag } from "antd";

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

  if (!post?.id) {
    return (
      <div className="max-width-content rounded-[2rem] border border-slate-100 bg-white px-8 py-16 text-center text-slate-400 shadow-xl shadow-slate-200/30">
        这篇帖子不存在，或者暂时无法访问。
      </div>
    );
  }

  const authorCard = post.user ? (
    <div className="inline-flex items-center gap-3 rounded-2xl border border-slate-100 bg-slate-50/80 px-4 py-3">
      <UserAvatar src={post.user.userAvatar} name={post.user.userName} size={38} />
      <div className="min-w-0 text-left">
        <div className="truncate text-sm font-black text-slate-800">{post.user.userName || "匿名用户"}</div>
        <div className="mt-1 text-xs text-slate-400">经验分享者</div>
      </div>
    </div>
  ) : null;

  return (
    <div className="max-width-content space-y-10">
      <section className="rounded-[2.5rem] border border-slate-100 bg-white px-8 py-10 shadow-2xl shadow-slate-200/40">
        <div className="space-y-6">
          <div className="space-y-4">
            <div className="flex flex-wrap items-center gap-3">
              <Tag color="blue" className="rounded-full px-3 py-1">经验帖</Tag>
              {post.tagList?.map((tag) => (
                <Tag key={tag} className="rounded-full px-3 py-1">{tag}</Tag>
              ))}
            </div>
            <h1 className="text-4xl font-black tracking-tight text-slate-900">{post.title}</h1>
          </div>

          <div className="flex flex-wrap items-center justify-between gap-4 border-b border-slate-100 pb-6">
            {post.user?.id ? (
              <UserProfileHoverCard user={post.user} placement="bottomLeft">
                {authorCard}
              </UserProfileHoverCard>
            ) : authorCard}
            <div className="flex flex-wrap items-center gap-4 text-sm text-slate-400">
              <span className="inline-flex items-center gap-1">
                <CalendarClock className="h-4 w-4" />
                {post.createTime ? new Date(post.createTime).toLocaleString("zh-CN") : "刚刚"}
              </span>
              <span className="inline-flex items-center gap-1">
                <ThumbsUp className="h-4 w-4" />
                {post.thumbNum || 0}
              </span>
              <span className="inline-flex items-center gap-1">
                <Heart className="h-4 w-4" />
                {post.favourNum || 0}
              </span>
            </div>
          </div>

          <PostActionBar
            postId={Number(post.id)}
            initialThumbNum={post.thumbNum || 0}
            initialFavourNum={post.favourNum || 0}
            initialHasThumb={!!post.hasThumb}
            initialHasFavour={!!post.hasFavour}
          />

          <div className="prose prose-slate max-w-none prose-headings:font-black prose-headings:text-slate-900">
            <MdViewer value={post.content} />
          </div>
        </div>
      </section>

      {relatedPostList.length ? (
        <section className="space-y-6">
          <div className="flex items-end justify-between">
            <div>
              <div className="text-xs font-black uppercase tracking-[0.2em] text-primary">Related Reading</div>
              <h2 className="mt-2 text-3xl font-black tracking-tight text-slate-900">相关帖子</h2>
            </div>
            <Link href="/posts" className="text-sm font-black text-slate-400 transition-colors hover:text-primary">
              查看更多帖子
            </Link>
          </div>
          <PostList postList={relatedPostList} />
        </section>
      ) : null}
    </div>
  );
}
