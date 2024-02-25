package com.adirmor.newlogin.inApp;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.adirmor.newlogin.Notification.NotificationPermissionHelper;
import com.adirmor.newlogin.R;
import com.adirmor.newlogin.Utils.FirebaseUtils;
import com.adirmor.newlogin.Utils.FunctionsUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Calendar;

public class FragmentDisplayActivity extends AppCompatActivity {

    private TasksFragment tasksFragment;
    private SettingsFragment settingsFragment;
    private MultiTasksFragment multiTasksFragment;
    private PlannedTasksFragment plannedTasksFragment;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_fragment_display);

        if (!NotificationPermissionHelper.hasNotificationPermission(this)) {
            NotificationPermissionHelper.requestNotificationPermission(this);
        }

        BottomNavigationView bottomNavigationView = findViewById (R.id.bottomNavigationView);
        settingsFragment = new SettingsFragment ();
        tasksFragment = new TasksFragment ();
        multiTasksFragment = new MultiTasksFragment ();
        plannedTasksFragment = new PlannedTasksFragment ();

        getSupportFragmentManager ().beginTransaction ().replace (R.id.mainFrameLayout, tasksFragment).commit ();

        bottomNavigationView.setOnItemSelectedListener (item -> {
            if (item.getItemId () == R.id.Tasks) {
                getSupportFragmentManager ().beginTransaction ().replace (R.id.mainFrameLayout, tasksFragment).commit ();
            }
            if (item.getItemId () == R.id.Plan_Tasks) {
                getSupportFragmentManager ().beginTransaction ().replace (R.id.mainFrameLayout, plannedTasksFragment).commit ();
            }
            if (item.getItemId () == R.id.Settings) {
                getSupportFragmentManager ().beginTransaction ().replace (R.id.mainFrameLayout, settingsFragment).commit ();
            }
            if (item.getItemId () == R.id.Lists_Page) {
                getSupportFragmentManager ().beginTransaction ().replace (R.id.mainFrameLayout, multiTasksFragment).commit ();
            }
            return true;
        });


        FunctionsUtils.createNotification (getApplicationContext ());
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Check if the notification permission is granted
        if (NotificationPermissionHelper.isNotificationPermissionGranted(requestCode, grantResults)) {
            Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            // Perform actions that require notification permission
        } else {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            // Handle the case where the user denied notification permission
        }
    }

    //delete daily tasks in 00:00.
    @Override
    protected void onResume() {
        super.onResume ();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences (this);
        int lastTimeStarted = settings.getInt ("last_time_started", -1);
        Calendar calendar = Calendar.getInstance ();
        int today = calendar.get (Calendar.DAY_OF_YEAR);

        if (today != lastTimeStarted) {
            FirebaseUtils.getDailyTaskModel ().get ().addOnCompleteListener (task -> {
               for (DocumentSnapshot documentSnapshot: task.getResult ()){
                   documentSnapshot.getReference ().delete ();
               }
            });

            SharedPreferences.Editor editor = settings.edit ();
            editor.putInt ("last_time_started", today);
            editor.commit ();
        }
    }

}
