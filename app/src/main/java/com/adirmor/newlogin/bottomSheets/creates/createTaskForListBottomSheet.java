package com.adirmor.newlogin.bottomSheets.creates;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.adirmor.newlogin.Adapters.TasksOfListAdapter;
import com.adirmor.newlogin.Models.TaskOfListModel;
import com.adirmor.newlogin.R;
import com.adirmor.newlogin.Utils.FunctionsUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class createTaskForListBottomSheet extends BottomSheetDialog {

    private final String listID;
    private final TasksOfListAdapter adapter;
    private final List<TaskOfListModel> taskOfListModelList;
    private final TextInputEditText description;
    public createTaskForListBottomSheet(@NonNull Context context, List<TaskOfListModel> taskOfListModelList,
                                        TasksOfListAdapter adapter, String listID) {
        super (context);
        @SuppressLint("InflateParams") View view = LayoutInflater.from (context).inflate(R.layout.add_task_to_list_of_tasks, null);
        setContentView (view);

        this.adapter = adapter;
        this.taskOfListModelList = taskOfListModelList;
        this.listID = listID;

        description = view.findViewById (R.id.task_of_list_description);
        description.requestFocus ();

        Button createTaskButton = view.findViewById (R.id.create_task_to_lists);
        createTaskButton.setOnClickListener (this::createTask);
    }

    private void createTask(View view) {
        if(description.getText ().toString ().isEmpty ())
            return;
        FunctionsUtils.createTaskForList (description.getText ().toString (), taskOfListModelList, adapter, listID);
        dismiss ();
    }
}
