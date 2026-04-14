import Link from "next/link";
import { listMyQuestionBankVoByPageUsingPost, listQuestionBankVoByPageUsingPost } from "@/api/questionBankController";
import { getLoginUserUsingGet } from "@/api/userController";
import {
  QUESTION_REVIEW_STATUS_ENUM,
  QUESTION_REVIEW_STATUS_TEXT_MAP,
} from "@/constants/question";
import { Compass } from "lucide-react";
import { headers } from "next/headers";
import BanksExplorer from "./components/BanksExplorer";

export const dynamic = "force-dynamic";

const MY_DRAFT_BANK_SECTIONS = [
  {
    status: QUESTION_REVIEW_STATUS_ENUM.PRIVATE,
    title: "我保存的私有题库",
    description: "这些题库只对你自己和管理员可见，可以先慢慢整理结构和内容。",
    accentClassName: "border-slate-200 bg-slate-50/80",
  },
  {
    status: QUESTION_REVIEW_STATUS_ENUM.PENDING,
    title: "我提交审核中的题库",
    description: "这些题库已经在审核队列中，通过后才会进入公开题库列表。",
    accentClassName: "border-amber-200 bg-amber-50/80",
  },
  {
    status: QUESTION_REVIEW_STATUS_ENUM.REJECTED,
    title: "我被驳回的题库",
    description: "你可以根据审核意见继续修改题库，再决定何时重新提交公开。",
    accentClassName: "border-rose-200 bg-rose-50/80",
  },
] as const;

async function loadMyDraftQuestionBanks(requestOptions: { headers: { cookie: string } }) {
  const sectionResults = await Promise.all(
    MY_DRAFT_BANK_SECTIONS.map(async (section) => {
      const res = (await listMyQuestionBankVoByPageUsingPost(
        {
          current: 1,
          pageSize: 3,
          reviewStatus: section.status,
          sortField: "updateTime",
          sortOrder: "descend",
        },
        requestOptions,
      )) as API.BaseResponsePageQuestionBankVO_;
      return {
        ...section,
        records: res.data?.records || [],
        total: Number(res.data?.total) || 0,
      };
    }),
  );
  return sectionResults.filter((section) => section.total > 0);
}

/**
 * 题库列表页面
 * @constructor
 */
export default async function BanksPage() {
  const cookieHeader = headers().get("cookie") || "";
  const requestOptions = {
    headers: {
      cookie: cookieHeader,
    },
  };
  let questionBankList: API.QuestionBankVO[] = [];
  let total = 0;
  let myDraftSections: Array<
    (typeof MY_DRAFT_BANK_SECTIONS)[number] & { records: API.QuestionBankVO[]; total: number }
  > = [];
  const pageSize = 12;

  try {
    const [publicRes, loginRes] = await Promise.allSettled([
      listQuestionBankVoByPageUsingPost(
        {
          pageSize,
          sortField: "createTime",
          sortOrder: "descend",
        },
        requestOptions,
      ),
      getLoginUserUsingGet(requestOptions),
    ]);
    const res =
      publicRes.status === "fulfilled"
        ? (publicRes.value as unknown as API.BaseResponsePageQuestionBankVO_)
        : undefined;
    const records = res?.data?.records ?? [];
    questionBankList = records;
    total = Number(res?.data?.total) || 0;
    if (loginRes.status === "fulfilled" && loginRes.value?.data?.id) {
      myDraftSections = await loadMyDraftQuestionBanks(requestOptions);
    }
  } catch (e) {
    console.error("获取题库列表失败", e);
  }

  return (
    <div id="banksPage" className="space-y-12 pb-20">
       {/* Header Section */}
      <section className="relative overflow-hidden rounded-[3rem] bg-white border border-slate-100 p-8 sm:p-16 text-slate-900 shadow-2xl shadow-slate-200/50">
         <div className="absolute top-0 right-0 -mt-10 -mr-10 h-64 w-64 rounded-full bg-primary/5 blur-3xl opacity-60" />
         <div className="relative z-10 space-y-4">
            <div className="flex items-center gap-2 text-primary font-black uppercase tracking-widest text-sm">
               <Compass className="h-4 w-4" />
               <span>Curated Collections</span>
            </div>
            <h1 className="text-4xl sm:text-6xl font-black tracking-tight text-slate-900">
               面试题库
            </h1>
            <p className="text-lg text-slate-500 font-medium max-w-2xl">
               按领域划分的专业题库，系统化攻克大厂面试知识点。
            </p>
         </div>
      </section>

      {myDraftSections.length ? (
        <section className="rounded-[3rem] border border-blue-100 bg-blue-50/70 p-6 sm:p-8 shadow-xl shadow-blue-100/40">
          <div className="flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between">
            <div className="space-y-2">
              <div className="text-sm font-black uppercase tracking-[0.32em] text-blue-500">
                My Workspace
              </div>
              <h2 className="text-2xl font-black text-slate-900">我自己的未公开题库</h2>
              <p className="max-w-2xl text-sm font-medium text-slate-500">
                公开题库页只展示审核通过的题库。你已登录时，这里会额外展示自己的私有、待审核和已驳回题库，方便继续沉淀和调整。
              </p>
            </div>
            <Link
              href="/user/center?tab=banks"
              className="inline-flex items-center justify-center rounded-2xl bg-slate-900 px-5 py-3 text-sm font-bold text-white transition hover:-translate-y-0.5 hover:bg-slate-800"
            >
              去我的题库管理
            </Link>
          </div>

          <div className="mt-6 grid gap-4 xl:grid-cols-3">
            {myDraftSections.map((section) => (
              <div
                key={section.status}
                className={`rounded-[2rem] border p-5 shadow-sm shadow-slate-200/40 ${section.accentClassName}`}
              >
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <div className="text-base font-black text-slate-900">{section.title}</div>
                    <div className="mt-1 text-sm text-slate-500">{section.description}</div>
                  </div>
                  <div className="rounded-2xl bg-white/90 px-3 py-2 text-center shadow-sm">
                    <div className="text-lg font-black text-slate-900">{section.total}</div>
                    <div className="text-[11px] font-semibold uppercase tracking-wide text-slate-400">当前数量</div>
                  </div>
                </div>

                <div className="mt-4 space-y-3">
                  {section.records.map((bank) => (
                    <Link
                      key={bank.id}
                      href={`/bank/${bank.id}`}
                      className="block rounded-[1.5rem] border border-white/90 bg-white/85 p-4 transition hover:-translate-y-0.5 hover:border-primary/20 hover:shadow-lg hover:shadow-slate-200/50"
                    >
                      <div className="flex items-start justify-between gap-3">
                        <div className="min-w-0 flex-1">
                          <div className="truncate text-sm font-black text-slate-900">{bank.title}</div>
                          <div className="mt-1 text-xs font-medium text-slate-500">
                            {QUESTION_REVIEW_STATUS_TEXT_MAP[Number(bank.reviewStatus ?? QUESTION_REVIEW_STATUS_ENUM.APPROVED)] || "未知状态"}
                            {bank.updateTime ? ` · ${new Date(bank.updateTime).toLocaleDateString("zh-CN")}` : ""}
                          </div>
                        </div>
                      </div>
                      {bank.description ? (
                        <div className="mt-3 line-clamp-2 text-xs font-medium text-slate-500">
                          {bank.description}
                        </div>
                      ) : null}
                      {Number(bank.reviewStatus) === QUESTION_REVIEW_STATUS_ENUM.REJECTED && bank.reviewMessage ? (
                        <div className="mt-3 rounded-2xl bg-rose-50 px-3 py-2 text-xs font-medium text-rose-600">
                          驳回原因：{bank.reviewMessage}
                        </div>
                      ) : null}
                    </Link>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </section>
      ) : null}

      <BanksExplorer initialQuestionBankList={questionBankList} initialTotal={total} initialPageSize={pageSize} />
    </div>
  );
}
