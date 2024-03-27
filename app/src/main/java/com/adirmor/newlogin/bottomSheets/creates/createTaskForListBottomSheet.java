package com.adirmor.newlogin.bottomSheets.creates;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.adirmor.newlogin.Adapters.TasksOfRoomAdapter;
import com.adirmor.newlogin.Models.TaskOfRoomModel;
import com.adirmor.newlogin.R;
import com.adirmor.newlogin.Utils.FunctionsUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class createTaskForListBottomSheet extends BottomSheetDialog {

    private final String listID;
    private final TasksOfRoomAdapter adapter;
    private final TextInputEditText description;
    public createTaskForListBottomSheet(@NonNull Context context, TasksOfRoomAdapter adapter, String listID) {
        super (context);
        @SuppressLint("InflateParams") View view = LayoutInflater.from (context).inflate(R.layout.add_task_to_list_of_tasks, null);
        setContentView (view);

        this.adapter = adapter;
        this.listID = listID;

        description = view.findViewById (R.id.task_of_list_description);
        description.requestFocus ();

        Button createTaskButton = view.findViewById (R.id.create_task_to_lists);
        createTaskButton.setOnClickListener (this::createTask);
    }

    private void createTask(View view) {
        if(description.getText ().toString ().isEmpty ())
            return;
        FunctionsUtils.createTaskForRoom (description.getText ().toString (), listID);
        dismiss ();
    }
}
