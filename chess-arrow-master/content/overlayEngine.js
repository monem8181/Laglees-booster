'use strict';

/**
 * SVG overlay engine that sits above the chess board.
 * Manages layers for highlights and arrows with touch passthrough.
 */
class OverlayEngine {
  constructor(boardDetector) {
    this._detector = boardDetector;
    this._svg = null;
    this._highlightLayer = null;
    this._arrowLayer = null;
    this._previewLayer = null;
    this._visible = true;
    this._raf = null;
  }

  init() {
    this._createSVG();
    this._detector.on('boardFound', () => this._reposition());
    this._detector.on('boardResized', () => this._reposition());
    this._startLoop();
  }

  _createSVG() {
    if (this._svg) this._svg.remove();

    this._svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
    this._svg.id = 'cam-overlay';
    this._svg.setAttribute('xmlns', 'http://www.w3.org/2000/svg');
    this._svg.style.cssText = `
      position: absolute;
      top: 0; left: 0;
      width: 100%; height: 100%;
      pointer-events: none;
      z-index: 1000;
      overflow: visible;
    `;

    // Marker definitions for arrowheads
    const defs = document.createElementNS('http://www.w3.org/2000/svg', 'defs');
    this._svg.appendChild(defs);

    // Highlight layer (below arrows)
    this._highlightLayer = document.createElementNS('http://www.w3.org/2000/svg', 'g');
    this._highlightLayer.id = 'cam-highlights';
    this._svg.appendChild(this._highlightLayer);

    // Arrow layer
    this._arrowLayer = document.createElementNS('http://www.w3.org/2000/svg', 'g');
    this._arrowLayer.id = 'cam-arrows';
    this._svg.appendChild(this._arrowLayer);

    // Preview layer (for in-progress drawings)
    this._previewLayer = document.createElementNS('http://www.w3.org/2000/svg', 'g');
    this._previewLayer.id = 'cam-preview';
    this._svg.appendChild(this._previewLayer);

    this._mountSVG();
  }

  _mountSVG() {
    const board = this._detector.board;
    if (!board) return;

    let container = board.parentElement;
    if (container) {
      const pos = getComputedStyle(container).position;
      if (pos === 'static') {
        container.style.position = 'relative';
      }
    } else {
      container = board;
    }

    container.appendChild(this._svg);
    this._reposition();
  }

  _reposition() {
    if (!this._svg || !this._detector.board) return;

    const board = this._detector.board;
    const parent = this._svg.parentElement;
    if (!parent) {
      this._mountSVG();
      return;
    }

    const boardRect = board.getBoundingClientRect();
    const parentRect = parent.getBoundingClientRect();

    const left = boardRect.left - parentRect.left;
    const top = boardRect.top - parentRect.top;

    this._svg.style.left = left + 'px';
    this._svg.style.top = top + 'px';
    this._svg.style.width = boardRect.width + 'px';
    this._svg.style.height = boardRect.height + 'px';
    this._svg.setAttribute('viewBox', `0 0 ${boardRect.width} ${boardRect.height}`);
  }

  _startLoop() {
    const tick = () => {
      if (!document.contains(this._svg)) {
        this._mountSVG();
      }
      this._raf = requestAnimationFrame(tick);
    };
    this._raf = requestAnimationFrame(tick);
  }

  getOrCreateMarker(color, thickness) {
    const id = 'cam-arrow-' + color.replace('#', '') + '-' + thickness;
    const defs = this._svg.querySelector('defs');
    let marker = defs.querySelector('#' + id);

    if (!marker) {
      marker = document.createElementNS('http://www.w3.org/2000/svg', 'marker');
      marker.id = id;
      marker.setAttribute('viewBox', '0 0 10 10');
      marker.setAttribute('refX', '8');
      marker.setAttribute('refY', '5');
      marker.setAttribute('markerWidth', '4');
      marker.setAttribute('markerHeight', '4');
      marker.setAttribute('orient', 'auto-start-reverse');

      const path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
      path.setAttribute('d', 'M 0 0 L 10 5 L 0 10 z');
      path.setAttribute('fill', color);
      marker.appendChild(path);
      defs.appendChild(marker);
    }

    return id;
  }

  addHighlight(square, color, opacity, bounds) {
    const rect = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
    rect.setAttribute('x', bounds.x);
    rect.setAttribute('y', bounds.y);
    rect.setAttribute('width', bounds.width);
    rect.setAttribute('height', bounds.height);
    rect.setAttribute('fill', color);
    rect.setAttribute('opacity', opacity);
    rect.setAttribute('data-square', square);
    rect.classList.add('cam-highlight');

    this._highlightLayer.appendChild(rect);
    return rect;
  }

  removeHighlight(square) {
    const el = this._highlightLayer.querySelector(`[data-square="${square}"]`);
    if (el) el.remove();
    return !!el;
  }

  addArrowElement(element) {
    this._arrowLayer.appendChild(element);
  }

  removeArrowElement(element) {
    if (element && element.parentNode === this._arrowLayer) {
      this._arrowLayer.removeChild(element);
    }
  }

  setPreview(element) {
    this.clearPreview();
    if (element) this._previewLayer.appendChild(element);
  }

  clearPreview() {
    while (this._previewLayer.firstChild) {
      this._previewLayer.removeChild(this._previewLayer.firstChild);
    }
  }

  clearHighlights() {
    while (this._highlightLayer.firstChild) {
      this._highlightLayer.removeChild(this._highlightLayer.firstChild);
    }
  }

  clearArrows() {
    while (this._arrowLayer.firstChild) {
      this._arrowLayer.removeChild(this._arrowLayer.firstChild);
    }
  }

  clearAll() {
    this.clearHighlights();
    this.clearArrows();
    this.clearPreview();
  }

  setVisible(visible) {
    this._visible = visible;
    if (this._svg) {
      this._svg.style.display = visible ? '' : 'none';
    }
  }

  get visible() { return this._visible; }
  get svg() { return this._svg; }
  get highlightLayer() { return this._highlightLayer; }
  get arrowLayer() { return this._arrowLayer; }

  destroy() {
    if (this._raf) cancelAnimationFrame(this._raf);
    if (this._svg) this._svg.remove();
  }
}

window.OverlayEngine = OverlayEngine;
