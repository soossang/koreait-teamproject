// 홈 전용: 상단 대표 영화(id=1) + 박스오피스(id 2~6) 렌더링
document.addEventListener("DOMContentLoaded", async () => {
  // ✅ 홈에서만 실행 (다른 페이지에 실수로 포함돼도 안전)
  const path = window.location.pathname;
  const isHome =
    path === "/" ||
    path === "" ||
    path === "/Member/index.html" ||
    path === "/index.html";

  if (!isHome) return;

  const heroTitle = document.getElementById("heroTitle");
  const heroMeta = document.getElementById("heroMeta");
  const heroPoster = document.getElementById("heroPoster");
  const heroReserveBtn = document.getElementById("heroReserveBtn");
  const heroDetailBtn = document.getElementById("heroDetailBtn");
  const boxofficeGrid = document.getElementById("boxofficeGrid");

  // --- helper ---
  const safeText = (v, fallback = "") => (v === null || v === undefined || v === "" ? fallback : String(v));

  // 1) 대표 영화(id=1) - 서버에서 가져오되, 실패하면 HTML 기본값 유지
  try {
    const res = await fetch("/api/home/featured", { headers: { "Accept": "application/json" } });
    if (res.ok) {
      const m = await res.json();
      if (m) {
        if (heroTitle) heroTitle.textContent = safeText(m.title, heroTitle.textContent);
        if (heroMeta) {
          const meta = [m.genre, m.rating].filter(Boolean).join(" · ");
          heroMeta.textContent = meta || heroMeta.textContent;
        }
        if (heroPoster && m.posterUrl) heroPoster.src = m.posterUrl;
        if (heroPoster && m.title) heroPoster.alt = m.title;
        if (heroReserveBtn && m.id) heroReserveBtn.href = `/movies/${m.id}`;
        if (heroDetailBtn && m.id) heroDetailBtn.href = `/movies/${m.id}`;
      }
    }
  } catch (e) {
    // ignore - fallback to HTML default
  }

  // 2) 박스오피스(영화 id=2,3,4,5,6)
  if (!boxofficeGrid) return;

  try {
    const res = await fetch("/api/home/boxoffice", { headers: { "Accept": "application/json" } });
    if (!res.ok) return;

    const list = await res.json();
    if (!Array.isArray(list)) return;

    // 기존 하드코딩 카드 제거 후 재렌더링
    boxofficeGrid.innerHTML = "";

    list.forEach((m) => {
      const id = m?.id;
      const title = safeText(m?.title, "제목 없음");
      const genre = safeText(m?.genre, "");
      const rating = safeText(m?.rating, "");
      const posterUrl = safeText(m?.posterUrl, "/Member/img/default-poster.png");

      const article = document.createElement("article");
      article.className = "movie-card";

      // 기존 카드 스타일(movie-poster background-image)과 맞춤
      article.innerHTML = `
        <div class="movie-poster" style="background-image:url('${posterUrl}')"></div>
        <div class="movie-info">
          <h3 class="movie-title">${title}</h3>
          <p class="movie-meta">${[genre, rating].filter(Boolean).join(" · ")}</p>
          <a class="movie-btn" href="/movies/${id}">예매</a>
        </div>
      `;

      boxofficeGrid.appendChild(article);
    });
  } catch (e) {
    // ignore
  }
});
