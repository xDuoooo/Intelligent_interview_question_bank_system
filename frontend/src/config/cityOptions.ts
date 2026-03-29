export type CityGroupOption = {
  label: string;
  options: { label: string; value: string }[];
};

export const CITY_GROUP_OPTIONS: CityGroupOption[] = [
  {
    label: "直辖市 / 特别行政区",
    options: [
      { label: "北京", value: "北京" },
      { label: "上海", value: "上海" },
      { label: "天津", value: "天津" },
      { label: "重庆", value: "重庆" },
      { label: "香港", value: "香港" },
      { label: "澳门", value: "澳门" },
    ],
  },
  {
    label: "华北 / 东北",
    options: [
      { label: "石家庄", value: "石家庄" },
      { label: "太原", value: "太原" },
      { label: "呼和浩特", value: "呼和浩特" },
      { label: "沈阳", value: "沈阳" },
      { label: "大连", value: "大连" },
      { label: "长春", value: "长春" },
      { label: "哈尔滨", value: "哈尔滨" },
    ],
  },
  {
    label: "华东",
    options: [
      { label: "南京", value: "南京" },
      { label: "苏州", value: "苏州" },
      { label: "无锡", value: "无锡" },
      { label: "杭州", value: "杭州" },
      { label: "宁波", value: "宁波" },
      { label: "合肥", value: "合肥" },
      { label: "福州", value: "福州" },
      { label: "厦门", value: "厦门" },
      { label: "南昌", value: "南昌" },
      { label: "济南", value: "济南" },
      { label: "青岛", value: "青岛" },
    ],
  },
  {
    label: "华中 / 华南",
    options: [
      { label: "郑州", value: "郑州" },
      { label: "武汉", value: "武汉" },
      { label: "长沙", value: "长沙" },
      { label: "广州", value: "广州" },
      { label: "深圳", value: "深圳" },
      { label: "佛山", value: "佛山" },
      { label: "东莞", value: "东莞" },
      { label: "南宁", value: "南宁" },
      { label: "海口", value: "海口" },
    ],
  },
  {
    label: "西南 / 西北",
    options: [
      { label: "成都", value: "成都" },
      { label: "贵阳", value: "贵阳" },
      { label: "昆明", value: "昆明" },
      { label: "拉萨", value: "拉萨" },
      { label: "西安", value: "西安" },
      { label: "兰州", value: "兰州" },
      { label: "西宁", value: "西宁" },
      { label: "银川", value: "银川" },
      { label: "乌鲁木齐", value: "乌鲁木齐" },
    ],
  },
];

export const CITY_OPTIONS = CITY_GROUP_OPTIONS.flatMap((group) => group.options);

export const CITY_VALUE_ENUM = Object.fromEntries(
  CITY_OPTIONS.map((item) => [
    item.value,
    {
      text: item.label,
    },
  ]),
);
