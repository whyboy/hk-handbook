"""Render the fixed, unpublished E02 sample locally. Not a publication validator."""
import argparse
import html
import json
from pathlib import Path
from urllib.parse import urlsplit

ROOT = Path(__file__).resolve().parent
SECTIONS = {
    'applicability': '适用条件与不适用情境',
    'materials': '准备事项与材料',
    'steps': '操作步骤',
    'costsAndTime': '费用与耗时',
    'warnings': '注意事项与待核验信息',
}


def render(data):
    if data['id'] != 'mtr-route' or data['status'] != 'draft':
        raise ValueError('This renderer accepts only the unpublished mtr-route draft')
    esc = lambda value: html.escape(str(value), quote=True)
    facts = {fact['id']: fact for fact in data['facts']}
    sources = {source['id']: source for source in data['sources']}
    for source in sources.values():
        url = urlsplit(source['url'])
        if url.scheme != 'https' or url.hostname != 'www.mtr.com.hk' or url.username or url.password:
            raise ValueError('The sample must link directly to the official HTTPS MTR site')
    nav = ''.join(f'<a href="#{key}">{label}</a>' for key, label in SECTIONS.items())
    sections = []
    for key, label in SECTIONS.items():
        section = data['sections'][key]
        parts = []
        if section['state'] != 'known':
            parts.append(f'<p>{esc(section["state"])}：{esc(section["reason"])}</p>')
        for block in section['blocks']:
            source_ids = sorted({sid for fid in block['factIds'] for sid in facts[fid]['sourceIds']})
            links = '、'.join(f'<a href="#{esc(sid)}">{esc(sid.upper())}</a>' for sid in source_ids if sid in sources)
            if any(sid not in sources for sid in source_ids):
                raise ValueError('Unresolved source reference')
            kind = {'official_fact': '官方说明', 'editorial_advice': '编辑建议', 'personal_experience': '个人经验'}[block['kind']]
            text = f'{esc(block["text"])} <small>（{kind}{"；" + links if links else ""}）</small>'
            parts.append(f'<li>{text}</li>' if key == 'steps' else f'<p>{text}</p>')
        content = ''.join(parts)
        if key == 'steps':
            content = f'<ol>{content}</ol>'
        sections.append(f'<section><h2 id="{key}">{label}</h2>{content}</section>')
    source_html = ''.join(f'<li id="{esc(s["id"])}">{esc(s["id"].upper())} · <a href="{esc(s["url"])}">{esc(s["organization"])} {esc(s["title"])}</a><p>{esc(s["scope"])} AI 查阅：{esc(s["consultedOn"])}；人工核验未执行。</p></li>' for s in sources.values())
    return f'''<!doctype html>
<html lang="zh-CN"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"><meta name="robots" content="noindex,nofollow">
<title>{esc(data['title'])} · 内部阅读样稿</title>
<style>
:root {{font-family:system-ui,sans-serif;color:#1f2937;background:#f5f6f8;color-scheme:light}}
* {{box-sizing:border-box}} body {{margin:0;line-height:1.8}} main {{max-width:760px;margin:24px auto;padding:28px;background:white;border:1px solid #d9dfe7;border-radius:12px}}
h1 {{font-size:clamp(1.65rem,5vw,2.15rem);line-height:1.4}} h2 {{font-size:1.25rem;margin-top:32px;scroll-margin-top:20px}}
a {{color:#075985;overflow-wrap:anywhere;text-underline-offset:3px}} a:focus-visible {{outline:3px solid #0369a1;outline-offset:4px}}
.status {{padding:16px;background:#fff7db;border-left:4px solid #946200}} .meta,small {{color:#4b5563}} nav {{padding:16px;background:#f1f5f9}} nav a {{display:inline-block;margin-right:14px}}
li {{margin-bottom:12px}} ol,ul {{padding-left:1.6rem}} footer {{border-top:1px solid #d9dfe7;margin-top:32px;padding-top:16px}}
@media(max-width:600px) {{main {{margin:0;padding:20px 16px;border:0;border-radius:0}}}}
</style></head><body><main id="top">
<p><a href="../MTR-READING.md">返回样稿评审说明</a></p>
<div class="status"><strong>内部阅读样稿，未发布。</strong>人工事实核验和创始人评审未完成。自动浏览器检查不替代人工核验；本页不提供收藏、分享或纠错业务。</div>
<article><p class="meta">生活落地 · 交通 · 游客／人才用户／家庭 · {esc(data['id'])} · 版本 {esc(data['revision'])}</p>
<h1>{esc(data['title'])}</h1><p>{esc(data['summary'])}</p>
<p class="meta">编辑：{esc(data['editedOn'])} · 发布日期：无 · 人工事实核验日期：无</p>
<nav aria-label="文章目录">{nav}<a href="#sources">官方来源</a></nav>
{''.join(sections)}
<section><h2 id="sources">官方来源与核验状态</h2><p>生效／失效日期：官方说明未提供统一期限，仍需人工核实；不得用查阅日期代替。</p><ul>{source_html}</ul></section>
<footer><p>仅用于 E02-T02 第一版内部阅读评审。此文件从 JSON 生成，不单独修改正文。</p>
<p><a href="../mtr-route.json">完整 JSON 草稿</a> · <a href="../MTR-SOURCES.md">事实账本</a> · <a href="../MTR-REVIEW.md">评审记录</a> · <a href="#top">返回顶部</a></p></footer>
</article></main></body></html>
'''


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true', help='Fail when the checked-in HTML differs')
    args = parser.parse_args()
    output = render(json.loads((ROOT / 'mtr-route.json').read_text(encoding='utf-8')))
    target = ROOT / 'reading/mtr-route.html'
    if args.check:
        if not target.exists() or target.read_text(encoding='utf-8') != output:
            raise SystemExit('Reading sample is stale; run python3 docs/content/render_sample.py')
        print('Reading sample matches JSON')
    else:
        target.write_text(output, encoding='utf-8')


if __name__ == '__main__':
    main()
