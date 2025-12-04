// ===== 공통: 쿼리스트링 파서 =====
function getQueryParam(name) {
    const params = new URLSearchParams(window.location.search);
    return params.get(name);
}

// ===== 공통: 헤더 로그인 상태 갱신 =====
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

    logoutLink.onclick = (e) => {
        e.preventDefault();
        localStorage.removeItem("accessToken");
        window.location.href = "/Member/index.html";
    };
}

// ===== 로그인 처리 =====
const loginForm = document.getElementById("loginForm");
if (loginForm) {
    loginForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        const loginMessage = document.getElementById("loginMessage");
        loginMessage.textContent = "";
        loginMessage.className = "auth-message";

        const loginId = document.getElementById("loginId").value.trim();
        const password = document.getElementById("password").value;

        if (!loginId || !password) {
            loginMessage.textContent = "아이디와 비밀번호를 입력해 주세요.";
            loginMessage.classList.add("error");
            return;
        }

        try {
            const res = await fetch("/api/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ loginId, password }),
            });

            if (!res.ok) {
                loginMessage.textContent =
                    "로그인에 실패했습니다. 아이디/비밀번호를 확인해 주세요.";
                loginMessage.classList.add("error");
                return;
            }

            const data = await res.json();
            if (!data.accessToken) {
                loginMessage.textContent = "서버 응답에 토큰이 없습니다.";
                loginMessage.classList.add("error");
                return;
            }

            // 토큰 저장
            localStorage.setItem("accessToken", data.accessToken);

            loginMessage.textContent = "로그인되었습니다. 잠시 후 이동합니다.";
            loginMessage.classList.add("success");

            const redirect = getQueryParam("redirect") || "/Member/mypage.html";
            setTimeout(() => {
                window.location.href = redirect;
            }, 600);
        } catch (err) {
            console.error(err);
            loginMessage.textContent = "네트워크 오류가 발생했습니다.";
            loginMessage.classList.add("error");
        }
    });
}

// ====================== 회원가입 처리 ======================
const signupForm = document.getElementById("signupForm");
if (signupForm) {
    signupForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        const msg = document.getElementById("signupMessage");
        msg.textContent = "";
        msg.className = "auth-message";

        const loginId = document.getElementById("signupLoginId").value.trim();
        const password = document.getElementById("signupPassword").value;
        const passwordConfirm = document.getElementById("signupPasswordConfirm").value;
        const email = document.getElementById("signupEmail").value.trim();
        const phone = document.getElementById("signupPhone").value.trim();

        if (!loginId || !password) {
            msg.textContent = "아이디와 비밀번호는 필수입니다.";
            msg.classList.add("error");
            return;
        }

        if (password !== passwordConfirm) {
            msg.textContent = "비밀번호와 비밀번호 확인이 일치하지 않습니다.";
            msg.classList.add("error");
            return;
        }

        try {
            const res = await fetch("/api/member/signup", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    loginId,
                    password,
                    email: email || null,
                    phone: phone || null,
                }),
            });

            // ✅ 실패한 경우, 백엔드에서 내려준 메시지 최대한 보여주기
            if (!res.ok) {
                let errorText = "";

                try {
                    // JSON으로 에러 내려주는 경우 (예: { "message": "아이디 중복" })
                    const data = await res.json();
                    errorText = data.message || JSON.stringify(data);
                } catch (e) {
                    // JSON이 아니면 그냥 텍스트로 받기 (예: 500 에러 HTML 포함)
                    try {
                        errorText = await res.text();
                    } catch (ignored) { }
                }

                console.error("회원가입 실패 응답:", errorText);
                msg.textContent =
                    errorText && errorText.length < 120
                        ? errorText
                        : "회원가입에 실패했습니다. 입력 내용을 확인해 주세요.";
                msg.classList.add("error");
                return;
            }

            // ✅ 성공
            msg.textContent = "회원가입이 완료되었습니다. 로그인 페이지로 이동합니다.";
            msg.classList.add("success");

            setTimeout(() => {
                window.location.href = "/Member/login.html";
            }, 800);
        } catch (err) {
            console.error(err);
            msg.textContent = "네트워크 오류가 발생했습니다.";
            msg.classList.add("error");
        }
    });
}


document.addEventListener("DOMContentLoaded", () => {
    updateAuthNav();
});
