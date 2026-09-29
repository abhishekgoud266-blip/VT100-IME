package com.vt100.ime;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.view.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VT100KeyboardView extends View {
    private final VT100ImeService service;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Key> keys = new ArrayList<>();

    private boolean shift, ctrl, alt, caps;
    private float downX, downY;
    private long downTime;
    private boolean touchpadGesture;

    private final float cmPx;
    private final float targetW;
    private final float targetH;

    private static class Key {
        RectF r;
        String normal;
        String shifted;
        int type;
        Key(RectF r, String normal, String shifted, int type) {
            this.r = r; this.normal = normal; this.shifted = shifted; this.type = type;
        }
    }

    private static final int TEXT = 0;
    private static final int SPECIAL = 1;
    private static final int MODIFIER = 2;
    private static final int TOUCHPAD = 3;

    public VT100KeyboardView(VT100ImeService service) {
        super(service);
        this.service = service;
        setFocusable(true);
        cmPx = getResources().getDisplayMetrics().xdpi / 2.54f;
        targetW = cmPx * 7.0f;
        targetH = cmPx * 4.4f;
        paint.setTypeface(android.graphics.Typeface.create("sans", android.graphics.Typeface.NORMAL));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxW = MeasureSpec.getSize(widthMeasureSpec);
        int w = Math.min(maxW, Math.round(targetW));
        int h = Math.round(targetH);
        setMeasuredDimension(w, h);
    }

    private float sx() { return getWidth() / 700f; }
    private float sy() { return getHeight() / 440f; }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        c.save();
        c.scale(sx(), sy());

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(android.graphics.Color.rgb(22,22,30));
        c.drawRect(0,0,700,440,paint);

        keys.clear();
        buildKeys();

        for (Key k : keys) {
            boolean active = (k.type == MODIFIER && isActive(k.normal));
            paint.setColor(active ? android.graphics.Color.rgb(122,162,247)
                    : android.graphics.Color.rgb(36,36,48));
            paint.setStyle(Paint.Style.FILL);
            c.drawRoundRect(k.r, 4, 4, paint);

            paint.setColor(android.graphics.Color.rgb(86,91,110));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.2f);
            c.drawRoundRect(k.r, 4, 4, paint);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(active ? android.graphics.Color.BLACK : android.graphics.Color.rgb(205,214,244));
            String label = k.normal;
            if (k.type == TEXT && shift && k.shifted != null && !k.shifted.isEmpty()) {
                label = k.shifted;
            }
            if (k.type == TOUCHPAD) label = "TOUCHPAD";
            float fs = label.length() > 5 ? 10 : 13;
            paint.setTextSize(fs);
            paint.setTextAlign(Paint.Align.CENTER);
            Paint.FontMetrics fm = paint.getFontMetrics();
            float ty = k.r.centerY() - (fm.ascent + fm.descent)/2;
            c.drawText(label, k.r.centerX(), ty, paint);
        }

        // Tiny status strip
        paint.setColor(android.graphics.Color.rgb(122,162,247));
        paint.setTextSize(8);
        paint.setTextAlign(Paint.Align.LEFT);
        c.drawText("7cm PC-TOUCH  " + (caps ? "CAPS " : "") + (shift ? "SHIFT " : "") +
                (ctrl ? "CTRL " : "") + (alt ? "ALT" : ""), 8, 435, paint);

        c.restore();
    }

    private boolean isActive(String s) {
        return (s.equals("Shift") && shift) || (s.equals("Ctrl") && ctrl) ||
               (s.equals("Alt") && alt) || (s.equals("Caps") && caps);
    }

    private void add(float x, float y, float w, float h, String n, String sh, int type) {
        keys.add(new Key(new RectF(x,y,x+w,y+h),n,sh,type));
    }

    private void buildKeys() {
        // Coordinates are a compact PC layout scaled into 700 × 440.
        float gap = 4;

        // Function row
        add(4, 4, 38, 34, "Esc", "", SPECIAL);
        for (int i=0;i<12;i++) add(46+i*40,4,36,34,"F"+(i+1),"",SPECIAL);
        add(530,4,52,34,"PrtSc","",SPECIAL);
        add(586,4,52,34,"Pause","",SPECIAL);
        add(642,4,54,34,"Break","",SPECIAL);

        // Number row
        String[] nums={"`","1","2","3","4","5","6","7","8","9","0","-","="};
        String[] sh={"~","!","@","#","$","%","^","&","*","(",")","_","+"};
        float x=4;
        add(x,42,44,38,"`","~",TEXT); x+=48;
        for(int i=1;i<13;i++){ add(x,42,42,38,nums[i],sh[i],TEXT); x+=46; }
        add(602,42,94,38,"Backspace","",SPECIAL);

        // Q row
        add(4,80,52,38,"Tab","",SPECIAL);
        String[] q={"Q","W","E","R","T","Y","U","I","O","P","[","]","\\"};
        String[] qs={"","", "", "", "", "", "", "", "", "", "{","}","|"};
        x=60;
        for(int i=0;i<13;i++){ add(x,80,42,38,q[i],qs[i],TEXT); x+=46; }

        // Home row
        add(4,118,64,38,"Caps","",MODIFIER);
        String[] a={"A","S","D","F","G","H","J","K","L",";","'"};
        String[] as={"","","","","","","","","",":","\""};
        x=72;
        for(int i=0;i<a.length;i++){ add(x,118,42,38,a[i],as[i],TEXT); x+=46; }
        add(584,118,112,38,"Enter","",SPECIAL);

        // Bottom letter row
        add(4,156,80,38,"Shift","",MODIFIER);
        String[] z={"Z","X","C","V","B","N","M",",",".","/"};
        String[] zs={"","","","","","","","<",">","?"};
        x=88;
        for(int i=0;i<z.length;i++){ add(x,156,42,38,z[i],zs[i],TEXT); x+=46; }
        add(554,156,68,38,"Shift","",MODIFIER);
        add(626,156,70,38,"Delete","",SPECIAL);

        // Lower control/navigation zone matching the sketch.
        add(4,198,48,34,"Ctrl","",MODIFIER);
        add(56,198,48,34,"Alt","",MODIFIER);

        add(108,198,260,70,"TOUCHPAD","",TOUCHPAD);

        add(372,198,58,34,"Home","",SPECIAL);
        add(434,198,58,34,"End","",SPECIAL);
        add(496,198,58,34,"Insert","",SPECIAL);
        add(558,198,64,34,"Delete","",SPECIAL);
        add(626,198,70,34,"Enter","",SPECIAL);

        add(372,236,58,34,"↑","",SPECIAL);
        add(434,236,58,34,"←","",SPECIAL);
        add(496,236,58,34,"↓","",SPECIAL);
        add(558,236,64,34,"→","",SPECIAL);
        add(626,236,70,34,"Space","",SPECIAL);

        add(4,274,48,34,"Ctrl","",MODIFIER);
        add(56,274,48,34,"Alt","",MODIFIER);
        add(108,274,260,70,"Space / Pad","",TOUCHPAD);
        add(372,274,58,34,"PgUp","",SPECIAL);
        add(434,274,58,34,"PgDn","",SPECIAL);
        add(496,274,58,34,"Tab","",SPECIAL);
        add(558,274,64,34,"Esc","",SPECIAL);
        add(626,274,70,34,"Space","",SPECIAL);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX()/sx();
        float y = e.getY()/sy();

        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            downX=x; downY=y; downTime=SystemClock.uptimeMillis();
            touchpadGesture = findKey(x,y,TOUCHPAD) != null;
            performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            return true;
        }

        if (e.getAction() == MotionEvent.ACTION_UP) {
            Key k=findKey(x,y,-1);
            float dx=x-downX, dy=y-downY;
            if (touchpadGesture && (Math.abs(dx)>24 || Math.abs(dy)>24)) {
                if (Math.abs(dx)>Math.abs(dy)) sendSpecial(dx>0 ? KeyEvent.KEYCODE_DPAD_RIGHT : KeyEvent.KEYCODE_DPAD_LEFT);
                else sendSpecial(dy>0 ? KeyEvent.KEYCODE_DPAD_DOWN : KeyEvent.KEYCODE_DPAD_UP);
                return true;
            }
            if (k != null) press(k);
            return true;
        }
        return true;
    }

    private Key findKey(float x,float y,int wantedType) {
        for(Key k:keys) if(k.r.contains(x,y) && (wantedType==-1 || k.type==wantedType)) return k;
        return null;
    }

    private void press(Key k) {
        if (k.type == TOUCHPAD) {
            commit(" ");
            return;
        }

        String n=k.normal;

        if (n.equals("Shift")) { shift=!shift; invalidate(); return; }
        if (n.equals("Ctrl")) { ctrl=!ctrl; invalidate(); return; }
        if (n.equals("Alt")) { alt=!alt; invalidate(); return; }
        if (n.equals("Caps")) { caps=!caps; invalidate(); return; }

        if (k.type == TEXT) {
            if (ctrl || alt) {
                int code=letterKeyCode(n);
                if (code != -1) {
                    int meta=0;
                    if(ctrl) meta |= KeyEvent.META_CTRL_ON;
                    if(alt) meta |= KeyEvent.META_ALT_ON;
                    if(shift) meta |= KeyEvent.META_SHIFT_ON;
                    sendKey(code, meta);
                }
                ctrl=false; alt=false; shift=false;
            } else {
                String out = shift ? k.shifted : k.normal;
                if (out == null || out.isEmpty()) out=k.normal;
                if (caps && out.length()==1 && Character.isLetter(out.charAt(0)))
                    out = out.equals(out.toUpperCase(Locale.US))
                            ? out.toLowerCase(Locale.US)
                            : out.toUpperCase(Locale.US);
                commit(out);
                shift=false;
            }
            invalidate();
            return;
        }

        int code=specialCode(n);
        if (code != -1) sendSpecial(code);
        if (!n.equals("Caps")) { shift=false; }
        invalidate();
    }

    private void commit(String s) {
        InputConnection ic=service.connection();
        if(ic!=null) ic.commitText(s,1);
    }

    private void sendSpecial(int code) {
        sendKey(code,0);
    }

    private void sendKey(int code,int meta) {
        InputConnection ic=service.connection();
        if(ic==null) return;
        long now=SystemClock.uptimeMillis();
        ic.sendKeyEvent(new KeyEvent(now,now,KeyEvent.ACTION_DOWN,code,0,meta));
        ic.sendKeyEvent(new KeyEvent(now,now,KeyEvent.ACTION_UP,code,0,meta));
    }

    private int letterKeyCode(String s) {
        if(s.length()!=1) return -1;
        char c=Character.toUpperCase(s.charAt(0));
        if(c>='A'&&c<='Z') return KeyEvent.KEYCODE_A+(c-'A');
        return -1;
    }

    private int specialCode(String s) {
        switch(s) {
            case "Esc": return KeyEvent.KEYCODE_ESCAPE;
            case "Tab": return KeyEvent.KEYCODE_TAB;
            case "Enter": return KeyEvent.KEYCODE_ENTER;
            case "Backspace": return KeyEvent.KEYCODE_DEL;
            case "Delete": return KeyEvent.KEYCODE_FORWARD_DEL;
            case "Home": return KeyEvent.KEYCODE_MOVE_HOME;
            case "End": return KeyEvent.KEYCODE_MOVE_END;
            case "↑": return KeyEvent.KEYCODE_DPAD_UP;
            case "↓": return KeyEvent.KEYCODE_DPAD_DOWN;
            case "←": return KeyEvent.KEYCODE_DPAD_LEFT;
            case "→": return KeyEvent.KEYCODE_DPAD_RIGHT;
            case "Space": return KeyEvent.KEYCODE_SPACE;
            case "PgUp": return KeyEvent.KEYCODE_PAGE_UP;
            case "PgDn": return KeyEvent.KEYCODE_PAGE_DOWN;
            case "Insert": return KeyEvent.KEYCODE_INSERT;
            case "F1": return KeyEvent.KEYCODE_F1;
            case "F2": return KeyEvent.KEYCODE_F2;
            case "F3": return KeyEvent.KEYCODE_F3;
            case "F4": return KeyEvent.KEYCODE_F4;
            case "F5": return KeyEvent.KEYCODE_F5;
            case "F6": return KeyEvent.KEYCODE_F6;
            case "F7": return KeyEvent.KEYCODE_F7;
            case "F8": return KeyEvent.KEYCODE_F8;
            case "F9": return KeyEvent.KEYCODE_F9;
            case "F10": return KeyEvent.KEYCODE_F10;
            case "F11": return KeyEvent.KEYCODE_F11;
            case "F12": return KeyEvent.KEYCODE_F12;
            default: return -1;
        }
    }

    public void resetTransientState() {
        shift=false; ctrl=false; alt=false;
        invalidate();
    }
}
