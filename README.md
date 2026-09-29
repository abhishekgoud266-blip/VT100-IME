# VT-100 PC Touch Keyboard — Android IME v0.1

This is the first native Android IME build based on the hand-drawn layout:

- Target physical proportion: ~7 cm wide × 4.3–4.5 cm high
- PC-style function row
- Number/symbol row
- QWERTY rows
- Caps / Shift / Ctrl / Alt
- Tab / Esc / Enter
- Backspace / Delete
- Home / End / Insert
- Arrow navigation
- Central touchpad area
- Swipe on touchpad = cursor/navigation arrow
- Tap on touchpad = Space
- Haptic key feedback
- Real `InputConnection` input into Android apps

## Build without a PC

This repository includes `.github/workflows/build.yml`.

1. Create a GitHub repository.
2. Upload this project.
3. Open **Actions**.
4. Run **Build VT-100 IME APK**.
5. Download the `VT100-IME-debug` artifact.
6. Install the APK on the Android phone.
7. Open the app and enable the keyboard in Android keyboard settings.
8. Select **VT-100 PC Touch** as the current keyboard.

## Important

This is V0.1. It is a native IME, not a remote keyboard and not a WebView pretending to be a keyboard.

The next iterations should merge the exact interaction behavior from the original VT-100 Terminal Pro HTML/CSS/JS into the native IME while keeping this physical layout.

A normal IME cannot create a universal system mouse pointer. The central pad in V0.1 therefore provides cursor/navigation behavior. A separate Accessibility Service can be added later if broader pointer-style interaction is required.
