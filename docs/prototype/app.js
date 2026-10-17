/* Standalone review prototype. No production API or submission. */
const articleId = 'mtr-route';
const title = '第一次乘港铁，怎样查路线和尾班车？';
const key = 'hk-handbook-review-favorites';
const main = document.querySelector('#main');
const notice = document.querySelector('#notice');
let favorite = false;
try { favorite = localStorage.getItem(key) === articleId; } catch { /* handled when saving */ }
const sources = `<ul><li><a href="https://www.mtr.com.hk/mtrmobile/en/transport/trip-planner/">S1 · 港铁 MTR Mobile 行程规划说明</a></li><li><a href="https://www.mtr.com.hk/en/customer/services/train_service_index.html">S2 · 港铁服务时间说明</a></li></ul>`;
const card = () => `<a class="card" href="#article/${articleId}"><span class="tag">交通 · 游客 / 人才用户 / 家庭</span><h2>${title}</h2><p>先确认起终点，再核对换乘与末班信息。</p><small>资料查阅：2026-10-17 · 人工核验待完成</small></a>`;
function message(text) { notice.textContent = text; }
function render() {
  const route = location.hash.slice(1) || 'home';
  message('');
  document.querySelectorAll('nav a').forEach(a => {
    a.removeAttribute('aria-current');
    if (a.hash === `#${route.split('/')[0]}`) a.setAttribute('aria-current','page');
  });
  if (route === 'home') {
    main.innerHTML = `<h1>你想先了解什么？</h1><p>免登录阅读，不必填写身份。按自己的问题找攻略。</p><div class="grid"><a href="#guides/bank">银行卡</a><a href="#guides/phone">电话卡</a><a href="#guides/transport">交通</a></div><a href="#guides">搜索全部攻略 →</a><h2>阅读样稿</h2>${card()}`;
  } else if (route === 'guides' || route.startsWith('guides/')) {
    main.innerHTML = `<h1>攻略库</h1><form id="search"><label>搜索问题<input id="query" type="search" placeholder="试试：港铁、尾班车"></label><label>分类<select id="category"><option value="all">全部</option><option value="bank">银行卡</option><option value="phone">电话卡</option><option value="transport">交通</option></select></label><label>适用人群<select id="audience"><option>全部人群</option><option>游客</option><option>人才用户</option><option>家庭</option></select></label><button>搜索</button></form><section id="results" aria-live="polite"></section>`;
    const category = document.querySelector('#category');
    category.value = ['bank','phone','transport'].includes(route.split('/')[1]) ? route.split('/')[1] : 'all';
    const search = e => {
      if (e) e.preventDefault();
      const q = document.querySelector('#query').value.trim().toLowerCase();
      const matches = (category.value === 'all' || category.value === 'transport') && (!q || `${title} 交通 地铁 换乘 路线 末班车 mtr`.toLowerCase().includes(q));
      document.querySelector('#results').innerHTML = matches ? `<p>1 篇样稿 · 适用于所选人群</p>${card()}` : '<p class="empty">暂无符合条件的攻略。试试其他关键词或选择全部分类。银行卡和电话卡内容尚未编写。</p>';
    };
    document.querySelector('#search').addEventListener('submit',search);
    category.addEventListener('change',search);
    document.querySelector('#audience').addEventListener('change',search);
    search();
  } else if (route === `article/${articleId}`) {
    main.innerHTML = `<article><a href="#guides">← 返回攻略库</a><h1>${title}</h1><p class="tag">交通 · 游客 / 人才用户 / 家庭</p><p class="notice">阅读样稿，未发布。资料查阅 2026-10-17；人工核验待完成，不能作为已核验攻略。</p><p>不熟悉港铁路线时，先用官方工具确认起终点、换乘和末班信息，再安排出行。</p><h2>适用条件</h2><p>计划乘港铁、能确定起终点的读者。本文只讲查询方法，不提供跨境资格或票种优惠判断。</p><h2>准备事项</h2><p>准备出发地与目的地名称，以及可访问官方查询工具的联网设备；查询路线不涉及申请材料。</p><h2>操作步骤</h2><ol><li>打开 MTR Mobile 的行程规划，在首页选择 “Where to?”。（S1）</li><li>选择起点和终点，查看建议路线及行程信息。（S1）</li><li>核对换乘和首尾班信息，尤其是晚间返程；官方服务时间页提供查询入口。（S2）</li><li>出发前重新查看官方服务资讯。此为编辑建议，不代表服务保证。</li></ol><h2>费用与耗时</h2><p>本文不列固定票价或耗时；按具体行程在官方工具查询。不要把估计行程时间当作抵达承诺。</p><h2>注意事项</h2><p>应用界面可能变化；如查询失败，使用下方官方说明入口。本文未验证具体路线，也未验证手机实操。个人经验：无，未虚构亲历。</p><h2>来源与核验</h2>${sources}<p>发布日期：未发布；编辑日期：2026-10-17；人工核验日期及责任人：待指定。</p><div class="actions"><button id="favorite" aria-pressed="${favorite}">${favorite?'取消收藏':'收藏到本设备'}</button><button id="share">复制分享链接</button><a href="#correction">内容纠错</a><a href="#home">返回首页</a></div><div id="share-result"></div></article>`;
    document.querySelector('#favorite').onclick = () => {
      try {
        if (favorite) localStorage.removeItem(key); else localStorage.setItem(key,articleId);
        favorite = !favorite; render(); message(favorite?'已收藏到本浏览器；清理数据后丢失，不跨设备同步。':'已取消收藏。');
      } catch { message('保存失败：浏览器未允许本地存储，收藏状态没有改变。'); }
    };
    document.querySelector('#share').onclick = async () => {
      const url = new URL(location.href); url.hash = `article/${articleId}`;
      try { await navigator.clipboard.writeText(url.href); message('已复制具体攻略链接。仅能在可访问此原型地址的环境打开。'); }
      catch {
        const label = document.createElement('label'); label.textContent = '无法自动复制，请手动复制链接';
        const input = document.createElement('input'); input.readOnly = true; input.value = url.href; label.append(input);
        document.querySelector('#share-result').replaceChildren(label); input.select();
      }
    };
  } else if (route === 'mine') {
    main.innerHTML = `<h1>我的收藏</h1><p>无需登录，只保存在本设备的当前浏览器，不跨设备同步。清理浏览器数据会丢失收藏。</p>${favorite?card():'<p class="empty">还没有收藏。阅读攻略后可保存到这里。</p>'}<a href="#guides">去看看攻略 →</a>`;
  } else if (route === 'correction') {
    main.innerHTML = `<h1>内容纠错</h1><p>关联攻略：${title}</p><p class="notice">仅演示提交结果，不向服务器发送或保存。请勿输入个人敏感资料。</p><form id="correction"><label>问题说明<textarea required maxlength="1000" name="details"></textarea></label><button>模拟提交纠错</button></form><a href="#article/${articleId}">返回攻略</a>`;
    document.querySelector('#correction').onsubmit = e => { e.preventDefault(); const input = e.target.elements.details; if (!input.value.trim()) { message('请填写具体问题。'); return; } input.value=''; message('模拟提交成功；实际未发送。正式流程需后台接收并由编辑核查，不承诺修改结果。'); };
  } else if (route === 'error') {
    main.innerHTML = '<h1>暂时无法加载攻略</h1><p class="empty">这是加载失败评审状态。请检查网络后重试。</p><a href="#guides">重试</a> · <a href="#home">返回首页</a>';
  } else {
    main.innerHTML = `<h1>${route==='article/withdrawn'?'攻略已下架':'页面或攻略不存在'}</h1><p class="empty">无法继续阅读，请查看其他攻略。</p><a href="#guides">返回攻略库</a> · <a href="#home">返回首页</a>`;
  }
  main.focus();
}
window.addEventListener('hashchange',render);
render();
