'use strict';

/**
 * Settings panel UI and persistence via chrome.storage.local.
 */
class SettingsManager {
  constructor(touchHandler, overlayEngine, toolbar) {
    this._touch = touchHandler;
    this._overlay = overlayEngine;
    this._toolbar = toolbar;
    this._panel = null;
    this._defaults = {
      defaultColor: '#22c55e',
      arrowThickness: 8,
      highlightOpacity: 0.35,
      gestureSensitivity: 12,
      longPressDuration: 400,
      toolbarPosition: { x: 20, y: 200 },
      enableAnimations: true,
      glowEnabled: true,
      snapToEdge: true,
      arrowOpacity: 0.85
    };
  }

  init() {
    this._toolbar.onSettingsOpen = () => this._showSettings();
    this._loadSettings();
  }

  _loadSettings() {
    if (typeof chrome !== 'undefined' && chrome.storage) {
      chrome.storage.local.get('settings', (data) => {
        this._applySettings(data.settings || this._defaults);
      });
    } else {
      const saved = localStorage.getItem('cam-settings');
      this._applySettings(saved ? JSON.parse(saved) : this._defaults);
    }
  }

  _saveSettings(settings) {
    if (typeof chrome !== 'undefined' && chrome.storage) {
      chrome.storage.local.set({ settings });
    } else {
      localStorage.setItem('cam-settings', JSON.stringify(settings));
    }
  }

  _applySettings(s) {
    this._touch.color = s.defaultColor || this._defaults.defaultColor;
    this._touch.thickness = s.arrowThickness || this._defaults.arrowThickness;
    this._touch.highlightOpacity = s.highlightOpacity ?? this._defaults.highlightOpacity;
    this._touch.gestureSensitivity = s.gestureSensitivity || this._defaults.gestureSensitivity;
    this._touch.longPressDuration = s.longPressDuration || this._defaults.longPressDuration;
    this._touch.glow = s.glowEnabled ?? this._defaults.glowEnabled;
    this._touch.opacity = s.arrowOpacity ?? this._defaults.arrowOpacity;

    if (s.toolbarPosition) {
      this._toolbar.setPosition(s.toolbarPosition.x, s.toolbarPosition.y);
    }
    this._toolbar.snapToEdge = s.snapToEdge ?? this._defaults.snapToEdge;
  }

  _getCurrentSettings() {
    return {
      defaultColor: this._touch.color,
      arrowThickness: this._touch._thickness,
      highlightOpacity: this._touch._highlightOpacity,
      gestureSensitivity: this._touch._gestureSensitivity,
      longPressDuration: this._touch._longPressDuration,
      toolbarPosition: this._toolbar._fabPos,
      enableAnimations: this._touch._animate,
      glowEnabled: this._touch._glow,
      snapToEdge: this._toolbar._snapToEdge,
      arrowOpacity: this._touch._opacity
    };
  }

  _showSettings() {
    if (this._panel) {
      this._panel.remove();
      this._panel = null;
      return;
    }

    this._panel = document.createElement('div');
    this._panel.id = 'cam-settings-panel';
    this._panel.style.cssText = `
      position: fixed;
      left: 0; top: 0; right: 0; bottom: 0;
      z-index: 10002;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(0, 0, 0, 0.6);
      backdrop-filter: blur(4px);
      -webkit-backdrop-filter: blur(4px);
    `;

    const s = this._getCurrentSettings();

    const card = document.createElement('div');
    card.style.cssText = `
      background: rgba(20, 20, 30, 0.95);
      backdrop-filter: blur(20px);
      -webkit-backdrop-filter: blur(20px);
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 20px;
      padding: 24px;
      width: 88vw;
      max-width: 340px;
      max-height: 80vh;
      overflow-y: auto;
      color: #e0e0e0;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
      font-size: 14px;
      box-shadow: 0 12px 40px rgba(0, 0, 0, 0.6);
    `;

    card.innerHTML = `
      <div style="text-align:center;margin-bottom:16px">
        <div style="font-size:18px;font-weight:700">⚙ Settings</div>
      </div>

      <div class="cam-setting-row">
        <label>Arrow Thickness</label>
        <input type="range" id="cam-s-thickness" min="2" max="20" value="${s.arrowThickness}" />
        <span id="cam-s-thickness-val">${s.arrowThickness}</span>
      </div>

      <div class="cam-setting-row">
        <label>Arrow Opacity</label>
        <input type="range" id="cam-s-opacity" min="0.1" max="1" step="0.05" value="${s.arrowOpacity}" />
        <span id="cam-s-opacity-val">${s.arrowOpacity}</span>
      </div>

      <div class="cam-setting-row">
        <label>Highlight Opacity</label>
        <input type="range" id="cam-s-hlopacity" min="0.1" max="0.8" step="0.05" value="${s.highlightOpacity}" />
        <span id="cam-s-hlopacity-val">${s.highlightOpacity}</span>
      </div>

      <div class="cam-setting-row">
        <label>Gesture Sensitivity</label>
        <input type="range" id="cam-s-sensitivity" min="4" max="30" value="${s.gestureSensitivity}" />
        <span id="cam-s-sensitivity-val">${s.gestureSensitivity}</span>
      </div>

      <div class="cam-setting-row">
        <label>Long Press (ms)</label>
        <input type="range" id="cam-s-longpress" min="200" max="800" step="50" value="${s.longPressDuration}" />
        <span id="cam-s-longpress-val">${s.longPressDuration}</span>
      </div>

      <div class="cam-setting-row">
        <label>
          <input type="checkbox" id="cam-s-animations" ${s.enableAnimations ? 'checked' : ''} />
          Enable Animations
        </label>
      </div>

      <div class="cam-setting-row">
        <label>
          <input type="checkbox" id="cam-s-glow" ${s.glowEnabled ? 'checked' : ''} />
          Glow Effect
        </label>
      </div>

      <div class="cam-setting-row">
        <label>
          <input type="checkbox" id="cam-s-snap" ${s.snapToEdge ? 'checked' : ''} />
          Snap FAB to Edge
        </label>
      </div>

      <div style="display:flex;gap:8px;margin-top:16px">
        <button class="cam-btn" id="cam-s-save" style="flex:1;padding:10px;font-weight:600">Save</button>
        <button class="cam-btn" id="cam-s-close" style="flex:1;padding:10px">Close</button>
      </div>
    `;

    this._panel.appendChild(card);
    document.body.appendChild(this._panel);

    // Slider value updates
    const bindSlider = (id) => {
      const el = card.querySelector('#' + id);
      const val = card.querySelector('#' + id + '-val');
      if (el && val) {
        el.addEventListener('input', () => { val.textContent = el.value; });
      }
    };
    bindSlider('cam-s-thickness');
    bindSlider('cam-s-opacity');
    bindSlider('cam-s-hlopacity');
    bindSlider('cam-s-sensitivity');
    bindSlider('cam-s-longpress');

    card.querySelector('#cam-s-save')?.addEventListener('click', () => {
      const newSettings = {
        defaultColor: this._touch.color,
        arrowThickness: parseInt(card.querySelector('#cam-s-thickness').value),
        arrowOpacity: parseFloat(card.querySelector('#cam-s-opacity').value),
        highlightOpacity: parseFloat(card.querySelector('#cam-s-hlopacity').value),
        gestureSensitivity: parseInt(card.querySelector('#cam-s-sensitivity').value),
        longPressDuration: parseInt(card.querySelector('#cam-s-longpress').value),
        toolbarPosition: this._toolbar._fabPos,
        enableAnimations: card.querySelector('#cam-s-animations').checked,
        glowEnabled: card.querySelector('#cam-s-glow').checked,
        snapToEdge: card.querySelector('#cam-s-snap').checked
      };

      this._saveSettings(newSettings);
      this._applySettings(newSettings);
      this._closeSettings();
    });

    card.querySelector('#cam-s-close')?.addEventListener('click', () => this._closeSettings());

    this._panel.addEventListener('touchstart', (e) => {
      if (e.target === this._panel) {
        e.preventDefault();
        this._closeSettings();
      }
    }, { passive: false });
  }

  _closeSettings() {
    if (this._panel) {
      this._panel.remove();
      this._panel = null;
    }
  }

  destroy() {
    this._closeSettings();
  }
}

window.SettingsManager = SettingsManager;
