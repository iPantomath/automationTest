package com.dhl.demp.mydmac.utils;

import io.fabric.sdk.android.BuildConfig;

/**
 * Created by robielok on 7/12/2018.
 */

public interface Constants {
    String AUTO_REG_SCOPE = "dmac-auto";
    String DEEP_LINK_HOST_NAME = "apps";
    String PARAM_INSTALL = "install";
    String PARAM_UNINSTALL_REQUIRED = "uninstall_required";
    String PARAM_ACTION = "action";
    String PARAM_OPEN_APP = "open_app";

    interface AppLinkAction {
        String DETAIL = "detail";
        String INSTALL = "install";
        String OPEN = "open";
    }

    interface BuildType {
        String RELEASE = "release";
        String DEBUG = "debug";
        String UAT = "uat";
    }

    interface StandardAppFlag {
        int FLAG_PHONE = 1;
        int FLAG_SMS = 2;
        int FLAG_CAMERA = 4;
        int FLAG_MAP = 8;
        int FLAG_CHROME = 16;
        int FLAG_SCAN_TO_CONNECT = 32;
        int FLAG_STAGE_NOW = 64;
        int FLAG_MOBICONTROL = 128;
    }

    interface Uris {
        String TERMS_OF_SERVICE = "https://dmac.dhl.com/terms";
        String PRIVACY_NOTICE = "https://dmac.dhl.com/privacy";
        String TERMS_OF_SERVICE_TEST = "https://dmac-test.dhl.com/terms";
        String PRIVACY_NOTICE_TEST = "https://dmac-test.dhl.com/privacy";
        String TERMS_OF_SERVICE_UAT = "https://dmac-uat.dhl.com/terms";
        String PRIVACY_NOTICE_UAT = "https://dmac-uat.dhl.com/privacy";
        String DISCLAIMER = "https://dmac.dhl.com/disclaimer";

    }

    interface AppPackageName {
        String SCAN_TO_CONNECT = "com.zebra.scantoconnect";
        String STAGE_NOW = "com.symbol.tool.stagenow";
        String MOBICONTROL = "net.soti.mobicontrol.androidwork";
    }

    String NOTIFICATION_CHANNEL_IMPORTANT = "important";
    String NOTIFICATION_CHANNEL_MINOR = "minor";
    String NOTIFICATION_CHANNEL_FG_SERVICE = "fg_service";

    String MAIN_FONT_PATH = "fonts/Delivery_A_Rg.ttf";
    String MAIN_FONT_BOLD_PATH = "fonts/Delivery_A_Bd.ttf";
    String MAIN_FONT_LIGHT_PATH = "fonts/Delivery_A_Lt.ttf";

    String EXTRA_NOTIFICATION_TYPE = "notification_type";
    String EXTRA_ACTION = "action";
    String EXTRA_APP_ID = "app_id";

    interface NotificationType {
        String DMAC_ACTION = "DMAC_ACTION";
    }

    interface NotificationAction {
        String OPEN = "open";
        String UPDATE_APP = "update_app";
    }

    interface AppUseCase {
        String INTERNAL = "internal";
        String EXTERNAL = "external";
    }
}
