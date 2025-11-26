const api = {
  top: (limit) => `/api/boxoffice?limit=${limit}`,
  genres: () => `/api/genres`,
  movies: (genre, page=0, size=20) =>
    genre ? `/api/movies?genre=${encodeURIComponent(genre)}&page=${page}&size=${size}`
          : `/api/movies?page=${page}&size=${size}`
};

let page = 0, size = 20, totalPages = 0, currentGenre = null;

function showTab(tab) {
  document.getElementById('tab-top').classList.toggle('active', tab==='top');
  document.getElementById('tab-genre').classList.toggle('active', tab==='genre');
  document.getElementById('panel-top').classList.toggle('hidden', tab!=='top');
  document.getElementById('panel-genre').classList.toggle('hidden', tab!=='genre');
}

async function loadTopBoxOffice() {
  const limit = document.getElementById('limitSelect').value;
  const res = await fetch(api.top(limit));
  const items = await res.json();
  const box = document.getElementById('topGrid');
  box.innerHTML = items.map((m, i) => cardHtml(m, i+1)).join('');
}

async function loadGenres() {
  const res = await fetch(api.genres());
  const genres = await res.json();
  const sel = document.getElementById('genreSelect');
  sel.innerHTML = `<option value="">전체</option>` + genres.map(g => `<option>${g}</option>`).join('');
}

async function loadMoviesByGenre() {
  currentGenre = document.getElementById('genreSelect').value || null;
  page = 0;
  await fetchMovies();
}

async function fetchMovies() {
  const res = await fetch(api.movies(currentGenre, page, size));
  const data = await res.json();
  totalPages = data.totalPages ?? 0;
  document.getElementById('genreGrid').innerHTML =
    (data.content || []).map(m => cardHtml(m)).join('');
  document.getElementById('pageInfo').textContent = `${page+1} / ${Math.max(totalPages,1)}`;
}

function nextPage() { if (page+1 < totalPages) { page++; fetchMovies(); } }
function prevPage() { if (page > 0) { page--; fetchMovies(); } }

function cardHtml(m, rank) {
  return `
    <div class="card">
      ${rank ? `<div class="muted">#${rank}</div>` : ``}
      <div class="title">${escapeHtml(m.title || '제목 없음')}</div>
      <div class="muted">${escapeHtml(m.genre || '-')}${m.year?` · ${m.year}`:''}</div>
      ${m.boxOfficeGross ? `<div class="muted">흥행: ${m.boxOfficeGross.toLocaleString()}</div>`:''}
    </div>`;
}

function escapeHtml(s){ return String(s ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c])); }

// 초기 로드
showTab('top');
loadTopBoxOffice();
loadGenres().then(loadMoviesByGenre);
