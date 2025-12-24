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
}

document.addEventListener("DOMContentLoaded", () => {
    updateAuthNav();
});
