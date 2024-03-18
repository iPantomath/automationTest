package com.dhl.demp.mydmac.fragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.dhl.demp.mydmac.LauncherPreference;

import mydmac.R;

/**
 *
 * Created by rd on 04/06/16.
 */
public class PictureFragment extends Fragment {
    public PictureFragment(){

    }

    public static PictureFragment newInstance(String url) {
        PictureFragment fragment = new PictureFragment();
        Bundle bundle = new Bundle();

        bundle.putString("URL", url);
        fragment.setArguments(bundle);
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.pager_picture, container, false);

        ImageView image = (ImageView)rootView.findViewById(R.id.picturepager);

        GlideUrl glideUrl = new GlideUrl( getArguments().getString("URL") , new LazyHeaders.Builder()
                .addHeader("Authorization", "Bearer " + LauncherPreference.getToken(getContext()))
                .build());

        Glide.with(getContext())
                .load(glideUrl)
                .into(image);
        return rootView;
    }
}
