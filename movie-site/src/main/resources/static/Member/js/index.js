<<<<<<< HEAD
// 공통: 헤더 로그인 상태 + 관리자 메뉴 갱신
async function updateAuthNav() {
    const token = localStorage.getItem("accessToken");

    const loginLink = document.querySelector('.auth-nav a[data-role="login"]');
    const signupLink = document.querySelector('.auth-nav a[data-role="signup"]');
    const mypageLink = document.querySelector('.auth-nav a[data-role="mypage"]');
    const adminLink = document.querySelector('.auth-nav a[data-role="admin"]');
    const logoutLink = document.querySelector('.auth-nav a[data-role="logout"]');

    if (!loginLink || !signupLink || !mypageLink || !logoutLink) return;

    // 기본 상태
    if (adminLink) adminLink.style.display = "none";

    if (!token) {
        // 비로그인
        loginLink.style.display = "inline-block";
        signupLink.style.display = "inline-block";
        mypageLink.style.display = "none";
        logoutLink.style.display = "none";
        return;
    }

    // 로그인 상태
    loginLink.style.display = "none";
    signupLink.style.display = "none";
    mypageLink.style.display = "inline-block";
    logoutLink.style.display = "inline-block";

    // 관리자 여부 확인: /api/member/me
    try {
        const res = await fetch('/api/member/me', {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (res.ok) {
            const me = await res.json();
            if (adminLink && me && me.role === 'ADMIN') {
                adminLink.style.display = "inline-block";
            }
        } else if (res.status === 401 || res.status === 403) {
            // 토큰이 만료/무효일 수 있음
            localStorage.removeItem("accessToken");
            loginLink.style.display = "inline-block";
            signupLink.style.display = "inline-block";
            mypageLink.style.display = "none";
            logoutLink.style.display = "none";
            if (adminLink) adminLink.style.display = "none";
        }
    } catch (e) {
        // 네트워크 에러는 무시
        console.warn('Failed to fetch /api/member/me', e);
    }

    // 로그아웃 클릭
    logoutLink.onclick = (e) => {
        e.preventDefault();
        localStorage.removeItem("accessToken");
        window.location.href = "/";
    };
=======
// ====== 공통: 토큰 읽기 ======
function getAuthToken() {
  return (
    localStorage.getItem("accessToken") ||
    localStorage.getItem("token") ||
    sessionStorage.getItem("accessToken") ||
    sessionStorage.getItem("token")
  );
>>>>>>> branch 'practice' of https://github.com/soossang/koreait-teamproject.git
}

<<<<<<< HEAD
document.addEventListener("DOMContentLoaded", () => {
    updateAuthNav();
=======
// ====== 공통: auth 관련 저장값 정리 ======
function clearAuthStorage() {
  // 1) 흔히 쓰는 키들 (명시 삭제)
  const keys = [
    "accessToken", "token", "jwt",
    "tokenType", "expiresIn",
    "loginId", "role", "memberId"
  ];

  keys.forEach((k) => {
    localStorage.removeItem(k);
    sessionStorage.removeItem(k);
  });

  // 2) 혹시 다른 이름으로 저장했을 가능성까지 커버(너무 과격하게 clear()는 안 하고 패턴 삭제)
  for (let i = localStorage.length - 1; i >= 0; i--) {
    const k = localStorage.key(i);
    if (!k) continue;
    if (/token|login|role|member/i.test(k)) localStorage.removeItem(k);
  }
  for (let i = sessionStorage.length - 1; i >= 0; i--) {
    const k = sessionStorage.key(i);
    if (!k) continue;
    if (/token|login|role|member/i.test(k)) sessionStorage.removeItem(k);
  }
}

// ====== 헤더 표시 토글 ======
function updateAuthNav() {
  const token = getAuthToken();

  const loginLink  = document.querySelector('.auth-nav a[data-role="login"]');
  const signupLink = document.querySelector('.auth-nav a[data-role="signup"]');
  const mypageLink = document.querySelector('.auth-nav a[data-role="mypage"]');
  const logoutLink = document.querySelector('.auth-nav a[data-role="logout"]');

  // ✅ "4개 다 있어야만"이 아니라, 있는 것만 토글
  if (token) {
    if (loginLink)  loginLink.style.display = "none";
    if (signupLink) signupLink.style.display = "none";
    if (mypageLink) mypageLink.style.display = "inline-block";
    if (logoutLink) logoutLink.style.display = "inline-block";
  } else {
    if (loginLink)  loginLink.style.display = "inline-block";
    if (signupLink) signupLink.style.display = "inline-block";
    if (mypageLink) mypageLink.style.display = "none";
    if (logoutLink) logoutLink.style.display = "none";
  }
}

// ====== 서버 세션 로그아웃(있으면 호출) ======
function requestServerLogout() {
  // 페이지 이동 중에도 요청이 최대한 살아남게 keepalive 사용
  try {
    fetch("/api/auth/logout", {
      method: "POST",
      credentials: "include",
      keepalive: true
    });
  } catch (e) {
    // 실패해도 프론트 로그아웃은 진행
  }
}

// ====== 핵심: 이벤트 위임으로 어디서든 로그아웃 동작 ======
function bindGlobalLogoutHandler() {
  // capture=true 로 걸면 a태그 기본 이동보다 먼저 가로챔(중요)
  document.addEventListener(
    "click",
    (e) => {
      const a = e.target.closest('a[data-role="logout"]');
      if (!a) return;

      e.preventDefault();

      // 1) 서버 로그아웃 요청(세션 invalidate)
      requestServerLogout();

      // 2) 토큰/로그인정보 삭제
      clearAuthStorage();

      // 3) 즉시 UI 갱신 (메인으로 이동하기 전에 메뉴 먼저 바꿈)
      updateAuthNav();

      // 4) 메인으로 이동 (이미 /여도 replace로 새로고침 효과)
      window.location.replace("/");
    },
    true
  );
}

// ====== 초기화 ======
document.addEventListener("DOMContentLoaded", () => {
  bindGlobalLogoutHandler();
  updateAuthNav();
});

// 뒤로가기/캐시 복원(bfcache)에서도 상태 갱신
window.addEventListener("pageshow", () => {
  updateAuthNav();
>>>>>>> branch 'practice' of https://github.com/soossang/koreait-teamproject.git
});
