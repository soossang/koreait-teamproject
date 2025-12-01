const $ = (s) => document.querySelector(s);
const fmt = (n) => Number(n||0).toLocaleString();
const esc = (s) => String(s??'').replace(/[&<>"']/g, m=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m]));
const show = (el)=>el.classList.remove('hidden');
const hide = (el)=>el.classList.add('hidden');

const tbody = $('#postTbody');
const emptyBox = $('#emptyBox');
const statusEl = $('#status');

async function loadPosts(){
  statusEl.textContent = '로딩중…';
  tbody.innerHTML = `<tr><td colspan="5"><div class="loader"></div></td></tr>`;
  hide(emptyBox);

  try {
    // 네 API 규칙에 맞게 변경 가능:
    // 예) /api/board/posts?page=0&size=20&sort=createdAt,desc
    const res = await fetch('/api/board/posts?page=0&size=20&sort=createdAt,desc', {headers:{'Accept':'application/json'}});
    if(!res.ok){
      tbody.innerHTML = '';
      show(emptyBox);
      statusEl.textContent = `오류 ${res.status}`;
      return;
    }
    const data = await res.json();
    const list = Array.isArray(data) ? data : (data.content || []);

    if(list.length === 0){
      tbody.innerHTML = '';
      show(emptyBox);
      statusEl.textContent = '(0건)';
      return;
    }

    hide(emptyBox);
    tbody.innerHTML = list.map((p, idx)=>`
      <tr>
        <td>${p.id ?? p.postId ?? idx+1}</td>
        <td>${esc(p.title || p.subject || '(제목없음)')}</td>
        <td>${esc(p.writer || p.author || '익명')}</td>
        <td>${fmt(p.viewCnt || p.views || 0)}</td>
        <td>${esc(p.createdAt || p.created_at || p.regDate || '')}</td>
      </tr>
    `).join('');
    statusEl.textContent = `(${list.length}건)`;
  } catch (e) {
    console.error(e);
    tbody.innerHTML = '';
    show(emptyBox);
    statusEl.textContent = '네트워크 오류';
  }
}

window.addEventListener('DOMContentLoaded', loadPosts);
$('#writeBtn').addEventListener('click', () => alert('글쓰기 폼 연결 예정'));
