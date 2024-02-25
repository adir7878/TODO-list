package com.adirmor.newlogin.bottomSheets.creates;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.NonNull;

import com.adirmor.newlogin.Adapters.DailyTaskAdapter;
import com.adirmor.newlogin.Adapters.PlannedTaskAdapter;
import com.adirmor.newlogin.Models.TaskModel;
import com.adirmor.newlogin.R;
import com.adirmor.newlogin.Utils.FunctionsUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@SuppressLint("UseSwitchCompatOrMaterialCode")
public class createPlannedTaskBottomSheet extends BottomSheetDialog {

    private final TextInputEditText Task;
    private final TimePicker timePicker;
    private final DatePicker datePicker;
    private final List<TaskModel> list;
    private final PlannedTaskAdapter adapter;

    public createPlannedTaskBottomSheet(@NonNull Context context, List<TaskModel> list, PlannedTaskAdapter adapter) {
        super (context);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.add_planned_task, null);
        setContentView(view);

        getWindow ().getAttributes ().windowAnimations = R.style.DialogAnimation;

        Task = view.findViewById (R.id.TaskContext);
        timePicker = view.findViewById (R.id.timePickerForPlannedTask);
        datePicker = view.findViewById (R.id.datePickerForPlannedTask);
        this.list = list;
        this.adapter = adapter;

        datePicker.setMinDate (Calendar.getInstance ().getTimeInMillis ());

        Button createTask = view.findViewById (R.id.createTask);
        createTask.setOnClickListener (this::createTask);

    }

    private void createTask(View view) {
        String task = Objects.requireNonNull (Task.getText ()).toString ().trim ();
        if(task.isEmpty ())
            return;

        try {
            FunctionsUtils.createPlannedTask(task, convertToTimeStamp (), getContext (), list, adapter);
        } catch (ParseException e) {
            throw new RuntimeException (e);
        }
        dismiss ();
    }

    //set Time for reminder.
    private Timestamp convertToTimeStamp() throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
        String dateTimeString = convertDate (datePicker) + " " + convertTime (timePicker);
        Date date = dateFormat.parse(dateTimeString);
        return new Timestamp (date);
    }
    @SuppressLint("DefaultLocale")
    private String convertTime(TimePicker timePicker) {
        int hour = timePicker.getHour ();
        int minutes = timePicker.getMinute ();

        return String.format ("%02d:%02d", hour, minutes);
    }
    private String convertDate(DatePicker datePicker) {
        int year = datePicker.getYear ();
        int month = datePicker.getMonth () + 1;
        int day = datePicker.getDayOfMonth ();

        return day + "/" + month + "/" + year;
    }
}
