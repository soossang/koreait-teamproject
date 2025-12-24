function $(sel){ return document.querySelector(sel); }
function show(el){ el && el.classList.remove('hidden'); }
function hide(el){ el && el.classList.add('hidden'); }

const genreSel = $('#genreSelect');
const dirSel   = $('#dirSelect');
const gridEl   = $('#movieGrid');
const emptyEl  = $('#emptyBox');
const statusEl = $('#status');
const loadBtn  = $('#loadBtn');

// ✅ pager div가 HTML에 없어도 JS가 자동으로 만들어 붙임
let pagerEl = $('#pager');
if(!pagerEl){
  pagerEl = document.createElement('div');
  pagerEl.id = 'pager';
  pagerEl.className = 'pager';
  // movieGrid 아래에 붙이기
  if(gridEl && gridEl.parentNode){
    gridEl.parentNode.insertBefore(pagerEl, emptyEl || null);
  }else{
    document.body.appendChild(pagerEl);
  }
}

let reqToken = 0;
let currentPage = 0;
let totalPages = 0;

// ✅ 장르 페이지 포스터(요청하신 4개만 매핑)
//   - 파일은 /static/RankingGenreboard/img/genre-posters/ 에 넣어둔 버전
//   - (추가로 더 넣고 싶으면 아래 MAP에 제목:파일만 늘리면 됨)
const POSTER_MAP = {
  '1987': encodeURI('/RankingGenreboard/img/genre-posters/1987.png'),
  '7번방의 선물': encodeURI('/RankingGenreboard/img/genre-posters/7번방의 선물.png'),
  '검사외전': encodeURI('/RankingGenreboard/img/genre-posters/검사외전.png'),
  '겨울왕국 1': encodeURI('/RankingGenreboard/img/genre-posters/겨울왕국 1.png'),
  '겨울왕국 2': encodeURI('/RankingGenreboard/img/genre-posters/겨울왕국 2.png'),
  '곡성': encodeURI('/RankingGenreboard/img/genre-posters/곡성.png'),
  '공조 1': encodeURI('/RankingGenreboard/img/genre-posters/공조 1.png'),
  '공조2: 인터내셔날': encodeURI('/RankingGenreboard/img/genre-posters/공조2 인터내셔날.png'),
  '과속스캔들': encodeURI('/RankingGenreboard/img/genre-posters/과속스캔들.png'),
  '관상': encodeURI('/RankingGenreboard/img/genre-posters/관상.png'),
  '광해, 왕이 된 남자': encodeURI('/RankingGenreboard/img/genre-posters/광해, 왕이 된 남자.png'),
  '괴물': encodeURI('/RankingGenreboard/img/genre-posters/괴물.png'),
  '국가대표 1': encodeURI('/RankingGenreboard/img/genre-posters/국가대표 1.png'),
  '국제시장': encodeURI('/RankingGenreboard/img/genre-posters/국제시장.png'),
  '군함도': encodeURI('/RankingGenreboard/img/genre-posters/군함도.png'),
  '극한직업': encodeURI('/RankingGenreboard/img/genre-posters/극한직업.png'),
  '기생충': encodeURI('/RankingGenreboard/img/genre-posters/기생충.png'),
  '내부자들': encodeURI('/RankingGenreboard/img/genre-posters/내부자들.png'),
  '늑대소년': encodeURI('/RankingGenreboard/img/genre-posters/늑대소년.png'),
  '다크 나이트 라이즈': encodeURI('/RankingGenreboard/img/genre-posters/다크 나이트 라이즈.png'),
  '닥터 스트레인지: 대혼돈의 멀티버스': encodeURI('/RankingGenreboard/img/genre-posters/닥터 스트레인지 대혼돈의 멀티버스.png'),
  '도둑들': encodeURI('/RankingGenreboard/img/genre-posters/도둑들.png'),
  '디워': encodeURI('/RankingGenreboard/img/genre-posters/디워.png'),
  '럭키': encodeURI('/RankingGenreboard/img/genre-posters/럭키.png'),
  '레미제라블': encodeURI('/RankingGenreboard/img/genre-posters/레미제라블.png'),
  '마스터': encodeURI('/RankingGenreboard/img/genre-posters/마스터.png'),
  '명량': encodeURI('/RankingGenreboard/img/genre-posters/명량.png'),
  '미녀는 괴로워': encodeURI('/RankingGenreboard/img/genre-posters/미녀는 괴로워.png'),
  '미션 임파서블 3': encodeURI('/RankingGenreboard/img/genre-posters/미션 임파서블 3.png'),
  '미션 임파서블: 고스트프로토콜': encodeURI('/RankingGenreboard/img/genre-posters/미션 임파서블 고스트프로토콜.png'),
  '미션 임파서블: 로그네이션': encodeURI('/RankingGenreboard/img/genre-posters/미션 임파서블 로그네이션.png'),
  '미션 임파서블: 폴아웃': encodeURI('/RankingGenreboard/img/genre-posters/미션 임파서블 폴아웃.png'),
  '밀정': encodeURI('/RankingGenreboard/img/genre-posters/밀정.png'),
  '반지의 제왕: 왕의 귀환': encodeURI('/RankingGenreboard/img/genre-posters/반지의 제왕 왕의 귀환.png'),
  '백두산': encodeURI('/RankingGenreboard/img/genre-posters/백두산.png'),
  '범죄도시1': encodeURI('/RankingGenreboard/img/genre-posters/범죄도시1.png'),
  '범죄도시2': encodeURI('/RankingGenreboard/img/genre-posters/범죄도시2.png'),
  '범죄도시3': encodeURI('/RankingGenreboard/img/genre-posters/범죄도시3.png'),
  '범죄도시4': encodeURI('/RankingGenreboard/img/genre-posters/범죄도시4.png'),
  '베를린': encodeURI('/RankingGenreboard/img/genre-posters/베를린.png'),
  '베테랑1': encodeURI('/RankingGenreboard/img/genre-posters/베테랑1.png'),
  '베테랑2': encodeURI('/RankingGenreboard/img/genre-posters/베테랑2.png'),
  '변호인': encodeURI('/RankingGenreboard/img/genre-posters/변호인.png'),
  '보헤미안 랩소디': encodeURI('/RankingGenreboard/img/genre-posters/보헤미안 랩소디.png'),
  '부산행': encodeURI('/RankingGenreboard/img/genre-posters/부산행.png'),
  '사도': encodeURI('/RankingGenreboard/img/genre-posters/사도.png'),
  '서울의 봄': encodeURI('/RankingGenreboard/img/genre-posters/서울의 봄.png'),
  '설국열차': encodeURI('/RankingGenreboard/img/genre-posters/설국열차.png'),
  '수상한 그녀': encodeURI('/RankingGenreboard/img/genre-posters/수상한 그녀.png'),
  '쉬리': encodeURI('/RankingGenreboard/img/genre-posters/쉬리.png'),
  '스파이더맨: 노 웨이 홈': encodeURI('/RankingGenreboard/img/genre-posters/스파이더맨 노 웨이 홈.png'),
  '스파이더맨: 파 프롬 홈': encodeURI('/RankingGenreboard/img/genre-posters/스파이더맨 파 프롬 홈.png'),
  '스파이더맨: 홈 커밍': encodeURI('/RankingGenreboard/img/genre-posters/스파이더맨 홈 커밍.png'),
  '신과함께-인과 연': encodeURI('/RankingGenreboard/img/genre-posters/신과함께-인과 연.png'),
  '신과함께-죄와 벌': encodeURI('/RankingGenreboard/img/genre-posters/신과함께-죄와 벌.png'),
  '실미도': encodeURI('/RankingGenreboard/img/genre-posters/실미도.png'),
  '써니': encodeURI('/RankingGenreboard/img/genre-posters/써니.png'),
  '아바타1': encodeURI('/RankingGenreboard/img/genre-posters/아바타1.png'),
  '아바타: 물의 길': encodeURI('/RankingGenreboard/img/genre-posters/아바타 물의 길.png'),
  '아이언맨 3': encodeURI('/RankingGenreboard/img/genre-posters/아이언맨 3.png'),
  '아저씨': encodeURI('/RankingGenreboard/img/genre-posters/아저씨.png'),
  '알라딘': encodeURI('/RankingGenreboard/img/genre-posters/알라딘.png'),
  '암살': encodeURI('/RankingGenreboard/img/genre-posters/암살.png'),
  '어벤져스1': encodeURI('/RankingGenreboard/img/genre-posters/어벤져스1.png'),
  '어벤져스: 에이지 오브 울트론': encodeURI('/RankingGenreboard/img/genre-posters/어벤져스 에이지 오브 울트론.png'),
  '어벤져스: 엔드게임': encodeURI('/RankingGenreboard/img/genre-posters/어벤져스 엔드게임.png'),
  '어벤져스: 인피니티 워': encodeURI('/RankingGenreboard/img/genre-posters/어벤져스 인피니티 워.png'),
  '엑시트': encodeURI('/RankingGenreboard/img/genre-posters/엑시트.png'),
  '엘리멘탈': encodeURI('/RankingGenreboard/img/genre-posters/엘리멘탈.png'),
  '연평해전': encodeURI('/RankingGenreboard/img/genre-posters/연평해전.png'),
  '왕의 남자': encodeURI('/RankingGenreboard/img/genre-posters/왕의 남자.png'),
  '웰컴 투 동막골': encodeURI('/RankingGenreboard/img/genre-posters/웰컴 투 동막골.png'),
  '은밀하게 위대하게': encodeURI('/RankingGenreboard/img/genre-posters/은밀하게 위대하게.png'),
  '인사이드 아웃 2': encodeURI('/RankingGenreboard/img/genre-posters/인사이드 아웃 2.png'),
  '인셉션': encodeURI('/RankingGenreboard/img/genre-posters/인셉션.png'),
  '인천상륙작전': encodeURI('/RankingGenreboard/img/genre-posters/인천상륙작전.png'),
  '인터스텔라': encodeURI('/RankingGenreboard/img/genre-posters/인터스텔라.png'),
  '전우치': encodeURI('/RankingGenreboard/img/genre-posters/전우치.png'),
  '좋은 놈, 나쁜 놈, 이상한 놈': encodeURI('/RankingGenreboard/img/genre-posters/좋은 놈, 나쁜 놈, 이상한 놈.png'),
  '쥬라기 월드: 폴른 킹덤': encodeURI('/RankingGenreboard/img/genre-posters/쥬라기 월드 폴른 킹덤.png'),
  '청년경찰': encodeURI('/RankingGenreboard/img/genre-posters/청년경찰.png'),
  '최종병기 활': encodeURI('/RankingGenreboard/img/genre-posters/최종병기 활.png'),
  '캡틴 마블': encodeURI('/RankingGenreboard/img/genre-posters/캡틴 마블.png'),
  '캡틴 아메리카: 시빌 워': encodeURI('/RankingGenreboard/img/genre-posters/캡틴 아메리카 시빌 워.png'),
  '킹스맨 : 시크릿 에이전트': encodeURI('/RankingGenreboard/img/genre-posters/킹스맨 시크릿 에이전트.png'),
  '타짜1': encodeURI('/RankingGenreboard/img/genre-posters/타짜1.png'),
  '탑건: 매버릭': encodeURI('/RankingGenreboard/img/genre-posters/탑건 매버릭.png'),
  '태극기 휘날리며': encodeURI('/RankingGenreboard/img/genre-posters/태극기 휘날리며.png'),
  '택시운전사': encodeURI('/RankingGenreboard/img/genre-posters/택시운전사.png'),
  '터널': encodeURI('/RankingGenreboard/img/genre-posters/터널.png'),
  '투사부일체': encodeURI('/RankingGenreboard/img/genre-posters/투사부일체.png'),
  '트랜스포머 3': encodeURI('/RankingGenreboard/img/genre-posters/트랜스포머 3.png'),
  '트랜스포머 1': encodeURI('/RankingGenreboard/img/genre-posters/트랜스포머 1.png'),
  '트랜스포머: 패자의 역습': encodeURI('/RankingGenreboard/img/genre-posters/트랜스포머 패자의 역습.png'),
  '파묘': encodeURI('/RankingGenreboard/img/genre-posters/파묘.png'),
  '한산: 용의 출현': encodeURI('/RankingGenreboard/img/genre-posters/한산 용의 출현.png'),
  '해운대': encodeURI('/RankingGenreboard/img/genre-posters/해운대.png'),
  '해적: 바다로 간 산적': encodeURI('/RankingGenreboard/img/genre-posters/해적 바다로 간 산적.png'),
  '화려한 휴가': encodeURI('/RankingGenreboard/img/genre-posters/화려한 휴가.png'),
  '히말라야': encodeURI('/RankingGenreboard/img/genre-posters/히말라야.png'),  
};

/** ✅ 네이버 검색 URL 생성 */
function buildNaverUrl(title){
  const q = encodeURIComponent(`영화 ${String(title ?? '').trim()}`).replace(/%20/g, '+');
  return `https://search.naver.com/search.naver?where=nexearch&query=${q}`;
}

async function loadTitles(page = 0){
  const my = ++reqToken;

  currentPage = Math.max(0, page);

  const genre = (genreSel?.value ?? '').trim();
  const dir   = (dirSel?.value ?? 'asc');

  const url = `/api/movies?genre=${encodeURIComponent(genre)}&dir=${encodeURIComponent(dir)}&page=${encodeURIComponent(currentPage)}&sortBy=movie`;

  statusEl && (statusEl.textContent = '로딩중…');
  gridEl && (gridEl.innerHTML = '');
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

    const pageText = totalPages > 0 ? ` (${currentPage + 1}/${totalPages})` : '';
    statusEl && (statusEl.textContent = `${totalElements}건${pageText}`);

    // ✅ 카드형 렌더링(4열) + 포스터(해당 영화만)
    gridEl.innerHTML = items.map(it => {
      const title = (it && it.movieNm) ? String(it.movieNm) : '';
      const safeTitle = escapeHtml(title);
      const href = buildNaverUrl(title);
      const poster = POSTER_MAP[title] || '';

      const posterHtml = poster
        ? `<img class="poster-img" src="${poster}" alt="${safeTitle} 포스터" loading="lazy" />`
        : `<div class="poster-placeholder" aria-label="포스터 없음">${safeTitle}</div>`;

      return `
        <div class="movie-card">
          <a class="movie-link" href="${href}" target="_blank" rel="noopener noreferrer">
            <div class="poster-wrap">${posterHtml}</div>
            <div class="movie-meta">
              <div class="movie-title" title="${safeTitle}">${safeTitle}</div>
              <div class="movie-sub">네이버에서 보기</div>
            </div>
          </a>
        </div>
      `;
    }).join('');

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

loadBtn?.addEventListener('click', () => loadTitles(0));
genreSel?.addEventListener('change', () => loadTitles(0));
dirSel?.addEventListener('change', () => loadTitles(0));
window.addEventListener('DOMContentLoaded', () => loadTitles(0));
