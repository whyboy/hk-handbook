// Home topics are navigation destinations, independent of guide categories.
export const topics = [
  { id: 'bank', title: '港卡办理', subtitle: '开户与准备' },
  { id: 'talent', title: '高才／优才申请', subtitle: '了解申请路径' },
  { id: 'phone', title: '电话卡办理', subtitle: '通信与网络' },
  { id: 'transport', title: '交通指南', subtitle: '出行与换乘' },
  { id: 'attractions', title: '景点推荐', subtitle: '探索香港' },
  { id: 'tax', title: '税务指南', subtitle: '了解税务事项' },
] as const
export type TopicId = (typeof topics)[number]['id']
