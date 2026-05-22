# Chess Arrow Master Mobile

A production-ready mobile browser extension for Chess.com that enables advanced touch-based arrow drawing and square highlighting on mobile devices — no mouse or keyboard required.

## Features

- **Touch-Based Arrows** – Long press + drag to draw arrows between squares
- **Square Highlighting** – Single tap to highlight squares with customizable colors
- **Piece Arrow Presets** – Knight (L-shaped), Bishop (diagonal), Rook (straight), Queen, King, Pawn movement patterns
- **Line Mode** – Draw straight lines without arrowheads
- **8 Colors** – Green, Blue, Red, Yellow, Purple, Orange, Cyan, White with opacity and glow controls
- **Floating Toolbar** – Draggable 🎯 button with glassmorphism menu, snaps to screen edges
- **Smart Board Detection** – Automatically finds Chess.com board in live games, puzzles, analysis, and game review
- **Orientation Support** – Works with flipped boards, portrait and landscape
- **Undo/Redo** – Full annotation history with undo and redo support
- **Settings** – Customizable arrow thickness, opacity, gesture sensitivity, long press duration, and more
- **Mobile-First** – Optimized for low-end Android phones, 60fps rendering, minimal RAM usage
- **Privacy** – No tracking, no analytics, no remote code, no ads

## Touch Gestures

| Gesture | Action |
|---------|--------|
| Single tap | Highlight square (in highlight mode) |
| Long press + drag | Draw arrow |
| Two-finger drag | Force annotation mode |
| Double tap | Remove highlight from square |
| Triple tap | Remove nearest arrow |

## Supported Browsers

| Browser | Status |
|---------|--------|
| Kiwi Browser | Supported |
| Lemur Browser | Supported |
| Quetta Browser | Supported |
| Edge Android | Optional |

## Installation

### From Source (Developer Mode)

1. Download or clone this repository
2. Open your Chromium-based mobile browser (Kiwi, Lemur, or Quetta)
3. Navigate to `chrome://extensions` (or the browser's extension page)
4. Enable **Developer mode**
5. Tap **Load unpacked** and select the `chess-arrow-master` folder
6. Navigate to [chess.com](https://www.chess.com) and start a game

### From ZIP

1. Download the latest release ZIP
2. Extract the contents
3. Follow steps 2–6 above with the extracted folder

## Usage

1. Open any Chess.com page with a board (live game, puzzle, analysis, etc.)
2. The 🎯 floating button appears on screen
3. Tap the button to open the toolbar
4. Choose a mode (Arrow, Line, or Highlight)
5. Select a color
6. Draw on the board using touch gestures
7. Use Undo/Clear to manage annotations
8. Open Settings to customize behavior

## Architecture

```
chess-arrow-master/
├── manifest.json          # Manifest V3 extension config
├── background.js          # Service worker for storage management
├── content/
│   ├── boardDetector.js   # Chess.com board detection with MutationObserver
│   ├── squareMapper.js    # Touch coordinate → chess square mapping
│   ├── overlayEngine.js   # SVG overlay rendering engine
│   ├── arrowRenderer.js   # Arrow/line SVG creation with piece presets
│   ├── touchHandler.js    # Touch gesture recognition system
│   ├── toolbar.js         # Floating toolbar UI
│   ├── settings.js        # Settings panel and persistence
│   └── main.js            # Entry point and initialization
├── styles/
│   ├── overlay.css        # SVG overlay and settings styles
│   └── toolbar.css        # Toolbar and menu styles
├── popup/
│   ├── popup.html         # Extension popup page
│   ├── popup.js           # Popup status logic
│   └── popup.css          # Popup styles
└── assets/icons/          # Extension icons (16, 48, 128px)
```

## Permissions

| Permission | Purpose |
|------------|---------|
| `storage` | Save user settings and annotations locally |
| `activeTab` | Access the current Chess.com tab |
| `host_permissions: chess.com` | Only activates on Chess.com pages |

## Performance

- SVG-based rendering for smooth 60fps animations
- `requestAnimationFrame` for optimized redraws
- Efficient `MutationObserver` and `ResizeObserver` for board tracking
- No memory leaks — all observers and listeners are properly cleaned up
- Lightweight touch gesture system with minimal overhead

## Security & Privacy

- No external network requests
- No analytics or tracking
- No remote code execution
- No advertisements
- No cryptocurrency mining
- All data stored locally via `chrome.storage.local`

## Important Note

This extension is **purely a visual annotation overlay tool**. It does NOT:
- Suggest or calculate moves
- Connect to any chess engine
- Analyze game positions
- Automate gameplay

It is designed to help players visually annotate and study positions using arrows and highlights.

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Floating button doesn't appear | Refresh the Chess.com page; ensure the extension is enabled |
| Arrows don't snap to squares | The board may still be loading; wait for it to fully render |
| Touch gestures conflict with piece movement | Disable the extension (tap 🎯 → toggle off) when playing moves |
| Board not detected after navigation | The extension auto-retries detection; try refreshing if it persists |
| Settings not saving | Ensure the extension has storage permission enabled |

## License

MIT
