import React from "react";

/**
 * 题库详情页加载骨架屏
 */
export default function BankLoading() {
  return (
    <div className="space-y-10 pb-20 animate-pulse">
      {/* Hero Card Skeleton */}
      <section className="relative overflow-hidden rounded-[3rem] bg-white border border-slate-100 shadow-2xl shadow-slate-200/50 p-8 sm:p-12">
        <div className="flex flex-col md:flex-row gap-10 items-start">
          <div className="relative h-32 w-32 sm:h-48 sm:w-48 rounded-[2.5rem] bg-slate-100 shrink-0" />
          
          <div className="flex-1 space-y-6 w-full">
            <div className="space-y-3">
              <div className="h-4 w-24 bg-slate-100 rounded-full" />
              <div className="h-12 w-2/3 bg-slate-200 rounded-2xl" />
              <div className="space-y-2">
                <div className="h-4 w-full bg-slate-50 rounded-full" />
                <div className="h-4 w-5/6 bg-slate-50 rounded-full" />
              </div>
            </div>

            <div className="flex flex-wrap gap-6 pt-2">
               <div className="h-10 w-40 bg-slate-50 rounded-2xl border border-slate-100/50" />
               <div className="h-10 w-40 bg-slate-50 rounded-2xl border border-slate-100/50" />
            </div>

            <div className="pt-4">
              <div className="h-14 w-48 bg-slate-200 rounded-2xl" />
            </div>
          </div>
        </div>
      </section>

      {/* Questions List Skeleton */}
      <section className="space-y-8">
        <div className="h-8 w-40 bg-slate-100 rounded-xl mb-6" />
        <div className="grid grid-cols-1 gap-4">
          {[1, 2, 3, 4, 5].map((i) => (
            <div key={i} className="h-24 w-full bg-white border border-slate-100 rounded-3xl p-6 flex items-center justify-between">
              <div className="flex items-center gap-4 w-full">
                <div className="h-12 w-12 bg-slate-50 rounded-2xl shrink-0" />
                <div className="space-y-2 w-full">
                  <div className="h-5 w-1/3 bg-slate-100 rounded-lg" />
                  <div className="h-4 w-1/2 bg-slate-50 rounded-lg" />
                </div>
              </div>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}
