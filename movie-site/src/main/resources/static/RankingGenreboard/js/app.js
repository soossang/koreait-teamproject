// ========= util =========
function $(sel){ return document.querySelector(sel); }
function escapeHtml(s){ return String(s ?? '').replace(/[&<>"']/g, m => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m])); }
function fmt(n){ return Number(n||0).toLocaleString(); }
function show(el){ el.classList.remove('hidden'); }
function hide(el){ el.classList.add('hidden'); }

// ========= elements/state =========
const sortSel  = $('#sortSelect');  // audi | sales | open | movie | screen
const dirSel   = $('#dirSelect');   // asc | desc
const grid     = $('#topGrid');
const emptyBox = $('#emptyBox');
const statusEl = $('#status');
const pagerEl  = $('#pager');

const PAGE_SIZE = 30;
let currentPage = 1; // 1-based
let reqToken = 0;

// ========= render =========
function renderCards(list, sortBy, dir, page, pageSize){
  if(!Array.isArray(list)) list = [];
  if(list.length === 0){
    grid.innerHTML = '';
    show(emptyBox);
    return;
  }
  hide(emptyBox);

  const key = (sortBy||'audi').toLowerCase();
  const isAudi   = key === 'audi';
  const isSales  = key === 'sales';
  const isOpen   = key === 'open';
  const isMovie  = key === 'movie';
  const isScreen = key === 'screen';

  const rankStart = (Math.max(page,1) - 1) * pageSize + 1; // 전역 순번 시작

  grid.innerHTML = list.map((it, idx) => {
    const rank = rankStart + idx;

    // 라인 구성 (정렬 키만 .hot 적용)
    const titleCls = `title title-ellipsis${isMovie?' hot':''}`;
    const openLine = it.openDt
      ? `<div class="muted${isOpen?' hot':''}">개봉: ${it.openDt}</div>`
      : `<div class="muted${isOpen?' hot':''}">개봉: -</div>`;
    const audiLine  = `<div class="muted${isAudi?' hot':''}">누적관객: ${fmt(it.audiAcc)}</div>`;
    const salesLine = `<div class="muted${isSales?' hot':''}">누적매출: ${fmt(it.salesAcc)}</div>`;
    const scrLine   = `<div class="muted${isScreen?' hot':''}">스크린수: ${it.screenCnt ?? '-'}</div>`;

    return `
      <div class="card">
        <div class="muted">
          <!-- 배지는 항상 빨강 -->
          <span class="badge badge--active" title="${key.toUpperCase()} ${dir==='asc'?'오름':'내림'}차순 전역순위">#${rank}</span>
        </div>
        <div class="${titleCls}">${escapeHtml(it.movieNm)}</div>
        ${openLine}
        ${audiLine}
        ${salesLine}
        ${scrLine}
      </div>
    `;
  }).join('');
}

function renderPager(totalPages){
  const tp = Math.max(totalPages, 1);
  const cp = Math.min(Math.max(currentPage, 1), tp);

  let html = '';
  html += `<button ${cp===1?'disabled':''} data-page="${cp-1}">이전</button>`;
  for(let p=1; p<=tp; p++){
    html += `<button ${p===cp?'class="active"':''} data-page="${p}">${p}</button>`;
  }
  html += `<button ${cp===tp?'disabled':''} data-page="${cp+1}">다음</button>`;
  pagerEl.innerHTML = html;

  pagerEl.querySelectorAll('button[data-page]').forEach(btn=>{
    btn.addEventListener('click', (e)=>{
      const p = parseInt(e.currentTarget.getAttribute('data-page'), 10);
      if(!isNaN(p)) gotoPage(p);
    });
  });
}

// ========= load =========
async function load(page){
  const myToken = ++reqToken;
  const sortBy = sortSel?.value || 'audi';
  const dir    = dirSel?.value  || 'desc';
  const size   = PAGE_SIZE;
  const p      = Math.max(page ?? currentPage, 1);

  statusEl.textContent = '로딩중…';
  grid.innerHTML = '<div class="loader"></div>';
  hide(emptyBox);

  try{
    const url = `/api/boxoffice/alltime-page?page=${encodeURIComponent(p)}&size=${encodeURIComponent(size)}&sortBy=${encodeURIComponent(sortBy)}&dir=${encodeURIComponent(dir)}`;
    const res = await fetch(url, { headers: { 'Accept': 'application/json' } });
    if(myToken !== reqToken) return;

    if(!res.ok){
      const t = await res.text();
      console.error('API 오류', res.status, t);
      grid.innerHTML = '';
      show(emptyBox);
      statusEl.textContent = `오류 ${res.status}`;
      return;
    }

    const pageData   = await res.json(); // Spring Page
    const list       = pageData.content || [];
    const total      = pageData.totalElements ?? list.length;
    const totalPages = pageData.totalPages ?? 1;
    currentPage      = (pageData.number ?? (p-1)) + 1; // 0-based → 1-based

    renderCards(list, sortBy, dir, currentPage, size);
    renderPager(totalPages);
    statusEl.textContent = `총 ${fmt(total)}건 · ${currentPage}/${totalPages}페이지 · 페이지당 ${PAGE_SIZE}개`;
  }catch(err){
    if(myToken !== reqToken) return;
    console.error(err);
    grid.innerHTML = '';
    show(emptyBox);
    statusEl.textContent = '네트워크 오류';
  }
}

function gotoPage(p){
  currentPage = Math.max(p, 1);
  load(currentPage);
}

// ========= events =========
sortSel.addEventListener('change', ()=>gotoPage(1));
dirSel.addEventListener('change',  ()=>gotoPage(1));
window.addEventListener('DOMContentLoaded', ()=>gotoPage(1));
