document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("loginForm");
  const loginIdEl = document.getElementById("loginId");
  const passwordEl = document.getElementById("password");
  const msgEl = document.getElementById("loginMessage");

  if (!form || !loginIdEl || !passwordEl) return;

  const showMsg = (text) => {
    if (!msgEl) return;
    msgEl.textContent = text || "";
    msgEl.style.display = text ? "block" : "none";
  };

  const clearMsg = () => showMsg("");

  // redirect 파라미터가 있으면 그곳으로, 없으면 홈
  const getRedirectUrl = () => {
    const redirect = new URLSearchParams(location.search).get("redirect");
    return redirect || "/";
  };

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    clearMsg();

    const loginId = (loginIdEl.value || "").trim();
    const password = (passwordEl.value || "").trim();

    if (!loginId || !password) {
      showMsg("아이디와 비밀번호를 입력해주세요.");
      return;
    }

    try {
      const res = await fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include", // ✅ 세션 쿠키(JSESSIONID) 유지: /board/write 튕김 방지
        body: JSON.stringify({ loginId, password }),
      });

      const data = await res.json().catch(() => ({}));

      if (!res.ok) {
        // 서버가 message를 주면 표시, 없으면 기본 문구
        showMsg(data.message || "로그인에 실패했습니다.");
        return;
      }

      // ✅ 서버 응답(JSON)에서 토큰 추출 (너가 확인한 그대로 token 필드)
      let token = data.token || data.accessToken || data.access_token || null;

      // 혹시 body에 없으면 Authorization 헤더에서도 시도 (안전장치)
      if (!token) {
        const auth = res.headers.get("Authorization") || res.headers.get("authorization");
        if (auth && auth.startsWith("Bearer ")) token = auth.substring(7);
      }

      // ✅ 핵심: 기존 index.js가 accessToken만 보니까 accessToken으로 저장
      if (token) {
        localStorage.setItem("accessToken", token);
        // 호환용으로 token도 저장(다른 스크립트가 token을 볼 수 있으니)
        localStorage.setItem("token", token);
      }

      // loginId/role 저장(선택)
      if (data.loginId) localStorage.setItem("loginId", data.loginId);
      if (data.role) localStorage.setItem("role", data.role);

      // ✅ 사용자가 싫어하는 문구는 절대 띄우지 않음
      clearMsg();

      // ✅ 바로 이동
      window.location.href = getRedirectUrl();
    } catch (err) {
      showMsg("네트워크 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    }
  });
});
