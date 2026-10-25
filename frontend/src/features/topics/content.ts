// Home topics are navigation destinations, independent of guide categories.
export const topics = [
  { id: 'bank', title: '港卡办理', subtitle: '开户与准备', mark: '卡' },
  { id: 'phone', title: '电话卡办理', subtitle: '通信与上网', mark: '话' },
  { id: 'metro', title: '地铁乘坐', subtitle: '线路与出行', mark: '铁' },
  { id: 'attractions', title: '景点推荐', subtitle: '发现香港', mark: '游' },
  { id: 'identity', title: '香港身份', subtitle: '身份与申请', mark: '身' },
  { id: 'housing', title: '港房租售', subtitle: '租房与置业', mark: '房' },
  { id: 'education', title: '香港教育', subtitle: '升学与教育', mark: '学' },
  { id: 'insurance', title: '香港保险', subtitle: '保障与投保', mark: '保' },
  { id: 'mpf', title: '强公积金', subtitle: '账户与供款', mark: '积' },
  { id: 'tax', title: '税务办理', subtitle: '报税与税务', mark: '税' },
  { id: 'driving', title: '驾驶证办理', subtitle: '牌照与换领', mark: '驾' },
] as const
export type TopicId = (typeof topics)[number]['id']
