package com.github.mjaremczuk.verificationtask;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import java.lang.ref.WeakReference;

public class PermissionManager {

    private static WeakReference<PermissionRequestCallback> callbackWeakReference;
    public static final int REQUEST_PERMISSION_CODE = 10930;

    public static void askPermissions(@NonNull Activity activity, PermissionRequestCallback callback, @NonNull String... permissions) {
        callbackWeakReference = new WeakReference<>(callback);
        for (String permission : permissions) {
            askPermission(activity, permission);
        }
    }

    public static void askPermission(@NonNull Activity activity, @NonNull String permission, PermissionRequestCallback callback) {
        callbackWeakReference = new WeakReference<>(callback);
        askPermission(activity, permission);
    }

    public static void onPermissionRequestResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionRequestCallback callback = callbackWeakReference.get();
        if (callback == null) {
            return;
        }
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    callback.onPermissionGranted(permissions[i]);
                } else {
                    callback.onPermissionDenied(permissions[i]);
                }
            }
        }
    }

    private static void askPermission(@NonNull Activity activity, @NonNull String permission) {
        int granted = ContextCompat.checkSelfPermission(activity, permission);
        if (granted != PackageManager.PERMISSION_GRANTED) {
            boolean shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
            if (shouldShowRationale) {
                askShowRationale(activity, permission);
            } else {
                requestPermission(activity, permission);
            }
        } else {
            PermissionRequestCallback callback = callbackWeakReference.get();
            if(callback != null){
                callback.onPermissionGranted(permission);
            }
        }
    }

    protected static void requestPermission(Activity activity, String permission) {
        ActivityCompat.requestPermissions(activity, new String[]{permission}, REQUEST_PERMISSION_CODE);
    }

    private static void askShowRationale(final Activity activity, final String permission) {
        PermissionRequestCallback callback = callbackWeakReference.get();
        if (callback != null) {
            callback.onShouldShowRationale(new RationaleResponse(activity, permission));
        }
    }

    interface PermissionRequestCallback {

        void onPermissionGranted(String permission);

        void onPermissionDenied(String permission);

        void onShouldShowRationale(RationaleResponse rationaleResponse);
    }

    public static class RationaleResponse {

        private final Activity activity;
        private final String permission;

        public RationaleResponse(Activity activity, String permission) {
            this.activity = activity;
            this.permission = permission;
        }

        void revoked() {
            PermissionRequestCallback callback = callbackWeakReference.get();
            if (callback != null) {
                callback.onPermissionDenied(permission);
            }
        }

        void granted() {
            requestPermission(activity, permission);
        }
    }
}
