'use strict';

/**
 * Creates SVG arrow elements with support for piece-based presets.
 */
class ArrowRenderer {
  constructor(squareMapper, overlayEngine) {
    this._mapper = squareMapper;
    this._overlay = overlayEngine;
  }

  /**
   * Create a straight arrow between two squares.
   */
  createArrow(fromSquare, toSquare, options = {}) {
    const {
      color = '#22c55e',
      thickness = 8,
      opacity = 0.85,
      glow = false,
      animate = true,
      type = 'normal'
    } = options;

    const from = this._mapper.squareToCoords(fromSquare);
    const to = this._mapper.squareToCoords(toSquare);
    if (!from || !to) return null;

    const group = document.createElementNS('http://www.w3.org/2000/svg', 'g');
    group.setAttribute('data-from', fromSquare);
    group.setAttribute('data-to', toSquare);
    group.setAttribute('data-type', type);
    group.classList.add('cam-arrow');

    let pathData;
    switch (type) {
      case 'knight':
        pathData = this._knightPath(from, to);
        break;
      case 'bishop':
        pathData = this._straightPath(from, to);
        break;
      case 'rook':
        pathData = this._rookPath(from, to);
        break;
      case 'queen':
        pathData = this._straightPath(from, to);
        break;
      case 'king':
        pathData = this._straightPath(from, to);
        break;
      case 'pawn':
        pathData = this._straightPath(from, to);
        break;
      case 'line':
        return this._createLine(from, to, color, thickness, opacity, glow, animate, group);
      default:
        pathData = this._straightPath(from, to);
    }

    const markerId = this._overlay.getOrCreateMarker(color, thickness);

    if (glow) {
      const glowPath = document.createElementNS('http://www.w3.org/2000/svg', 'path');
      glowPath.setAttribute('d', pathData);
      glowPath.setAttribute('stroke', color);
      glowPath.setAttribute('stroke-width', thickness + 6);
      glowPath.setAttribute('fill', 'none');
      glowPath.setAttribute('opacity', '0.25');
      glowPath.setAttribute('stroke-linecap', 'round');
      glowPath.setAttribute('stroke-linejoin', 'round');
      glowPath.setAttribute('filter', 'blur(4px)');
      group.appendChild(glowPath);
    }

    const path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
    path.setAttribute('d', pathData);
    path.setAttribute('stroke', color);
    path.setAttribute('stroke-width', thickness);
    path.setAttribute('fill', 'none');
    path.setAttribute('opacity', opacity);
    path.setAttribute('stroke-linecap', 'round');
    path.setAttribute('stroke-linejoin', 'round');
    path.setAttribute('marker-end', `url(#${markerId})`);

    if (animate) {
      const len = path.getTotalLength ? 500 : 500;
      path.style.strokeDasharray = len;
      path.style.strokeDashoffset = len;
      path.style.transition = 'stroke-dashoffset 0.3s ease-out';
      requestAnimationFrame(() => {
        path.style.strokeDashoffset = '0';
      });
    }

    group.appendChild(path);
    return group;
  }

  /**
   * Create a preview arrow (no animation, for live drawing).
   */
  createPreview(fromCoords, toCoords, color = '#22c55e', thickness = 8) {
    const markerId = this._overlay.getOrCreateMarker(color, thickness);

    const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
    line.setAttribute('x1', fromCoords.x);
    line.setAttribute('y1', fromCoords.y);
    line.setAttribute('x2', toCoords.x);
    line.setAttribute('y2', toCoords.y);
    line.setAttribute('stroke', color);
    line.setAttribute('stroke-width', thickness);
    line.setAttribute('opacity', '0.6');
    line.setAttribute('stroke-linecap', 'round');
    line.setAttribute('marker-end', `url(#${markerId})`);

    return line;
  }

  _straightPath(from, to) {
    return `M ${from.x} ${from.y} L ${to.x} ${to.y}`;
  }

  _knightPath(from, to) {
    const dx = to.x - from.x;
    const dy = to.y - from.y;
    const midX = from.x + dx;
    const midY = from.y;

    if (Math.abs(dx) > Math.abs(dy)) {
      return `M ${from.x} ${from.y} L ${to.x} ${from.y} L ${to.x} ${to.y}`;
    }
    return `M ${from.x} ${from.y} L ${from.x} ${to.y} L ${to.x} ${to.y}`;
  }

  _rookPath(from, to) {
    const dx = Math.abs(to.x - from.x);
    const dy = Math.abs(to.y - from.y);

    if (dx > dy) {
      return `M ${from.x} ${from.y} L ${to.x} ${from.y} L ${to.x} ${to.y}`;
    }
    return `M ${from.x} ${from.y} L ${from.x} ${to.y} L ${to.x} ${to.y}`;
  }

  _createLine(from, to, color, thickness, opacity, glow, animate, group) {
    if (glow) {
      const glowLine = document.createElementNS('http://www.w3.org/2000/svg', 'line');
      glowLine.setAttribute('x1', from.x);
      glowLine.setAttribute('y1', from.y);
      glowLine.setAttribute('x2', to.x);
      glowLine.setAttribute('y2', to.y);
      glowLine.setAttribute('stroke', color);
      glowLine.setAttribute('stroke-width', thickness + 6);
      glowLine.setAttribute('opacity', '0.25');
      glowLine.setAttribute('stroke-linecap', 'round');
      glowLine.setAttribute('filter', 'blur(4px)');
      group.appendChild(glowLine);
    }

    const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
    line.setAttribute('x1', from.x);
    line.setAttribute('y1', from.y);
    line.setAttribute('x2', to.x);
    line.setAttribute('y2', to.y);
    line.setAttribute('stroke', color);
    line.setAttribute('stroke-width', thickness);
    line.setAttribute('fill', 'none');
    line.setAttribute('opacity', opacity);
    line.setAttribute('stroke-linecap', 'round');

    if (animate) {
      line.style.opacity = '0';
      line.style.transition = 'opacity 0.2s ease-in';
      requestAnimationFrame(() => { line.style.opacity = opacity; });
    }

    group.appendChild(line);
    group.setAttribute('data-type', 'line');
    return group;
  }
}

window.ArrowRenderer = ArrowRenderer;
