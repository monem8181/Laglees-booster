'use strict';

/**
 * Handles all touch/pointer gestures for the annotation overlay.
 * Supports: single tap, long press + drag, two-finger drag, double tap, triple tap.
 */
class TouchHandler {
  constructor(boardDetector, squareMapper, overlayEngine, arrowRenderer) {
    this._detector = boardDetector;
    this._mapper = squareMapper;
    this._overlay = overlayEngine;
    this._arrows = arrowRenderer;
    this._enabled = false;
    this._mode = 'arrow'; // arrow | line | highlight
    this._arrowType = 'normal';
    this._color = '#22c55e';
    this._thickness = 8;
    this._opacity = 0.85;
    this._glow = true;
    this._animate = true;
    this._highlightOpacity = 0.35;
    this._longPressDuration = 400;
    this._gestureSensitivity = 12;

    // State
    this._touchStartTime = 0;
    this._touchStartPos = null;
    this._touchStartSquare = null;
    this._longPressTimer = null;
    this._isLongPress = false;
    this._isDragging = false;
    this._twoFingerMode = false;
    this._tapCount = 0;
    this._tapTimer = null;
    this._lastTapTime = 0;
    this._lastTapSquare = null;

    // Annotation data
    this._annotations = { arrows: [], highlights: new Map() };
    this._undoStack = [];
    this._redoStack = [];

    // Touch event element
    this._touchTarget = null;

    // Bound handlers
    this._onTouchStart = this._handleTouchStart.bind(this);
    this._onTouchMove = this._handleTouchMove.bind(this);
    this._onTouchEnd = this._handleTouchEnd.bind(this);
    this._onTouchCancel = this._handleTouchCancel.bind(this);
  }

  init() {
    this._detector.on('boardFound', () => this._attachListeners());
    if (this._detector.board) this._attachListeners();
  }

  _attachListeners() {
    this._detachListeners();

    const board = this._detector.board;
    if (!board) return;

    this._touchTarget = board;

    board.addEventListener('touchstart', this._onTouchStart, { passive: false });
    board.addEventListener('touchmove', this._onTouchMove, { passive: false });
    board.addEventListener('touchend', this._onTouchEnd, { passive: false });
    board.addEventListener('touchcancel', this._onTouchCancel, { passive: false });
  }

  _detachListeners() {
    if (!this._touchTarget) return;
    this._touchTarget.removeEventListener('touchstart', this._onTouchStart);
    this._touchTarget.removeEventListener('touchmove', this._onTouchMove);
    this._touchTarget.removeEventListener('touchend', this._onTouchEnd);
    this._touchTarget.removeEventListener('touchcancel', this._onTouchCancel);
    this._touchTarget = null;
  }

  _handleTouchStart(e) {
    if (!this._enabled) return;

    if (e.touches.length === 2) {
      e.preventDefault();
      this._twoFingerMode = true;
      this._clearLongPress();
      const t = e.touches[0];
      this._touchStartPos = { x: t.clientX, y: t.clientY };
      this._touchStartSquare = this._mapper.clientToSquare(t.clientX, t.clientY);
      return;
    }

    if (e.touches.length !== 1) return;

    const touch = e.touches[0];
    const square = this._mapper.clientToSquare(touch.clientX, touch.clientY);
    if (!square) return;

    e.preventDefault();

    this._touchStartTime = Date.now();
    this._touchStartPos = { x: touch.clientX, y: touch.clientY };
    this._touchStartSquare = square;
    this._isLongPress = false;
    this._isDragging = false;
    this._twoFingerMode = false;

    this._longPressTimer = setTimeout(() => {
      this._isLongPress = true;
    }, this._longPressDuration);
  }

  _handleTouchMove(e) {
    if (!this._enabled) return;

    if (this._twoFingerMode && e.touches.length >= 1) {
      e.preventDefault();
      const t = e.touches[0];
      this._isDragging = true;
      this._updatePreview(t.clientX, t.clientY);
      return;
    }

    if (e.touches.length !== 1 || !this._touchStartPos) return;

    const touch = e.touches[0];
    const dx = touch.clientX - this._touchStartPos.x;
    const dy = touch.clientY - this._touchStartPos.y;
    const dist = Math.sqrt(dx * dx + dy * dy);

    if (dist > this._gestureSensitivity) {
      if (this._isLongPress || this._mode === 'arrow' || this._mode === 'line') {
        e.preventDefault();
        this._isDragging = true;
        this._clearLongPress();
        this._updatePreview(touch.clientX, touch.clientY);
      }
    }
  }

  _handleTouchEnd(e) {
    if (!this._enabled) return;

    this._clearLongPress();

    const now = Date.now();

    if (this._twoFingerMode && this._isDragging) {
      e.preventDefault();
      this._finalizeDrag(e);
      this._twoFingerMode = false;
      this._isDragging = false;
      this._overlay.clearPreview();
      return;
    }

    if (this._isDragging) {
      e.preventDefault();
      this._finalizeDrag(e);
      this._isDragging = false;
      this._overlay.clearPreview();
      return;
    }

    if (!this._touchStartSquare) return;

    e.preventDefault();

    // Tap detection
    const timeSinceLast = now - this._lastTapTime;
    const sameSquare = this._lastTapSquare === this._touchStartSquare;

    if (timeSinceLast < 350 && sameSquare) {
      this._tapCount++;
    } else {
      this._tapCount = 1;
    }

    this._lastTapTime = now;
    this._lastTapSquare = this._touchStartSquare;

    clearTimeout(this._tapTimer);

    const sq = this._touchStartSquare;

    this._tapTimer = setTimeout(() => {
      if (this._tapCount === 1) {
        this._handleSingleTap(sq);
      } else if (this._tapCount === 2) {
        this._handleDoubleTap(sq);
      } else if (this._tapCount >= 3) {
        this._handleTripleTap(sq);
      }
      this._tapCount = 0;
    }, 300);

    this._resetState();
  }

  _handleTouchCancel() {
    this._clearLongPress();
    this._overlay.clearPreview();
    this._resetState();
  }

  _handleSingleTap(square) {
    if (this._mode === 'highlight') {
      this._toggleHighlight(square);
    }
  }

  _handleDoubleTap(square) {
    // Remove highlight from square
    const removed = this._overlay.removeHighlight(square);
    if (removed) {
      this._pushUndo({ type: 'removeHighlight', square, color: this._annotations.highlights.get(square) });
      this._annotations.highlights.delete(square);
    }
  }

  _handleTripleTap(square) {
    // Remove nearest arrow from/to this square
    const coords = this._mapper.squareToCoords(square);
    if (!coords) return;

    const arrowEls = this._overlay.arrowLayer.querySelectorAll('.cam-arrow');
    let nearest = null;
    let nearestDist = Infinity;

    for (const el of arrowEls) {
      const from = el.getAttribute('data-from');
      const to = el.getAttribute('data-to');
      if (from === square || to === square) {
        nearest = el;
        break;
      }
      const fc = this._mapper.squareToCoords(from);
      const tc = this._mapper.squareToCoords(to);
      if (fc && tc) {
        const d = Math.min(
          Math.hypot(coords.x - fc.x, coords.y - fc.y),
          Math.hypot(coords.x - tc.x, coords.y - tc.y)
        );
        if (d < nearestDist) {
          nearestDist = d;
          nearest = el;
        }
      }
    }

    if (nearest) {
      const from = nearest.getAttribute('data-from');
      const to = nearest.getAttribute('data-to');
      this._overlay.removeArrowElement(nearest);
      this._annotations.arrows = this._annotations.arrows.filter(
        a => !(a.from === from && a.to === to)
      );
      this._pushUndo({ type: 'removeArrow', from, to, color: this._color });
    }
  }

  _updatePreview(clientX, clientY) {
    const fromCoords = this._mapper.squareToCoords(this._touchStartSquare);
    const rect = this._detector.rect;
    if (!fromCoords || !rect) return;

    const toX = clientX - rect.left;
    const toY = clientY - rect.top;

    const preview = this._arrows.createPreview(fromCoords, { x: toX, y: toY }, this._color, this._thickness);
    this._overlay.setPreview(preview);
  }

  _finalizeDrag(e) {
    const ct = e.changedTouches;
    if (!ct || ct.length === 0) return;

    const touch = ct[0];
    const endSquare = this._mapper.clientToSquare(touch.clientX, touch.clientY);

    if (!endSquare || endSquare === this._touchStartSquare) return;

    const type = this._mode === 'line' ? 'line' : this._arrowType;

    const arrow = this._arrows.createArrow(this._touchStartSquare, endSquare, {
      color: this._color,
      thickness: this._thickness,
      opacity: this._opacity,
      glow: this._glow,
      animate: this._animate,
      type
    });

    if (arrow) {
      this._overlay.addArrowElement(arrow);
      const entry = {
        from: this._touchStartSquare,
        to: endSquare,
        color: this._color,
        thickness: this._thickness,
        type
      };
      this._annotations.arrows.push(entry);
      this._pushUndo({ type: 'addArrow', from: entry.from, to: entry.to, color: entry.color, thickness: entry.thickness, arrowType: entry.type });
      this._redoStack = [];
    }
  }

  _toggleHighlight(square) {
    if (this._annotations.highlights.has(square)) {
      this._overlay.removeHighlight(square);
      this._pushUndo({ type: 'removeHighlight', square, color: this._annotations.highlights.get(square) });
      this._annotations.highlights.delete(square);
    } else {
      const bounds = this._mapper.squareBounds(square);
      if (!bounds) return;
      this._overlay.addHighlight(square, this._color, this._highlightOpacity, bounds);
      this._annotations.highlights.set(square, this._color);
      this._pushUndo({ type: 'addHighlight', square, color: this._color });
      this._redoStack = [];
    }
  }

  _pushUndo(action) {
    this._undoStack.push(action);
    if (this._undoStack.length > 100) this._undoStack.shift();
  }

  undo() {
    const action = this._undoStack.pop();
    if (!action) return;
    this._redoStack.push(action);

    switch (action.type) {
      case 'addArrow':
        this._removeArrowByData(action.from, action.to);
        break;
      case 'addHighlight':
        this._overlay.removeHighlight(action.square);
        this._annotations.highlights.delete(action.square);
        break;
      case 'removeArrow':
        this._restoreArrow(action);
        break;
      case 'removeHighlight':
        this._restoreHighlight(action);
        break;
    }
  }

  redo() {
    const action = this._redoStack.pop();
    if (!action) return;
    this._undoStack.push(action);

    switch (action.type) {
      case 'addArrow':
        this._restoreArrow(action);
        break;
      case 'addHighlight':
        this._restoreHighlight(action);
        break;
      case 'removeArrow':
        this._removeArrowByData(action.from, action.to);
        break;
      case 'removeHighlight':
        this._overlay.removeHighlight(action.square);
        this._annotations.highlights.delete(action.square);
        break;
    }
  }

  _removeArrowByData(from, to) {
    const els = this._overlay.arrowLayer.querySelectorAll('.cam-arrow');
    for (const el of els) {
      if (el.getAttribute('data-from') === from && el.getAttribute('data-to') === to) {
        this._overlay.removeArrowElement(el);
        break;
      }
    }
    this._annotations.arrows = this._annotations.arrows.filter(
      a => !(a.from === from && a.to === to)
    );
  }

  _restoreArrow(action) {
    const arrow = this._arrows.createArrow(action.from, action.to, {
      color: action.color || this._color,
      thickness: action.thickness || this._thickness,
      type: action.arrowType || 'normal'
    });
    if (arrow) {
      this._overlay.addArrowElement(arrow);
      this._annotations.arrows.push({
        from: action.from,
        to: action.to,
        color: action.color,
        thickness: action.thickness,
        type: action.arrowType || 'normal'
      });
    }
  }

  _restoreHighlight(action) {
    const bounds = this._mapper.squareBounds(action.square);
    if (!bounds) return;
    this._overlay.addHighlight(action.square, action.color, this._highlightOpacity, bounds);
    this._annotations.highlights.set(action.square, action.color);
  }

  clearAll() {
    this._overlay.clearAll();
    this._annotations = { arrows: [], highlights: new Map() };
    this._undoStack = [];
    this._redoStack = [];
  }

  _clearLongPress() {
    if (this._longPressTimer) {
      clearTimeout(this._longPressTimer);
      this._longPressTimer = null;
    }
  }

  _resetState() {
    this._touchStartPos = null;
    this._touchStartSquare = null;
    this._isLongPress = false;
    this._isDragging = false;
    this._twoFingerMode = false;
  }

  // Public setters
  set enabled(v) { this._enabled = v; }
  get enabled() { return this._enabled; }
  set mode(v) { this._mode = v; }
  get mode() { return this._mode; }
  set arrowType(v) { this._arrowType = v; }
  get arrowType() { return this._arrowType; }
  set color(v) { this._color = v; }
  get color() { return this._color; }
  set thickness(v) { this._thickness = v; }
  set opacity(v) { this._opacity = v; }
  set glow(v) { this._glow = v; }
  set highlightOpacity(v) { this._highlightOpacity = v; }
  set longPressDuration(v) { this._longPressDuration = v; }
  set gestureSensitivity(v) { this._gestureSensitivity = v; }
  get annotations() { return this._annotations; }

  exportAnnotations() {
    return {
      arrows: [...this._annotations.arrows],
      highlights: Object.fromEntries(this._annotations.highlights)
    };
  }

  destroy() {
    this._detachListeners();
    this._clearLongPress();
    clearTimeout(this._tapTimer);
  }
}

window.TouchHandler = TouchHandler;
