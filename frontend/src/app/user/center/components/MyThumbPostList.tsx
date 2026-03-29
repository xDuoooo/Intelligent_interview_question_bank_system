import React, { useCallback, useEffect, useState } from "react";
import { CalendarClock, Heart, ThumbsUp } from "lucide-react";
import { Empty, Pagination, Spin, Tag, message } from "antd";
import Link from "next/link";
import { listMyThumbPostByPageUsingPost } from "@/api/postThumbController";

export default function MyThumbPostList() {
  const [postList, setPostList] = useState<API.PostVO[]>([]);
  const [current, setCurrent] = useState(1);
  const [pageSize] = useState(6);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);

  const fetchPostList = useCallback(async (page = current) => {
    setLoading(true);
    try {
      const res = await listMyThumbPostByPageUsingPost({
        current: page,
        pageSize,
        sortField: "updateTime",
        sortOrder: "descend",
      });
      setPostList(res.data?.records || []);
      setTotal(Number(res.data?.total || 0));
      setCurrent(page);
    } catch (error: any) {
      message.error(error?.message || "获取点赞帖子失败");
    } finally {
      setLoading(false);
    }
  }, [current, pageSize]);

  useEffect(() => {
    void fetchPostList(1);
  }, [fetchPostList]);

  return (
    <Spin spinning={loading}>
      {postList.length ? (
        <div className="space-y-5">
          {postList.map((item) => (
            <div
              key={String(item.id)}
              className="rounded-[1.75rem] border border-slate-100 bg-white p-6 shadow-sm shadow-slate-100"
            >
              <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                <div className="min-w-0 flex-1">
                  <div className="mb-3 flex flex-wrap gap-2">
                    {(item.tagList || []).slice(0, 4).map((tag) => (
                      <Tag key={tag} className="m-0 rounded-full border-slate-200 bg-slate-50 px-3 py-1">
                        {tag}
                      </Tag>
                    ))}
                  </div>
                  <Link
                    href={`/post/${item.id}`}
                    className="block text-xl font-black leading-8 text-slate-900 transition-colors hover:text-primary"
                  >
                    {item.title}
                  </Link>
                  <div className="mt-3 flex flex-wrap items-center gap-4 text-sm text-slate-400">
                    <span className="inline-flex items-center gap-1.5">
                      <CalendarClock size={14} />
                      {item.updateTime ? new Date(item.updateTime).toLocaleString("zh-CN") : "刚刚"}
                    </span>
                    <span className="inline-flex items-center gap-1.5">
                      <ThumbsUp size={14} />
                      点赞 {item.thumbNum || 0}
                    </span>
                    <span className="inline-flex items-center gap-1.5">
                      <Heart size={14} />
                      收藏 {item.favourNum || 0}
                    </span>
                  </div>
                </div>
                <div className="rounded-2xl border border-blue-100 bg-blue-50 px-4 py-3 text-sm font-bold text-blue-600">
                  已点赞
                </div>
              </div>
            </div>
          ))}

          <div className="flex justify-end pt-2">
            <Pagination
              current={current}
              pageSize={pageSize}
              total={total}
              showSizeChanger={false}
              onChange={(page) => {
                void fetchPostList(page);
              }}
            />
          </div>
        </div>
      ) : (
        <div className="rounded-[2rem] border border-dashed border-slate-200 bg-slate-50/60 px-8 py-14 text-center">
          <Empty
            image={Empty.PRESENTED_IMAGE_SIMPLE}
            description={
              <div className="space-y-2">
                <div className="text-base font-bold text-slate-700">还没有点赞过帖子</div>
                <div className="text-sm text-slate-400">看到有共鸣的内容，点个赞也会保留你的社区足迹。</div>
              </div>
            }
          />
          <Link
            href="/posts"
            className="mt-4 inline-flex items-center justify-center rounded-2xl bg-primary px-6 py-3 font-black text-white transition hover:opacity-90"
          >
            去社区逛逛
          </Link>
        </div>
      )}
    </Spin>
  );
}
