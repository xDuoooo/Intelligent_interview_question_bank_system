import { headers } from "next/headers";
import Link from "next/link";
import { searchPostVoByPageUsingPost } from "@/api/postController";
import PostList from "@/components/PostList";
import { FilePlus2 } from "lucide-react";

export const dynamic = "force-dynamic";

export default async function PostsPage() {
  const cookie = headers().get("cookie") || "";
  let postList: API.PostVO[] = [];
  try {
    const res = await searchPostVoByPageUsingPost(
      {
        current: 1,
        pageSize: 12,
        sortField: "createTime",
        sortOrder: "descend",
      },
      {
        headers: {
          cookie,
        },
      },
    );
    postList = res.data?.records || [];
  } catch (error) {
    console.error("获取帖子列表失败", error);
  }

  return (
    <div className="max-width-content space-y-10">
      <section className="rounded-[3rem] border border-slate-100 bg-white px-8 py-12 shadow-2xl shadow-slate-200/40">
        <div className="flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">
          <div className="space-y-4">
            <div className="text-xs font-black uppercase tracking-[0.2em] text-primary">Community</div>
            <h1 className="text-4xl font-black tracking-tight text-slate-900">经验交流社区</h1>
            <p className="max-w-3xl text-base leading-8 text-slate-500">
              这里聚合了面试经验、系统设计思路、项目复盘和学习心得。你可以把它当成题库之外的补充认知层。
            </p>
          </div>
          <Link
            href="/posts/create"
            className="inline-flex items-center gap-2 rounded-2xl bg-primary px-6 py-3 text-sm font-black text-primary-foreground shadow-xl shadow-primary/20 transition-all hover:bg-primary/90"
          >
            <FilePlus2 className="h-4 w-4" />
            发布经验帖
          </Link>
        </div>
      </section>
      <PostList postList={postList} />
    </div>
  );
}
