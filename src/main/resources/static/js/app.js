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
