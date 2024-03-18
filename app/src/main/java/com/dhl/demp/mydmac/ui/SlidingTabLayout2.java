package com.dhl.demp.mydmac.ui;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Typeface;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

import com.dhl.demp.mydmac.utils.Constants;

import mydmac.R;

public class SlidingTabLayout2 extends SlidingTabLayout {
    private int tabsCount = 1;

    public SlidingTabLayout2(Context context) {
        super(context);
    }

    public SlidingTabLayout2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlidingTabLayout2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    /**
     * Create a default view to be used for tabs. This is called if a custom tab view is not set via
     * {@link #setCustomTabView(int, int)}.
     */
    protected TextView createDefaultTabView(Context context) {
        Typeface typeFace = Typeface.createFromAsset(context.getAssets(), Constants.MAIN_FONT_PATH);

        TextView textView = new TextView(context);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, TAB_VIEW_TEXT_SIZE_SP);
        textView.setTypeface(typeFace);
        textView.setAllCaps(true);
        textView.setTextColor(getResources().getColor(R.color.primary_text_default_material_light));

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        textView.setWidth(size.x / tabsCount);

        TypedValue outValue = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground,
                outValue, true);
        textView.setBackgroundResource(outValue.resourceId);

        int padding = (int) (TAB_VIEW_PADDING_DIPS * getResources().getDisplayMetrics().density);
        textView.setPadding(padding, padding, padding, padding);
        textView.setTextColor(getResources().getColor(R.color.dhltheme_color));

        return textView;
    }

    @Override
    public void setViewPager(ViewPager viewPager) {
        final PagerAdapter adapter = viewPager.getAdapter();
        tabsCount = adapter != null ? adapter.getCount() : 1;

        super.setViewPager(viewPager);
    }
}
