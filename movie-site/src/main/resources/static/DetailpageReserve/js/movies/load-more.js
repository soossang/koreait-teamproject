document.addEventListener("DOMContentLoaded", () => {
  const items = Array.from(document.querySelectorAll(".movie-item"));
  const btn = document.getElementById("loadMoreBtn");
  const hint = document.getElementById("loadMoreHint");

  if (!items.length || !btn) return;

  const batchSize = 12; // ✅ 한 번에 더 보여줄 개수 (원하면 8/16 등으로 조정)
  let visibleCount = 0;

  const update = () => {
    items.forEach((el, idx) => {
      el.style.display = idx < visibleCount ? "" : "none";
    });

    hint.textContent = `${Math.min(visibleCount, items.length)} / ${items.length} 표시중`;

    if (visibleCount >= items.length) {
      btn.style.display = "none";
      hint.textContent = `마지막 영화까지 모두 표시했습니다. (${items.length}개)`;
    } else {
      btn.style.display = "";
    }
  };

  // 초기 표시
  visibleCount = Math.min(batchSize, items.length);
  update();

  btn.addEventListener("click", () => {
    visibleCount = Math.min(visibleCount + batchSize, items.length);
    update();
    // 더보기 누른 뒤 화면이 자연스럽게 내려가도록(선택)
    btn.scrollIntoView({ behavior: "smooth", block: "center" });
  });
});
