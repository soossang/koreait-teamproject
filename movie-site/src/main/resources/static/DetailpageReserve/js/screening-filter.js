document.addEventListener('DOMContentLoaded', function () {
    const table = document.getElementById('screeningTable');
    if (!table) {
        // 상영 정보가 없는 영화 상세 페이지일 수 있음
        return;
    }

    const rows = Array.from(table.querySelectorAll('tbody tr'));
    const brandButtons = document.querySelectorAll('.brand-btn');
    const theaterSelect = document.getElementById('theaterSelect');

    // "CGV 유성노은점 (대전 유성구 지족동)"
    // -> brand: CGV, location: "유성노은점 (대전 유성구 지족동)"
    function parseTheater(theaterName) {
        const firstSpace = theaterName.indexOf(' ');
        if (firstSpace === -1) {
            return { brand: '기타', location: theaterName };
        }
        const brand = theaterName.substring(0, firstSpace);      // CGV / 롯데시네마 / 메가박스
        const location = theaterName.substring(firstSpace + 1);  // 나머지 전부
        return { brand, location };
    }

    // 선택된 브랜드에 맞춰 지점 옵션 채우기
    function refreshTheaterOptions(selectedBrand) {
        const locations = new Set();

        rows.forEach(row => {
            const theaterName = row.dataset.theater;
            const info = parseTheater(theaterName);

            if (selectedBrand === 'ALL' || info.brand === selectedBrand) {
                locations.add(info.location);
            }
        });

        theaterSelect.innerHTML = '';
        const defaultOpt = document.createElement('option');
        defaultOpt.value = '';
        defaultOpt.textContent = '전체 지점';
        theaterSelect.appendChild(defaultOpt);

        locations.forEach(loc => {
            const opt = document.createElement('option');
            opt.value = loc;
            opt.textContent = loc;
            theaterSelect.appendChild(opt);
        });
    }

    // 실제 필터 적용
    function applyFilter() {
        const activeBtn = document.querySelector('.brand-btn.active');
        const selectedBrand = activeBtn ? activeBtn.dataset.brand : 'ALL';
        const selectedLocation = theaterSelect.value;

        rows.forEach(row => {
            const theaterName = row.dataset.theater;
            const info = parseTheater(theaterName);

            let visible = true;

            if (selectedBrand !== 'ALL' && info.brand !== selectedBrand) {
                visible = false;
            }
            if (selectedLocation && info.location !== selectedLocation) {
                visible = false;
            }

            row.style.display = visible ? '' : 'none';
        });
    }

    // 브랜드 버튼 클릭 이벤트
    brandButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            brandButtons.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');

            refreshTheaterOptions(btn.dataset.brand);
            theaterSelect.value = '';
            applyFilter();
        });
    });

    // 지점 선택 변경 이벤트
    theaterSelect.addEventListener('change', applyFilter);

    // 초기: 전체 기준으로 지점 옵션 채우기
    if (rows.length > 0) {
        refreshTheaterOptions('ALL');
    }
});
