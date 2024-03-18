package com.dhl.demp.mydmac.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dhl.demp.mydmac.LauncherPreference;

import java.io.File;
import java.util.ArrayList;

import mydmac.R;

public class AppsShortcutAdapter extends BaseAdapter{

    private ArrayList<String> appNameList = new ArrayList<String>();
    private ArrayList<String> appUrlList = new ArrayList<String>();
    private ArrayList<String> appIconList = new ArrayList<>();
    private Context context;

    private TextView appName;
    private TextView appUrl;
    private ImageView shortcutIcon;
    private LinearLayout editButton;
    private LinearLayout deleteButton;
    private LinearLayout editIcon;

//    String shortName;
//    String shortUrl;

    public AppsShortcutAdapter(Context context, ArrayList<String> appName, ArrayList<String> appUrl, ArrayList<String> appIcon) {
        this.appNameList = appName;
        this.appUrlList = appUrl;
        this.appIconList = appIcon;
        this.context = context;
    }

    @Override
    public int getCount() {
        return appNameList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        view = LayoutInflater.from(parent.getContext().getApplicationContext()).inflate(R.layout.item_shortcut_list, parent, false);
        appName = view.findViewById(R.id.list_app_name);
        appUrl = view.findViewById(R.id.list_app_url);
        shortcutIcon = view.findViewById(R.id.shortcut_icon);
        editButton = view.findViewById(R.id.edit_button);
        deleteButton = view.findViewById(R.id.delete_button);
        editIcon = view.findViewById(R.id.editIcon_button);

            appName.setText(appNameList.get(position));
            appUrl.setText(appUrlList.get(position));

            try {
                if (appIconList.get(position).equals("default")) {
                    shortcutIcon.setImageResource(R.drawable.shortcut_app_icon);
                } else {
                    Glide.with(view.getContext())
                            .load(new File(appIconList.get(position)))
                            .into(shortcutIcon);
                }
            }catch(Exception ex){
                shortcutIcon.setImageResource(R.drawable.shortcut_app_icon);
            }

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appNameList = LauncherPreference.getShortcutAppNameList(context);
                appNameList.remove(position);
                LauncherPreference.setAppNameList(context, appNameList);

                appUrlList = LauncherPreference.getShortcutAppURLList(context);
                appUrlList.remove(position);
                LauncherPreference.setAppURLList(context, appUrlList);

                appIconList = LauncherPreference.getIconPath(context);
                deleteIconExtStorage(appIconList.get(position));
                appIconList.remove(position);
                LauncherPreference.setIconPath(context, appIconList);
                if (LauncherPreference.getShortcutAppNameList(context).size()==0){
                    AdapterInterface adapterInterface = (AdapterInterface) context;
                    adapterInterface.listIsEmpty();
                }
                notifyDataSetChanged();

            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                appNameList = LauncherPreference.getShortcutAppNameList(context);
                appUrlList = LauncherPreference.getShortcutAppURLList(context);
                LauncherPreference.setEditApp(context, true);
                LauncherPreference.setIsChangeIcon(context, false);
                AdapterInterface adapterInterface = (AdapterInterface) context;
                adapterInterface.sendEditData(appNameList.get(position), appUrlList.get(position), position);

            }
        });

        editIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LauncherPreference.setIsChangeIcon(context, true);
                AdapterInterface adapterInterface = (AdapterInterface) context;
                adapterInterface.sendEditData(appNameList.get(position), appUrlList.get(position), position);
            }
        });

        return view;

    }

    public void adapterData(int position){
        AdapterInterface adapterInterface = (AdapterInterface) context;
        adapterInterface.sendEditData(appNameList.get(position), appUrlList.get(position), position);
    }

    private void deleteIconExtStorage(String imagePath){

        File iconToDelete = new File(imagePath);
        if (iconToDelete.exists()) {
            iconToDelete.delete();
        }
    }

    public interface AdapterInterface{
        void sendEditData(String editAppName, String editAppUrl,int position);

        void listIsEmpty();
    }
}
