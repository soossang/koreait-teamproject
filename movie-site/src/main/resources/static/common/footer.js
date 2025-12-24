(function () {
  // Film Box 공통 Footer (안전 삽입 버전)
  function inject() {
    try {
      // 중복 삽입 방지
      if (document.getElementById('filmBoxFooter')) return;

      // 스타일 로드(한 번만)
      if (!document.getElementById('filmBoxFooterStyles')) {
        var link = document.createElement('link');
        link.id = 'filmBoxFooterStyles';
        link.rel = 'stylesheet';
        link.href = '/common/footer.css';
        (document.head || document.getElementsByTagName('head')[0] || document.documentElement).appendChild(link);
      }

      var footer = document.createElement('footer');
      footer.id = 'filmBoxFooter';
      footer.className = 'site-footer';

      footer.innerHTML = `
        <div class="fb-footer__inner">
          <div class="fb-footer__top">
            <div class="fb-footer__brand">
              <div class="fb-footer__mark">FB</div>
              <div class="fb-footer__brandtext">
                <div class="fb-footer__name">Film Box</div>
                <div class="fb-footer__tagline">영화 · 예매 · 랭킹 · 커뮤니티를 한 곳에서.</div>
              </div>
            </div>

            <nav class="fb-footer__nav" aria-label="Footer menu">
              <a href="/">홈</a>
              <a href="/movies">영화/예매</a>
              <a href="/ranking">흥행영화순위</a>
              <a href="/genre">장르</a>
              <a href="/board">게시판</a>
              <span class="fb-footer__sep" aria-hidden="true"></span>
              <a href="#" onclick="return false;">개인정보처리방침</a>
              <a href="#" onclick="return false;">이용약관</a>
            </nav>
          </div>

          <div class="fb-footer__info">
            <div class="fb-footer__row">
              <span class="k">사업자(법인명)</span><span class="v">(주)필름박스</span>
              <span class="k">사업자번호</span><span class="v">000-00-00000</span>
            </div>
            <div class="fb-footer__row">
              <span class="k">대표</span><span class="v">홍길동</span>
              <span class="k">주소</span><span class="v">서울특별시 ○○구 ○○로 00</span>
            </div>
            <div class="fb-footer__row">
              <span class="k">전화문의</span><span class="v">02-0000-0000</span>
              <span class="k">E-mail</span><span class="v">support@filmbox.local</span>
            </div>
            <div class="fb-footer__row">
              <span class="k">운영시간</span><span class="v">평일 10:00 - 18:00</span>
              <span class="k">고객센터</span><span class="v">문의는 이메일로 부탁드립니다.</span>
            </div>
          </div>

          <div class="fb-footer__bottom">
            <p>copyright © <span id="footerYear"></span> Film Box. ALL RIGHTS RESERVED</p>
          </div>
        </div>
      `;

      var container = document.getElementById('siteFooter');
      if (container && container.parentNode) {
        container.parentNode.replaceChild(footer, container);
      } else if (document.body) {
        document.body.appendChild(footer);
      }

      var y = document.getElementById('footerYear');
      if (y) y.textContent = new Date().getFullYear();
    } catch (e) {
      // footer 때문에 앱이 죽지 않도록 무시
      // console.debug('[FilmBox footer] inject failed', e);
    }
  }

  function ready(fn) {
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', fn);
    } else {
      fn();
    }
  }

  // body가 늦게 생기는 환경 방어
  ready(function () {
    if (document.body) inject();
    else setTimeout(inject, 50);
  });
})();