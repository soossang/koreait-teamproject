// ========= util =========
function $(sel){ return document.querySelector(sel); }
function escapeHtml(s){ return String(s ?? '').replace(/[&<>"']/g, m => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m])); }
function fmt(n){ return Number(n||0).toLocaleString(); }
function show(el){ el.classList.remove('hidden'); }
function hide(el){ el.classList.add('hidden'); }

// ========= elements =========
const limitSel = $('#limitSelect');
const sortSel  = $('#sortSelect');
const dirSel   = $('#dirSelect');
const grid     = $('#topGrid');
const emptyBox = $('#emptyBox');
const statusEl = $('#status');

let reqToken = 0; // 요청 경합 방지

// ========= render =========
function render(list){
  if(!Array.isArray(list)) list = [];
  if(list.length === 0){
    grid.innerHTML = '';
    show(emptyBox);
    statusEl.textContent = '(0건)';
    return;
  }
  hide(emptyBox);
  grid.innerHTML = list.map(it => `
    <div class="card">
      <div class="muted"><span class="badge">#${it.rankNo}</span></div>
      <div class="title title-ellipsis">${escapeHtml(it.movieNm)}</div>
      <div class="muted">${it.openDt ? '개봉: ' + it.openDt : ''}</div>
      <div class="muted">누적관객: ${fmt(it.audiAcc)}</div>
      <div class="muted">누적매출: ${fmt(it.salesAcc)}</div>
      <div class="muted">스크린수: ${it.screenCnt ?? '-'}</div>
    </div>
  `).join('');
  statusEl.textContent = `(${list.length}건)`;
}

// ========= load from API =========
async function load(){
  const myToken = ++reqToken;
  const limit  = limitSel?.value || 10;
  const sortBy = sortSel?.value  || 'audi';
  const dir    = dirSel?.value   || 'desc';

  statusEl.textContent = '로딩중…';
  grid.innerHTML = '<div class="loader"></div>';
  hide(emptyBox);

  try{
    const url = `/api/boxoffice/alltime?limit=${encodeURIComponent(limit)}&sortBy=${encodeURIComponent(sortBy)}&dir=${encodeURIComponent(dir)}`;
    const res = await fetch(url, { headers: { 'Accept': 'application/json' } });

    if(myToken !== reqToken) return; // 이전 응답 무시

    if(!res.ok){
      const text = await res.text();
      console.error('API 오류', res.status, text);
      grid.innerHTML = '';
      show(emptyBox);
      statusEl.textContent = `오류 ${res.status}`;
      return;
    }
    const data = await res.json();
    render(data);
  }catch(err){
    if(myToken !== reqToken) return;
    console.error(err);
    grid.innerHTML = '';
    show(emptyBox);
    statusEl.textContent = '네트워크 오류';
  }
}

// ========= events =========
limitSel.addEventListener('change', load);
sortSel.addEventListener('change', load);
dirSel.addEventListener('change', load);
window.addEventListener('DOMContentLoaded', load);
