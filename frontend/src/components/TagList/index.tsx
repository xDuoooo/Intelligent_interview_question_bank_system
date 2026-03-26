import React from "react";

interface Props {
  tagList?: string[];
}

/**
 * 标签列表组件
 * @param props
 * @constructor
 */
const TagList = (props: Props) => {
  const { tagList = [] } = props;

  return (
    <div className="flex flex-wrap gap-2">
      {tagList.map((tag) => {
        return (
          <span 
            key={tag} 
            className="px-2.5 py-1 text-xs font-bold rounded-lg bg-slate-100 text-slate-600 border border-slate-200/50 hover:bg-primary/5 hover:text-primary hover:border-primary/20 transition-all cursor-default"
          >
            {tag}
          </span>
        );
      })}
    </div>
  );
};

export default TagList;

