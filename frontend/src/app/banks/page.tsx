import { listQuestionBankVoByPageUsingPost } from "@/api/questionBankController";
import { Compass } from "lucide-react";
import { headers } from "next/headers";
import BanksExplorer from "./components/BanksExplorer";

export const dynamic = "force-dynamic";

/**
 * 题库列表页面
 * @constructor
 */
export default async function BanksPage() {
  let questionBankList: API.QuestionBankVO[] = [];
  let total = 0;
  const pageSize = 12;

  try {
    const res = (await listQuestionBankVoByPageUsingPost({
      pageSize,
      sortField: "createTime",
      sortOrder: "descend",
    }, {
      headers: {
        cookie: headers().get("cookie") || "",
      }
    })) as unknown as API.BaseResponsePageQuestionBankVO_;
    const records = res.data?.records ?? [];
    questionBankList = records;
    total = Number(res.data?.total) || 0;
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

      <BanksExplorer initialQuestionBankList={questionBankList} initialTotal={total} initialPageSize={pageSize} />
    </div>
  );
}
