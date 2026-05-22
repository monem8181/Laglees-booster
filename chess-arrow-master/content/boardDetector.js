'use strict';

/**
 * Detects and tracks the Chess.com board element, orientation, and dimensions.
 * Uses MutationObserver and ResizeObserver for dynamic updates.
 */
class BoardDetector {
  constructor() {
    this._board = null;
    this._rect = null;
    this._flipped = false;
    this._listeners = [];
    this._mutationObs = null;
    this._resizeObs = null;
    this._initialized = false;
    this._boundOnResize = () => this._onResize();
    this._boundOnOrientationChange = () => setTimeout(() => this._onResize(), 300);
    this._selectors = [
      'wc-chess-board',
      'chess-board',
      '.board',
      '#board-single',
      '#board-play-computer',
      '#board-vs-personalities',
      '#board-puzzle',
      '#board-review',
      '#board-analysis',
      '[class*="board"]'
    ];
  }

  init() {
    if (!this._initialized) {
      this._initialized = true;
      this._observeMutations();
      window.addEventListener('resize', this._boundOnResize);
      window.addEventListener('orientationchange', this._boundOnOrientationChange);
    }
    this._detect();
  }

  _detect() {
    let el = null;

    for (const sel of this._selectors) {
      el = document.querySelector(sel);
      if (el) break;
    }

    if (!el) {
      const candidates = document.querySelectorAll('[class*="board"]');
      for (const c of candidates) {
        const r = c.getBoundingClientRect();
        const ratio = r.width / (r.height || 1);
        if (r.width > 100 && ratio > 0.9 && ratio < 1.1) {
          el = c;
          break;
        }
      }
    }

    if (el && el !== this._board) {
      this._board = el;
      this._updateRect();
      this._detectOrientation();
      this._observeResize();
      this._emit('boardFound', { board: el, rect: this._rect, flipped: this._flipped });
    }
  }

  _updateRect() {
    if (!this._board) return;
    this._rect = this._board.getBoundingClientRect();
  }

  _detectOrientation() {
    if (!this._board) return;

    const flippedAttr = this._board.getAttribute('flipped');
    if (flippedAttr !== null) {
      this._flipped = flippedAttr === 'true' || flippedAttr === '';
      return;
    }

    const classFlip = this._board.classList.contains('flipped') ||
                      this._board.closest('.flipped') !== null;
    if (classFlip) {
      this._flipped = true;
      return;
    }

    const coords = this._board.querySelectorAll('[class*="coordinate"]');
    if (coords.length > 0) {
      const firstCoord = coords[0];
      const text = firstCoord.textContent?.trim();
      if (text === '8' || text === 'h') {
        this._flipped = true;
        return;
      }
    }

    this._flipped = false;
  }

  _observeMutations() {
    this._mutationObs = new MutationObserver(() => {
      if (!this._board || !document.contains(this._board)) {
        this._board = null;
      }
      this._detect();
      this._detectOrientation();
    });

    this._mutationObs.observe(document.body, {
      childList: true,
      subtree: true,
      attributes: true,
      attributeFilter: ['class', 'flipped', 'style']
    });
  }

  _observeResize() {
    if (this._resizeObs) this._resizeObs.disconnect();

    this._resizeObs = new ResizeObserver(() => {
      this._updateRect();
      this._emit('boardResized', { rect: this._rect });
    });

    if (this._board) {
      this._resizeObs.observe(this._board);
    }
  }

  _onResize() {
    this._updateRect();
    this._emit('boardResized', { rect: this._rect });
  }

  on(event, fn) {
    this._listeners.push({ event, fn });
  }

  off(event, fn) {
    this._listeners = this._listeners.filter(l => l.event !== event || l.fn !== fn);
  }

  _emit(event, data) {
    for (const l of this._listeners) {
      if (l.event === event) l.fn(data);
    }
  }

  get board() { return this._board; }
  get rect() { this._updateRect(); return this._rect; }
  get flipped() { return this._flipped; }

  destroy() {
    if (this._mutationObs) this._mutationObs.disconnect();
    if (this._resizeObs) this._resizeObs.disconnect();
    window.removeEventListener('resize', this._boundOnResize);
    window.removeEventListener('orientationchange', this._boundOnOrientationChange);
    this._initialized = false;
    this._listeners = [];
  }
}

window.BoardDetector = BoardDetector;
