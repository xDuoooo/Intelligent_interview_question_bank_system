import Link from "next/link";
import { listMyQuestionBankVoByPageUsingPost, listQuestionBankVoByPageUsingPost } from "@/api/questionBankController";
import { getLoginUserUsingGet } from "@/api/userController";
import {
  QUESTION_REVIEW_STATUS_ENUM,
} from "@/constants/question";
import { Compass } from "lucide-react";
import BanksExplorer from "./components/BanksExplorer";
import QuestionBankList from "@/components/QuestionBankList";
import { buildServerRequestOptions, type ServerRequestOptions } from "@/libs/serverRequestOptions";

export const dynamic = "force-dynamic";

const MY_DRAFT_BANK_SECTIONS = [
  {
    status: QUESTION_REVIEW_STATUS_ENUM.PRIVATE,
    title: "我保存的私有题库",
    description: "这些题库只对你自己和管理员可见，可以先慢慢整理结构和内容。",
  },
  {
    status: QUESTION_REVIEW_STATUS_ENUM.PENDING,
    title: "我提交审核中的题库",
    description: "这些题库已经在审核队列中，通过后才会进入公开题库列表。",
  },
  {
    status: QUESTION_REVIEW_STATUS_ENUM.REJECTED,
    title: "我被驳回的题库",
    description: "你可以根据审核意见继续修改题库，再决定何时重新提交公开。",
  },
] as const;

async function loadMyDraftQuestionBanks(requestOptions: ServerRequestOptions) {
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
  const requestOptions = buildServerRequestOptions();
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
          reviewStatus: QUESTION_REVIEW_STATUS_ENUM.APPROVED,
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
        <section className="rounded-[3rem] border border-white bg-white/50 p-6 shadow-2xl shadow-slate-200/50 backdrop-blur-sm sm:p-10">
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

          <div className="mt-8 space-y-8">
            {myDraftSections.map((section) => (
              <div
                key={section.status}
                className="space-y-4 rounded-[2rem] border border-slate-100 bg-white/70 p-5 shadow-sm shadow-slate-200/40"
              >
                <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
                  <div>
                    <div className="text-lg font-black text-slate-900">{section.title}</div>
                    <div className="mt-1 text-sm font-medium leading-6 text-slate-500">{section.description}</div>
                  </div>
                  <div className="rounded-2xl bg-slate-50 px-4 py-3 text-center ring-1 ring-slate-100">
                    <div className="text-lg font-black text-slate-900">{section.total}</div>
                    <div className="text-[11px] font-semibold uppercase tracking-wide text-slate-400">当前数量</div>
                  </div>
                </div>
                <QuestionBankList
                  questionBankList={section.records}
                  showReviewStatus
                  showUpdateTime
                />
              </div>
            ))}
          </div>
        </section>
      ) : null}

      <BanksExplorer initialQuestionBankList={questionBankList} initialTotal={total} initialPageSize={pageSize} />
    </div>
  );
}
