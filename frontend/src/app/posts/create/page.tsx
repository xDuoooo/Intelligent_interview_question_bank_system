"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { message } from "antd";
import { ArrowLeft, Sparkles } from "lucide-react";
import { addPostUsingPost } from "@/api/postController";
import PostEditorForm from "@/components/PostEditorForm";

export default function CreatePostPage() {
  const router = useRouter();
  const [submitting, setSubmitting] = useState(false);

  return (
    <div className="max-width-content space-y-8">
      <section className="rounded-[2.5rem] border border-slate-100 bg-white px-8 py-10 shadow-2xl shadow-slate-200/40">
        <div className="space-y-4">
          <Link
            href="/posts"
            className="inline-flex items-center gap-2 text-sm font-black text-slate-400 transition-colors hover:text-primary"
          >
            <ArrowLeft className="h-4 w-4" />
            返回经验社区
          </Link>
          <div className="inline-flex items-center gap-2 rounded-full border border-primary/10 bg-primary/5 px-4 py-2 text-xs font-black uppercase tracking-[0.2em] text-primary">
            <Sparkles className="h-4 w-4" />
            Share Experience
          </div>
          <h1 className="text-4xl font-black tracking-tight text-slate-900">发布经验帖</h1>
          <p className="max-w-3xl text-base leading-8 text-slate-500">
            把你的项目复盘、面试追问、系统设计思路或者刷题心得沉淀下来，让它真正帮助到后来的人。
          </p>
        </div>
      </section>

      <section className="rounded-[2.5rem] border border-slate-100 bg-white px-6 py-8 shadow-xl shadow-slate-200/30 sm:px-8">
        <PostEditorForm
          submitText="发布帖子"
          submitting={submitting}
          onSubmit={async (values) => {
            setSubmitting(true);
            try {
              const res = await addPostUsingPost(values);
              message.success("帖子发布成功");
              if (res.data) {
                router.push(`/post/${res.data}`);
              } else {
                router.push("/posts");
              }
            } catch (error: any) {
              message.error(error?.message || "发布失败");
            } finally {
              setSubmitting(false);
            }
          }}
        />
      </section>
    </div>
  );
}
