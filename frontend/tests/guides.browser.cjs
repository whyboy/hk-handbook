// Run against a built H5 served with /api proxying to the real backend.
// Playwright is an external test tool; synthetic content is intercepted only in this test.
const { chromium } = require('playwright');
const { expect } = require('playwright/test');
const assert = require('node:assert/strict');
const base = process.env.H5_TEST_BASE_URL || 'http://127.0.0.1:4173';
const fixture = require('../../backend/src/test/resources/content-fixtures/test-guide.json');
const summary = Object.fromEntries(['id','revision','title','summary','category','audiences','keywords','editedOn','reviewDueOn','effectivePeriod'].map(k=>[k,fixture[k]]));
summary.publishedOn = fixture.publication.publishedOn;
summary.verifiedOn = fixture.facts[0].verification.verifiedOn;
const detail = { ...summary, sections: fixture.sections,
  facts: fixture.facts.map(({id,claim,conditions,sourceIds})=>({id,claim,conditions,sourceIds})),
  sources: fixture.sources.map(({id,organization,title,url,consultedOn,effectivePeriod})=>({id,organization,title,url,consultedOn,effectivePeriod})), related: [] };
(async()=>{
 const browser=await chromium.launch({headless:true});
 try {
 const page=await browser.newPage({viewport:{width:390,height:844}});
 const errors=[];page.on('pageerror',e=>errors.push(e.message));
 const active=()=>page.locator('uni-page:visible');
 const click=label=>active().locator('uni-button').filter({hasText:new RegExp('^'+label+'$')}).click();
 let entry=0;
 const go=path=>page.goto(base+'/?testEntry='+ ++entry +'#/pages/'+path);
 // Unintercepted production-empty API: no fake samples or draft content.
 await go('index/index');await expect(active().getByText('暂无已发布攻略',{exact:true})).toBeVisible();
 await click('搜索：香港银行卡 / 高才 / 手机卡搜索攻略');
 await expect(active().getByText('暂无已发布攻略',{exact:true})).toBeVisible();
 await go('guides/detail?id=mtr-route');await expect(active().getByText('攻略不存在或暂不可用',{exact:true})).toBeVisible();
 console.log('PASS actual backend: empty home/list and draft 404');
 let failList=false, failMore=false, detailStatus=200, delayList=false, malformed=false;
 const requests=[];
 await page.route('**/api/guides**',async route=>{
  const url=new URL(route.request().url());requests.push(url);
  if(url.pathname!='/api/guides') {
   return route.fulfill({status:detailStatus,contentType:'application/json',body:JSON.stringify(detailStatus===200 ? malformed ? {} : detail : {code:'CONTENT_UNAVAILABLE'})});
  }
  const p=Number(url.searchParams.get('page')),size=Number(url.searchParams.get('size')),q=url.searchParams.get('q'),category=url.searchParams.get('category'),audience=url.searchParams.get('audience');
  if(delayList)await new Promise(r=>setTimeout(r,300));
  if(q==='slow')await new Promise(r=>setTimeout(r,500));
  if(failList||(failMore&&p===2))return route.fulfill({status:503,contentType:'application/json',body:'{}'});
  let all=Array.from({length:21},(_,i)=>({...summary,id:i===0?'test-guide':`test-${i}`,title:`测试攻略 ${i+1}`,category:i%2?'bank':'life'}));
  if(category)all=all.filter(item=>item.category===category);
  if(audience==='family')all=[];
  if(q==='none')all=[];
  if(q==='slow'||q==='fast')all=[{...summary,title:q==='slow'?'旧搜索结果':'新搜索结果'}];
  return route.fulfill({contentType:'application/json',body:JSON.stringify({items:all.slice((p-1)*size,p*size),total:all.length,page:p,size})});
 });
 await go('guides/index');await expect(active().locator('.guide-card')).toHaveCount(20);
 failMore=true;await click('加载更多');await expect(active().getByText('加载更多失败，已有结果保留，请重试。')).toBeVisible();
 await expect(active().locator('.guide-card')).toHaveCount(20);
 failMore=false;await click('重试加载更多');await expect(active().locator('.guide-card')).toHaveCount(21);
 await click('银行卡');await expect(active().locator('.guide-card')).toHaveCount(10);
 await click('家庭');await expect(active().getByText('没有找到相关攻略',{exact:true})).toBeVisible();
 await click('清除筛选');await expect(active().locator('.guide-card')).toHaveCount(20);
 await active().locator('input').fill('slow');await active().locator('input').press('Enter');
 await expect.poll(()=>requests.some(u=>u.searchParams.get('q')==='slow')).toBe(true);
 await active().locator('input').fill('fast'); await click('搜索');
 await expect.poll(()=>requests.some(u=>u.searchParams.get('q')==='fast')).toBe(true);
 await expect(active().locator('.guide-card')).toContainText('新搜索结果');
 await page.waitForTimeout(650);await expect(active().locator('.guide-card')).toContainText('新搜索结果');
 assert(!await active().getByText('旧搜索结果',{exact:true}).count());
 console.log('PASS paging/retry, intersecting filters, empty search, stale request ignored');
 await click('清除筛选');await click('生活落地');await expect(active().locator('.guide-card')).toHaveCount(11);
 await active().locator('.guide-card').first().click();await expect(active().getByText('事实依据与适用条件',{exact:true})).toBeVisible();
 await expect(active().getByText('test-reviewer',{exact:false})).toHaveCount(0);
 await click('返回攻略列表');await expect(active().locator('.category.selected').first()).toHaveText('生活落地');
 await expect(active().locator('.guide-card')).toHaveCount(11);
 for(const width of [320,390,768,1280]){
  await page.setViewportSize({width,height:844});
  for(const path of ['guides/index','guides/detail?id=test-guide']){
   await go(path);await expect(active().getByText(path.includes('detail')?'事实依据与适用条件':'共 21 篇攻略',{exact:true})).toBeVisible();
   assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth),false,`${path} ${width} overflow`);
   if(width===390)await page.screenshot({path:`/tmp/hk-e02-t04-${path.includes('detail')?'detail':'list'}.png`,fullPage:true});
  }
 }
 await page.reload();await expect(active().getByText('事实依据与适用条件',{exact:true})).toBeVisible();
 await page.context().route('https://example.org/test', route=>route.fulfill({contentType:'text/html',body:'<title>Test source</title>'}));
 const popupReady=page.waitForEvent('popup');await click('打开官方来源');const popup=await popupReady;
 await popup.waitForLoadState();assert.equal(popup.url(),'https://example.org/test');assert.equal(await popup.evaluate(()=>window.opener),null);await popup.close();
 await click('返回首页');await expect(active().locator('.guide-card')).toHaveCount(3);
 await active().locator('.guide-card').first().click();await expect(active().getByText('官方来源',{exact:true})).toBeVisible();
 console.log('PASS detail, home cards, retained category, reload and four responsive widths');
 detailStatus=410;await page.reload();await expect(active().getByText('这篇攻略已下架',{exact:true})).toBeVisible();
 await expect(active().getByText('Synthetic test statement',{exact:false})).toHaveCount(0);
 detailStatus=404;await page.reload();await expect(active().getByText('攻略不存在或暂不可用',{exact:true})).toBeVisible();
 detailStatus=503;await page.reload();await expect(active().getByText('攻略加载失败',{exact:true})).toBeVisible();
 detailStatus=200;await click('重试');await expect(active().getByText('官方来源',{exact:true})).toBeVisible();
 malformed=true;await page.reload();await expect(active().getByText('攻略加载失败',{exact:true})).toBeVisible();malformed=false;
 await go('guides/detail?id=%3Cscript%3E');await expect(active().getByText('攻略不存在或暂不可用',{exact:true})).toBeVisible();
 failList=true;await go('guides/index');await expect(active().getByText('攻略加载失败',{exact:true})).toBeVisible();
 failList=false;delayList=true;await click('重试');await expect(active().getByText('正在查找攻略',{exact:true})).toBeVisible();
 await expect(active().locator('.guide-card')).toHaveCount(20);delayList=false;
 await go('tools/intro?scene=bank');await click('查看相关攻略');await expect(active().locator('.category.selected').first()).toHaveText('银行卡');
 await expect(active().locator('.guide-card')).toHaveCount(10);
 await go('checklist/detail?demo=layout');await click('查看生活落地攻略');await expect(active().locator('.category.selected').first()).toHaveText('生活落地');
 await expect(active().locator('.guide-card')).toHaveCount(11);
 assert.equal(errors.length,0,errors.join('\n'));
 console.log('PASS 404/410/malformed/error/loading/retry and contextual navigation; no runtime errors');
 } finally {await browser.close()}
})().catch(e=>{console.error(e);process.exit(1)});
