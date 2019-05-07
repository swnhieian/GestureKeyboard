package com.shiweinan.gesturekeyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import android.graphics.Paint;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

/**
 * TODO: document your custom view class.
 */
public class InputView extends View {
    Lexicon recognizer;
    MainActivity mainView;
    List<PointF> gesture;
    String[] phrase;

    public InputView(Context context) {
        super(context);
        init();
    }

    public InputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public InputView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        recognizer = new Lexicon(getContext());
        gesture = new ArrayList<>();
        phrase = "a problem with the machine".split(" ");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        System.out.println(event.toString());
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                gesture.clear();
                gesture.add(new PointF(event.getX(), event.getY()));
                break;
            case MotionEvent.ACTION_MOVE:
                gesture.add(new PointF(event.getX(), event.getY()));
                break;
            case MotionEvent.ACTION_UP:

                List<String> results = recognizer.recognize(gesture, mainView.word);
                mainView.showCandidates(results);
                gesture.clear();
                break;
            default:
                break;
        }
        return true;//super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        SharedPreferences sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getContext());
        int width = Integer.parseInt(sharedPreferences.getString("kbd_width", "200"));
        int height = Integer.parseInt(sharedPreferences.getString("kbd_height", "200"));
        int top = Integer.parseInt(sharedPreferences.getString("kbd_top", "1000"));
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int left = (dm.widthPixels - width) / 2;
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width, height);
        layoutParams.leftMargin = left;
        layoutParams.topMargin = top;
        setLayoutParams(layoutParams);
        super.onDraw(canvas);
    }

    public void setMainView(MainActivity main) {
        this.mainView = main;
    }
}
