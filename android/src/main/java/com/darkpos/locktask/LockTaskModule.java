package com.darkpos.locktask;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.UiThreadUtil;

public class LockTaskModule extends ReactContextBaseJavaModule {
    private static final String TAG = "LockTaskModule";
    private static final String CLASSNAME = "com.darkpos.locktask.LockTaskReceiver";
    private final ReactApplicationContext reactContext;

    public LockTaskModule(@NonNull ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @NonNull
    @Override
    public String getName() {
        return "LockTask";
    }

    private ComponentName getAdminComponent() {
        return new ComponentName(reactContext, CLASSNAME);
    }

    /* Device Owner */

    @ReactMethod
    public void setDeviceOwner() {
        try {
            String deviceAdminReceiverName = reactContext.getPackageName() + "/" + CLASSNAME;

            Log.d(TAG, "setDeviceOwner request: " + deviceAdminReceiverName);
            Process proc = Runtime.getRuntime().exec("dpm set-device-owner " + deviceAdminReceiverName);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

            // Read the output from the command
            Log.d(TAG, "setDeviceOwner output:");
            String s = null;
            while ((s = stdInput.readLine()) != null) {
                Log.d(TAG, s);
            }

            // Read any errors from the attempted command
            Log.d(TAG, "setDeviceOwner error:");
            while ((s = stdError.readLine()) != null) {
                Log.d(TAG, s);
            }

            Log.d(TAG, "setDeviceOwner done");
        } catch (Exception e) {
            Log.e(TAG, "setDeviceOwner failed with exception: ", e);
        }
    }

    /* Device Admin */

    @ReactMethod
    public void isDeviceAdminEnabled(Promise promise) {
        DevicePolicyManager dpm = (DevicePolicyManager) reactContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
        boolean enabled = dpm.isAdminActive(getAdminComponent());
        promise.resolve(enabled);
    }

    @ReactMethod
    public void requestDeviceAdmin() {
        try {
            final Activity activity = getCurrentActivity();
            if (activity == null) {
                Log.d(TAG, "requestDeviceAdmin failed: activity is null");
                return;
            }

            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, getAdminComponent());
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "This app requires Device Admin to enable kiosk mode.");
            activity.startActivity(intent);
            Log.d(TAG, "requestDeviceAdmin success");
        } catch (Exception e) {
            Log.e(TAG, "requestDeviceAdmin failed with exception: ", e);
        }
    }

    @ReactMethod
    public void removeDeviceAdmin() {
        try {
            DevicePolicyManager dpm = (DevicePolicyManager) reactContext.getSystemService(Context.DEVICE_POLICY_SERVICE);

            // remove active admin
            dpm.removeActiveAdmin(getAdminComponent());
            Log.d(TAG, "removeDeviceAdmin removed active admin");

            // application is device owner
            String packageName = reactContext.getPackageName();
            if (!dpm.isDeviceOwnerApp(packageName)) {
                Log.d(TAG, "removeDeviceAdmin failed: package (" + packageName + ") is not device owner");
                return;
            }

            // clear device owner
            dpm.clearDeviceOwnerApp(packageName);
            Log.d(TAG, "removeDeviceAdmin cleared device owner for package: " + packageName);
        } catch (Exception e) {
            Log.e(TAG, "removeDeviceAdmin failed with exception: ", e);
        }
    }

    /* Lock Task Mode */

    @ReactMethod
    public void setLockTaskPackages() {
        DevicePolicyManager dpm = (DevicePolicyManager) reactContext.getSystemService(Context.DEVICE_POLICY_SERVICE);

        // application is device owner
        String packageName = reactContext.getPackageName();
        if (!dpm.isDeviceOwnerApp(packageName)) {
            Log.d(TAG, "setLockTaskPackages failed: package (" + packageName + ") is not device owner");
            return;
        }

        // application is whitelisted
        boolean isWhitelisted = false;
        String[] packageNames = dpm.getLockTaskPackages(getAdminComponent());
        if (packageNames.length > 0) {
            for (String p : packageNames) {
                if (p.equals(packageName)) {
                    isWhitelisted = true;
                }
            }
        }

        if (!isWhitelisted) {
            dpm.setLockTaskPackages(getAdminComponent(), new String[]{ packageName });
            Log.d(TAG, "setLockTaskPackages whitelisted package: " + packageName);
        }

        // application locktask permitted
        if (!dpm.isLockTaskPermitted(packageName)) {
            Log.d(TAG, "setLockTaskPackages failed: package (" + packageName + ") is not permitted to locktask");
            return;
        }

        Log.d(TAG, "setLockTaskPackages success");
    }

    @ReactMethod
    public void startLockTask() {
        Activity activity = getCurrentActivity();
        if (activity == null) {
            Log.d(TAG, "startLockTask failed: activity is null");
            return;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Log.d(TAG, "startLockTask failed: incompatible build version");
            return;
        }

        activity.startLockTask();
        Log.d(TAG, "startLockTask success");
    }

    @ReactMethod
    public void stopLockTask() {
        Activity activity = getCurrentActivity();
        if (activity == null) {
            Log.d(TAG, "stopLockTask failed: activity is null");
            return;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Log.d(TAG, "stopLockTask failed: incompatible build version");
            return;
        }

        activity.stopLockTask();
        Log.d(TAG, "stopLockTask success");
    }

    /* Immersive fullscreen */

    @ReactMethod
    public void enableImmersiveMode() {
        UiThreadUtil.runOnUiThread(() -> {
            Activity activity = getCurrentActivity();
            if (activity == null) {
                Log.d(TAG, "enableImmersiveMode failed: activity is null");
                return;
            }

            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            View decor = window.getDecorView();
            decor.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );

            Log.d(TAG, "enableImmersiveMode success");
        });
    }
}
