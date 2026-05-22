'use strict';

document.addEventListener('DOMContentLoaded', () => {
  const statusDot = document.getElementById('status-dot');
  const statusText = document.getElementById('status-text');

  chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
    const tab = tabs[0];
    if (tab && tab.url && tab.url.includes('chess.com')) {
      statusDot.classList.add('active');
      statusText.textContent = 'Active on Chess.com';
    } else {
      statusDot.classList.add('inactive');
      statusText.textContent = 'Open Chess.com to use';
    }
  });
});
