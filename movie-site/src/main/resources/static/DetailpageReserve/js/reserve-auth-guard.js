// 예약/예매 기능 로그인 가드
(function () {
  function isLoggedIn() {
    return !!localStorage.getItem("accessToken");
  }

  function buildRedirectUrl(targetUrl) {
    // targetUrl이 없으면 현재 페이지로
    const target = targetUrl || (window.location.pathname + window.location.search);
    return "/login?redirect=" + encodeURIComponent(target);
  }

  function requireLoginOrRedirect(targetUrl) {
    if (isLoggedIn()) return true;

    alert("로그인을 완료한 후에 예매하기가 가능합니다.");
    window.location.href = buildRedirectUrl(targetUrl);
    return false;
  }

  // 1) 예매하기 버튼 클릭 가드 (a.btn-reserve)
  document.addEventListener("click", function (e) {
    const a = e.target.closest("a.btn-reserve");
    if (!a) return;

    // 매진/비활성 버튼은 제외
    if (a.classList.contains("disabled") || a.getAttribute("aria-disabled") === "true") return;

    const href = a.getAttribute("href");
    if (!requireLoginOrRedirect(href)) {
      e.preventDefault();
      e.stopPropagation();
    }
  }, true);

  // 2) /reserve/{id} (좌석선택 페이지) 진입 가드: body에 data-require-login="true"가 있으면 체크
  document.addEventListener("DOMContentLoaded", function () {
    const body = document.body;
    if (body && body.dataset && body.dataset.requireLogin === "true") {
      requireLoginOrRedirect(window.location.pathname + window.location.search);
    }
  });
})();
