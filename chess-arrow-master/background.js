'use strict';

chrome.runtime.onInstalled.addListener((details) => {
  if (details.reason === 'install') {
    chrome.storage.local.set({
      settings: {
        defaultColor: '#22c55e',
        arrowThickness: 8,
        highlightOpacity: 0.35,
        gestureSensitivity: 12,
        longPressDuration: 400,
        toolbarPosition: { x: 20, y: 200 },
        enableAnimations: true,
        glowEnabled: true,
        snapToEdge: true
      },
      annotations: {}
    });
  }
});

chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
  if (message.type === 'GET_SETTINGS') {
    chrome.storage.local.get('settings', (data) => {
      sendResponse(data.settings || {});
    });
    return true;
  }

  if (message.type === 'SAVE_SETTINGS') {
    chrome.storage.local.set({ settings: message.payload }, () => {
      sendResponse({ success: true });
    });
    return true;
  }

  if (message.type === 'SAVE_ANNOTATIONS') {
    chrome.storage.local.get('annotations', (data) => {
      const annotations = data.annotations || {};
      annotations[message.url] = message.payload;
      chrome.storage.local.set({ annotations }, () => {
        sendResponse({ success: true });
      });
    });
    return true;
  }

  if (message.type === 'LOAD_ANNOTATIONS') {
    chrome.storage.local.get('annotations', (data) => {
      const annotations = data.annotations || {};
      sendResponse(annotations[message.url] || null);
    });
    return true;
  }
});
