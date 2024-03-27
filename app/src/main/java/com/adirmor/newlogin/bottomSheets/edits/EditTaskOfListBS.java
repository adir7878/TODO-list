package com.adirmor.newlogin.bottomSheets.edits;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.adirmor.newlogin.Adapters.TasksOfRoomAdapter;
import com.adirmor.newlogin.Models.TaskOfRoomModel;
import com.adirmor.newlogin.R;
import com.adirmor.newlogin.Utils.FirebaseUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

public class EditTaskOfListBS extends BottomSheetDialog {
    private final TextInputEditText TEXT;
    private final Button EDIT;
    private final String id;
    private final TasksOfRoomAdapter adapter;
    private int adapterPosition;
    private TaskOfRoomModel model;
    public EditTaskOfListBS(@NonNull Context context, int adapterPosition, TaskOfRoomModel model, String id, TasksOfRoomAdapter adapter) {
        super (context);
        this.id = id;
        this.adapter = adapter;
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate (R.layout.edit_task_of_list, null);
        setContentView (view);

        TEXT = view.findViewById(R.id.EditTaskContext);
        EDIT = view.findViewById (R.id.EditText);
        this.adapterPosition = adapterPosition;
        this.model = model;

        TEXT.setText (model.getDescription ());
        
        EDIT.setOnClickListener (this::EditTask);
    }

    private void EditTask(View view) {
        String TaskText = TEXT.getText().toString();

        if (TaskText.isEmpty ()) {
            Toast.makeText(getContext (), "Cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseUtils.getTasksOfRoomCollection (id).whereEqualTo ("id", model.getId ()).get ().addOnCompleteListener (task -> {
            task.getResult ().getDocuments ().get (0).getReference ().update ("description", TaskText);
        });
        dismiss ();
    }

    //if the user didn't clicked on CREATE button it will get the object back to place
    @Override
    protected void onStop() {
        super.onStop ();
        adapter.notifyItemChanged (adapterPosition);
    }
}
