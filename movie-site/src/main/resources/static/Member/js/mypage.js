// ==============================================================
// ⚠️ 주의
//  - 이 프로젝트는 공통 헤더 토글/관리자 메뉴 표시를 /Member/js/index.js 에서 처리합니다.
//  - 그런데 이 파일에서 updateAuthNav 라는 같은 이름을 쓰면, index.js의 함수를 덮어써서
//    "관리자" 메뉴가 마이페이지에서만 안 보이는 문제가 생깁니다.
//  - 그래서 마이페이지 전용 함수는 이름을 바꿔서(로컬) 충돌을 방지합니다.
// ==============================================================

function getAuthTokenLocal() {
    return (
        localStorage.getItem("accessToken") ||
        localStorage.getItem("token") ||
        sessionStorage.getItem("accessToken") ||
        sessionStorage.getItem("token")
    );
}

// ===== (fallback용) 헤더 로그인 상태 업데이트 =====
// index.js가 로드되지 않은 페이지에서만 사용됩니다.
function updateAuthNavLocal() {
    const token = getAuthTokenLocal();

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

    // logout 동작은 공통 스크립트(index.js)가 처리합니다.
}

function redirectToLogin() {
    const here = window.location.pathname + window.location.search;
    window.location.href = "/login?redirect=" + encodeURIComponent(here);
}

async function restoreAuthTokenFromSession() {
    try {
        const response = await fetch("/api/auth/session-token", {
            credentials: "include",
        });
        if (!response.ok) return null;

        const data = await response.json();
        const token = data.token || data.accessToken || null;
        if (!token) return null;

        localStorage.setItem("accessToken", token);
        localStorage.setItem("token", token);
        if (data.loginId) localStorage.setItem("loginId", data.loginId);
        if (data.role) localStorage.setItem("role", data.role);
        return token;
    } catch (error) {
        console.warn("로그인 세션으로 토큰을 복구하지 못했습니다.", error);
        return null;
    }
}

function fetchMyProfile(token) {
    return fetch("/api/member/me", {
        headers: { Authorization: "Bearer " + token },
        credentials: "include",
    });
}

function escapeHtml(value) {
    return (value ?? "")
        .toString()
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function renderMemberProfile(member) {
    const box = document.getElementById("profileBox");
    if (!box) return;

    box.innerHTML = `
        <p><b>아이디</b>: ${escapeHtml(member.loginId || "-")}</p>
        <p><b>이메일</b>: ${escapeHtml(member.email || "-")}</p>
        <p><b>휴대폰</b>: ${escapeHtml(member.phone || "-")}</p>
    `;
}

function setupAccountEditor(token, initialMember) {
    const toggleButton = document.getElementById("accountEditToggle");
    const panel = document.getElementById("accountEditPanel");
    const form = document.getElementById("accountEditForm");
    const cancelButton = document.getElementById("accountEditCancel");
    const submitButton = document.getElementById("accountEditSubmit");
    const message = document.getElementById("accountEditMessage");
    const loginIdInput = document.getElementById("accountLoginId");
    const currentPasswordInput = document.getElementById("accountCurrentPassword");
    const newPasswordInput = document.getElementById("accountNewPassword");
    const newPasswordConfirmInput = document.getElementById("accountNewPasswordConfirm");
    const emailInput = document.getElementById("accountEmail");
    const phoneInput = document.getElementById("accountPhone");

    if (!toggleButton || !panel || !form || !submitButton) return;

    let member = initialMember;

    const showMessage = (text, type = "error") => {
        if (!message) return;
        message.textContent = text || "";
        message.classList.toggle("is-error", Boolean(text) && type === "error");
        message.classList.toggle("is-success", Boolean(text) && type === "success");
    };

    const clearPasswords = () => {
        if (currentPasswordInput) currentPasswordInput.value = "";
        if (newPasswordInput) newPasswordInput.value = "";
        if (newPasswordConfirmInput) newPasswordConfirmInput.value = "";
    };

    const fillForm = () => {
        if (loginIdInput) loginIdInput.value = member.loginId || "";
        if (emailInput) emailInput.value = member.email || "";
        if (phoneInput) phoneInput.value = member.phone || "";
        clearPasswords();
        showMessage("");
    };

    const setPanelOpen = (open) => {
        panel.hidden = !open;
        toggleButton.setAttribute("aria-expanded", String(open));
        toggleButton.textContent = open ? "계정 수정 닫기" : "내 계정 수정";
        if (open) {
            fillForm();
            currentPasswordInput?.focus();
        }
    };

    toggleButton.addEventListener("click", () => setPanelOpen(panel.hidden));
    cancelButton?.addEventListener("click", () => setPanelOpen(false));

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        showMessage("");

        const currentPassword = currentPasswordInput?.value || "";
        const newPassword = newPasswordInput?.value || "";
        const newPasswordConfirm = newPasswordConfirmInput?.value || "";
        const email = emailInput?.value.trim() || "";
        const phone = phoneInput?.value.trim() || "";
        const normalizedPhone = phone.replace(/[\s-]/g, "");

        if (!currentPassword) {
            showMessage("현재 비밀번호를 입력해주세요.");
            currentPasswordInput?.focus();
            return;
        }
        if (newPassword || newPasswordConfirm) {
            if (newPassword.length < 4) {
                showMessage("새 비밀번호는 4자 이상으로 입력해주세요.");
                newPasswordInput?.focus();
                return;
            }
            if (newPassword !== newPasswordConfirm) {
                showMessage("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
                newPasswordConfirmInput?.focus();
                return;
            }
        }
        if (!email && !normalizedPhone) {
            showMessage("이메일 또는 휴대폰 번호 중 하나는 반드시 입력해야 합니다.");
            emailInput?.focus();
            return;
        }
        if (emailInput && email && !emailInput.checkValidity()) {
            showMessage("올바른 이메일 형식을 입력해주세요.");
            emailInput.focus();
            return;
        }
        if (normalizedPhone && !/^\d{10,11}$/.test(normalizedPhone)) {
            showMessage("휴대폰 번호는 숫자 10~11자리로 입력해주세요.");
            phoneInput?.focus();
            return;
        }

        submitButton.disabled = true;
        submitButton.textContent = "변경 중...";

        try {
            const response = await fetch("/api/member/me/account", {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: "Bearer " + token,
                },
                body: JSON.stringify({
                    currentPassword,
                    newPassword: newPassword || null,
                    newPasswordConfirm: newPasswordConfirm || null,
                    email: email || null,
                    phone: normalizedPhone || null,
                }),
            });

            if (response.status === 401 || response.status === 403) {
                alert("로그인을 완료한 후에 이용 가능합니다.");
                redirectToLogin();
                return;
            }

            const data = await response.json().catch(() => ({}));
            if (!response.ok) {
                throw new Error(data.message || "계정 정보를 변경하지 못했습니다.");
            }

            member = data;
            renderMemberProfile(member);
            if (emailInput) emailInput.value = member.email || "";
            if (phoneInput) phoneInput.value = member.phone || "";
            clearPasswords();
            showMessage("계정 정보가 변경되었습니다.", "success");
        } catch (error) {
            console.error(error);
            showMessage(error.message || "네트워크 오류가 발생했습니다.");
        } finally {
            submitButton.disabled = false;
            submitButton.textContent = "변경완료";
        }
    });
}

function setMsg(message, type) {
    const result = document.getElementById("reservationResult");
    if (!result) return;

    if (!message) {
        result.innerHTML = "";
        return;
    }

    const cls = type === "error" ? "reserve-error" : "";
    result.innerHTML = `<div class="${cls}">${message}</div>`;
}

async function fetchReservation(reservationNumber, token) {
    const res = await fetch(`/api/reservations/${encodeURIComponent(reservationNumber)}`, {
        headers: {
            Authorization: "Bearer " + token,
        },
    });

    if (res.status === 401 || res.status === 403) {
        alert("로그인을 완료한 후에 이용 가능합니다.");
        redirectToLogin();
        throw new Error("로그인이 필요합니다.");
    }

    // 에러 응답은 text로도 올 수 있어서 안전하게 처리
    let data = null;
    try {
        data = await res.json();
    } catch (e) {
        // ignore
    }

    if (!res.ok) {
        const msg = (data && data.message) ? data.message : (data ? JSON.stringify(data) : "예매 정보를 불러오지 못했습니다.");
        throw new Error(msg);
    }

    return data;
}

async function updateReservation(reservationNumber, payload, token) {
    const res = await fetch(`/api/reservations/${encodeURIComponent(reservationNumber)}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            Authorization: "Bearer " + token,
        },
        body: JSON.stringify(payload),
    });

    if (res.status === 401 || res.status === 403) {
        alert("로그인을 완료한 후에 이용 가능합니다.");
        redirectToLogin();
        throw new Error("로그인이 필요합니다.");
    }

    let data = null;
    try {
        data = await res.json();
    } catch (e) {
        // ignore (204 No Content 등)
    }

    if (!res.ok) {
        const msg = (data && data.message)
            ? data.message
            : (data ? JSON.stringify(data) : "예매 수정에 실패했습니다.");
        throw new Error(msg);
    }

    return data;
}

async function cancelReservation(reservationNumber, token) {
    const res = await fetch(`/api/reservations/${encodeURIComponent(reservationNumber)}`, {
        method: "DELETE",
        headers: {
            Authorization: "Bearer " + token,
        },
    });

    if (res.status === 401 || res.status === 403) {
        alert("로그인을 완료한 후에 이용 가능합니다.");
        redirectToLogin();
        throw new Error("로그인이 필요합니다.");
    }

    let data = null;
    try {
        data = await res.json();
    } catch (e) {
        // ignore (204 No Content 등)
    }

    if (!res.ok) {
        const msg = (data && data.message)
            ? data.message
            : (data ? JSON.stringify(data) : "예매 취소에 실패했습니다.");
        throw new Error(msg);
    }

    return data;
}


async function fetchMyReservationNumbers(token) {
    const res = await fetch("/api/reservations/my-numbers", {
        headers: { Authorization: "Bearer " + token },
    });

    if (res.status === 401 || res.status === 403) {
        alert("로그인을 완료한 후에 이용 가능합니다.");
        redirectToLogin();
        throw new Error("로그인이 필요합니다.");
    }

    let data = null;
    try { data = await res.json(); } catch (e) { /* ignore */ }

    if (!res.ok) {
        const msg = (data && data.message) ? data.message : "예매번호 찾기에 실패했습니다.";
        throw new Error(msg);
    }
    return Array.isArray(data) ? data : [];
}

function renderReservationNumberList(items) {
    const box = document.getElementById("reservationNumberList");
    if (!box) return;

    if (!items || items.length === 0) {
        box.innerHTML = "<div>예매 내역이 없습니다.</div>";
        return;
    }

    const esc = (v) => (v ?? "").toString().replace(/</g, "&lt;").replace(/>/g, "&gt;");
    box.innerHTML = `
      <div class="reserve-list">
        ${items.map(it => `
          <div class="reserve-item">
            <div>
              <div class="reserve-item-title">${esc(it.movieTitle)} <span style="opacity:.75">(${esc(it.theaterName)})</span></div>
              <div class="reserve-item-sub">상영: ${esc(it.screeningTime)} · 예매번호: <b>${esc(it.reservationNumber)}</b></div>
              <div class="reserve-item-sub">좌석: ${esc(it.seats || "-")} · 인원: ${esc(it.reservedCount)}</div>
            </div>
            <button class="reserve-use-btn" type="button" data-num="${esc(it.reservationNumber)}">이 번호로 조회</button>
          </div>
        `).join("")}
      </div>
    `;

    box.querySelectorAll(".reserve-use-btn").forEach(btn => {
        btn.addEventListener("click", () => {
            const num = btn.getAttribute("data-num");
            const input = document.getElementById("reservationNumberInput");
            const searchBtn = document.getElementById("reservationSearchBtn");
            if (input) input.value = num || "";
            if (searchBtn) searchBtn.click();
        });
    });
}

// 혹시 inline onclick 등을 쓰는 경우를 대비해 전역에도 노출
window.cancelReservation = cancelReservation;
window.updateReservation = updateReservation;

// ================== 좌석 선택 모달 ==================
async function fetchReservedSeats(screeningId, excludeReservationNumber, token) {
    const qs = excludeReservationNumber ? `?excludeReservationNumber=${encodeURIComponent(excludeReservationNumber)}` : "";
    const res = await fetch(`/api/screenings/${encodeURIComponent(screeningId)}/reserved-seats${qs}`, {
        headers: { Authorization: "Bearer " + token }
    });

    if (res.status === 401 || res.status === 403) {
        alert("로그인을 완료한 후에 이용 가능합니다.");
        redirectToLogin();
        throw new Error("로그인이 필요합니다.");
    }

    let data = null;
    try { data = await res.json(); } catch (e) { /* ignore */ }

    if (!res.ok) {
        const msg = (data && data.message) ? data.message : "좌석 정보를 불러오지 못했습니다.";
        throw new Error(msg);
    }

    const seats = data && data.reservedSeats ? data.reservedSeats : [];
    // reservedSeats가 Set으로 내려와도 JSON에서는 배열로 들어옴
    return Array.isArray(seats) ? seats : Object.values(seats);
}

function parseSeatList(seatStr) {
    if (!seatStr) return [];
    return seatStr.split(",").map(s => s.trim()).filter(Boolean);
}

function formatSeatList(list) {
    // 보기 좋게 정렬: A1, A2 ... B1 ...
    const parse = (s) => {
        const m = /^([A-Z]+)(\d+)$/.exec(s);
        return m ? [m[1], Number(m[2])] : ["Z", 999];
    };
    return [...list].sort((a,b) => {
        const [ra, ca] = parse(a);
        const [rb, cb] = parse(b);
        if (ra === rb) return ca - cb;
        return ra.localeCompare(rb);
    }).join(",");
}

function openSeatModalUI() {
    const modal = document.getElementById("seatModal");
    if (!modal) return;
    modal.classList.remove("hidden");
    modal.setAttribute("aria-hidden", "false");
}

function closeSeatModalUI() {
    const modal = document.getElementById("seatModal");
    if (!modal) return;
    modal.classList.add("hidden");
    modal.setAttribute("aria-hidden", "true");
}

async function openSeatModal(reservation, token) {
    // reservation.screeningId가 없으면(구버전 응답) 모달을 열 수 없음
    if (!reservation || reservation.screeningId == null) {
        throw new Error("상영 정보(screeningId)를 찾을 수 없습니다. 서버 응답 DTO를 확인해 주세요.");
    }

    const modal = document.getElementById("seatModal");
    const grid = document.getElementById("seatGrid");
    const help = document.getElementById("seatHelpText");
    const confirmBtn = document.getElementById("seatModalConfirm");
    const closeBtn = document.getElementById("seatModalClose");
    const backdrop = document.getElementById("seatModalBackdrop");

    if (!modal || !grid || !confirmBtn) throw new Error("좌석 모달 요소를 찾지 못했습니다.");

    // 모달 닫기 이벤트(중복 바인딩 방지)
    if (!modal.__bound) {
        if (closeBtn) closeBtn.addEventListener("click", closeSeatModalUI);
        if (backdrop) backdrop.addEventListener("click", closeSeatModalUI);
        document.addEventListener("keydown", (e) => {
            if (e.key === "Escape") closeSeatModalUI();
        });
        modal.__bound = true;
    }

    // 현재 인원(선택해야 하는 좌석 수)
    const countEl = document.getElementById("editCount");
    const requiredCount = Number(countEl && countEl.value ? countEl.value : reservation.reservedCount) || 1;

    // 현재 선택된 좌석(기존 값)
    const seatsEl = document.getElementById("editSeats");
    const currentSeats = parseSeatList(seatsEl ? seatsEl.value : reservation.seats);

    // 서버에서 이미 예약된 좌석 가져오기(현재 예약은 제외)
    const reservedSeats = await fetchReservedSeats(reservation.screeningId, reservation.reservationNumber, token);
    const reservedSet = new Set(reservedSeats.map(s => s.trim()));
    const selectedSet = new Set(currentSeats.map(s => s.trim()).filter(Boolean));

    // 선택된 좌석이 requiredCount를 초과하면 잘라내기(안전장치)
    if (selectedSet.size > requiredCount) {
        const sliced = [...selectedSet].slice(0, requiredCount);
        selectedSet.clear();
        sliced.forEach(s => selectedSet.add(s));
    }

    const rows = ["A", "B", "C", "D", "E"];
    const cols = [1,2,3,4,5,6,7,8,9,10];

    const updateHelp = () => {
        if (!help) return;
        help.textContent = `선택된 좌석: ${formatSeatList([...selectedSet]) || "-"} (${selectedSet.size}/${requiredCount})`;
    };

    // 그리드 렌더
    grid.innerHTML = "";
    rows.forEach((row) => {
        const rowEl = document.createElement("div");
        rowEl.className = "seat-row";

        const label = document.createElement("div");
        label.className = "row-label";
        label.textContent = row;
        rowEl.appendChild(label);

        const area = document.createElement("div");
        area.className = "seat-area";

        cols.forEach((col) => {
            const seatId = `${row}${col}`;
            const btn = document.createElement("button");
            btn.type = "button";
            btn.className = "seat";
            btn.dataset.seat = seatId;
            btn.textContent = col;

            const isReserved = reservedSet.has(seatId);
            if (isReserved) {
                btn.classList.add("reserved");
                btn.disabled = true;
            } else if (selectedSet.has(seatId)) {
                btn.classList.add("selected");
            }

            btn.addEventListener("click", () => {
                if (btn.disabled) return;

                if (selectedSet.has(seatId)) {
                    selectedSet.delete(seatId);
                    btn.classList.remove("selected");
                } else {
                    if (selectedSet.size >= requiredCount) {
                        alert(`좌석은 ${requiredCount}개까지 선택할 수 있습니다.`);
                        return;
                    }
                    selectedSet.add(seatId);
                    btn.classList.add("selected");
                }
                updateHelp();
            });

            area.appendChild(btn);
        });

        rowEl.appendChild(area);
        grid.appendChild(rowEl);
    });

    updateHelp();
    openSeatModalUI();

    // 확인 버튼 (매번 최신 핸들러로 교체)
    confirmBtn.onclick = () => {
        if (selectedSet.size !== requiredCount) {
            alert(`좌석을 ${requiredCount}개 선택해 주세요.`);
            return;
        }
        const val = formatSeatList([...selectedSet]);
        if (seatsEl) seatsEl.value = val;
        closeSeatModalUI();
    };
}

function renderReservation(r) {
    const result = document.getElementById("reservationResult");
    if (!result) return;

    const esc = (v) => (v ?? "").toString().replace(/</g, "&lt;").replace(/>/g, "&gt;");

    result.innerHTML = `
      <div class="reserve-card">
        <div class="reserve-grid">
          <div class="reserve-label">예매번호</div><div class="reserve-value">${esc(r.reservationNumber)}</div>
          <div class="reserve-label">영화</div><div class="reserve-value">${esc(r.movieTitle)}</div>
          <div class="reserve-label">극장</div><div class="reserve-value">${esc(r.theaterName)}</div>
          <div class="reserve-label">상영 시간</div><div class="reserve-value">${esc(r.screeningTime)}</div>
          <div class="reserve-label">예매자</div><div class="reserve-value">${esc(r.name)}</div>
          <div class="reserve-label">연락처</div><div class="reserve-value">${esc(r.phone)}</div>
          <div class="reserve-label">예매 인원</div><div class="reserve-value">${esc(r.reservedCount)}</div>
          <div class="reserve-label">좌석</div><div class="reserve-value">${esc(r.seats)}</div>
        </div>

        <div class="reserve-edit">
          <input id="editName" type="text" placeholder="예매자" value="${esc(r.name)}" />
          <input id="editPhone" type="text" placeholder="연락처" value="${esc(r.phone)}" />
          <input id="editCount" type="number" min="1" placeholder="인원" value="${esc(r.reservedCount)}" />
          <div class="seat-edit-row">
          <input id="editSeats" type="text" placeholder="좌석(예: A1,A2)" value="${esc(r.seats)}" readonly />
          <button id="openSeatModalBtn" class="reserve-seat-btn" type="button">좌석 선택</button>
        </div>
          <button id="saveReservationBtn" class="reserve-save" type="button">수정 저장</button>
          <button id="cancelReservationBtn" class="reserve-cancel" type="button">예매 취소</button>
        </div>
      </div>
    `;

    // 현재 조회된 예약을 저장해두고, 좌석 선택 모달에 사용
    window.__currentReservation = r;

    const openSeatBtn = document.getElementById("openSeatModalBtn");
    if (openSeatBtn) {
        openSeatBtn.addEventListener("click", async () => {
            const token = localStorage.getItem("accessToken");
            if (!token) {
                alert("로그인을 완료한 후에 이용 가능합니다.");
                redirectToLogin();
                return;
            }
            try {
                await openSeatModal(r, token);
            } catch (e) {
                console.error(e);
                setMsg(e.message || "좌석 정보를 불러오지 못했습니다.", "error");
            }
        });
    }

    const saveBtn = document.getElementById("saveReservationBtn");
    if (!saveBtn) return;

    saveBtn.addEventListener("click", async () => {
        const token = localStorage.getItem("accessToken");
        if (!token) {
            alert("로그인을 완료한 후에 이용 가능합니다.");
            redirectToLogin();
            return;
        }

        const payload = {
            name: document.getElementById("editName").value.trim(),
            phone: document.getElementById("editPhone").value.trim(),
            reservedCount: Number(document.getElementById("editCount").value),
            seats: document.getElementById("editSeats").value.trim(),
        };

        try {
            await updateReservation(r.reservationNumber, payload, token);
            alert("수정이 완료 되었습니다.");
            window.location.href = "/movies";
            return;
        } catch (e) {
            console.error(e);
            setMsg(e.message || "예매 수정에 실패했습니다.", "error");
        }
    });

    const cancelBtn = document.getElementById("cancelReservationBtn");
    if (cancelBtn) {
        cancelBtn.addEventListener("click", async () => {
            const token = localStorage.getItem("accessToken");
            if (!token) {
                alert("로그인을 완료한 후에 이용 가능합니다.");
                redirectToLogin();
                return;
            }

            const ok = confirm("정말 예매를 취소하겠습니까?");
            if (!ok) return;

            try {
                await cancelReservation(r.reservationNumber, token);
                window.location.href = "/movies";
            } catch (e) {
                console.error(e);
                alert(e.message || "예매 취소에 실패했습니다.");
            }
        });
    }

}

document.addEventListener("DOMContentLoaded", async () => {
    // 공통 헤더 갱신보다 먼저 토큰을 확보한다. 헤더와 프로필이 동시에
    // 인증 API를 호출하며 토큰 저장소를 변경하는 경쟁 조건을 방지한다.
    let token = getAuthTokenLocal();

    // 브라우저 토큰이 없더라도 로그인 세션이 살아 있으면 자동 복구한다.
    if (!token) {
        token = await restoreAuthTokenFromSession();
        if (!token) {
            alert("로그인을 완료한 후에 이용 가능합니다.");
            redirectToLogin();
            return;
        }
    }

    // ===== 프로필 =====
    const box = document.getElementById("profileBox");
    try {
        let res = await fetchMyProfile(token);

        if ([400, 401, 403, 404].includes(res.status)) {
            // JWT 키 교체 또는 토큰 만료 시 서버 로그인 세션으로 한 번 재발급한다.
            const restoredToken = await restoreAuthTokenFromSession();
            if (restoredToken) {
                token = restoredToken;
                res = await fetchMyProfile(token);
            }
        }

        if ([400, 401, 403, 404].includes(res.status)) {
            if (typeof window.clearAuthStorage === "function") window.clearAuthStorage();
            alert("로그인을 완료한 후에 이용 가능합니다.");
            redirectToLogin();
            return;
        }

        if (!res.ok) {
            box.innerHTML = "<p>회원 정보를 불러오지 못했습니다.</p>";
        } else {
            const me = await res.json();
            renderMemberProfile(me);
            setupAccountEditor(token, me);

            // 회원정보 조회가 성공한 뒤 헤더의 로그인/관리자 UI를 갱신한다.
            if (typeof window.updateAuthNav === "function") {
                await window.updateAuthNav();
            } else {
                updateAuthNavLocal();
            }
        }
    } catch (e) {
        console.error(e);
        box.innerHTML = "<p>네트워크 오류가 발생했습니다.</p>";
    }


    // ===== 예매번호 찾기 =====
    const findBtn = document.getElementById("findReservationNumbersBtn");
    if (findBtn) {
        findBtn.addEventListener("click", async () => {
            const token = localStorage.getItem("accessToken");
            if (!token) {
                alert("로그인을 완료한 후에 이용 가능합니다.");
                redirectToLogin();
                return;
            }
            const box = document.getElementById("reservationNumberList");
            if (box) box.innerHTML = "조회 중...";
            try {
                const items = await fetchMyReservationNumbers(token);
                renderReservationNumberList(items);
            } catch (e) {
                console.error(e);
                if (box) box.innerHTML = `<div class="reserve-error">${(e && e.message) ? e.message : "예매번호 찾기에 실패했습니다."}</div>`;
            }
        });
    }

    // ===== 예매 조회/수정 =====
    const input = document.getElementById("reservationNumberInput");
    const btn = document.getElementById("reservationSearchBtn");

    if (btn && input) {
        btn.addEventListener("click", async () => {
            const num = input.value.trim();
            if (!num) {
                setMsg("예매번호를 입력해 주세요.", "error");
                return;
            }
            if (num.length !== 10) {
                setMsg("예매번호는 10자리입니다.", "error");
                return;
            }

            setMsg("조회 중...", "success");

            try {
                const r = await fetchReservation(num, token);
                renderReservation(r);
            } catch (e) {
                console.error(e);
                setMsg(e.message || "예매 정보를 불러오지 못했습니다.", "error");
            }
        });
    }
});
