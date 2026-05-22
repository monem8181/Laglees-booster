'use strict';

/**
 * Chess Arrow Master – Main entry point.
 * Initializes all modules when Chess.com board is detected.
 */
(function () {
  if (window.__camInitialized) return;
  window.__camInitialized = true;

  const boardDetector = new BoardDetector();
  const squareMapper = new SquareMapper(boardDetector);
  const overlayEngine = new OverlayEngine(boardDetector);
  const arrowRenderer = new ArrowRenderer(squareMapper, overlayEngine);
  const touchHandler = new TouchHandler(boardDetector, squareMapper, overlayEngine, arrowRenderer);
  const toolbar = new Toolbar(touchHandler, overlayEngine);
  const settings = new SettingsManager(touchHandler, overlayEngine, toolbar);

  boardDetector.on('boardFound', (data) => {
    overlayEngine.init();
    touchHandler.init();
    toolbar.init();
    settings.init();
    touchHandler.enabled = true;
  });

  let initAttempts = 0;
  const maxAttempts = 60;

  function tryInit() {
    boardDetector.init();

    if (!boardDetector.board && initAttempts < maxAttempts) {
      initAttempts++;
      setTimeout(tryInit, 1000);
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', tryInit);
  } else {
    tryInit();
  }
})();
