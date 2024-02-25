package com.adirmor.newlogin.bottomSheets.edits;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.adirmor.newlogin.Adapters.DailyTaskAdapter;
import com.adirmor.newlogin.Models.TaskModel;
import com.adirmor.newlogin.R;
import com.adirmor.newlogin.Utils.FirebaseUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class EditDailyTaskBS extends BottomSheetDialog {

    private final TextInputEditText TEXT;
    private final int adapterPosition;
    private final List<TaskModel> taskModels;
    private final DailyTaskAdapter adapter;
    public EditDailyTaskBS(@NonNull Context context, int adapterPosition,
                           List<TaskModel> taskModels, DailyTaskAdapter adapter) {
        super (context);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate (R.layout.edit_task, null);
        setContentView (view);


        TEXT = view.findViewById(R.id.EditTaskContext);
        Button EDIT = view.findViewById (R.id.EditText);
        this.adapterPosition = adapterPosition;
        this.taskModels = taskModels;
        this.adapter = adapter;

        TEXT.setText (taskModels.get (adapterPosition).getDescription ());
        
        EDIT.setOnClickListener (this::EditTask);

    }
    private void EditTask(View view) {
        String TaskText = String.valueOf (TEXT.getText());

        if (TaskText.isEmpty ()) {
            Toast.makeText(getContext (), "Cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseUtils.getDailyTaskModel ().whereEqualTo("id", taskModels.get (adapterPosition).getId ()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                taskModels.get (adapterPosition).setDescription (TaskText);
                task.getResult ().getDocuments ().get (0).getReference ().set (taskModels.get (adapterPosition)).addOnSuccessListener(aVoid -> {
                    adapter.notifyItemChanged (adapterPosition);
                });
            }
        });
        dismiss ();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setOnDismissListener(dialogInterface -> {
            adapter.notifyItemChanged (adapterPosition);
        });
    }
}
