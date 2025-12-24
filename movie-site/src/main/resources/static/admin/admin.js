(function () {
  const token = () => localStorage.getItem('accessToken');

  const $ = (sel) => document.querySelector(sel);
  const $$ = (sel) => Array.from(document.querySelectorAll(sel));

  const alertBox = $('#alert');
  function showAlert(message, type='ok') {
    if (!alertBox) return;
    alertBox.textContent = message;
    alertBox.className = 'alert ' + (type === 'error' ? 'error' : 'ok');
    alertBox.style.display = 'block';
    clearTimeout(showAlert._t);
    showAlert._t = setTimeout(() => {
      alertBox.style.display = 'none';
    }, 2500);
  }

  async function apiFetch(url, opts={}) {
    const t = token();
    const headers = Object.assign({ 'Content-Type': 'application/json' }, opts.headers || {});
    if (t) headers['Authorization'] = `Bearer ${t}`;
    const res = await fetch(url, { ...opts, headers });
    let body = null;
    try { body = await res.json(); } catch (e) {}
    return { res, body };
  }

  async function ensureAdmin() {
    const t = token();
    if (!t) {
      window.location.href = '/login';
      return false;
    }
    const { res, body } = await apiFetch('/api/member/me', { method: 'GET' });
    if (!res.ok || !body || body.role !== 'ADMIN') {
      showAlert('관리자 권한이 없습니다.', 'error');
      setTimeout(() => (window.location.href = '/'), 900);
      return false;
    }
    return true;
  }

  // ===== Tabs =====
  function setTab(tab) {
    $$('.tab').forEach(b => b.classList.toggle('active', b.dataset.tab === tab));
    $('#panel-members').style.display = tab === 'members' ? '' : 'none';
    $('#panel-board').style.display = tab === 'board' ? '' : 'none';
    $('#panel-reservations').style.display = tab === 'reservations' ? '' : 'none';
  }

  // ===== Data caches =====
  let members = [];
  let posts = [];
  let reservations = [];

  function filterList(list, q, fields) {
    if (!q) return list;
    const query = q.toLowerCase();
    return list.filter(item => fields.some(f => {
      const val = (item[f] ?? '').toString().toLowerCase();
      return val.includes(query);
    }));
  }

  // ===== Renderers =====
  function badge(text, cls) {
    return `<span class="badge ${cls}">${text}</span>`;
  }

  function renderMembers(q='') {
    const body = $('#membersBody');
    if (!body) return;
    const list = filterList(members, q, ['id','loginId','email','phone','role']);

    body.innerHTML = list.map(m => {
      const roleBadge = m.role === 'ADMIN' ? badge('ADMIN', 'admin') : badge('USER', 'user');
      const activeBadge = m.active ? badge('ON', 'on') : badge('OFF', 'off');
      return `
        <tr>
          <td class="mono">${m.id}</td>
          <td>${m.loginId ?? ''}</td>
          <td>${m.email ?? ''}</td>
          <td class="mono">${m.phone ?? ''}</td>
          <td>${roleBadge}</td>
          <td>${activeBadge}</td>
          <td>
            <button class="btn small" data-action="toggleActive" data-id="${m.id}">활성 전환</button>
            <button class="btn small danger" data-action="deleteMember" data-id="${m.id}">삭제</button>
          </td>
        </tr>`;
    }).join('');
  }

  function renderBoard(q='') {
    const body = $('#boardBody');
    if (!body) return;
    const list = filterList(posts, q, ['id','title','author']);
    body.innerHTML = list.map(p => {
      const created = p.createdAt ? p.createdAt.replace('T',' ') : '';
      return `
        <tr>
          <td class="mono">${p.id}</td>
          <td>${escapeHtml(p.title ?? '')}</td>
          <td>${escapeHtml(p.author ?? '')}</td>
          <td class="mono">${p.viewCount ?? 0}</td>
          <td class="mono">${p.commentCount ?? 0}</td>
          <td class="mono">${created}</td>
          <td>
            <button class="btn small danger" data-action="deletePost" data-id="${p.id}">삭제</button>
          </td>
        </tr>`;
    }).join('');
  }

  function renderReservations(q='') {
    const body = $('#reservationsBody');
    if (!body) return;
    const list = filterList(reservations, q, ['reservationNumber','movieTitle','theaterName','name','phone','seats']);
    body.innerHTML = list.map(r => {
      const st = r.screeningTime ? r.screeningTime.replace('T',' ') : '';
      const rt = r.reservedAt ? r.reservedAt.replace('T',' ') : '';
      return `
        <tr>
          <td class="mono">${r.reservationNumber}</td>
          <td>${escapeHtml(r.movieTitle ?? '')}</td>
          <td>${escapeHtml(r.theaterName ?? '')}</td>
          <td class="mono">${st}</td>
          <td>${escapeHtml(r.name ?? '')}</td>
          <td class="mono">${escapeHtml(r.phone ?? '')}</td>
          <td class="mono">${r.reservedCount ?? 0}</td>
          <td class="mono">${escapeHtml(r.seats ?? '')}</td>
          <td class="mono">${rt}</td>
          <td><button class="btn small danger" data-action="cancelReservation" data-id="${r.reservationNumber}">취소</button></td>
        </tr>`;
    }).join('');
  }

  function escapeHtml(s) {
    return (s || '').toString()
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#39;');
  }

  // ===== Loaders =====
  async function loadMembers() {
    const { res, body } = await apiFetch('/api/admin/member');
    if (!res.ok) throw new Error(body?.message || '회원 목록을 불러오지 못했습니다.');
    members = body || [];
  }

  async function loadBoard() {
    const { res, body } = await apiFetch('/api/admin/board/posts');
    if (!res.ok) throw new Error(body?.message || '게시글 목록을 불러오지 못했습니다.');
    posts = body || [];
  }

  async function loadReservations() {
    const { res, body } = await apiFetch('/api/admin/reservations');
    if (!res.ok) throw new Error(body?.message || '예매 목록을 불러오지 못했습니다.');
    reservations = body || [];
  }

  // ===== Actions =====

  async function toggleActive(id) {
    const m = members.find(x => x.id === Number(id));
    if (!m) return;
    const next = !m.active;
    const { res, body } = await apiFetch(`/api/admin/member/${id}`, {
      method: 'PATCH',
      body: JSON.stringify({ active: next })
    });
    if (!res.ok) throw new Error(body?.message || '활성 상태 변경 실패');
    showAlert('활성 상태 변경 완료');
    await loadMembers();
  }

  async function deleteMember(id) {
    const m = members.find(x => x.id === Number(id));
    if (!m) return;

    if (String(m.loginId || '').toLowerCase() === 'admin') {
      showAlert('기본 관리자 계정은 삭제할 수 없습니다.', 'error');
      return;
    }

    if (!confirm(`회원 [${m.loginId}] 를 삭제할까요? (되돌릴 수 없습니다)`)) return;

    const { res, body } = await apiFetch(`/api/admin/member/${id}`, { method: 'DELETE' });
    if (!res.ok) throw new Error(body?.message || '회원 삭제 실패');
    showAlert('회원 삭제 완료');
    await loadMembers();
  }


  async function deletePost(id) {
    if (!confirm('게시글을 삭제할까요? (댓글도 함께 삭제됩니다)')) return;
    const { res, body } = await apiFetch(`/api/admin/board/posts/${id}`, { method: 'DELETE' });
    if (!res.ok) throw new Error(body?.message || '게시글 삭제 실패');
    showAlert('게시글 삭제 완료');
    await loadBoard();
  }

  async function cancelReservation(num) {
    if (!confirm('예매를 취소할까요?')) return;
    const { res, body } = await apiFetch(`/api/admin/reservations/${encodeURIComponent(num)}`, { method: 'DELETE' });
    if (!res.ok) throw new Error(body?.message || '예매 취소 실패');
    showAlert('예매 취소 완료');
    await loadReservations();
  }

  async function refreshAll() {
    const q = $('#searchInput')?.value || '';
    const activeTab = $('.tab.active')?.dataset.tab || 'members';

    try {
      if (activeTab === 'members') {
        await loadMembers();
        renderMembers(q);
      } else if (activeTab === 'board') {
        await loadBoard();
        renderBoard(q);
      } else {
        await loadReservations();
        renderReservations(q);
      }
    } catch (e) {
      showAlert(e.message || '오류가 발생했습니다.', 'error');
    }
  }

  // ===== Event wiring =====
  document.addEventListener('DOMContentLoaded', async () => {
    const ok = await ensureAdmin();
    if (!ok) return;

    setTab('members');

    // initial load
    await refreshAll();

    // tabs
    $$('.tab').forEach(btn => {
      btn.addEventListener('click', async () => {
        setTab(btn.dataset.tab);
        await refreshAll();
      });
    });

    // search
    $('#searchInput')?.addEventListener('input', () => {
      const q = $('#searchInput').value || '';
      const tab = $('.tab.active')?.dataset.tab || 'members';
      if (tab === 'members') renderMembers(q);
      else if (tab === 'board') renderBoard(q);
      else renderReservations(q);
    });

    // refresh
    $('#refreshBtn')?.addEventListener('click', refreshAll);

    // delegation for buttons
    document.body.addEventListener('click', async (e) => {
      const t = e.target;
      if (!(t instanceof HTMLElement)) return;
      const action = t.dataset.action;
      const id = t.dataset.id;
      if (!action || !id) return;
      try {
        if (action === 'toggleActive') await toggleActive(id);
        if (action === 'deleteMember') await deleteMember(id);
        if (action === 'deletePost') await deletePost(id);
        if (action === 'cancelReservation') await cancelReservation(id);
        await refreshAll();
      } catch (err) {
        showAlert(err.message || '처리 중 오류', 'error');
      }
    });
  });
})();
