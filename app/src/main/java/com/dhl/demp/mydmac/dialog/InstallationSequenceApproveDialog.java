package com.dhl.demp.mydmac.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.dhl.demp.mydmac.installer.AppInstallerService;
import com.dhl.demp.mydmac.obj.InstallationAppItem;

import mydmac.R;

/**
 * Created by robielok on 11/7/2017.
 */

public class InstallationSequenceApproveDialog extends DialogFragment {
    private static final String ARGS_APP = "app";
    private static final String ARGS_ITEMS = "items";

    private InstallationAppItem[] items;
    private InstallationAppItem app;

    public static InstallationSequenceApproveDialog newInstance(InstallationAppItem app, InstallationAppItem[] dependencies) {
        InstallationSequenceApproveDialog instance = new InstallationSequenceApproveDialog();

        Bundle args = new Bundle(2);
        args.putParcelable(ARGS_APP, app);
        args.putParcelableArray(ARGS_ITEMS, dependencies);
        instance.setArguments(args);

        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        items = (InstallationAppItem[]) getArguments().getParcelableArray(ARGS_ITEMS);
        app = getArguments().getParcelable(ARGS_APP);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View rootView = layoutInflater.inflate(R.layout.installation_sequence_approve_dialog, null, false);
        initView(layoutInflater, rootView);

        return new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.apps_to_install, app.appName))
                .setView(rootView)
                .setPositiveButton(R.string.market_action_install, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startInstallation();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
    }

    private void startInstallation() {
        AppInstallerService.installApp(getContext(), app.appId, app.appName, app.version, app.source, app.md5, items, false);
    }

    private void initView(LayoutInflater layoutInflater, View rootView) {
        LinearLayout container = (LinearLayout) rootView.findViewById(R.id.container);
        for (int i = 0; i < items.length; i++) {
            View viewItem = layoutInflater.inflate(R.layout.installation_sequence_dialog_item, container, false);
            ((TextView) viewItem.findViewById(R.id.app_name)).setText(items[i].appName);
            ((TextView) viewItem.findViewById(R.id.app_version)).setText(getString(R.string.version_template, items[i].version));

            container.addView(viewItem);
        }
    }
}
