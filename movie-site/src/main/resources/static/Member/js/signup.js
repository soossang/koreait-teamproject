document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("signupForm");
  if (!form) return;

  const loginIdEl = document.getElementById("signupLoginId");
  const pwEl = document.getElementById("signupPassword");
  const pw2El = document.getElementById("signupPasswordConfirm");
  const emailEl = document.getElementById("signupEmail");
  const phoneEl = document.getElementById("signupPhone");
  const msgEl = document.getElementById("signupMessage");

  const showMsg = (text) => {
    if (!msgEl) return;
    msgEl.textContent = text || "";
    msgEl.style.display = text ? "block" : "none";
  };

  const clearMsg = () => showMsg("");

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    clearMsg();

    const loginId = (loginIdEl?.value || "").trim();
    const password = (pwEl?.value || "").trim();
    const passwordConfirm = (pw2El?.value || "").trim();
    const email = (emailEl?.value || "").trim();
    const phone = (phoneEl?.value || "").trim();

    if (!loginId) return showMsg("아이디를 입력해주세요.");
    if (!password) return showMsg("비밀번호를 입력해주세요.");
    if (password.length < 4) return showMsg("비밀번호는 4자 이상으로 입력해주세요.");
    if (password !== passwordConfirm) return showMsg("비밀번호가 일치하지 않습니다.");

    const payload = {
      loginId,
      password,
      email: email || null,
      phone: phone || null,
    };

    try {
      const res = await fetch("/api/auth/signup", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      const data = await res.json().catch(() => ({}));

      if (!res.ok) {
        // 서버에서 message 내려주면 우선 사용
        return showMsg(data.message || "회원가입에 실패했습니다.");
      }

      // 성공
      showMsg(data.message || "회원가입이 완료되었습니다.");
      // 잠깐 보여주고 로그인으로 이동
      setTimeout(() => {
        window.location.href = "/login";
      }, 500);
    } catch (err) {
      showMsg("네트워크 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    }
  });
});
