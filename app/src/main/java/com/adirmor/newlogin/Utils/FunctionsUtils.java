package com.adirmor.newlogin.Utils;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.adirmor.newlogin.Adapters.PlannedTaskAdapter;
import com.adirmor.newlogin.Adapters.RoomAdapter;
import com.adirmor.newlogin.Models.RoomModel;
import com.adirmor.newlogin.Models.TaskOfRoomModel;
import com.adirmor.newlogin.Models.UserModel;
import com.adirmor.newlogin.Notification.AlertReceiver;
import com.adirmor.newlogin.Models.TaskModel;
import com.adirmor.newlogin.Adapters.DailyTaskAdapter;
import com.google.firebase.Timestamp;
import com.permissionx.guolindev.PermissionX;

import java.util.Calendar;
import java.util.List;

public class FunctionsUtils {

    public static void createDailyTask(String taskText, Timestamp timestamp, Context context, List<TaskModel> tasks, DailyTaskAdapter adapter) {
        TaskModel task = new TaskModel (taskText, timestamp);
        FirebaseUtils.getDailyTaskModel ().document ().set(task).addOnSuccessListener(unused -> {
            tasks.add (task);
            adapter.notifyItemInserted(tasks.size() - 1);
            if(timestamp != null)
                setAlarm (context, task);
        });
    }
    public static void createPlannedTask(String taskText, Timestamp timestamp, Context context, List<TaskModel> tasks, PlannedTaskAdapter adapter) {
        TaskModel task = new TaskModel (taskText, timestamp);
        FirebaseUtils.getPlannedTaskModel ().document ().set(task).addOnSuccessListener(unused -> {
            tasks.add (task);
            adapter.notifyItemInserted(tasks.size() - 1);
            if(timestamp != null)
                setAlarm (context, task);
        });
    }

    public static void createTaskForRoom(String description, String RoomID){
        TaskOfRoomModel model = new TaskOfRoomModel (description);
        FirebaseUtils.getTasksOfRoomCollection (RoomID).add (model);
    }

    public static void isCompleted_TasksOfList(TaskOfRoomModel model, String id) {
        model.setCompleted (!model.isCompleted ());
        FirebaseUtils.getTasksOfRoomCollection (id).whereEqualTo ("id", model.getId ()).get ().addOnCompleteListener (task -> {
            task.getResult ().getDocuments ().get (0).getReference ().update ("completed", model.isCompleted ());
        });
    }

    public static void addUserToRoomWithCode(String roomCode, Context context, List<RoomModel> roomModels, RoomAdapter adapter){

        FirebaseUtils.getSpecificRoomByCode (roomCode).get ().addOnCompleteListener (task -> {
            if(task.isSuccessful ()){

                if (task.getResult ().getDocuments ().size () == 0) {
                    Toast.makeText (context, "Room Not Found.", Toast.LENGTH_SHORT).show ();
                    return;
                }

                RoomModel roomModel = task.getResult ().toObjects (RoomModel.class).get (0);

                if(roomModel.getBlockedUsersID ().contains (FirebaseUtils.getCurrentUserId ())) {
                    Toast.makeText (context, "You've got blocked from this room.", Toast.LENGTH_SHORT).show ();
                    return;
                }

                if(roomModel.getParticipantIDs ().contains (FirebaseUtils.getCurrentUserId ())){
                    Toast.makeText (context, "Already in Room.", Toast.LENGTH_SHORT).show ();
                    return;
                }
                roomModels.add (roomModel);
                adapter.notifyItemInserted (roomModels.size ());

                FirebaseUtils.getUserModel ().get ().addOnCompleteListener (userModel -> {
                    UserModel myUserModel = userModel.getResult ().toObject (UserModel.class);
                    FirebaseUtils.getUserModelOfRoomCollection (roomModel.getId ())
                            .document (myUserModel.getId ()).set (myUserModel);
                });

                roomModel.getParticipantIDs ().add (FirebaseUtils.getCurrentUserId ());
                task.getResult ().getDocuments ().get (0).getReference ().update ("participantIDs", roomModel.getParticipantIDs ());

            }else {
                Toast.makeText (context, "Room Code Are Invalid.", Toast.LENGTH_SHORT).show ();
            }
        });
    }
    public static void deleteUserFromRoomBySwiping(String roomID){
        FirebaseUtils.getUserModelOfRoomCollection (roomID).document (FirebaseUtils.getCurrentUserId ()).delete ();
    }

    /*Send Notification*/
    public static void createNotification(Context context){
        CharSequence name = "Tasks reminder";
        String description = "tasks";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel notificationChannel = new NotificationChannel ("Todo list", name, importance);
        notificationChannel.setDescription (description);

        NotificationManager notificationManager = context.getSystemService (NotificationManager.class);
        notificationManager.createNotificationChannel (notificationChannel);
    }
    @SuppressLint("ScheduleExactAlarm")
    public static void setAlarm(Context context, TaskModel task){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService (Context.ALARM_SERVICE);
        Intent intent = new Intent (context, AlertReceiver.class);
        intent.putExtra ("name", task.getDescription ());

        PendingIntent pendingIntent = PendingIntent.getBroadcast (context, task.getRequestCode (), intent, PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance ();
        calendar.setTime (task.getSelectedTimestamp ().toDate ());

        alarmManager.setExact (AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis (), pendingIntent);
    }
    public static void cancelAlarm(Context context, TaskModel task){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService (Context.ALARM_SERVICE);
        Intent intent = new Intent (context, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast (context, task.getRequestCode (), intent, PendingIntent.FLAG_IMMUTABLE);

        alarmManager.cancel (pendingIntent);
    }

    public static void askPermissions(FragmentActivity fragmentActivity){
        PermissionX.init (fragmentActivity).permissions (Manifest.permission.POST_NOTIFICATIONS).explainReasonBeforeRequest().onExplainRequestReason ((scope, deniedList) -> {
            scope.showRequestReasonDialog(deniedList, "Allow \"TODO LIST\" To Send You Notifications", "Got it!", "Cancel");
        }).onForwardToSettings ((scope, deniedList) -> {
            scope.showForwardToSettingsDialog(deniedList, "You need to allow necessary permissions in Settings manually", "OK", "Cancel");
        }).request ((allGranted, grantedList, deniedList) -> {
            if(allGranted){
                Log.e (TAG, "askPermissions: All permissions are granted");
            }else{
                Log.e (TAG, "askPermissions: These permissions are denied: " + deniedList);
            }
        });
    }

}
