"use client";
import React from "react";
import Link from "next/link";
import Image from "next/image";

interface Props {
    questionBankList: API.QuestionBankVO[];
}

/**
 * 题库列表组件
 * @param props
 * @constructor
 */
const QuestionBankList = (props: Props) => {
    const {questionBankList = []} = props;
    const processedList = questionBankList.map(bank => ({
        ...bank,
        picture: (bank.picture || '/assets/logo.png')
    }));

    return (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {processedList.map((bank) => (
                <Link
                    key={bank.id}
                    href={`/bank/${bank.id}`}
                    className="group relative flex flex-col items-center p-6 rounded-[2rem] bg-white ring-1 ring-slate-200 hover:ring-primary/50 hover:shadow-xl hover:shadow-primary/10 transition-all duration-500"
                >
                    <div
                        className="relative h-20 w-20 mb-4 rounded-3xl overflow-hidden ring-4 ring-slate-50 group-hover:ring-primary/20 transition-all shadow-inner">
                        <Image
                            src={bank.picture || "/assets/logo.png"}
                            fill
                            alt={bank.title || "题库"}
                            className="object-cover group-hover:scale-110 transition-transform duration-500"
                        />
                    </div>
                    <h3 className="text-lg font-black text-foreground group-hover:text-primary mb-2 transition-colors text-center">
                        {bank.title}
                    </h3>
                    <p className="text-sm text-muted-foreground line-clamp-2 text-center font-medium opacity-80 group-hover:opacity-100 transition-opacity">
                        {bank.description}
                    </p>
                </Link>
            ))}
        </div>
    );
};

export default QuestionBankList;

