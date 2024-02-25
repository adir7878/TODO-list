package com.adirmor.newlogin.Notification;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class NotificationPermissionHelper {

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;

    // Check if the app has notification permissions
    public static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return NotificationManagerCompat.from(context).areNotificationsEnabled();
        } else {
            // For versions below Oreo, you can assume permission is granted
            return true;
        }
    }

    // Request notification permissions
    public static void requestNotificationPermission(Activity activity) {
        if (!hasNotificationPermission(activity)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Redirect to the app's notification settings
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, activity.getPackageName());
                activity.startActivity(intent);
            } else {
                // Request permission for older versions
                ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.RECEIVE_BOOT_COMPLETED}, NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    // Check the result of permission request
    public static boolean isNotificationPermissionGranted(int requestCode, int[] grantResults) {
        return requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }
}
