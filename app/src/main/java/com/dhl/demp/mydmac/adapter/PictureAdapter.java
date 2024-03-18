package com.dhl.demp.mydmac.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;


import com.dhl.demp.mydmac.fragment.PictureFragment;


/**
 * Created by rd on 04/06/16.
 */




public class PictureAdapter extends FragmentStatePagerAdapter {
    private List<String> pictures;
    public PictureAdapter(FragmentManager fm, List<String> pictures) {
        super(fm);
        this.pictures = new ArrayList<>(pictures);
    }

    @Override
    public Fragment getItem(int position) {
        return PictureFragment.newInstance(pictures.get(position));
    }

    @Override
    public int getCount() {
        return pictures.size();
    }
}
