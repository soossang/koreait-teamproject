const fmt = (n) => Number(n||0).toLocaleString();
const esc = (s) => String(s ?? '').replace(/[&<>"']/g, m => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m]));
const show = el => el.classList.remove('hidden');
const hide = el => el.classList.add('hidden');

// ===== elements =====
const genreSel = $('#genreSelect');
const sizeSel  = $('#sizeSelect');
const sortSel  = $('#sortSelect');   // audi|sales|open|movie|screen
const dirSel   = $('#dirSelect');    // asc|desc
const statusEl = $('#status');
const grid     = $('#grid');
const emptyBox = $('#emptyBox');

let reqToken = 0;

function render(list){
  if(!Array.isArray(list) || list.length === 0){
    grid.innerHTML = '';
    show(emptyBox);
    statusEl.textContent = '(0건)';
    return;
  }
  hide(emptyBox);

  grid.innerHTML = list.map((m, i) => `
    <div class="card">
      <div class="muted"><span class="badge">#${i+1}</span></div>
      <div class="title title-ellipsis">${esc(m.title || m.movieNm || m.name)}</div>
      <div class="muted">${m.openDt || m.releaseDate ? '개봉: ' + (m.openDt || m.releaseDate) : ''}</div>
      <div class="muted">장르: ${esc(m.genre || genreSel.value)}</div>
      <div class="muted">누적관객: ${fmt(m.audiAcc || m.audience || m.audienceAcc)}</div>
      <div class="muted">누적매출: ${fmt(m.salesAcc || m.sales)}</div>
      <div class="muted">스크린수: ${m.screenCnt ?? '-'}</div>
    </div>
  `).join('');

  statusEl.textContent = `(${list.length}건)`;
}

async function search(){
  const my = ++reqToken;
  const genre = genreSel.value;
  const size  = sizeSel.value || 20;
  const sort  = sortSel.value || 'audi';
  const dir   = dirSel.value || 'desc';

  statusEl.textContent = '로딩중…';
  grid.innerHTML = '<div class="loader"></div>';
  hide(emptyBox);

  try {
    // ▼ 필요 시 서버 API 파라미터 규약에 맞게 수정하세요.
    // /api/movies?genre=장르&limit=20&sortBy=audi&dir=desc
    const url = `/api/movies?genre=${encodeURIComponent(genre)}&limit=${encodeURIComponent(size)}&sortBy=${encodeURIComponent(sort)}&dir=${encodeURIComponent(dir)}`;
    const res = await fetch(url, { headers: { 'Accept': 'application/json' } });

    if (my !== reqToken) return;

    if (!res.ok) {
      const t = await res.text();
      console.error('API 오류', res.status, t);
      grid.innerHTML = '';
      show(emptyBox);
      statusEl.textContent = `오류 ${res.status}`;
      return;
    }

    const data = await res.json();
    const list = Array.isArray(data) ? data : (data.content || []); // Page 지원
    render(list);
  } catch (e) {
    if (my !== reqToken) return;
    console.error(e);
    grid.innerHTML = '';
    show(emptyBox);
    statusEl.textContent = '네트워크 오류';
  }
}

// 이벤트
$('#searchBtn').addEventListener('click', search);
window.addEventListener('DOMContentLoaded', search);
