// 헤더 로그인 상태 갱신
function updateAuthNav() {
    const token = localStorage.getItem("accessToken");

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

    // 로그아웃 클릭 이벤트 (중복 등록 방지용 먼저 제거 후 다시 등록)
    logoutLink.onclick = (e) => {
        e.preventDefault();
        localStorage.removeItem("accessToken");
        // 메인으로 이동
        window.location.href = "/Member/index.html";
    };
}

document.addEventListener("DOMContentLoaded", () => {
    updateAuthNav();

    // 나중에 /api/movies 연동할 때 여기서 fetch 호출하면 됨
    // 지금은 하드코딩 카드 그대로 사용
});
