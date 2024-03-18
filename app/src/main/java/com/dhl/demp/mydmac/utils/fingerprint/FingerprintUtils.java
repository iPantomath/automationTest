package com.dhl.demp.mydmac.utils.fingerprint;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;

import com.dhl.demp.mydmac.obj.BiometricSensorState;

/**
 * Created by robielok on 9/20/2017.
 */

public class FingerprintUtils {
    public static BiometricSensorState getFingerprintSensorState(@NonNull Context context) {
        BiometricManager biometricManager = BiometricManager.from(context);
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_SUCCESS: return BiometricSensorState.READY;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE: return BiometricSensorState.NOT_SUPPORTED;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED: return BiometricSensorState.NO_BIOMETRIC_ENROLMENT;
        }

        return BiometricSensorState.NO_BIOMETRIC_ENROLMENT;
    }

    public static BiometricFeature getAvailableFeatures(@NonNull Context context) {
        PackageManager packageManager = context.getPackageManager();
        boolean hasFingerprint = packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT);
        boolean hasFaceId = packageManager.hasSystemFeature(PackageManager.FEATURE_FACE);
        boolean hasIris = packageManager.hasSystemFeature(PackageManager.FEATURE_IRIS);

        if (!hasFingerprint && !hasFaceId && !hasIris) {
            return BiometricFeature.UNKNOWN;
        }
        if (hasFingerprint && !hasFaceId && !hasIris) {
            return BiometricFeature.FINGERPRINT;
        }
        if (!hasFingerprint && hasFaceId && !hasIris) {
            return BiometricFeature.FACE_ID;
        }
        if (!hasFingerprint && !hasFaceId && hasIris) {
            return BiometricFeature.IRIS;
        }

        return BiometricFeature.MULTIPLE;
    }

    public enum BiometricFeature {
        UNKNOWN, FINGERPRINT, FACE_ID, IRIS, MULTIPLE
    }
}
