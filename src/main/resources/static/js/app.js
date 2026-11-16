/**
 * VietTravel Hub - Modern Global Interactions
 */

document.addEventListener('DOMContentLoaded', () => {
  initSearchTabs();
  initCountdownTimers();
  initSeatMapSelector();
  initFormProtection();
  initAirportSwap();
  initStationSwap();
  initCatalogPlaceComboboxes();
  initCatalogToolbar();
  initBookingVisualSeatMap();
});

/**
 * 1. Search Tabs & Showcase Switcher (Flight vs Train) on Homepage
 */
function switchMainTab(target) {
  const tabs = document.querySelectorAll('.search-tab-btn');
  const flightPanel = document.getElementById('flightSearchPanel');
  const trainPanel = document.getElementById('trainSearchPanel');
  const flightShowcase = document.getElementById('flightShowcase');
  const trainShowcase = document.getElementById('trainShowcase');

  tabs.forEach(t => {
    if (t.getAttribute('data-tab') === target) {
      t.classList.add('active');
    } else {
      t.classList.remove('active');
    }
  });

  if (target === 'flight') {
    if (flightPanel) flightPanel.style.display = 'block';
    if (trainPanel) trainPanel.style.display = 'none';
    if (flightShowcase) flightShowcase.style.display = 'block';
    if (trainShowcase) trainShowcase.style.display = 'none';
  } else {
    if (flightPanel) flightPanel.style.display = 'none';
    if (trainPanel) trainPanel.style.display = 'block';
    if (flightShowcase) flightShowcase.style.display = 'none';
    if (trainShowcase) trainShowcase.style.display = 'block';
  }
}

function initSearchTabs() {
  const tabs = document.querySelectorAll('.search-tab-btn');
  if (!tabs.length) return;

  tabs.forEach(tab => {
    tab.addEventListener('click', () => {
      const target = tab.getAttribute('data-tab');
      switchMainTab(target);
    });
  });

  // Also support direct toggle buttons inside sections
  document.querySelectorAll('[data-switch-to]').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.preventDefault();
      const target = btn.getAttribute('data-switch-to');
      switchMainTab(target);
      window.scrollTo({ top: 380, behavior: 'smooth' });
    });
  });

  // Switch if URL contains ?tab=train
  try {
    const params = new URLSearchParams(window.location.search);
    if (params.get('tab') === 'train') {
      switchMainTab('train');
    }
  } catch (e) {
    // ignore
  }
}

/**
 * 2. Live Countdown Timer for Booking Hold
 */
function initCountdownTimers() {
  const timerElements = document.querySelectorAll('[data-countdown-target]');
  if (!timerElements.length) return;

  timerElements.forEach(el => {
    const targetIso = el.getAttribute('data-countdown-target');
    if (!targetIso) return;

    const targetTime = new Date(targetIso).getTime();

    function update() {
      const now = new Date().getTime();
      const diff = targetTime - now;

      if (diff <= 0) {
        el.innerHTML = '<span style="color: var(--danger); font-weight: 800;">Hết thời gian giữ chỗ! Vui lòng đặt lại.</span>';
        el.classList.add('urgent');
        return;
      }

      const minutes = Math.floor(diff / (1000 * 60));
      const seconds = Math.floor((diff % (1000 * 60)) / 1000);

      const formatted = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
      const displaySpan = el.querySelector('.timer-display');
      if (displaySpan) {
        displaySpan.textContent = formatted;
      } else {
        el.textContent = formatted;
      }

      if (minutes < 3) {
        el.classList.add('urgent');
      }
    }

    update();
    setInterval(update, 1000);
  });
}

/**
 * 3. Interactive Train Seat Map Selector
 */
function initSeatMapSelector() {
  const seatCards = document.querySelectorAll('.seat-card:not(.occupied)');
  if (!seatCards.length) return;

  const countDisplay = document.getElementById('selectedSeatCount');
  const maxSeats = 10;

  function updateCount() {
    const checked = document.querySelectorAll('.seat-card input[type="checkbox"]:checked');
    if (countDisplay) {
      countDisplay.textContent = checked.length;
    }
  }

  seatCards.forEach(card => {
    const checkbox = card.querySelector('input[type="checkbox"]');
    if (!checkbox) return;

    // Check initial state
    if (checkbox.checked) {
      card.classList.add('selected');
    }

    card.addEventListener('click', (e) => {
      // If user clicked directly on the checkbox, avoid double toggle
      if (e.target !== checkbox) {
        // Check max
        const currentlyChecked = document.querySelectorAll('.seat-card input[type="checkbox"]:checked');
        if (!checkbox.checked && currentlyChecked.length >= maxSeats) {
          alert(`Mỗi đơn chỉ được chọn tối đa ${maxSeats} chỗ.`);
          return;
        }
        checkbox.checked = !checkbox.checked;
      }

      if (checkbox.checked) {
        card.classList.add('selected');
      } else {
        card.classList.remove('selected');
      }
      updateCount();
    });
  });

  updateCount();
}

/**
 * 4. Double-Click Protection
 */
function initFormProtection() {
  const forms = document.querySelectorAll('form[data-protect]');
  forms.forEach(form => {
    form.addEventListener('submit', () => {
      const submitBtn = form.querySelector('button[type="submit"]');
      if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = `Đang xử lý...`;
      }
    });
  });
}

/**
 * 5. Admin Consultation Reschedule Row Toggle
 */
window.toggleReschedule = function(id) {
  const row = document.getElementById('reschedule-row-' + id);
  if (!row) return;
  row.style.display = row.style.display === 'none' ? 'table-row' : 'none';
};

/**
 * 6. Quick Airport Swap Button Handler
 */
function getAirportShortName(selectElem) {
  if (!selectElem || selectElem.selectedIndex < 0) return '';
  const opt = selectElem.options[selectElem.selectedIndex];
  if (!opt || !opt.value) return '';
  const rawText = opt.textContent || opt.innerText || '';
  let name = rawText.split('(')[0].trim();
  if (name.includes('TP. Hồ Chí Minh') || name.includes('Sài Gòn')) return 'TP.HCM';
  if (name.includes('Nha Trang')) return 'Nha Trang';
  return name;
}

function updateSwapButtonText(btn, form) {
  if (!btn || !form) return;
  const depSelect = form.querySelector('select[name="departureAirport"]');
  const arrSelect = form.querySelector('select[name="arrivalAirport"]');
  const labelSpan = btn.querySelector('span');
  if (!labelSpan) return;

  const depCity = getAirportShortName(depSelect);
  const arrCity = getAirportShortName(arrSelect);

  if (depCity && arrCity) {
    labelSpan.innerHTML = `Đảo Chiều Bay (${depCity} &harr; ${arrCity})`;
  } else if (depCity) {
    labelSpan.innerHTML = `Đảo Chiều Bay (${depCity} &harr; ...)`;
  } else if (arrCity) {
    labelSpan.innerHTML = `Đảo Chiều Bay (... &harr; ${arrCity})`;
  } else {
    labelSpan.innerHTML = `Đảo Chiều Bay`;
  }
}

function initAirportSwap() {
  const swapBtns = document.querySelectorAll('#swapAirportsBtn, .btn-swap-airports');
  swapBtns.forEach(btn => {
    const form = btn.closest('form') || btn.closest('.card') || btn.closest('#flightSearchPanel') || document;
    const depSelect = form.querySelector('select[name="departureAirport"]');
    const arrSelect = form.querySelector('select[name="arrivalAirport"]');
    let rotationDegree = 0;

    if (depSelect && arrSelect) {
      // Sync label initially
      updateSwapButtonText(btn, form);

      // Listen to changes on both selects to update the button label dynamically
      depSelect.addEventListener('change', () => updateSwapButtonText(btn, form));
      arrSelect.addEventListener('change', () => updateSwapButtonText(btn, form));
    }

    btn.addEventListener('click', (e) => {
      e.preventDefault();
      if (depSelect && arrSelect) {
        const temp = depSelect.value;
        depSelect.value = arrSelect.value;
        arrSelect.value = temp;

        // Visual rotation feedback
        const icon = btn.querySelector('svg');
        if (icon) {
          rotationDegree += 180;
          icon.style.transition = 'transform 0.3s ease';
          icon.style.transform = `rotate(${rotationDegree}deg)`;
        }

        // Update button text after swap
        updateSwapButtonText(btn, form);

        // Trigger change event for any dependent watchers
        depSelect.dispatchEvent(new Event('change', { bubbles: true }));
        arrSelect.dispatchEvent(new Event('change', { bubbles: true }));
      }
    });
  });
}

/**
 * 7. Quick Train Station Swap Button Handler
 */
function getStationShortName(selectElem) {
  if (!selectElem || selectElem.selectedIndex < 0) return '';
  const opt = selectElem.options[selectElem.selectedIndex];
  if (!opt || !opt.value) return '';
  const rawText = opt.textContent || opt.innerText || '';
  return rawText.split('(')[0].trim();
}

function updateSwapStationButtonText(btn, form) {
  if (!btn || !form) return;
  const depSelect = form.querySelector('select[name="departureStationId"]');
  const arrSelect = form.querySelector('select[name="arrivalStationId"]');
  const labelSpan = btn.querySelector('span');
  if (!labelSpan) return;

  const depStation = getStationShortName(depSelect);
  const arrStation = getStationShortName(arrSelect);

  if (depStation && arrStation) {
    labelSpan.innerHTML = `Đảo Chiều Tàu (${depStation} &harr; ${arrStation})`;
  } else if (depStation) {
    labelSpan.innerHTML = `Đảo Chiều Tàu (${depStation} &harr; ...)`;
  } else if (arrStation) {
    labelSpan.innerHTML = `Đảo Chiều Tàu (... &harr; ${arrStation})`;
  } else {
    labelSpan.innerHTML = `Đảo Chiều Tàu`;
  }
}

function initStationSwap() {
  const swapBtns = document.querySelectorAll('#swapStationsBtn, .btn-swap-stations');
  swapBtns.forEach(btn => {
    const form = btn.closest('form') || btn.closest('.card') || btn.closest('#trainSearchPanel') || document;
    const depSelect = form.querySelector('select[name="departureStationId"]');
    const arrSelect = form.querySelector('select[name="arrivalStationId"]');
    let rotationDegree = 0;

    if (depSelect && arrSelect) {
      // Sync label initially
      updateSwapStationButtonText(btn, form);

      // Listen to changes on both selects to update label dynamically
      depSelect.addEventListener('change', () => updateSwapStationButtonText(btn, form));
      arrSelect.addEventListener('change', () => updateSwapStationButtonText(btn, form));
    }

    btn.addEventListener('click', (e) => {
      e.preventDefault();
      if (depSelect && arrSelect) {
        const temp = depSelect.value;
        depSelect.value = arrSelect.value;
        arrSelect.value = temp;

        // Visual rotation feedback
        const icon = btn.querySelector('svg');
        if (icon) {
          rotationDegree += 180;
          icon.style.transition = 'transform 0.3s ease';
          icon.style.transform = `rotate(${rotationDegree}deg)`;
        }

        // Update button text after swap
        updateSwapStationButtonText(btn, form);

        // Trigger change event
        depSelect.dispatchEvent(new Event('change', { bubbles: true }));
        arrSelect.dispatchEvent(new Event('change', { bubbles: true }));
      }
    });
  });
}

/**
 * 8. Quick-Select Comboboxes for Departure & Arrival Places on Catalog Page
 */
function initCatalogPlaceComboboxes() {
  const comboboxes = Array.from(document.querySelectorAll('[data-place-combobox]'));
  if (!comboboxes.length) return;

  const config = window.VIETTRAVEL_CATALOG_PLACES || {
    transportType: 'ALL',
    airDepartures: [],
    airArrivals: [],
    trainDepartures: [],
    trainArrivals: []
  };

  function normalizeVi(str) {
    return (str || '')
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/đ/g, 'd')
      .replace(/Đ/g, 'D')
      .toLowerCase()
      .trim();
  }

  const AIR_CURATED = [
    { type: 'AIR', value: 'Hà Nội (HAN)', label: 'Hà Nội (HAN)', sub: 'Sân bay Nội Bài · Miền Bắc', code: 'HAN', aliases: ['ha noi', 'noi bai', 'han'] },
    { type: 'AIR', value: 'Sài Gòn (SGN)', label: 'Sài Gòn / TP.HCM (SGN)', sub: 'Sân bay Tân Sơn Nhất · Miền Nam', code: 'SGN', aliases: ['sai gon', 'tp ho chi minh', 'ho chi minh', 'tphcm', 'tp.hcm', 'tan son nhat', 'sgn'] },
    { type: 'AIR', value: 'Đà Nẵng (DAD)', label: 'Đà Nẵng (DAD)', sub: 'Sân bay Quốc tế Đà Nẵng · Miền Trung', code: 'DAD', aliases: ['da nang', 'dad'] },
    { type: 'AIR', value: 'Phú Quốc (PQC)', label: 'Phú Quốc (PQC)', sub: 'Sân bay Phú Quốc · Kiên Giang', code: 'PQC', aliases: ['phu quoc', 'pqc'] },
    { type: 'AIR', value: 'Nha Trang (CXR)', label: 'Nha Trang (CXR)', sub: 'Sân bay Cam Ranh · Khánh Hòa', code: 'CXR', aliases: ['nha trang', 'cam ranh', 'cxr'] },
    { type: 'AIR', value: 'Huế (HUI)', label: 'Huế (HUI)', sub: 'Sân bay Phú Bài · Thừa Thiên Huế', code: 'HUI', aliases: ['hue', 'phu bai', 'hui'] },
    { type: 'AIR', value: 'Hải Phòng (HPH)', label: 'Hải Phòng (HPH)', sub: 'Sân bay Cát Bi · Miền Bắc', code: 'HPH', aliases: ['hai phong', 'cat bi', 'hph'] }
  ];

  const TRAIN_CURATED = [
    { type: 'TRAIN', value: 'Ga Hà Nội', label: 'Ga Hà Nội', sub: 'Trung tâm Thủ đô · Tuyến Thống Nhất', code: '', aliases: ['ga ha noi', 'ha noi'] },
    { type: 'TRAIN', value: 'Ga Đà Nẵng', label: 'Ga Đà Nẵng', sub: 'Đà Nẵng · Tuyến đèo Hải Vân', code: '', aliases: ['ga da nang', 'da nang'] },
    { type: 'TRAIN', value: 'Ga Sài Gòn', label: 'Ga Sài Gòn', sub: 'TP. Hồ Chí Minh · Ga cuối Nam', code: '', aliases: ['ga sai gon', 'sai gon', 'tp hcm', 'ho chi minh'] },
    { type: 'TRAIN', value: 'Ga Huế', label: 'Ga Huế', sub: 'Thừa Thiên Huế · Miền Trung', code: '', aliases: ['ga hue', 'hue'] },
    { type: 'TRAIN', value: 'Ga Nha Trang', label: 'Ga Nha Trang', sub: 'Khánh Hòa · Nam Trung Bộ', code: '', aliases: ['ga nha trang', 'nha trang'] },
    { type: 'TRAIN', value: 'Ga Vinh', label: 'Ga Vinh', sub: 'Nghệ An · Bắc Trung Bộ', code: '', aliases: ['ga vinh', 'vinh'] }
  ];

  function matchesDbPlace(item, dbPlace) {
    if (!dbPlace) return false;
    const dbNorm = normalizeVi(dbPlace);
    const valNorm = normalizeVi(item.value);
    if (dbNorm === valNorm) return true;
    if (item.code && dbPlace.toUpperCase().includes('(' + item.code + ')')) return true;
    if (item.code === 'SGN' && !dbNorm.startsWith('ga ') && (dbNorm.includes('ho chi minh') || dbNorm.includes('sai gon'))) {
      return true;
    }
    return false;
  }

  function buildGroupItems(curatedList, dbPlaces, type) {
    const safeDbPlaces = Array.isArray(dbPlaces) ? dbPlaces : [];
    const items = curatedList.map(item => ({
      ...item,
      available: safeDbPlaces.some(dbPlace => matchesDbPlace(item, dbPlace))
    }));

    safeDbPlaces.forEach(dbPlace => {
      if (!dbPlace || !dbPlace.trim()) return;
      const alreadyIncluded = items.some(item => matchesDbPlace(item, dbPlace));
      if (!alreadyIncluded) {
        items.push({
          type,
          value: dbPlace.trim(),
          label: dbPlace.trim(),
          sub: 'Chuyến khả dụng trong hệ thống',
          code: '',
          aliases: [normalizeVi(dbPlace)],
          available: true
        });
      }
    });

    return items;
  }

  function getGroupsForField(role) {
    const isDeparture = role === 'departure';
    const airItems = buildGroupItems(
      AIR_CURATED,
      isDeparture ? config.airDepartures : config.airArrivals,
      'AIR'
    );
    const trainItems = buildGroupItems(
      TRAIN_CURATED,
      isDeparture ? config.trainDepartures : config.trainArrivals,
      'TRAIN'
    );

    if (config.transportType === 'AIR') {
      return [{ title: 'Sân Bay Nội Địa', type: 'AIR', items: airItems }];
    }
    if (config.transportType === 'TRAIN') {
      return [{ title: 'Nhà Ga Đường Sắt', type: 'TRAIN', items: trainItems }];
    }
    return [
      { title: 'Sân Bay Nội Địa', type: 'AIR', items: airItems },
      { title: 'Nhà Ga Đường Sắt', type: 'TRAIN', items: trainItems }
    ];
  }

  function createSvgIcon(type) {
    const ns = 'http://www.w3.org/2000/svg';
    const svg = document.createElementNS(ns, 'svg');
    svg.setAttribute('width', '14');
    svg.setAttribute('height', '14');
    svg.setAttribute('viewBox', '0 0 24 24');
    svg.setAttribute('fill', 'none');
    svg.setAttribute('stroke', 'currentColor');
    svg.setAttribute('stroke-width', '2.1');
    svg.setAttribute('stroke-linecap', 'round');
    svg.setAttribute('stroke-linejoin', 'round');

    if (type === 'AIR') {
      const path = document.createElementNS(ns, 'path');
      path.setAttribute('d', 'M17.8 19.2 16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.5-.1 1 .3 1.3L9 12l-2 3H4l-1 1 3 2 2 3 1-1v-3l3-2 3.5 5.3c.3.4.8.5 1.3.3l.5-.2c.4-.3.6-.7.5-1.2z');
      svg.appendChild(path);
    } else if (type === 'TRAIN') {
      const rect = document.createElementNS(ns, 'rect');
      rect.setAttribute('width', '16');
      rect.setAttribute('height', '16');
      rect.setAttribute('x', '4');
      rect.setAttribute('y', '3');
      rect.setAttribute('rx', '2');
      svg.appendChild(rect);

      const p1 = document.createElementNS(ns, 'path');
      p1.setAttribute('d', 'M4 11h16');
      svg.appendChild(p1);

      const p2 = document.createElementNS(ns, 'path');
      p2.setAttribute('d', 'M12 3v8');
      svg.appendChild(p2);
    } else if (type === 'CHECK') {
      svg.setAttribute('width', '15');
      svg.setAttribute('height', '15');
      svg.setAttribute('class', 'place-check-icon');
      const poly = document.createElementNS(ns, 'polyline');
      poly.setAttribute('points', '20 6 9 17 4 12');
      svg.appendChild(poly);
    }
    return svg;
  }

  function itemMatchesQuery(item, queryNorm) {
    if (!queryNorm) return true;
    const haystack = [
      normalizeVi(item.value),
      normalizeVi(item.label),
      normalizeVi(item.sub),
      normalizeVi(item.code),
      ...(item.aliases || []).map(normalizeVi)
    ].join(' ');
    return haystack.includes(queryNorm);
  }

  function isSameSelectedValue(item, currentValue) {
    if (!currentValue || !currentValue.trim()) return false;
    const currNorm = normalizeVi(currentValue);
    if (currNorm === normalizeVi(item.value) || currNorm === normalizeVi(item.label)) {
      return true;
    }
    if (item.code && currNorm === normalizeVi(item.code)) {
      return true;
    }
    return false;
  }

  const controllers = [];

  function closeAll(exceptRoot) {
    controllers.forEach(ctrl => {
      if (ctrl.root !== exceptRoot) {
        ctrl.close();
      }
    });
  }

  comboboxes.forEach(root => {
    const role = root.getAttribute('data-place-combobox');
    const input = root.querySelector('.place-input');
    const clearBtn = root.querySelector('.place-clear-btn');
    const toggleBtn = root.querySelector('.place-picker-btn');
    const dropdown = root.querySelector('.place-dropdown');
    if (!input || !toggleBtn || !dropdown) return;

    const groups = getGroupsForField(role);
    let isOpen = false;
    let highlightedIndex = -1;
    let visibleOptionButtons = [];

    function syncClearButton() {
      if (!clearBtn) return;
      clearBtn.hidden = input.value.trim().length === 0;
    }

    function updateHighlight(nextIndex) {
      if (!visibleOptionButtons.length) {
        highlightedIndex = -1;
        return;
      }
      if (nextIndex < 0) nextIndex = visibleOptionButtons.length - 1;
      if (nextIndex >= visibleOptionButtons.length) nextIndex = 0;

      visibleOptionButtons.forEach((btn, idx) => {
        if (idx === nextIndex) {
          btn.classList.add('is-highlighted');
          btn.scrollIntoView({ block: 'nearest' });
        } else {
          btn.classList.remove('is-highlighted');
        }
      });
      highlightedIndex = nextIndex;
    }

    function selectPlace(value) {
      input.value = value;
      syncClearButton();
      close();
    }

    function renderList(showAll) {
      const currentVal = input.value;
      const queryNorm = showAll ? '' : normalizeVi(currentVal);
      dropdown.textContent = '';
      visibleOptionButtons = [];
      highlightedIndex = -1;

      const header = document.createElement('div');
      header.className = 'place-dropdown-header';

      const headerTitle = document.createElement('span');
      if (config.transportType === 'AIR') {
        headerTitle.textContent = 'Chọn nhanh sân bay';
      } else if (config.transportType === 'TRAIN') {
        headerTitle.textContent = 'Chọn nhanh nhà ga';
      } else {
        headerTitle.textContent = 'Chọn nhanh sân bay / nhà ga';
      }
      header.appendChild(headerTitle);

      if (currentVal.trim().length > 0) {
        const resetBtn = document.createElement('button');
        resetBtn.type = 'button';
        resetBtn.className = 'place-dropdown-reset';
        resetBtn.textContent = 'Tất cả điểm';
        resetBtn.addEventListener('click', (e) => {
          e.preventDefault();
          e.stopPropagation();
          selectPlace('');
          input.focus();
        });
        header.appendChild(resetBtn);
      }
      dropdown.appendChild(header);

      const listContainer = document.createElement('div');
      listContainer.className = 'place-dropdown-list';

      let totalMatches = 0;
      groups.forEach(group => {
        const matchedItems = group.items.filter(item => itemMatchesQuery(item, queryNorm));
        if (!matchedItems.length) return;
        totalMatches += matchedItems.length;

        const groupLabel = document.createElement('div');
        groupLabel.className = 'place-group-label' + (group.type === 'TRAIN' ? ' is-train' : '');
        groupLabel.appendChild(createSvgIcon(group.type));
        const groupText = document.createElement('span');
        groupText.textContent = group.title;
        groupLabel.appendChild(groupText);
        listContainer.appendChild(groupLabel);

        matchedItems.forEach(item => {
          const isSelected = isSameSelectedValue(item, currentVal);
          const optBtn = document.createElement('button');
          optBtn.type = 'button';
          optBtn.className = 'place-option' + (isSelected ? ' is-selected' : '');
          optBtn.setAttribute('role', 'option');
          optBtn.setAttribute('aria-selected', isSelected ? 'true' : 'false');
          optBtn.setAttribute('data-place-option', item.value);

          const main = document.createElement('div');
          main.className = 'place-option-main';

          const iconWrap = document.createElement('span');
          iconWrap.className = 'place-option-icon' + (item.type === 'TRAIN' ? ' is-train' : '');
          iconWrap.appendChild(createSvgIcon(item.type));
          main.appendChild(iconWrap);

          const textWrap = document.createElement('div');
          textWrap.className = 'place-option-text';

          const titleEl = document.createElement('span');
          titleEl.className = 'place-option-title';
          titleEl.textContent = item.label;
          textWrap.appendChild(titleEl);

          const subEl = document.createElement('span');
          subEl.className = 'place-option-sub';
          subEl.textContent = item.sub;
          textWrap.appendChild(subEl);

          main.appendChild(textWrap);
          optBtn.appendChild(main);

          const meta = document.createElement('div');
          meta.className = 'place-option-meta';
          if (item.available) {
            const pill = document.createElement('span');
            pill.className = 'place-status-pill';
            pill.textContent = 'Có vé';
            meta.appendChild(pill);
          }
          if (isSelected) {
            meta.appendChild(createSvgIcon('CHECK'));
          }
          optBtn.appendChild(meta);

          optBtn.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            selectPlace(item.value);
          });

          listContainer.appendChild(optBtn);
          visibleOptionButtons.push(optBtn);
        });
      });

      if (totalMatches === 0) {
        const emptyEl = document.createElement('div');
        emptyEl.className = 'place-dropdown-empty';
        emptyEl.textContent = 'Không có trong danh sách gợi ý. Bấm "Lọc Chuyến" để tìm theo từ khóa "' + currentVal.trim() + '".';
        listContainer.appendChild(emptyEl);
      }

      dropdown.appendChild(listContainer);

      if (isOpen && totalMatches > 0) {
        const selectedBtn = visibleOptionButtons.find(b => b.classList.contains('is-selected'));
        if (selectedBtn && showAll) {
          selectedBtn.scrollIntoView({ block: 'nearest' });
        }
      }
    }

    function open(showAll) {
      closeAll(root);
      isOpen = true;
      renderList(showAll);
      dropdown.hidden = false;
      root.classList.add('is-open');
      input.setAttribute('aria-expanded', 'true');
      toggleBtn.setAttribute('aria-expanded', 'true');
    }

    function close() {
      if (!isOpen) return;
      isOpen = false;
      highlightedIndex = -1;
      dropdown.hidden = true;
      root.classList.remove('is-open');
      input.setAttribute('aria-expanded', 'false');
      toggleBtn.setAttribute('aria-expanded', 'false');
    }

    syncClearButton();

    toggleBtn.addEventListener('click', (e) => {
      e.preventDefault();
      e.stopPropagation();
      if (isOpen) {
        close();
      } else {
        open(true);
      }
    });

    input.addEventListener('focus', () => {
      if (!isOpen) {
        open(true);
      }
    });

    input.addEventListener('click', () => {
      if (!isOpen) {
        open(true);
      }
    });

    input.addEventListener('input', () => {
      syncClearButton();
      open(false);
    });

    input.addEventListener('keydown', (e) => {
      if (e.key === 'ArrowDown') {
        e.preventDefault();
        if (!isOpen) {
          open(true);
          updateHighlight(0);
        } else {
          updateHighlight(highlightedIndex + 1);
        }
      } else if (e.key === 'ArrowUp') {
        e.preventDefault();
        if (!isOpen) {
          open(true);
          updateHighlight(visibleOptionButtons.length - 1);
        } else {
          updateHighlight(highlightedIndex - 1);
        }
      } else if (e.key === 'Enter') {
        if (isOpen && highlightedIndex >= 0 && visibleOptionButtons[highlightedIndex]) {
          e.preventDefault();
          const chosen = visibleOptionButtons[highlightedIndex].getAttribute('data-place-option');
          if (chosen !== null) {
            selectPlace(chosen);
          }
        }
      } else if (e.key === 'Escape') {
        if (isOpen) {
          e.preventDefault();
          close();
        }
      }
    });

    if (clearBtn) {
      clearBtn.addEventListener('click', (e) => {
        e.preventDefault();
        e.stopPropagation();
        input.value = '';
        syncClearButton();
        input.focus();
        open(true);
      });
    }

    controllers.push({ root, close, syncClearButton });
  });

  document.addEventListener('mousedown', (e) => {
    const inside = e.target.closest('[data-place-combobox]');
    if (!inside) {
      closeAll(null);
    }
  });

  document.addEventListener('focusin', (e) => {
    const inside = e.target.closest('[data-place-combobox]');
    if (!inside) {
      closeAll(null);
    }
  });

  const swapBtn = document.getElementById('catalogPlaceSwapBtn');
  if (swapBtn) {
    let swapDeg = 0;
    swapBtn.addEventListener('click', () => {
      const depInput = document.getElementById('departurePlaceInput');
      const arrInput = document.getElementById('arrivalPlaceInput');
      if (!depInput || !arrInput) return;

      const tmp = depInput.value;
      depInput.value = arrInput.value;
      arrInput.value = tmp;

      const svg = swapBtn.querySelector('svg');
      if (svg) {
        swapDeg += 180;
        svg.style.transform = `rotate(${swapDeg}deg)`;
      }

      controllers.forEach(c => {
        c.syncClearButton();
        c.close();
      });
    });
  }
}

/**
 * 8. Real-time Catalog Filter, Sort & Client Pagination
 */
function initCatalogToolbar() {
  const container = document.getElementById('catalogCardsContainer');
  if (!container) return;

  const items = Array.from(container.querySelectorAll('.ticket-result-item'));
  if (!items.length) return;

  const sortSelect = document.getElementById('catalogSortSelect');
  const carrierSelect = document.getElementById('catalogCarrierSelect');
  const timeRangeSelect = document.getElementById('catalogTimeRangeSelect');
  const resetBtn = document.getElementById('catalogResetFiltersBtn');
  const clearSubFilterBtn = document.getElementById('catalogClearSubFilterBtn');
  const visibleCountEl = document.getElementById('catalogVisibleCount');
  const noMatchesEl = document.getElementById('catalogNoFilterMatches');
  const paginationBar = document.getElementById('catalogPaginationBar');

  const pageSize = 6;
  let currentPage = 1;

  function filterAndSort() {
    const carrier = carrierSelect ? carrierSelect.value : 'ALL';
    const timeRange = timeRangeSelect ? timeRangeSelect.value : 'ALL';
    const sortVal = sortSelect ? sortSelect.value : 'time_asc';

    // 1. Filter
    const matched = items.filter(el => {
      // Carrier check
      if (carrier !== 'ALL') {
        const itemCarrier = el.getAttribute('data-carrier') || '';
        if (!itemCarrier.toLowerCase().includes(carrier.toLowerCase())) {
          return false;
        }
      }
      // Hour range check
      if (timeRange !== 'ALL') {
        const hour = parseInt(el.getAttribute('data-hour') || '0', 10);
        if (timeRange === 'MORNING' && (hour < 6 || hour >= 12)) return false;
        if (timeRange === 'AFTERNOON' && (hour < 12 || hour >= 18)) return false;
        if (timeRange === 'EVENING' && (hour < 18 || hour >= 24)) return false;
        if (timeRange === 'NIGHT' && hour >= 6) return false;
      }
      return true;
    });

    // 2. Sort
    matched.sort((a, b) => {
      const priceA = parseFloat(a.getAttribute('data-price') || '0');
      const priceB = parseFloat(b.getAttribute('data-price') || '0');
      const timeA = a.getAttribute('data-time') || '';
      const timeB = b.getAttribute('data-time') || '';

      if (sortVal === 'price_asc') return priceA - priceB;
      if (sortVal === 'price_desc') return priceB - priceA;
      if (sortVal === 'time_desc') return timeB.localeCompare(timeA);
      return timeA.localeCompare(timeB); // default time_asc
    });

    // 3. Count & empty state
    if (visibleCountEl) visibleCountEl.textContent = matched.length;

    if (matched.length === 0) {
      items.forEach(el => el.style.display = 'none');
      if (noMatchesEl) noMatchesEl.style.display = 'block';
      if (paginationBar) paginationBar.innerHTML = '';
      return;
    }

    if (noMatchesEl) noMatchesEl.style.display = 'none';

    // 4. Paginate
    const totalPages = Math.ceil(matched.length / pageSize);
    if (currentPage > totalPages) currentPage = 1;

    const startIdx = (currentPage - 1) * pageSize;
    const endIdx = startIdx + pageSize;

    items.forEach(el => el.style.display = 'none');
    matched.forEach((el, idx) => {
      if (idx >= startIdx && idx < endIdx) {
        el.style.display = 'block';
        container.appendChild(el);
      }
    });

    // 5. Render Pagination
    renderPagination(totalPages, matched.length);
  }

  function renderPagination(totalPages, totalItems) {
    if (!paginationBar) return;
    if (totalPages <= 1) {
      paginationBar.innerHTML = '';
      return;
    }

    let html = '';
    html += `<button type="button" class="btn btn-sm btn-outline ${currentPage === 1 ? 'disabled' : ''}" data-page="${currentPage - 1}" ${currentPage === 1 ? 'disabled' : ''}>&larr; Trước</button>`;

    for (let p = 1; p <= totalPages; p++) {
      html += `<button type="button" class="btn btn-sm ${p === currentPage ? 'btn-primary' : 'btn-outline'}" data-page="${p}" style="min-width: 34px;">${p}</button>`;
    }

    html += `<button type="button" class="btn btn-sm btn-outline ${currentPage === totalPages ? 'disabled' : ''}" data-page="${currentPage + 1}" ${currentPage === totalPages ? 'disabled' : ''}>Tiếp &rarr;</button>`;

    paginationBar.innerHTML = html;

    paginationBar.querySelectorAll('button[data-page]').forEach(btn => {
      btn.addEventListener('click', () => {
        const p = parseInt(btn.getAttribute('data-page'), 10);
        if (p >= 1 && p <= totalPages && p !== currentPage) {
          currentPage = p;
          filterAndSort();
          window.scrollTo({ top: container.offsetTop - 90, behavior: 'smooth' });
        }
      });
    });
  }

  if (sortSelect) sortSelect.addEventListener('change', () => { currentPage = 1; filterAndSort(); });
  if (carrierSelect) carrierSelect.addEventListener('change', () => { currentPage = 1; filterAndSort(); });
  if (timeRangeSelect) timeRangeSelect.addEventListener('change', () => { currentPage = 1; filterAndSort(); });

  function resetAll() {
    if (sortSelect) sortSelect.value = 'time_asc';
    if (carrierSelect) carrierSelect.value = 'ALL';
    if (timeRangeSelect) timeRangeSelect.value = 'ALL';
    currentPage = 1;
    filterAndSort();
  }

  if (resetBtn) resetBtn.addEventListener('click', resetAll);
  if (clearSubFilterBtn) clearSubFilterBtn.addEventListener('click', resetAll);

  // Initial run
  filterAndSort();
}

/**
 * 9. Interactive Visual Seat Map for Booking Form
 */
function initBookingVisualSeatMap() {
  const section = document.getElementById('visualSeatMapSection');
  if (!section) return;

  const grid = document.getElementById('dynamicSeatGrid');
  if (!grid) return;

  const transportType = section.getAttribute('data-transport-type') || 'AIR';
  const baseSeat = section.getAttribute('data-base-seat') || (transportType === 'AIR' ? '12A' : 'Toa 2 - Ghế 15');
  const paxCards = Array.from(document.querySelectorAll('.passenger-input-card'));
  const paxInputs = Array.from(document.querySelectorAll('.pax-seat-input'));
  const selectingLabel = document.getElementById('currentSelectingPaxLabel');

  let activePaxIdx = 0;

  // Pre-occupied seat patterns
  const occupiedAirSeats = new Set(['11B', '11E', '13C', '14D', '15A', '15F', '10C', '10D']);
  const occupiedTrainSeats = new Set(['03', '07', '10', '18', '21']);

  function updateActivePaxUI() {
    if (selectingLabel) {
      selectingLabel.textContent = `Hành khách #${activePaxIdx + 1}`;
    }
    paxCards.forEach((card, idx) => {
      if (idx === activePaxIdx) {
        card.style.borderColor = 'var(--primary)';
        card.style.boxShadow = '0 0 0 2px rgba(0, 98, 210, 0.2)';
        card.style.background = '#ffffff';
      } else {
        card.style.borderColor = 'var(--border)';
        card.style.boxShadow = 'none';
        card.style.background = 'var(--surface-alt)';
      }
    });
  }

  // Setup click listener for passenger cards
  paxCards.forEach((card, idx) => {
    card.addEventListener('click', (e) => {
      // Don't intercept input focus
      if (e.target.tagName === 'INPUT' || e.target.tagName === 'SELECT') return;
      activePaxIdx = idx;
      updateActivePaxUI();
    });

    const selectBtn = card.querySelector('.btn-select-pax-for-seat');
    if (selectBtn) {
      selectBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        activePaxIdx = idx;
        updateActivePaxUI();
        section.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
      });
    }
  });

  function renderGrid() {
    grid.innerHTML = '';
    const currentAssignments = paxInputs.map(inp => (inp.value || '').trim());

    if (transportType === 'AIR') {
      // Parse base row
      let baseRow = 12;
      const m = baseSeat.match(/^(\d+)/);
      if (m) baseRow = parseInt(m[1], 10);
      const startRow = Math.max(1, baseRow - 1);
      const endRow = startRow + 4; // 5 rows = 30 seats

      const cols = ['A', 'B', 'C', 'D', 'E', 'F'];

      for (let r = startRow; r <= endRow; r++) {
        for (let c of cols) {
          const seatCode = `${String(r).padStart(2, '0')}${c}`;
          const isAisle = (c === 'C' || c === 'D');
          const isWindow = (c === 'A' || c === 'F');
          const typeNote = isWindow ? 'Cửa sổ' : (isAisle ? 'Lối đi' : 'Giữa');

          const assignedPaxIdx = currentAssignments.indexOf(seatCode);
          const isOccupied = assignedPaxIdx === -1 && occupiedAirSeats.has(seatCode);

          const seatCard = document.createElement('div');
          seatCard.className = 'seat-card';
          seatCard.setAttribute('data-seat-code', seatCode);

          if (assignedPaxIdx !== -1) {
            seatCard.classList.add('selected');
            seatCard.innerHTML = `
              <div class="seat-number" style="color: var(--accent);">${seatCode}</div>
              <div class="seat-type-tag">${typeNote}</div>
              <div class="seat-status-tag" style="background: #ffedd5; color: #c2410c; font-weight: 700;">Khách #${assignedPaxIdx + 1}</div>
            `;
          } else if (isOccupied) {
            seatCard.classList.add('occupied');
            seatCard.innerHTML = `
              <div class="seat-number">${seatCode}</div>
              <div class="seat-type-tag">${typeNote}</div>
              <div class="seat-status-tag occupied">Đã bán</div>
            `;
          } else {
            seatCard.innerHTML = `
              <div class="seat-number">${seatCode}</div>
              <div class="seat-type-tag">${typeNote}</div>
              <div class="seat-status-tag available">Trống</div>
            `;
            seatCard.addEventListener('click', () => selectSeatForCurrentPax(seatCode));
          }

          grid.appendChild(seatCard);
        }
      }
    } else {
      // TRAIN SEAT GRID
      let prefix = 'Toa 2 - Ghế ';
      if (baseSeat.includes('Toa') || baseSeat.includes('Ghế') || baseSeat.includes('Giường')) {
        const pm = baseSeat.match(/^(.*?-\s*(?:Ghế|Giường)\s*)/i);
        if (pm) prefix = pm[1];
      }

      for (let i = 1; i <= 24; i++) {
        const numStr = String(i).padStart(2, '0');
        const seatCode = `${prefix}${numStr}`;
        const typeNote = i % 2 === 1 ? 'Cửa sổ' : 'Lối đi';

        const assignedPaxIdx = currentAssignments.indexOf(seatCode);
        const isOccupied = assignedPaxIdx === -1 && occupiedTrainSeats.has(numStr);

        const seatCard = document.createElement('div');
        seatCard.className = 'seat-card';
        seatCard.setAttribute('data-seat-code', seatCode);

        if (assignedPaxIdx !== -1) {
          seatCard.classList.add('selected');
          seatCard.innerHTML = `
            <div class="seat-number" style="color: var(--accent);">${numStr}</div>
            <div class="seat-type-tag">${typeNote}</div>
            <div class="seat-status-tag" style="background: #ffedd5; color: #c2410c; font-weight: 700;">Khách #${assignedPaxIdx + 1}</div>
          `;
        } else if (isOccupied) {
          seatCard.classList.add('occupied');
          seatCard.innerHTML = `
            <div class="seat-number">${numStr}</div>
            <div class="seat-type-tag">${typeNote}</div>
            <div class="seat-status-tag occupied">Đã bán</div>
          `;
        } else {
          seatCard.innerHTML = `
            <div class="seat-number">${numStr}</div>
            <div class="seat-type-tag">${typeNote}</div>
            <div class="seat-status-tag available">Trống</div>
          `;
          seatCard.addEventListener('click', () => selectSeatForCurrentPax(seatCode));
        }

        grid.appendChild(seatCard);
      }
    }
  }

  function selectSeatForCurrentPax(seatCode) {
    if (paxInputs[activePaxIdx]) {
      paxInputs[activePaxIdx].value = seatCode;
    }

    // Advance to next pax if multiple passengers
    if (paxInputs.length > 1) {
      activePaxIdx = (activePaxIdx + 1) % paxInputs.length;
    }

    updateActivePaxUI();
    renderGrid();
  }

  // Initial render
  updateActivePaxUI();
  renderGrid();
}
