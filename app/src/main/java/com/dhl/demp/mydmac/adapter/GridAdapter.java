package com.dhl.demp.mydmac.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.dhl.demp.mydmac.LauncherPreference;

import java.io.File;
import java.util.List;

import mydmac.R;

/**
 * Created by rd on 11/10/16.
 */

public class GridAdapter extends BaseAdapter {
    private List<AppInfo> apps;
    private Context context;
    private int titleColor;

    public GridAdapter(Context context, int titleColor, List<AppInfo> list) {
        this.titleColor = titleColor;
        apps = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return apps.size();
    }

    @Override
    public Object getItem(int position) {
        return apps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final AppInfo app = apps.get(position);
        Holder holder;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        if (convertView == null) {
            convertView = inflater.inflate(R.layout.launcher_grid_item, parent, false);

            holder = new Holder();
            holder.title = (TextView) convertView.findViewById(R.id.launcher_item_title);
            holder.icon = (ImageView) convertView.findViewById(R.id.launcher_item_icon);
            holder.item = (LinearLayout) convertView.findViewById(R.id.launcher_item);

            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        app.bind(holder, titleColor);

        return convertView;
    }

    public void update(List<AppInfo> list) {
        apps = list;
        notifyDataSetChanged();
    }

    public void setTitleColor(int titleColor) {
        this.titleColor = titleColor;
    }

    private static class Holder {
        public ImageView icon;
        public LinearLayout item;
        public TextView title;
    }

    public static abstract class AppInfo {
        protected String packageName;
        protected String appName;
        protected boolean pwa;

        public AppInfo(String packageName, String appName) {
            this.packageName = packageName;
            this.appName = appName;
            this.pwa = false;
        }

        public AppInfo(String packageName, String appName, boolean pwa) {
            this.packageName = packageName;
            this.appName = appName;
            this.pwa = pwa;
        }

        public void bind(Holder holder, int titleColor) {
            holder.title.setTextColor(titleColor);
            holder.title.setText(appName);
            setIcon(holder);

            holder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                    if(!pwa){
                    Context context = v.getContext();
                    Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
                    if (intent != null) {
                        context.startActivity(intent);
                    }
                    }else{
                        Context context = v.getContext();
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setData(Uri.parse(packageName).normalizeScheme());
                        context.startActivity(intent);
                    }
                }catch (Exception ex) {
                        Log.i(this.toString(), ex.toString());
                    }
                    }
            });
        }

        protected abstract void setIcon(Holder holder);
    }

    public static class DMACAppInfo extends AppInfo {
        protected String marketplaceIcon;

        public DMACAppInfo(String packageName, String appName, String marketplaceIcon) {
            super(packageName, appName);
            this.marketplaceIcon = marketplaceIcon;
        }

        @Override
        protected void setIcon(Holder holder) {
            if (!TextUtils.isEmpty(marketplaceIcon)) {
                Context context = holder.icon.getContext();
                GlideUrl glideUrl = new GlideUrl(marketplaceIcon, new LazyHeaders.Builder()
                        .addHeader("Authorization", "Bearer " + LauncherPreference.getToken(context))
                        .build());

                Glide
                        .with(context)
                        .load(glideUrl)
                        .error(R.drawable.apps_list_default_icon)
                        .into(holder.icon);
            } else {
                holder.icon.setImageResource(R.drawable.apps_list_default_icon);
            }
        }
    }

    public static class SystemAppInfo extends AppInfo {
        public SystemAppInfo(String packageName, String appName) {
            super(packageName, appName);
        }

        @Override
        protected void setIcon(Holder holder) {
            Context context = holder.icon.getContext();
            PackageManager pm = context.getPackageManager();
            try {
                Drawable applicationIcon = pm.getApplicationIcon(packageName);
                holder.icon.setImageDrawable(applicationIcon);
            } catch (PackageManager.NameNotFoundException e) {
                holder.icon.setImageResource(R.drawable.apps_list_default_icon);
            }
        }
    }

    public static class PwaAppInfo extends AppInfo {
        protected String iconUrl;
        public PwaAppInfo(String packageName, String appName,String iconUrl) {
            super(packageName, appName, true);
            this.iconUrl = iconUrl;
        }

        @Override
        protected void setIcon(Holder holder) {
            Context context = holder.icon.getContext();
            try {
                if(iconUrl.equals("default")) {
                    holder.icon.setImageResource(R.drawable.shortcut_app_icon);
                }else {
                    //Todo: fetch iconURL and transform it to Drawable
                    Glide.with(context)
                            .load(new File(iconUrl))
                            .into(holder.icon);
                }
            } catch (Exception e) {
                holder.icon.setImageResource(R.drawable.apps_list_default_icon);
            }
        }
    }
}
