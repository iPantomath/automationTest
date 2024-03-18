package com.dhl.demp.mydmac.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import com.dhl.demp.mydmac.adapter.PictureAdapter;

import java.util.ArrayList;
import java.util.List;

import mydmac.R;

/**
 * Created by rd on 04/06/16.
 */
public class ActivityGallery extends FragmentActivity {

    private static final String EXTRA_URLS = "urls";
    private static final String EXTRA_POSITION = "position";

    public static void start(Context context, ArrayList<String> pictureUrls, int position) {
        Intent imgDetail = new Intent(context, ActivityGallery.class);

        imgDetail.putStringArrayListExtra(EXTRA_URLS , pictureUrls);
        imgDetail.putExtra(EXTRA_POSITION , position);

        context.startActivity(imgDetail);
    }

    private ViewPager viewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        List<String> pictureUrls = getIntent().getStringArrayListExtra(EXTRA_URLS);
        int position = getIntent().getIntExtra(EXTRA_POSITION, 0);

        viewPager = (ViewPager)findViewById(R.id.pager);
        viewPager.setAdapter(new PictureAdapter(getSupportFragmentManager(), pictureUrls));
        viewPager.setCurrentItem(position);
    }
}
