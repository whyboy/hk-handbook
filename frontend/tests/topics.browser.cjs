// Run against the built H5. Content API is empty here; topic pages need no API.
const { chromium } = require('playwright');
const { expect } = require('playwright/test');
const assert = require('node:assert/strict');
const base = process.env.H5_TEST_BASE_URL || 'http://127.0.0.1:14174';
const topics = [
 ['bank','港卡办理'],['phone','电话卡办理'],['metro','地铁乘坐'],['attractions','景点推荐'],
 ['identity','香港身份'],['housing','港房租售'],['education','香港教育'],['insurance','香港保险'],
 ['mpf','强公积金'],['tax','税务办理'],['driving','驾驶证办理']
];
(async () => {
 const browser = await chromium.launch();
 try {
  const page = await browser.newPage(); const errors=[]; const api=[];
  page.on('pageerror', e=>errors.push(e.message));
  await page.route('**/api/**', route=>{
   const u=new URL(route.request().url());api.push(u.pathname);
   return route.fulfill({contentType:'application/json',body:JSON.stringify({items:[],total:0,page:Number(u.searchParams.get('page')||1),size:Number(u.searchParams.get('size')||20)})});
  });
  const active=()=>page.locator('uni-page:visible');
  for(const width of [320,390,768,1280]) {
   await page.setViewportSize({width,height:844});
   await page.goto(base+'/#/pages/index/index');
   await expect(active().locator('.search')).toBeVisible();
   await expect(active().getByText(/推荐攻略|暂无已发布攻略|查看全部攻略|我的收藏与记录|赴港办事，先了解，再出发/)).toHaveCount(0);
   await expect(active().locator('.brand-name')).toHaveText('香港百事通');
   const cards=active().locator('.task-card');await expect(cards).toHaveCount(11);
   await expect(active().getByText('查看全部工具 →',{exact:true})).toHaveCount(0);
   const boxes=await cards.evaluateAll(els=>els.map(el=>{const r=el.getBoundingClientRect();return {x:r.x,y:r.y,width:r.width,right:r.right}}));
   assert.equal(new Set(boxes.map(b=>Math.round(b.y))).size,4);
   assert.equal(new Set(boxes.map(b=>Math.round(b.x))).size,3);
   assert(boxes.every(b=>b.x>=0&&b.right<=width));
   assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth),false);
   if(width===390)await page.screenshot({path:'/tmp/hk-topics-home-390.png',fullPage:true});
   for(const [id,title] of topics) {
    await active().locator('.task-card').nth(topics.findIndex(t=>t[0]===id)).click();
    await expect(page).toHaveURL(new RegExp('/pages/topics/'+id));
    await expect(active().locator('.title')).toHaveText(title);
    await expect(active().getByText('内容整理中',{exact:true})).toBeVisible();
    await expect(active().getByText(/有用|无用|评论/)).toHaveCount(0);
    assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth),false);
    await active().getByText('返回首页',{exact:true}).click();
    await expect(active().locator('.task-card')).toHaveCount(11);
   }
  }
  for(const [id,title] of topics) {
   await page.goto(base+'/#/pages/topics/'+id);await page.reload();
   await expect(active().locator('.title')).toHaveText(title);
   await active().getByText('返回首页',{exact:true}).click();
  }
  assert.deepEqual(api, [], 'home and topics must not request content');
  await active().locator('.search').click();
  await expect(active().getByText('暂无已发布攻略',{exact:true})).toBeVisible();
  assert.equal(page.url().includes('/pages/guides/index'),false,'home search must not switch to the news tab route');
  for(const label of ['工具','资讯','我的','首页']) {
   await page.locator('uni-tabbar .uni-tabbar__label').filter({hasText:new RegExp('^'+label+'$')}).click();
   await expect(page.locator('uni-tabbar')).toBeVisible();
  }
  await page.locator('uni-tabbar .uni-tabbar__label').filter({hasText:/^资讯$/}).click();
  await expect(active().getByText('暂无已发布资讯',{exact:true})).toBeVisible();
  for(const label of ['最新','香港学校','香港政策']) await expect(active().getByText(label,{exact:true})).toBeVisible();
  assert(api.every(path=>path==='/api/guides'));
  assert.deepEqual(errors,[]);
  console.log('PASS 11 topics, H5 three-column layout at four widths, direct/reload/back, search/tabs, no voting or runtime errors');
 } finally {await browser.close()}
})().catch(e=>{console.error(e);process.exit(1)});
