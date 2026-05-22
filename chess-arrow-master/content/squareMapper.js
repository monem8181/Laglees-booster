'use strict';

/**
 * Maps touch/pointer coordinates to chess squares (e.g. e4, g7).
 * Handles board orientation, resize, and flipping.
 */
class SquareMapper {
  constructor(boardDetector) {
    this._detector = boardDetector;
    this._files = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'];
    this._ranks = ['1', '2', '3', '4', '5', '6', '7', '8'];
  }

  /**
   * Convert page coordinates to a chess square name.
   * @param {number} pageX
   * @param {number} pageY
   * @returns {string|null} e.g. "e4" or null if outside board
   */
  coordsToSquare(pageX, pageY) {
    const rect = this._detector.rect;
    if (!rect) return null;

    const x = pageX - rect.left - window.scrollX;
    const y = pageY - rect.top - window.scrollY;

    if (x < 0 || y < 0 || x > rect.width || y > rect.height) return null;

    const sqW = rect.width / 8;
    const sqH = rect.height / 8;

    let col = Math.floor(x / sqW);
    let row = Math.floor(y / sqH);

    col = Math.max(0, Math.min(7, col));
    row = Math.max(0, Math.min(7, row));

    if (this._detector.flipped) {
      col = 7 - col;
      row = 7 - row;
    }

    const file = this._files[col];
    const rank = this._ranks[7 - row];

    return file + rank;
  }

  /**
   * Convert client coordinates (from touch/pointer events) to a chess square.
   */
  clientToSquare(clientX, clientY) {
    const rect = this._detector.rect;
    if (!rect) return null;

    const x = clientX - rect.left;
    const y = clientY - rect.top;

    if (x < 0 || y < 0 || x > rect.width || y > rect.height) return null;

    const sqW = rect.width / 8;
    const sqH = rect.height / 8;

    let col = Math.floor(x / sqW);
    let row = Math.floor(y / sqH);

    col = Math.max(0, Math.min(7, col));
    row = Math.max(0, Math.min(7, row));

    if (this._detector.flipped) {
      col = 7 - col;
      row = 7 - row;
    }

    const file = this._files[col];
    const rank = this._ranks[7 - row];

    return file + rank;
  }

  /**
   * Get the center coordinates (relative to the board) for a given square.
   * @param {string} square e.g. "e4"
   * @returns {{ x: number, y: number }|null}
   */
  squareToCoords(square) {
    if (!square || square.length !== 2) return null;
    const rect = this._detector.rect;
    if (!rect) return null;

    const fileIdx = this._files.indexOf(square[0]);
    const rankIdx = this._ranks.indexOf(square[1]);
    if (fileIdx === -1 || rankIdx === -1) return null;

    let col = fileIdx;
    let row = 7 - rankIdx;

    if (this._detector.flipped) {
      col = 7 - col;
      row = 7 - row;
    }

    const sqW = rect.width / 8;
    const sqH = rect.height / 8;

    return {
      x: col * sqW + sqW / 2,
      y: row * sqH + sqH / 2
    };
  }

  /**
   * Get the bounding box for a square relative to the board.
   */
  squareBounds(square) {
    if (!square || square.length !== 2) return null;
    const rect = this._detector.rect;
    if (!rect) return null;

    const fileIdx = this._files.indexOf(square[0]);
    const rankIdx = this._ranks.indexOf(square[1]);
    if (fileIdx === -1 || rankIdx === -1) return null;

    let col = fileIdx;
    let row = 7 - rankIdx;

    if (this._detector.flipped) {
      col = 7 - col;
      row = 7 - row;
    }

    const sqW = rect.width / 8;
    const sqH = rect.height / 8;

    return {
      x: col * sqW,
      y: row * sqH,
      width: sqW,
      height: sqH
    };
  }

  get squareSize() {
    const rect = this._detector.rect;
    if (!rect) return 0;
    return rect.width / 8;
  }
}

window.SquareMapper = SquareMapper;
