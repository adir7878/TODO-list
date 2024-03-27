package com.adirmor.newlogin.inApp;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.adirmor.newlogin.Models.UserModel;
import com.adirmor.newlogin.R;
import com.adirmor.newlogin.Utils.FirebaseUtils;
import com.adirmor.newlogin.Utils.FunctionsUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.permissionx.guolindev.PermissionX;

public class FragmentDisplayActivity extends AppCompatActivity {

    private TasksFragment tasksFragment;
    private SettingsFragment settingsFragment;
    private MultiTasksFragment multiTasksFragment;
    private PlannedTasksFragment plannedTasksFragment;


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_fragment_display);

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
        FirebaseUtils.getUserModel ().get ().addOnCompleteListener (task -> {
           if(task.isSuccessful ()){
               UserModel userModel = task.getResult ().toObject (UserModel.class);
               task.getResult ().getReference ().update ("id", userModel.getId ());
           }
        });
    }
}
