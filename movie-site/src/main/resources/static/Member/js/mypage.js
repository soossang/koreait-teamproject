function updateAuthNav() {
    const token = localStorage.getItem("accessToken");

    const loginLink = document.querySelector('.auth-nav a[data-role="login"]');
    const signupLink = document.querySelector('.auth-nav a[data-role="signup"]');
    const mypageLink = document.querySelector('.auth-nav a[data-role="mypage"]');
    const logoutLink = document.querySelector('.auth-nav a[data-role="logout"]');

    if (!loginLink || !signupLink || !mypageLink || !logoutLink) return;

    if (token) {
        loginLink.style.display = "none";
        signupLink.style.display = "none";
        mypageLink.style.display = "inline-block";
        logoutLink.style.display = "inline-block";
    } else {
        loginLink.style.display = "inline-block";
        signupLink.style.display = "inline-block";
        mypageLink.style.display = "none";
        logoutLink.style.display = "none";
    }

    logoutLink.onclick = (e) => {
        e.preventDefault();
        localStorage.removeItem("accessToken");
        window.location.href = "/Member/index.html";
    };
}

document.addEventListener("DOMContentLoaded", async () => {
    const token = localStorage.getItem("accessToken");

    // 비로그인 상태면 로그인 페이지로
    if (!token) {
        window.location.href = "/Member/login.html?redirect=/Member/mypage.html";
        return;
    }

    updateAuthNav();

    const box = document.getElementById("profileBox");
    if (!box) return;

    try {
        // 백엔드에 /api/member/me 같은 엔드포인트가 있다면 여기에 연결
        const res = await fetch("/api/member/me", {
            headers: {
                Authorization: "Bearer " + token,
            },
        });

        if (!res.ok) {
            box.innerHTML = "<p>회원 정보를 불러오지 못했습니다.</p>";
            return;
        }

        const me = await res.json();

        box.innerHTML = `
            <p><strong>아이디</strong> : ${me.loginId || ""}</p>
            <p><strong>이메일</strong> : ${me.email || "-"}</p>
            <p><strong>휴대폰</strong> : ${me.phone || "-"}</p>
        `;
    } catch (e) {
        console.error(e);
        box.innerHTML = "<p>네트워크 오류가 발생했습니다.</p>";
    }
});
