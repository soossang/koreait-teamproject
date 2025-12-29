// ==============================================================
// Film Box 공통 헤더 인증 UI
//  - 로그인/회원가입/마이페이지/로그아웃/관리자 메뉴 토글
//  - (있다면) 서버 세션 로그아웃 엔드포인트 호출
// ==============================================================

// ====== 공통: 토큰 읽기 ======
function getAuthToken() {
  return (
    localStorage.getItem("accessToken") ||
    localStorage.getItem("token") ||
    sessionStorage.getItem("accessToken") ||
    sessionStorage.getItem("token")
  );
}

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

  // 2) 혹시 다른 이름으로 저장했을 가능성까지 커버
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

// ====== 헤더 표시 토글 + 관리자 메뉴 갱신 ======
async function updateAuthNav() {
  const token = getAuthToken();

  // 어떤 페이지는 (과거 버전 헤더 등) 관리자 링크가 없을 수 있어서
  // JS에서 자동으로 생성해 전체 페이지에서 동일하게 동작하도록 보강한다.
  const authNav = document.querySelector('.auth-nav');

  const loginLink = document.querySelector('.auth-nav a[data-role="login"]');
  const signupLink = document.querySelector('.auth-nav a[data-role="signup"]');
  const mypageLink = document.querySelector('.auth-nav a[data-role="mypage"]');
  let adminLink = document.querySelector('.auth-nav a[data-role="admin"]');
  const logoutLink = document.querySelector('.auth-nav a[data-role="logout"]');

  // 관리자 링크가 없으면 동적으로 추가 (logout 앞에 끼워 넣기)
  if (!adminLink && authNav) {
    adminLink = document.createElement('a');
    adminLink.href = '/admin';
    adminLink.textContent = '관리자';
    adminLink.setAttribute('data-role', 'admin');
    adminLink.style.display = 'none';

    if (logoutLink && logoutLink.parentElement === authNav) {
      authNav.insertBefore(adminLink, logoutLink);
    } else {
      authNav.appendChild(adminLink);
    }
  }

  // 기본값
  if (adminLink) adminLink.style.display = "none";

  if (!token) {
    if (loginLink) loginLink.style.display = "inline-block";
    if (signupLink) signupLink.style.display = "inline-block";
    if (mypageLink) mypageLink.style.display = "none";
    if (logoutLink) logoutLink.style.display = "none";
    return;
  }

  // 로그인 상태
  if (loginLink) loginLink.style.display = "none";
  if (signupLink) signupLink.style.display = "none";
  if (mypageLink) mypageLink.style.display = "inline-block";
  if (logoutLink) logoutLink.style.display = "inline-block";

  // 관리자 여부 확인: /api/member/me
  try {
    const res = await fetch("/api/member/me", {
      headers: { Authorization: `Bearer ${token}` },
      credentials: "include"
    });

    if (res.ok) {
      const me = await res.json();
      if (adminLink && me && me.role === "ADMIN") {
        adminLink.style.display = "inline-block";
      }
      return;
    }

    // 401/403이면 토큰 만료/무효일 수 있음 → 정리 후 비로그인 UI
    if (res.status === 401 || res.status === 403) {
      clearAuthStorage();
      if (adminLink) adminLink.style.display = "none";
      if (loginLink) loginLink.style.display = "inline-block";
      if (signupLink) signupLink.style.display = "inline-block";
      if (mypageLink) mypageLink.style.display = "none";
      if (logoutLink) logoutLink.style.display = "none";
    }
  } catch (e) {
    // 네트워크 에러는 무시
    console.warn("Failed to fetch /api/member/me", e);
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

// ====== 이벤트 위임으로 어디서든 로그아웃 동작 ======
function bindGlobalLogoutHandler() {
  // capture=true 로 걸면 a태그 기본 이동보다 먼저 가로챔
  document.addEventListener(
    "click",
    (e) => {
      const a = e.target.closest('a[data-role="logout"]');
      if (!a) return;

      e.preventDefault();
      requestServerLogout();
      clearAuthStorage();
      updateAuthNav();
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
});
