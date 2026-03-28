import { listQuestionVoByPageUsingPost } from "@/api/questionController";
import QuestionTable from "@/components/QuestionTable";
import { Sparkles } from "lucide-react";
import { headers } from "next/headers";

export const dynamic = "force-dynamic";

/**
 * 题目列表页面
 * @constructor
 */
export default async function QuestionsPage({
  searchParams,
}: {
  searchParams: { q: string };
}) {
  // 获取 url 的查询参数
  const searchText = searchParams.q || "";
  // 题目列表和总数
  let questionList: API.QuestionVO[] = [];
  let total = 0;

  try {
    const res = (await listQuestionVoByPageUsingPost({
      searchText,
      pageSize: 12,
      sortField: "createTime",
      sortOrder: "descend",
    }, {
      headers: {
        cookie: headers().get("cookie") || "",
      }
    })) as unknown as API.BaseResponsePageQuestionVO_;
    questionList = res.data?.records ?? [];
    total = res.data?.total ?? 0;
  } catch (e) {
    console.error("获取题目列表失败", e);
  }

  return (
    <div id="questionsPage" className="space-y-12 pb-20">
      {/* Header Section */}
      <section className="relative overflow-hidden rounded-[3rem] bg-white border border-slate-100 p-8 sm:p-16 text-slate-900 shadow-2xl shadow-slate-200/50">
         <div className="absolute top-0 right-0 -mt-10 -mr-10 h-64 w-64 rounded-full bg-primary/5 blur-3xl opacity-60" />
         <div className="relative z-10 space-y-4">
            <div className="flex items-center gap-2 text-primary font-black uppercase tracking-widest text-sm">
               <Sparkles className="h-4 w-4" />
               <span>Over 10,000+ Questions</span>
            </div>
            <h1 className="text-4xl sm:text-6xl font-black tracking-tight text-slate-900">
               题目大全
            </h1>
            <p className="text-lg text-slate-500 font-medium max-w-2xl">
               探索精选的面试真题，涵盖前端、后端、架构、算法等多个领域。
            </p>
         </div>
      </section>

      {/* Main Content Area */}
      <section className="bg-white/50 backdrop-blur-sm rounded-[3rem] p-6 sm:p-10 border border-white shadow-2xl shadow-slate-200/50">
        <QuestionTable
          defaultQuestionList={questionList}
          defaultTotal={total}
          defaultSearchParams={{
            searchText: searchText,
          }}
        />
      </section>
    </div>
  );
}
