package com.adirmor.newlogin.Notification;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.adirmor.newlogin.Models.UserModel;
import com.adirmor.newlogin.Utils.FirebaseUtils;
import com.adirmor.newlogin.inApp.FragmentDisplayActivity;
import com.adirmor.newlogin.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AlertReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent i = new Intent (context, FragmentDisplayActivity.class);
        intent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity (context, 0, i, PendingIntent.FLAG_IMMUTABLE);

        FirebaseUtils.getUserModel ().get ().addOnCompleteListener (task -> {

            UserModel userModel = task.getResult ().toObject (UserModel.class);

            NotificationCompat.Builder builder = new NotificationCompat.Builder (context, "Todo list")
                    .setSmallIcon (R.mipmap.todo_list_app_icon)
                    .setContentTitle ("TODO list")
                    .setContentText (userModel.getUsername () + "! You've to " + intent.getExtras ().getString ("name"))
                    .setAutoCancel (true)
                    .setDefaults (NotificationCompat.DEFAULT_ALL)
                    .setPriority (NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent (pendingIntent);
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from (context);
            if (ActivityCompat.checkSelfPermission (context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            notificationManagerCompat.notify (123, builder.build ());
        });



    }
}
