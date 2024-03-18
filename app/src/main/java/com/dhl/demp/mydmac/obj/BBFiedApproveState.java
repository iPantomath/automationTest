package com.dhl.demp.mydmac.obj;

/**
 * Created by robielok on 10/25/2017.
 */

public interface BBFiedApproveState {
    String VISIBLE = "visible";//smartphone shows application icon and [Request] button is visible and enabled
    String REQUESTED = "requested";//smartphone shows application icon and [Request] button is visible but not enabled
    String APPROVED = "approved";//smartphone shows application icon and [Request] button is visible but not enabled
    String REG_REQUEST = "regRequest";//smartphone shows application icon and [Request] button is visible but not enabled
    String INSTALL_ALLOWED = "installAllowed";//application is ready to be installed or installed
}
