package com.adirmor.newlogin.Utils;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.adirmor.newlogin.Adapters.PlannedTaskAdapter;
import com.adirmor.newlogin.Adapters.RoomAdapter;
import com.adirmor.newlogin.Adapters.TasksOfRoomAdapter;
import com.adirmor.newlogin.Models.RoomModel;
import com.adirmor.newlogin.Models.TaskOfRoomModel;
import com.adirmor.newlogin.Models.UserModel;
import com.adirmor.newlogin.Notification.AlertReceiver;
import com.adirmor.newlogin.Models.TaskModel;
import com.adirmor.newlogin.Adapters.DailyTaskAdapter;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;

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

    public static void createTaskForRoom(String description, List<TaskOfRoomModel> modelsArray, String RoomID){
        TaskOfRoomModel model = new TaskOfRoomModel (description);
        FirebaseUtils.getTasksOfRoomCollection (RoomID, collectionReference -> {
            if(collectionReference == null)
                return;
            collectionReference.add (model);
        });
    }

    public static void isCompleted_TasksOfList(int position, TaskOfRoomModel model, String id) {
        model.setCompleted (!model.isCompleted ());
        FirebaseUtils.getTasksOfRoomCollection (id, collectionReference -> {
            if(collectionReference == null)
                return;
            collectionReference.whereEqualTo ("id", model.getId ()).get ().addOnCompleteListener (task -> {
                if(task.isSuccessful ()){
                    task.getResult ().getDocuments ().get (0).getReference ().update ("completed", model.isCompleted ());
                }
            });
        });
//        FirebaseUtils.getSpecificRoom (id).get ().addOnCompleteListener (task -> {
//            RoomModel roomModel = task.getResult ().toObjects (RoomModel.class).get (0);
//            roomModel.getTasks ().set (position, model);
//            task.getResult ().getDocuments ().get (0).getReference ().update ("tasks", roomModel.getTasks ());
//        });
    }

    public static void addUserToRoomWithCode(String roomCode, List<RoomModel> roomModelList, RoomAdapter adapter, Context context){

        FirebaseUtils.getSpecificRoomByCode (roomCode).get ().addOnCompleteListener (task -> {
            if(task.isSuccessful ()){

                if (task.getResult ().getDocuments ().size () == 0) {
                    Toast.makeText (context, "Nah bruh", Toast.LENGTH_SHORT).show ();
                    return;
                }

                RoomModel roomModel = task.getResult ().toObjects (RoomModel.class).get (0);

                if(roomModel.getParticipantIDs ().contains (FirebaseUtils.getCurrentUserId ())){
                    Toast.makeText (context, "Already in Room.", Toast.LENGTH_SHORT).show ();
                    return;
                }

                roomModel.getParticipantIDs ().add (FirebaseUtils.getCurrentUserId ());
                task.getResult ().getDocuments ().get (0).getReference ().update ("participantIDs", roomModel.getParticipantIDs ());

                roomModelList.add (roomModel);
                adapter.notifyItemInserted (roomModelList.size ());

            }else {
                Toast.makeText (context, "Room Code Are Invalid.", Toast.LENGTH_SHORT).show ();
            }
        });
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

}
