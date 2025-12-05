// ===== 공통: 쿼리스트링 =====
function getQueryParam(name) {
    const params = new URLSearchParams(window.location.search);
    return params.get(name);
}

// ===== 공통: 헤더 상태 업데이트 =====
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

// ===== 로그인 =====
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
                loginMessage.textContent = "로그인 실패: 아이디/비밀번호를 확인해주세요.";
                loginMessage.classList.add("error");
                return;
            }

            const data = await res.json();

            if (!data.accessToken) {
                loginMessage.textContent = "서버에서 토큰이 전달되지 않았습니다.";
                loginMessage.classList.add("error");
                return;
            }

            // 토큰 저장
            localStorage.setItem("accessToken", data.accessToken);

            loginMessage.textContent = "로그인 성공! 잠시 후 이동합니다.";
            loginMessage.classList.add("success");

            const redirect = getQueryParam("redirect") || "/Member/mypage.html";
            setTimeout(() => {
                window.location.href = redirect;
            }, 800);
        } catch (error) {
            console.error(error);
            loginMessage.textContent = "네트워크 오류가 발생했습니다.";
            loginMessage.classList.add("error");
        }
    });
}

// ===== 회원가입 =====
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
            msg.textContent = "비밀번호가 일치하지 않습니다.";
            msg.classList.add("error");
            return;
        }

        try {
            const res = await fetch("/api/auth/signup", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    loginId,
                    password,
                    email: email || null,
                    phone: phone || null,
                }),
            });

            if (!res.ok) {
                let errorText = "";

                try {
                    const data = await res.json();
                    errorText = data.message || JSON.stringify(data);
                } catch {
                    try {
                        errorText = await res.text();
                    } catch { }
                }

                msg.textContent =
                    errorText && errorText.length < 120
                        ? errorText
                        : "회원가입에 실패했습니다.";

                msg.classList.add("error");
                return;
            }

            msg.textContent = "회원가입 완료! 로그인 페이지로 이동합니다.";
            msg.classList.add("success");

            setTimeout(() => {
                window.location.href = "/Member/login.html";
            }, 800);
        } catch (error) {
            console.error(error);
            msg.textContent = "네트워크 오류가 발생했습니다.";
            msg.classList.add("error");
        }
    });
}

document.addEventListener("DOMContentLoaded", () => {
    updateAuthNav();
});
