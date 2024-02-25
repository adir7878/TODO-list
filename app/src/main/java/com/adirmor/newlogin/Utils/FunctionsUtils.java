package com.adirmor.newlogin.Utils;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.adirmor.newlogin.Adapters.PlannedTaskAdapter;
import com.adirmor.newlogin.Adapters.TasksOfListAdapter;
import com.adirmor.newlogin.Models.ListsModel;
import com.adirmor.newlogin.Models.TaskOfListModel;
import com.adirmor.newlogin.Notification.AlertReceiver;
import com.adirmor.newlogin.Models.TaskModel;
import com.adirmor.newlogin.Adapters.DailyTaskAdapter;
import com.google.firebase.Timestamp;

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

    public static void createTaskForList(String description, List<TaskOfListModel> modelsArray, TasksOfListAdapter adapter, String listID){
        TaskOfListModel model = new TaskOfListModel (description);
        FirebaseUtils.getSpecificList (listID).get ().addOnCompleteListener (task -> {
            List<ListsModel> listsModelList = task.getResult ().toObjects (ListsModel.class);
            modelsArray.add (model);
            listsModelList.get (0).setList (modelsArray);
            task.getResult ().getDocuments ().get (0).getReference ().set (listsModelList.get (0)).addOnSuccessListener (unused -> {
                adapter.notifyItemInserted (modelsArray.size () - 1);
            });
        });
    }

    public static void isCompleted_TasksOfList(int position,  List<TaskOfListModel> taskModels, String id) {
        taskModels.get (position).setCompleted (!taskModels.get (position).isCompleted ());
        FirebaseUtils.getSpecificList (id).get ().addOnCompleteListener (task -> {
            List<ListsModel> taskOfListModelList = task.getResult ().toObjects (ListsModel.class);
            taskOfListModelList.get (0).setList (taskModels);
            task.getResult ().getDocuments ().get (0).getReference ().set (taskOfListModelList.get (0));
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
