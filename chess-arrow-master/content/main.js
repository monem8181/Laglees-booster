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

  let modulesInitialized = false;

  boardDetector.on('boardFound', () => {
    if (!modulesInitialized) {
      modulesInitialized = true;
      overlayEngine.init();
      touchHandler.init();
      toolbar.init();
      settings.init();
      touchHandler.enabled = true;
    }
  });

  let retryCount = 0;
  const maxRetries = 60;

  function tryInit() {
    boardDetector.init();

    if (!boardDetector.board && retryCount < maxRetries) {
      retryCount++;
      setTimeout(tryInit, 1000);
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', tryInit);
  } else {
    tryInit();
  }
})();
