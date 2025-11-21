const boardApi = {
  list: (page=0,size=20)=> `/api/board/posts?page=${page}&size=${size}`,
  get: (id)=> `/api/board/posts/${id}?increaseView=true`,
  create: ()=> `/api/board/posts`,
  addComment: (id)=> `/api/board/posts/${id}/comments`
};

let curPostId = null;

async function createPost(){
  const payload = {
    title: document.getElementById('title').value,
    author: document.getElementById('author').value,
    content: document.getElementById('content').value
  };
  await fetch(boardApi.create(), {method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(payload)});
  document.getElementById('postForm').reset();
  await loadPosts();
  return false;
}

async function loadPosts(){
  const res = await fetch(boardApi.list());
  const data = await res.json();
  const tbody = document.getElementById('postTbody');
  tbody.innerHTML = (data.content||[]).map(p =>
    `<tr onclick="openPost(${p.id})" style="cursor:pointer">
       <td>${p.id}</td><td>${escapeHtml(p.title)}</td><td>${escapeHtml(p.author)}</td><td>${p.viewCount}</td>
     </tr>`).join('');
}

async function openPost(id){
  const res = await fetch(boardApi.get(id));
  const p = await res.json();
  curPostId = id;
  document.getElementById('postDetail').classList.remove('hidden');
  document.getElementById('dTitle').textContent = p.title;
  document.getElementById('dAuthor').textContent = p.author;
  document.getElementById('dDate').textContent = p.createdAt ?? '';
  document.getElementById('dViews').textContent = p.viewCount ?? 0;
  document.getElementById('dContent').textContent = p.content;
  document.getElementById('commentList').innerHTML = (p.comments||[]).map(c =>
    `<li class="muted">${escapeHtml(c.author)}: ${escapeHtml(c.content)} (${c.createdAt??''})</li>`).join('');
}

async function addComment(){
  if(!curPostId) return false;
  const payload = {
    author: document.getElementById('cAuthor').value,
    content: document.getElementById('cContent').value
  };
  await fetch(boardApi.addComment(curPostId), {method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(payload)});
  document.getElementById('commentForm').reset();
  await openPost(curPostId);
  return false;
}

function escapeHtml(s){ return String(s ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c])); }

loadPosts();
