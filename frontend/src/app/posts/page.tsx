import { headers } from "next/headers";
import Link from "next/link";
import { listMyPostVoByPageUsingPost, listPostVoByPageUsingPost } from "@/api/postController";
import PostList from "@/components/PostList";
import { FilePlus2 } from "lucide-react";

export const dynamic = "force-dynamic";

export default async function PostsPage() {
  const cookie = headers().get("cookie") || "";
  let postList: API.PostVO[] = [];
  let myPostList: API.PostVO[] = [];

  const [postListResult, myPostListResult] = await Promise.allSettled([
    listPostVoByPageUsingPost(
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
    ),
    listMyPostVoByPageUsingPost(
      {
        current: 1,
        pageSize: 6,
        sortField: "createTime",
        sortOrder: "descend",
      },
      {
        headers: {
          cookie,
        },
      },
    ),
  ]);

  if (postListResult.status === "fulfilled") {
    postList = postListResult.value.data?.records || [];
  } else {
    console.error("获取帖子列表失败", postListResult.reason);
  }

  if (myPostListResult.status === "fulfilled") {
    myPostList = myPostListResult.value.data?.records || [];
  } else {
    console.error("获取我的帖子失败", myPostListResult.reason);
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

      {myPostList.length ? (
        <section className="space-y-6">
          <div className="flex items-end justify-between">
            <div>
              <div className="text-xs font-black uppercase tracking-[0.2em] text-primary">My Community</div>
              <h2 className="mt-2 text-3xl font-black tracking-tight text-slate-900">我的社区投稿</h2>
              <p className="mt-2 text-sm leading-7 text-slate-500">
                这里会展示你最近发布的帖子，包括待审核和已驳回内容，方便你直接回看和修改。
              </p>
            </div>
            <Link href="/user/center?tab=posts" className="text-sm font-black text-slate-400 transition-colors hover:text-primary">
              去个人中心查看全部
            </Link>
          </div>
          <PostList postList={myPostList} />
        </section>
      ) : null}

      <PostList postList={postList} />
    </div>
  );
}
