import React from "react";
import { Button, Input, Select } from "antd";
import { RotateCcw, Search } from "lucide-react";

type OptionValue = string | number;

interface Option {
  label: string;
  value: OptionValue;
}

interface Props {
  keyword: string;
  keywordPlaceholder: string;
  onKeywordChange: (value: string) => void;
  onSearch: () => void;
  onReset: () => void;
  loading?: boolean;
  statusOptions?: Option[];
  statusValue?: OptionValue;
  onStatusChange?: (value: OptionValue) => void;
  sortOptions?: Option[];
  sortValue?: OptionValue;
  onSortChange?: (value: OptionValue) => void;
}

export default function RecordFilterToolbar(props: Props) {
  const {
    keyword,
    keywordPlaceholder,
    onKeywordChange,
    onSearch,
    onReset,
    loading,
    statusOptions,
    statusValue,
    onStatusChange,
    sortOptions,
    sortValue,
    onSortChange,
  } = props;

  return (
    <div className="rounded-[1.75rem] border border-slate-100 bg-white p-5 shadow-sm shadow-slate-100">
      <div className="grid gap-3 lg:grid-cols-[minmax(0,1.4fr)_220px_220px_auto]">
        <Input
          allowClear
          size="large"
          value={keyword}
          placeholder={keywordPlaceholder}
          onChange={(event) => onKeywordChange(event.target.value)}
          onPressEnter={onSearch}
        />
        {statusOptions && onStatusChange ? (
          <Select
            size="large"
            value={statusValue}
            options={statusOptions}
            onChange={onStatusChange}
          />
        ) : (
          <div className="hidden lg:block" />
        )}
        {sortOptions && onSortChange ? (
          <Select
            size="large"
            value={sortValue}
            options={sortOptions}
            onChange={onSortChange}
          />
        ) : (
          <div className="hidden lg:block" />
        )}
        <div className="flex gap-3">
          <Button
            type="primary"
            size="large"
            loading={loading}
            icon={<Search size={16} />}
            className="flex-1 rounded-2xl font-bold"
            onClick={onSearch}
          >
            查询
          </Button>
          <Button
            size="large"
            icon={<RotateCcw size={16} />}
            className="rounded-2xl font-bold"
            onClick={onReset}
          >
            重置
          </Button>
        </div>
      </div>
    </div>
  );
}
