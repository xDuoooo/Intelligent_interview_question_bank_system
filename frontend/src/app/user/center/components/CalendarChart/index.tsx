import React, { useCallback, useEffect, useState } from "react";
import ReactECharts from "echarts-for-react";
import dayjs from "dayjs";
import { message } from "antd";
import { getMyQuestionHistoryRecordUsingGet } from "@/api/userQuestionHistoryController";
import { useSelector } from "react-redux";
import { RootState } from "@/stores";
import ACCESS_ENUM from "@/access/accessEnum";
import "./index.css";

interface Props {}

/**
 * 刷题日历图
 * @param props
 * @constructor
 */
const CalendarChart = (props: Props) => {
  // 刷题记录列表
  const [dataList, setDataList] = useState<any[]>([]);
  const year = new Date().getFullYear();

  const loginUser = useSelector((state: RootState) => state.loginUser);

  // 请求后端获取数据
  const fetchDataList = useCallback(async () => {
    if (!loginUser || !loginUser.id || loginUser.userRole === ACCESS_ENUM.NOT_LOGIN) {
      return;
    }
    try {
      const res = await getMyQuestionHistoryRecordUsingGet({
        year,
      });
      setDataList(res.data || []);
    } catch (e: any) {
      message.error("获取刷题记录失败，" + e.message);
    }
  }, [loginUser, year]);

  // 当登录用户变化时重新获取数据
  useEffect(() => {
    void fetchDataList();
  }, [fetchDataList]);

  // 计算图表所需的数据
  const optionsData = dataList.map((item) => {
    return [item.date, item.count];
  });

  // 图表配置
  const options = {
    visualMap: {
      show: false,
      min: 0,
      max: 10,
      inRange: {
        // 颜色从灰色到深绿色
        color: ["#efefef", "lightgreen", "darkgreen"],
      },
    },
    tooltip: {
      position: "top",
      formatter: (params: any) => {
        return `${params.value[0]} 做题：${params.value[1]} 道`;
      },
    },
    calendar: {
      range: year,
      left: 20,
      // 单元格自动宽度，高度为 16 像素
      cellSize: ["auto", 16],
      yearLabel: {
        position: "top",
        formatter: `${year} 年刷题记录`,
      },
    },
    series: {
      type: "heatmap",
      coordinateSystem: "calendar",
      data: optionsData,
    },
  };

  return <ReactECharts className="calendar-chart" option={options} />;
};

export default CalendarChart;
