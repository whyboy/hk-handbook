const { chromium } = require('playwright');
const { spawn } = require('node:child_process');
const assert = require('node:assert/strict');
const server = spawn('python3', ['-m','http.server','4173','--bind','127.0.0.1','--directory',__dirname], {stdio:'ignore'});
const base = 'http://127.0.0.1:4173/';
(async () => {
  let browser;
  try {
    for (let i=0;i<30;i++) {
      try { await fetch(base); break; } catch { await new Promise(r=>setTimeout(r,100)); }
    }
    browser = await chromium.launch({headless:true});
    const page = await browser.newPage({viewport:{width:390,height:844}});
    const errors=[];page.on('pageerror',e=>errors.push(e.message));
    const has = async text => page.waitForFunction(value => document.querySelector('main').innerText.includes(value), text);
    const go = async hash => { await page.goto(base+hash); await page.waitForTimeout(50); };
    await go('#home');
    await page.getByRole('link',{name:'交通',exact:true}).click();
    await has('1 篇样稿');
    await page.locator('#query').fill('不存在的查询');
    await page.getByRole('button',{name:'搜索',exact:true}).click();await has('暂无符合条件');
    await page.locator('#query').fill('尾班车');
    await page.getByRole('button',{name:'搜索',exact:true}).click();await has('1 篇样稿');
    for (const audience of ['游客','人才用户','家庭']) {
      await page.locator('#audience').selectOption(audience);await has('1 篇样稿');
    }
    await go('#guides/bank');await has('暂无符合条件');
    await go('#article/mtr-route');await has('人工核验待完成');
    await page.getByRole('button',{name:'收藏到本设备'}).click();
    await go('#mine');await has('第一次乘港铁');
    await page.reload();await has('第一次乘港铁');
    await go('#article/mtr-route');await page.getByRole('button',{name:'取消收藏'}).click();
    await go('#mine');await has('还没有收藏');
    await go('#article/mtr-route');
    await page.evaluate(()=>Object.defineProperty(navigator,'clipboard',{value:{writeText:()=>Promise.reject(new Error('denied'))},configurable:true}));
    await page.getByRole('button',{name:'复制分享链接'}).click();
    const shared=await page.locator('#share-result input').inputValue();
    assert.ok(shared.endsWith('#article/mtr-route'));
    const other=await browser.newPage();await other.goto(shared);assert.ok((await other.locator('h1').innerText()).includes('港铁'));await other.close();
    await go('#correction');await page.locator('textarea').fill('   ');await page.getByRole('button',{name:'模拟提交纠错'}).click();
    assert.match(await page.locator('#notice').innerText(),/请填写/);
    await page.locator('textarea').fill('来源需要重新核验');await page.getByRole('button',{name:'模拟提交纠错'}).click();
    assert.match(await page.locator('#notice').innerText(),/实际未发送/);
    await go('#error');await has('暂时无法加载');await page.getByRole('link',{name:'重试',exact:true}).click();await has('攻略库');
    await go('#article/missing');await has('不存在');
    await go('#article/withdrawn');await has('已下架');
    await go('#article/mtr-route');
    await page.evaluate(()=>{Storage.prototype.setItem=()=>{throw new Error('blocked')};});
    await page.getByRole('button',{name:'收藏到本设备'}).click();assert.match(await page.locator('#notice').innerText(),/保存失败/);
    assert.ok(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth),'mobile layout overflow');
    assert.deepEqual(errors,[]);
    console.log('PASS: navigation, search, audience filters, empty states, persistent favorites, share fallback/deep link, correction, errors, storage denial, mobile overflow, JS errors');
  } finally { if(browser) await browser.close();server.kill(); }
})().catch(e=>{console.error(e);process.exitCode=1;});
