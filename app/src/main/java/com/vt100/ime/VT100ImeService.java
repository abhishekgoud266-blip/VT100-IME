package com.vt100.ime;

import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.inputmethod.InputConnection;

public class VT100ImeService extends InputMethodService {
    private VT100KeyboardView keyboardView;

    @Override
    public View onCreateInputView() {
        keyboardView = new VT100KeyboardView(this);
        return keyboardView;
    }

    @Override
    public void onStartInputView(android.view.inputmethod.EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        if (keyboardView != null) {
            keyboardView.resetTransientState();
        }
    }

    public InputConnection connection() {
        return getCurrentInputConnection();
    }
}
