// ========= util =========
function $(sel){ return document.querySelector(sel); }
function escapeHtml(s){ return String(s ?? '').replace(/[&<>"']/g, m => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m])); }
function fmt(n){ return Number(n||0).toLocaleString(); }
function show(el){ el.classList.remove('hidden'); }
function hide(el){ el.classList.add('hidden'); }

// ========= elements =========
const limitSel = $('#limitSelect');
const sortSel  = $('#sortSelect');   // audi | sales | open | movie | screen
const dirSel   = $('#dirSelect');    // asc | desc
const grid     = $('#topGrid');
const emptyBox = $('#emptyBox');
const statusEl = $('#status');

let reqToken = 0; // 요청 경합 방지

// 정렬 라벨(선택)
const sortLabel = {
  audi:   '관객수',
  sales:  '매출액',
  open:   '개봉일',
  movie:  '영화명',
  screen: '스크린수'
};

// ========= render =========
// 현재 정렬(sortBy/dir)에 맞춘 "순위(배지)"를 index+1로 표시
function render(list, sortBy, dir){
  if(!Array.isArray(list)) list = [];
  if(list.length === 0){
    grid.innerHTML = '';
    show(emptyBox);
    statusEl.textContent = '(0건)';
    return;
  }

  hide(emptyBox);
  grid.innerHTML = list.map((it, idx) => {
    const rank = idx + 1; // 현재 정렬 결과의 순위
    return `
      <div class="card">
        <div class="muted">
          <span class="badge" title="${sortLabel[sortBy]||'정렬'} ${dir==='asc'?'오름차순':'내림차순'} 순위">#${rank}</span>
        </div>
        <div class="title title-ellipsis">${escapeHtml(it.movieNm)}</div>
        <div class="muted">${it.openDt ? '개봉: ' + it.openDt : ''}</div>
        <div class="muted">누적관객: ${fmt(it.audiAcc)}</div>
        <div class="muted">누적매출: ${fmt(it.salesAcc)}</div>
        <div class="muted">스크린수: ${it.screenCnt ?? '-'}</div>
        <!-- 필요하면 원래(관객수 기준) 순위도 참고용으로 표시 가능:
        <div class="muted">관객순위: #${it.rankNo}</div>
        -->
      </div>
    `;
  }).join('');

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
    render(data, sortBy, dir);
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
