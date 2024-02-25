package com.adirmor.newlogin.bottomSheets.edits;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.adirmor.newlogin.Adapters.TasksOfListAdapter;
import com.adirmor.newlogin.Models.ListsModel;
import com.adirmor.newlogin.Models.TaskOfListModel;
import com.adirmor.newlogin.R;
import com.adirmor.newlogin.Utils.FirebaseUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class EditTaskOfListBS extends BottomSheetDialog {
    private final TextInputEditText TEXT;
    private final Button EDIT;
    private final String id;
    private int adapterPosition;
    private List<TaskOfListModel> taskModels;
    private TasksOfListAdapter adapter;
    public EditTaskOfListBS(@NonNull Context context, int adapterPosition, List<TaskOfListModel> taskModels, TasksOfListAdapter adapter, String id) {
        super (context);
        this.id = id;
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate (R.layout.edit_task_of_list, null);
        setContentView (view);

        TEXT = view.findViewById(R.id.EditTaskContext);
        EDIT = view.findViewById (R.id.EditText);
        this.adapterPosition = adapterPosition;
        this.taskModels = taskModels;
        this.adapter = adapter;

        TEXT.setText (taskModels.get (adapterPosition).getDescription ());
        
        EDIT.setOnClickListener (this::EditTask);
    }

    private void EditTask(View view) {
        String TaskText = TEXT.getText().toString();

        if (TaskText.isEmpty ()) {
            Toast.makeText(getContext (), "Cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseUtils.getSpecificList (id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ListsModel listsModelList = task.getResult ().toObjects (ListsModel.class).get (0);
                taskModels.get (adapterPosition).setDescription (TaskText);
                listsModelList.setList (taskModels);
                task.getResult ().getDocuments ().get (0).getReference ().set (listsModelList).addOnSuccessListener (unused -> {
                    adapter.notifyItemChanged (adapterPosition);
                });
            }
        });
        dismiss ();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setOnDismissListener (dialogInterface -> {
            adapter.notifyItemChanged (adapterPosition);
        });
    }
}
