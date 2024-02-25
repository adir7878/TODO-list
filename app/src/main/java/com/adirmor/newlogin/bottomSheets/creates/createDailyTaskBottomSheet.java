package com.adirmor.newlogin.bottomSheets.creates;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TimePicker;

import androidx.annotation.NonNull;

import com.adirmor.newlogin.Adapters.DailyTaskAdapter;
import com.adirmor.newlogin.Models.TaskModel;
import com.adirmor.newlogin.R;
import com.adirmor.newlogin.Utils.FunctionsUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@SuppressLint("UseSwitchCompatOrMaterialCode")
public class createDailyTaskBottomSheet extends BottomSheetDialog {

    private final TextInputEditText Task;
    private final Switch setReminder;
    private final TimePicker timePicker;
    private final List<TaskModel> list;
    private final DailyTaskAdapter adapter;

    public createDailyTaskBottomSheet(@NonNull Context context, List<TaskModel> list, DailyTaskAdapter adapter) {
        super (context);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.add_daily_task, null);
        setContentView(view);

        getWindow ().getAttributes ().windowAnimations = R.style.DialogAnimation;

        Task = view.findViewById (R.id.TaskContext);
        setReminder = view.findViewById (R.id.setReminderSwitcher);
        timePicker = view.findViewById (R.id.timePickerForTask);
        this.list = list;
        this.adapter = adapter;


        Button createTask = view.findViewById (R.id.createTask);
        createTask.setOnClickListener (this::createTask);

        setReminder.setOnClickListener (v ->{
            timePicker.setEnabled (setReminder.isChecked ());
        });
    }

    private void createTask(View view) {
        String task = Objects.requireNonNull (Task.getText ()).toString ().trim ();
        if(task.isEmpty ())
            return;

        try {
            FunctionsUtils.createDailyTask (task, convertToTimeStamp (convertTime (timePicker)), getContext (), list, adapter);
        } catch (ParseException e) {
            throw new RuntimeException (e);
        }
        dismiss ();
    }

    private Timestamp convertToTimeStamp(String time) throws ParseException {
        if (!setReminder.isChecked ())
            return null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
        String dateTimeString = getCurrentDate() + " " + time;
        Date date = dateFormat.parse(dateTimeString);
        return new com.google.firebase.Timestamp (date);
    }

    @SuppressLint("DefaultLocale")
    private String convertTime(TimePicker timePicker) {
        int hour = timePicker.getHour ();
        int minutes = timePicker.getMinute ();

        return String.format ("%02d:%02d", hour, minutes);
    }

    private String getCurrentDate() {
        // Create a SimpleDateFormat object with the desired format
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // Get the current date
        Date currentDate = new Date();

        // Format the date to the desired string representation
        String dateString = dateFormat.format(currentDate);

        // Return the formatted date string
        return dateString;
    }
}
