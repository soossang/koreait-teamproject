function $(sel){ return document.querySelector(sel); }
function show(el){ el.classList.remove('hidden'); }
function hide(el){ el.classList.add('hidden'); }

const genreSel = $('#genreSelect');
const dirSel   = $('#dirSelect');
const limitInp = $('#limitInput');
const listEl   = $('#titleList');
const emptyEl  = $('#emptyBox');
const statusEl = $('#status');
const loadBtn  = $('#loadBtn');

let reqToken = 0;

async function loadTitles(){
  const my = ++reqToken;

  const genre = genreSel.value.trim();
  const dir   = dirSel.value || 'asc';
  const limit = Math.max(1, Math.min(parseInt(limitInp.value||'100',10), 200));

  // 기존 API 재사용: 제목만 뽑아서 쓰기
  const url = `/api/movies?genre=${encodeURIComponent(genre)}&limit=${encodeURIComponent(limit)}&sortBy=movie&dir=${encodeURIComponent(dir)}`;

  statusEl.textContent = '로딩중…';
  listEl.innerHTML = '';
  hide(emptyEl);

  try{
    const res = await fetch(url, { headers: { 'Accept':'application/json' } });
    if(my !== reqToken) return;
    if(!res.ok){
      statusEl.textContent = `오류 ${res.status}`;
      show(emptyEl);
      return;
    }
    const data = await res.json(); // [{movieNm: "...", ...}]
    const items = Array.isArray(data) ? data : [];

    if(items.length === 0){
      show(emptyEl);
      statusEl.textContent = '0건';
      return;
    }

    hide(emptyEl);
    statusEl.textContent = `${items.length}건`;

    listEl.innerHTML = items.map(it => `
      <li>
        <span class="dot" aria-hidden="true"></span>
        <span>${escapeHtml(it.movieNm)}</span>
      </li>
    `).join('');
  }catch(e){
    console.error(e);
    if(my !== reqToken) return;
    statusEl.textContent = '네트워크 오류';
    show(emptyEl);
  }
}

function escapeHtml(s){
  return String(s ?? '').replace(/[&<>"']/g, m => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m]));
}

loadBtn.addEventListener('click', loadTitles);
window.addEventListener('DOMContentLoaded', loadTitles);
