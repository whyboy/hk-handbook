export const newsCategories = [
  { id: '', label: '最新' }, { id: 'school', label: '香港学校' }, { id: 'policy', label: '香港政策' },
] as const
export type NewsCategory = (typeof newsCategories)[number]['id']
// Real items require a verified source and update process before being added.
export interface NewsItem {
  id: string
  category: Exclude<NewsCategory, ''>
  title: string
  summary: string
  publisher: string
  publishedOn: string
  verifiedOn: string
  sourceUrl: string
}
export const newsItems: readonly NewsItem[] = []
