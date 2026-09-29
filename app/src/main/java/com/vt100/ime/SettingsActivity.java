package com.vt100.ime;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 50, 40, 40);
        root.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = new TextView(this);
        title.setText("VT-100 PC Touch Keyboard");
        title.setTextSize(24);
        title.setPadding(0, 0, 0, 24);

        TextView info = new TextView(this);
        info.setText(
            "A compact PC-style Android IME based on your 7 cm × 4.3–4.5 cm design.\\n\\n" +
            "1. Enable it in Android keyboard settings.\\n" +
            "2. Select it as your current keyboard.\\n" +
            "3. Open a browser/text field and test the keys.\\n\\n" +
            "This first build uses the central pad for space + swipe cursor navigation."
        );
        info.setTextSize(16);
        info.setPadding(0, 0, 0, 30);

        Button enable = new Button(this);
        enable.setText("Open Keyboard Settings");
        enable.setOnClickListener(v ->
            startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        );

        Button choose = new Button(this);
        choose.setText("Choose Keyboard");
        choose.setOnClickListener(v ->
            ((android.view.inputmethod.InputMethodManager)
                getSystemService(INPUT_METHOD_SERVICE)).showInputMethodPicker()
        );

        root.addView(title);
        root.addView(info, new LinearLayout.LayoutParams(-1, 0, 1));
        root.addView(enable, new LinearLayout.LayoutParams(-1, -2));
        root.addView(choose, new LinearLayout.LayoutParams(-1, -2));

        setContentView(root);
    }
}
