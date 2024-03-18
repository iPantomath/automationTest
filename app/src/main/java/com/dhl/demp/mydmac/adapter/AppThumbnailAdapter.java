package com.dhl.demp.mydmac.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.dhl.demp.mydmac.LauncherPreference;
import com.dhl.demp.mydmac.activity.ActivityGallery;

import java.util.ArrayList;

import mydmac.R;

/**
 * Created by hofy on 7. 4. 2016.
 */
public class AppThumbnailAdapter  extends RecyclerView.Adapter<AppThumbnailAdapter.ViewHolder>{

        private final ArrayList<String> mValues;
        private Context mContext;
        public AppThumbnailAdapter(ArrayList<String> items,Context context) {
            mValues = items;
            mContext = context;

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.app_thumbnail_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);

            GlideUrl glideUrl = new GlideUrl( holder.mItem , new LazyHeaders.Builder()
                    .addHeader("Authorization", "Bearer " + LauncherPreference.getToken(mContext))
                    .build());

            Glide.with(mContext)
                    .load(glideUrl)
                    .error(R.drawable.homepage_logo)
                    .into(holder.thumb);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) {
                       position = 0;
                    }

                    ActivityGallery.start(mContext, mValues, position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final ImageView thumb;
            public String mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                thumb = (ImageView) view.findViewById(R.id.image_view_thumb);

            }

            @Override
            public String toString() {
                return super.toString() + " '" + mItem + "'";
            }
        }


}
