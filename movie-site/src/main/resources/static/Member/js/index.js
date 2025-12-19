/**
 * 공통 인증 스크립트
 * - 헤더 메뉴 표시/숨김
 * - 토큰 키 호환(token/accessToken)
 * - 로그아웃(토큰/세션 정리)
 * - (선택) /login 페이지에서 로그인 폼이 있으면 여기서 직접 처리
 */

// ---------------------------
// 1) 토큰/사용자 정보 저장 키
// ---------------------------
const AUTH_KEYS = {
  // 기존 프로젝트에서 쓰던 키
  accessToken: "accessToken",
  // 서버 응답(JSON)에서 내려오는 필드명과 동일하게 보관하는 경우 대비
  token: "token",
  tokenType: "tokenType",
  loginId: "loginId",
  role: "role",
};

// ---------------------------
// 2) 토큰 유틸
// ---------------------------
function getStoredToken() {
  // 1순위: accessToken
  let t = localStorage.getItem(AUTH_KEYS.accessToken);
  if (t && t.trim()) return t.trim();

  // 2순위: token (혹시 다른 코드가 token으로 저장했을 때)
  t = localStorage.getItem(AUTH_KEYS.token);
  if (t && t.trim()) {
    // accessToken으로 자동 마이그레이션 (헤더 표시 정상화 목적)
    localStorage.setItem(AUTH_KEYS.accessToken, t.trim());
    return t.trim();
  }

  return null;
}

function setStoredAuth(data, responseHeaders) {
  // data: { tokenType, token, expiresIn, loginId, role } 형태 예상
  // (네가 Network에서 본 응답 구조와 동일)

  let token = null;

  // ✅ 1) JSON body에서 token을 우선으로 잡는다
  if (data && typeof data === "object") {
    token = data.token || data.accessToken || data.access_token || null;
  }

  // ✅ 2) 혹시 body에 없으면 Authorization 헤더에서도 찾는다(보완)
  if (!token && responseHeaders) {
    const auth = responseHeaders.get("Authorization") || responseHeaders.get("authorization");
    if (auth && auth.startsWith("Bearer ")) token = auth.substring(7);
  }

  // ✅ 요구사항: “토큰 없다고 경고 띄우지 말고 로그인 되게”
  // → token이 없더라도 여기서 에러를 띄우지 않고 그냥 넘어간다.
  if (token) {
    localStorage.setItem(AUTH_KEYS.accessToken, token);
    // 호환용으로 token 키에도 같이 저장해두면 다른 코드가 있어도 안전함
    localStorage.setItem(AUTH_KEYS.token, token);
  }

  if (data && typeof data === "object") {
    if (data.tokenType) localStorage.setItem(AUTH_KEYS.tokenType, data.tokenType);
    if (data.loginId) localStorage.setItem(AUTH_KEYS.loginId, data.loginId);
    if (data.role) localStorage.setItem(AUTH_KEYS.role, data.role);
  }
}

function clearStoredAuth() {
  localStorage.removeItem(AUTH_KEYS.accessToken);
  localStorage.removeItem(AUTH_KEYS.token);
  localStorage.removeItem(AUTH_KEYS.tokenType);
  localStorage.removeItem(AUTH_KEYS.loginId);
  localStorage.removeItem(AUTH_KEYS.role);
}

// ---------------------------
// 3) 헤더 메뉴 갱신
// ---------------------------
function updateAuthNav() {
  const token = getStoredToken();

  const loginLink = document.querySelector('.auth-nav a[data-role="login"]');
  const signupLink = document.querySelector('.auth-nav a[data-role="signup"]');
  const mypageLink = document.querySelector('.auth-nav a[data-role="mypage"]');
  const logoutLink = document.querySelector('.auth-nav a[data-role="logout"]');

  if (!loginLink || !signupLink || !mypageLink || !logoutLink) return;

  if (token) {
    // 로그인 상태
    loginLink.style.display = "none";
    signupLink.style.display = "none";
    mypageLink.style.display = "inline-block";
    logoutLink.style.display = "inline-block";
  } else {
    // 비로그인 상태
    loginLink.style.display = "inline-block";
    signupLink.style.display = "inline-block";
    mypageLink.style.display = "none";
    logoutLink.style.display = "none";
  }

  // 로그아웃 클릭 이벤트
  logoutLink.onclick = async (e) => {
    e.preventDefault();

    // 1) 서버 세션 로그아웃도 같이 호출 (세션 기반 /board/write 튕김 방지)
    try {
      await fetch("/api/auth/logout", {
        method: "POST",
        credentials: "include",
      });
    } catch (_) {
      // 네트워크 실패해도 로컬 정리는 진행
    }

    // 2) 로컬 토큰/정보 제거
    clearStoredAuth();

    // 3) 홈으로 이동 (기존 /Member/index.html 은 thymeleaf 구조에서 깨질 가능성 있어 / 로 통일)
    window.location.href = "/";
  };
}

// ---------------------------
// 4) 로그인 에러 문구 숨김(범용)
// ---------------------------
function hideTokenWarningIfExists() {
  // "서버에서 토큰이 전달되지 않았습니다." 문구가 DOM에 있으면 숨긴다.
  // (정확한 id/class를 모르는 상황이라 텍스트 기반으로 처리)
  const nodes = Array.from(document.querySelectorAll("p, div, span, small, label"));
  for (const el of nodes) {
    const txt = (el.textContent || "").trim();
    if (txt === "서버에서 토큰이 전달되지 않았습니다.") {
      el.style.display = "none";
    }
  }
}

// ---------------------------
// 5) /login 페이지에서 로그인 폼이 있으면 여기서 처리(옵션)
// ---------------------------
function attachLoginHandlerIfPresent() {
  // login 페이지가 아니어도, 로그인 폼이 있으면 동작하게(재사용성)
  const form =
    document.querySelector("form#loginForm") ||
    document.querySelector('form[data-auth="login"]') ||
    document.querySelector("form");

  if (!form) return;

  // 폼 안에 loginId/password가 없으면 로그인 폼이 아니므로 종료
  const loginIdInput =
    form.querySelector('input[name="loginId"]') ||
    form.querySelector("#loginId") ||
    form.querySelector('input[name="username"]');

  const passwordInput =
    form.querySelector('input[name="password"]') ||
    form.querySelector("#password");

  if (!loginIdInput || !passwordInput) return;

  // 에러 표시 영역(있으면 사용)
  const errorBox =
    document.querySelector("#loginError") ||
    document.querySelector(".login-error") ||
    document.querySelector('[data-role="login-error"]');

  const showError = (msg) => {
    if (errorBox) {
      errorBox.textContent = msg;
      errorBox.style.display = "block";
    }
  };

  const hideError = () => {
    if (errorBox) {
      errorBox.textContent = "";
      errorBox.style.display = "none";
    }
    // 토큰 경고 문구도 같이 숨김
    hideTokenWarningIfExists();
  };

  // ✅ 캡처 단계(true)로 걸어서, 기존 깨진 submit 핸들러가 있어도 먼저 가로챈다
  form.addEventListener(
    "submit",
    async (e) => {
      e.preventDefault();
      e.stopPropagation();

      hideError();

      const loginId = (loginIdInput.value || "").trim();
      const password = (passwordInput.value || "").trim();

      if (!loginId || !password) {
        showError("아이디와 비밀번호를 입력해주세요.");
        return;
      }

      try {
        const res = await fetch("/api/auth/login", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          credentials: "include", // ✅ 세션(JSESSIONID) 유지
          body: JSON.stringify({ loginId, password }),
        });

        const data = await res.json().catch(() => ({}));

        if (!res.ok) {
          // 서버에서 message 내려오면 표시
          showError(data.message || "로그인에 실패했습니다.");
          return;
        }

        // ✅ 토큰/사용자정보 저장 (토큰 없다고 경고 띄우지 않음)
        setStoredAuth(data, res.headers);

        // ✅ 성공이면 바로 이동 (redirect 파라미터 우선)
        const redirect = new URLSearchParams(location.search).get("redirect");
        window.location.href = redirect || "/";
      } catch (err) {
        showError("네트워크 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
      }
    },
    true
  );
}

// ---------------------------
// 6) 초기화
// ---------------------------
document.addEventListener("DOMContentLoaded", () => {
  // 이미 토큰이 있는데 경고 문구가 남아있는 경우 숨김
  hideTokenWarningIfExists();

  updateAuthNav();

  // /login에서 폼이 있으면 여기서 로그인 처리까지 담당
  attachLoginHandlerIfPresent();
});
