package com.adirmor.newlogin.Adapters;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.adirmor.newlogin.R;

public class TasksHolderView extends RecyclerView.ViewHolder {


    CheckBox checkBox;
    TextView dateDisplay;
    ImageView speaker;

    public TasksHolderView(@NonNull View itemView) {
        super (itemView);
        checkBox = itemView.findViewById (R.id.checkTaskCompletedAndTask);
        dateDisplay = itemView.findViewById (R.id.dateOfTask);
        speaker = itemView.findViewById (R.id.task_speaker);
    }
}
