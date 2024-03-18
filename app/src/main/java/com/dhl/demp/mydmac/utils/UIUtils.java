package com.dhl.demp.mydmac.utils;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Created by robielok on 10/23/2017.
 */

public class UIUtils {
    public static ObjectAnimator createViewRotateXAnimation(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotationX", 360f, 0f);
        animator.setInterpolator(new TimeInterpolator() {
            @Override
            public float getInterpolation(float input) {
                return input;
            }
        });
        animator.setDuration(1000);
        animator.setRepeatCount(ValueAnimator.INFINITE);

        return animator;
    }

    public static void shakeView(View view) {
        ObjectAnimator.ofFloat(view, "translationX", 0, 25, -25, 25, -25,15, -15, 6, -6, 0).start();
    }

    public static void hideKeyboard(Context context, EditText editText) {
        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void showKeyboard(Context context, EditText editText) {
        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(editText, 0);
    }
}

