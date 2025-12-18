function $(sel){ return document.querySelector(sel); }
function show(el){ el && el.classList.remove('hidden'); }
function hide(el){ el && el.classList.add('hidden'); }

const genreSel = $('#genreSelect');
const dirSel   = $('#dirSelect');
const listEl   = $('#titleList');
const emptyEl  = $('#emptyBox');
const statusEl = $('#status');
const loadBtn  = $('#loadBtn');

// ✅ pager div가 HTML에 없어도 JS가 자동으로 만들어 붙임
let pagerEl = $('#pager');
if(!pagerEl){
  pagerEl = document.createElement('div');
  pagerEl.id = 'pager';
  pagerEl.className = 'pager';
  // titleList 아래에 붙이기
  if(listEl && listEl.parentNode){
    listEl.parentNode.insertBefore(pagerEl, emptyEl || null);
  }else{
    document.body.appendChild(pagerEl);
  }
}

let reqToken = 0;
let currentPage = 0;
let totalPages = 0;

async function loadTitles(page = 0){
  const my = ++reqToken;

  currentPage = Math.max(0, page);

  const genre = (genreSel?.value ?? '').trim();
  const dir   = (dirSel?.value ?? 'asc');

  // ✅ 20개 페이징은 서버가 처리 (page만 넘김)
  const url = `/api/movies?genre=${encodeURIComponent(genre)}&dir=${encodeURIComponent(dir)}&page=${encodeURIComponent(currentPage)}&sortBy=movie`;

  statusEl && (statusEl.textContent = '로딩중…');
  listEl && (listEl.innerHTML = '');
  pagerEl && (pagerEl.innerHTML = '');
  hide(emptyEl);

  try{
    const res = await fetch(url, { headers: { 'Accept':'application/json' } });
    if(my !== reqToken) return;

    if(!res.ok){
      statusEl && (statusEl.textContent = `오류 ${res.status}`);
      show(emptyEl);
      return;
    }

    const data = await res.json();

    // ✅ 호환 처리:
    //  - 새 응답: {content: [...], page, totalPages, totalElements}
    //  - 예전 응답: [{movieNm:...}, ...] (혹시 남아있을까봐)
    let items = [];
    let totalElements = 0;

    if(Array.isArray(data)){
      items = data;
      totalElements = data.length;
      totalPages = 1;
      currentPage = 0;
    }else{
      items = Array.isArray(data.content) ? data.content : [];
      totalElements = Number.isFinite(data.totalElements) ? data.totalElements : items.length;
      totalPages = Number.isFinite(data.totalPages) ? data.totalPages : 0;
      currentPage = Number.isFinite(data.page) ? data.page : currentPage;
    }

    if(items.length === 0){
      show(emptyEl);
      statusEl && (statusEl.textContent = '0건');
      pagerEl && (pagerEl.innerHTML = '');
      return;
    }

    hide(emptyEl);

    // 상태 텍스트: "542건 (3/28)"
    const pageText = totalPages > 0 ? ` (${currentPage + 1}/${totalPages})` : '';
    statusEl && (statusEl.textContent = `${totalElements}건${pageText}`);

    // 목록 렌더
    listEl.innerHTML = items.map(it => `
      <li>
        <span class="dot" aria-hidden="true"></span>
        <span>${escapeHtml(it.movieNm)}</span>
      </li>
    `).join('');

    renderPager(totalPages, currentPage);

  }catch(e){
    console.error(e);
    if(my !== reqToken) return;
    statusEl && (statusEl.textContent = '네트워크 오류');
    show(emptyEl);
  }
}

function renderPager(totalPages, currentPage){
  if(!pagerEl) return;
  if(!Number.isFinite(totalPages) || totalPages <= 1){
    pagerEl.innerHTML = '';
    return;
  }

  const prevDisabled = currentPage <= 0;
  const nextDisabled = currentPage >= totalPages - 1;

  let html = '';

  html += `<button class="pg-btn" ${prevDisabled ? 'disabled' : ''} data-page="${currentPage - 1}">이전</button>`;

  // ✅ 1~끝 번호 전부 출력
  for(let p = 0; p < totalPages; p++){
    html += `<button class="pg-num ${p === currentPage ? 'active' : ''}" data-page="${p}">${p + 1}</button>`;
  }

  html += `<button class="pg-btn" ${nextDisabled ? 'disabled' : ''} data-page="${currentPage + 1}">다음</button>`;

  pagerEl.innerHTML = html;

  pagerEl.querySelectorAll('button[data-page]').forEach(btn => {
    btn.addEventListener('click', () => {
      if(btn.disabled) return;
      const p = parseInt(btn.dataset.page, 10);
      if(!Number.isNaN(p)) loadTitles(p);
    });
  });
}


function escapeHtml(s){
  return String(s ?? '').replace(/[&<>"']/g, m => ({
    '&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#39;'
  }[m]));
}

// 버튼 클릭 시 1페이지(0)부터
loadBtn?.addEventListener('click', () => loadTitles(0));

// 장르/정렬 바꾸면 자동으로 1페이지부터 다시 불러오기(원치 않으면 지워도 됨)
genreSel?.addEventListener('change', () => loadTitles(0));
dirSel?.addEventListener('change', () => loadTitles(0));

// 최초 진입 시 자동 로딩
window.addEventListener('DOMContentLoaded', () => loadTitles(0));
