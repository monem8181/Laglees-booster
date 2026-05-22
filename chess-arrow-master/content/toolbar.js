'use strict';

/**
 * Floating draggable toolbar for Chess Arrow Master.
 * Glassmorphism UI with mobile-optimized controls.
 */
class Toolbar {
  constructor(touchHandler, overlayEngine) {
    this._touch = touchHandler;
    this._overlay = overlayEngine;
    this._fab = null;
    this._menu = null;
    this._isOpen = false;
    this._isDragging = false;
    this._dragStart = null;
    this._fabPos = { x: 20, y: 200 };
    this._snapToEdge = true;
    this._onSettingsOpen = null;

    this._colors = [
      { name: 'Green', hex: '#22c55e' },
      { name: 'Blue', hex: '#3b82f6' },
      { name: 'Red', hex: '#ef4444' },
      { name: 'Yellow', hex: '#eab308' },
      { name: 'Purple', hex: '#a855f7' },
      { name: 'Orange', hex: '#f97316' },
      { name: 'Cyan', hex: '#06b6d4' },
      { name: 'White', hex: '#ffffff' }
    ];
  }

  init() {
    this._createFAB();
    this._createMenu();
    document.body.appendChild(this._fab);
    document.body.appendChild(this._menu);
  }

  _createFAB() {
    this._fab = document.createElement('div');
    this._fab.id = 'cam-fab';
    this._fab.innerHTML = '🎯';
    this._fab.style.cssText = `
      position: fixed;
      left: ${this._fabPos.x}px;
      top: ${this._fabPos.y}px;
      width: 48px;
      height: 48px;
      border-radius: 50%;
      background: rgba(30, 30, 40, 0.85);
      backdrop-filter: blur(12px);
      -webkit-backdrop-filter: blur(12px);
      border: 1px solid rgba(255, 255, 255, 0.15);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 22px;
      cursor: pointer;
      z-index: 10001;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.4);
      user-select: none;
      touch-action: none;
      transition: transform 0.2s, box-shadow 0.2s;
    `;

    let startX, startY, startLeft, startTop, moved;

    this._fab.addEventListener('touchstart', (e) => {
      e.preventDefault();
      const t = e.touches[0];
      startX = t.clientX;
      startY = t.clientY;
      startLeft = this._fabPos.x;
      startTop = this._fabPos.y;
      moved = false;
      this._fab.style.transform = 'scale(1.1)';
    }, { passive: false });

    this._fab.addEventListener('touchmove', (e) => {
      e.preventDefault();
      const t = e.touches[0];
      const dx = t.clientX - startX;
      const dy = t.clientY - startY;
      if (Math.abs(dx) > 5 || Math.abs(dy) > 5) moved = true;

      this._fabPos.x = Math.max(0, Math.min(window.innerWidth - 48, startLeft + dx));
      this._fabPos.y = Math.max(0, Math.min(window.innerHeight - 48, startTop + dy));

      this._fab.style.left = this._fabPos.x + 'px';
      this._fab.style.top = this._fabPos.y + 'px';
    }, { passive: false });

    this._fab.addEventListener('touchend', (e) => {
      e.preventDefault();
      this._fab.style.transform = '';
      if (this._snapToEdge) this._snapFAB();
      if (!moved) this._toggleMenu();
    }, { passive: false });
  }

  _snapFAB() {
    const midX = window.innerWidth / 2;
    if (this._fabPos.x + 24 < midX) {
      this._fabPos.x = 8;
    } else {
      this._fabPos.x = window.innerWidth - 56;
    }
    this._fab.style.left = this._fabPos.x + 'px';
    this._fab.style.transition = 'left 0.25s ease-out';
    setTimeout(() => { this._fab.style.transition = ''; }, 300);
  }

  _createMenu() {
    this._menu = document.createElement('div');
    this._menu.id = 'cam-menu';
    this._menu.style.cssText = `
      position: fixed;
      left: 0; top: 0; right: 0; bottom: 0;
      z-index: 10000;
      display: none;
      align-items: center;
      justify-content: center;
      background: rgba(0, 0, 0, 0.5);
      backdrop-filter: blur(4px);
      -webkit-backdrop-filter: blur(4px);
    `;

    const panel = document.createElement('div');
    panel.id = 'cam-panel';
    panel.style.cssText = `
      background: rgba(25, 25, 35, 0.92);
      backdrop-filter: blur(20px);
      -webkit-backdrop-filter: blur(20px);
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 20px;
      padding: 20px;
      width: 88vw;
      max-width: 340px;
      max-height: 80vh;
      overflow-y: auto;
      box-shadow: 0 12px 40px rgba(0, 0, 0, 0.6);
      color: #e0e0e0;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
      font-size: 14px;
    `;

    panel.innerHTML = this._buildMenuHTML();
    this._menu.appendChild(panel);

    this._menu.addEventListener('touchstart', (e) => {
      if (e.target === this._menu) {
        e.preventDefault();
        this._toggleMenu();
      }
    }, { passive: false });

    panel.addEventListener('touchstart', (e) => e.stopPropagation(), { passive: false });
  }

  _buildMenuHTML() {
    const activeColor = this._touch.color;
    const activeMode = this._touch.mode;
    const isEnabled = this._touch.enabled;

    return `
      <div style="text-align:center;margin-bottom:14px">
        <div style="font-size:18px;font-weight:700;letter-spacing:0.5px">Chess Arrow Master</div>
        <div style="font-size:11px;color:#888;margin-top:2px">Touch Annotation Tool</div>
      </div>

      <div style="display:flex;justify-content:center;margin-bottom:14px">
        <button id="cam-toggle" class="cam-btn ${isEnabled ? 'cam-active' : ''}" style="width:100%;padding:10px;font-size:15px;font-weight:600">
          ${isEnabled ? '● ENABLED' : '○ DISABLED'}
        </button>
      </div>

      <div class="cam-section-title">MODE</div>
      <div style="display:flex;gap:6px;flex-wrap:wrap;margin-bottom:12px">
        <button class="cam-btn cam-mode ${activeMode === 'arrow' ? 'cam-active' : ''}" data-mode="arrow">Arrow</button>
        <button class="cam-btn cam-mode ${activeMode === 'line' ? 'cam-active' : ''}" data-mode="line">Line</button>
        <button class="cam-btn cam-mode ${activeMode === 'highlight' ? 'cam-active' : ''}" data-mode="highlight">Highlight</button>
      </div>

      <div class="cam-section-title">PIECE PRESETS</div>
      <div style="display:flex;gap:6px;flex-wrap:wrap;margin-bottom:12px">
        <button class="cam-btn cam-preset ${this._touch.arrowType === 'normal' ? 'cam-active' : ''}" data-preset="normal">Normal</button>
        <button class="cam-btn cam-preset ${this._touch.arrowType === 'knight' ? 'cam-active' : ''}" data-preset="knight">♞ Knight</button>
        <button class="cam-btn cam-preset ${this._touch.arrowType === 'bishop' ? 'cam-active' : ''}" data-preset="bishop">♝ Bishop</button>
        <button class="cam-btn cam-preset ${this._touch.arrowType === 'rook' ? 'cam-active' : ''}" data-preset="rook">♜ Rook</button>
        <button class="cam-btn cam-preset ${this._touch.arrowType === 'queen' ? 'cam-active' : ''}" data-preset="queen">♛ Queen</button>
        <button class="cam-btn cam-preset ${this._touch.arrowType === 'king' ? 'cam-active' : ''}" data-preset="king">♚ King</button>
        <button class="cam-btn cam-preset ${this._touch.arrowType === 'pawn' ? 'cam-active' : ''}" data-preset="pawn">♟ Pawn</button>
      </div>

      <div class="cam-section-title">COLOR</div>
      <div style="display:flex;gap:8px;flex-wrap:wrap;margin-bottom:12px;justify-content:center">
        ${this._colors.map(c => `
          <div class="cam-color-swatch ${activeColor === c.hex ? 'cam-color-active' : ''}"
               data-color="${c.hex}"
               style="background:${c.hex}"
               title="${c.name}"></div>
        `).join('')}
      </div>

      <div class="cam-section-title">ACTIONS</div>
      <div style="display:flex;gap:6px;flex-wrap:wrap;margin-bottom:12px">
        <button class="cam-btn" id="cam-undo">↩ Undo</button>
        <button class="cam-btn" id="cam-redo">↪ Redo</button>
        <button class="cam-btn" id="cam-clear">✕ Clear All</button>
        <button class="cam-btn" id="cam-toggle-overlay">${this._overlay.visible ? '👁 Hide' : '👁 Show'}</button>
        <button class="cam-btn" id="cam-settings">⚙ Settings</button>
      </div>
    `;
  }

  _toggleMenu() {
    this._isOpen = !this._isOpen;
    if (this._isOpen) {
      this._menu.querySelector('#cam-panel').innerHTML = this._buildMenuHTML();
      this._menu.style.display = 'flex';
      this._bindMenuEvents();
      requestAnimationFrame(() => {
        this._menu.style.opacity = '1';
      });
    } else {
      this._menu.style.display = 'none';
    }
  }

  _bindMenuEvents() {
    const panel = this._menu.querySelector('#cam-panel');

    panel.querySelector('#cam-toggle')?.addEventListener('click', () => {
      this._touch.enabled = !this._touch.enabled;
      this._refreshMenu();
    });

    panel.querySelectorAll('.cam-mode').forEach(btn => {
      btn.addEventListener('click', () => {
        this._touch.mode = btn.dataset.mode;
        this._refreshMenu();
      });
    });

    panel.querySelectorAll('.cam-preset').forEach(btn => {
      btn.addEventListener('click', () => {
        this._touch.arrowType = btn.dataset.preset;
        this._touch.mode = 'arrow';
        this._refreshMenu();
      });
    });

    panel.querySelectorAll('.cam-color-swatch').forEach(swatch => {
      swatch.addEventListener('click', () => {
        this._touch.color = swatch.dataset.color;
        this._refreshMenu();
      });
    });

    panel.querySelector('#cam-undo')?.addEventListener('click', () => {
      this._touch.undo();
    });

    panel.querySelector('#cam-redo')?.addEventListener('click', () => {
      this._touch.redo();
    });

    panel.querySelector('#cam-clear')?.addEventListener('click', () => {
      this._touch.clearAll();
      this._refreshMenu();
    });

    panel.querySelector('#cam-toggle-overlay')?.addEventListener('click', () => {
      this._overlay.setVisible(!this._overlay.visible);
      this._refreshMenu();
    });

    panel.querySelector('#cam-settings')?.addEventListener('click', () => {
      this._toggleMenu();
      if (this._onSettingsOpen) this._onSettingsOpen();
    });
  }

  _refreshMenu() {
    const panel = this._menu.querySelector('#cam-panel');
    if (panel) {
      panel.innerHTML = this._buildMenuHTML();
      this._bindMenuEvents();
    }
  }

  set onSettingsOpen(fn) { this._onSettingsOpen = fn; }

  setPosition(x, y) {
    this._fabPos = { x, y };
    if (this._fab) {
      this._fab.style.left = x + 'px';
      this._fab.style.top = y + 'px';
    }
  }

  set snapToEdge(v) { this._snapToEdge = v; }

  destroy() {
    if (this._fab) this._fab.remove();
    if (this._menu) this._menu.remove();
  }
}

window.Toolbar = Toolbar;
