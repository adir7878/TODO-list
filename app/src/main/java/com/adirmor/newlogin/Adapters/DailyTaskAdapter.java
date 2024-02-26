package com.adirmor.newlogin.Adapters;

import android.content.Context;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.adirmor.newlogin.Models.TaskModel;
import com.adirmor.newlogin.R;
import com.adirmor.newlogin.Utils.FunctionsUtils;
import com.adirmor.newlogin.Utils.FirebaseUtils;
import com.adirmor.newlogin.inApp.TasksFragment;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DailyTaskAdapter extends abstractAdapterForTasks {

    // Text to speech engine
    private TextToSpeech textToSpeech;

    // Context of the adapter
    private final Context context;

    // List of task models
    private final List<TaskModel> taskModels;

    // List of completed task models
    private final List<TaskModel> completedTasks;

    // Constructor
    public DailyTaskAdapter(Context context, List<TaskModel> tasks, List<TaskModel> completedTasks) {
        this.context = context;
        this.taskModels = tasks;
        this.completedTasks = completedTasks;
    }

    // Create ViewHolder
    @NonNull
    @Override
    public TasksHolderView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TasksHolderView(LayoutInflater.from(context).inflate(R.layout.task_ui_for_recycler_view, parent, false));
    }

    // Bind data to ViewHolder
    @Override
    public void onBindViewHolder(@NonNull TasksHolderView holder, int position) {
        TaskModel taskModel = taskModels.get(position);
        holder.checkBox.setChecked(taskModel.isChecked());
        holder.checkBox.setText(taskModel.getDescription ());
        if (taskModel.getSelectedTimestamp() != null)
            holder.dateDisplay.setText(new SimpleDateFormat("HH:mm", Locale.US).format(taskModel.getSelectedTimestamp().toDate()));

        // Click listener for checkbox to mark task as completed
        holder.checkBox.setOnClickListener(v -> {
            holder.checkBox.setEnabled (false);

            taskModels.remove(taskModel);
            notifyItemRemoved(holder.getAdapterPosition());

            completedTasks.add(taskModel);
            TasksFragment.adapterOfCompletedTasks.notifyItemInserted(completedTasks.size());

            FirebaseUtils.getDailyTaskModel ().whereEqualTo("id", taskModel.getId()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    taskModel.setChecked(!taskModel.isChecked());
                    FunctionsUtils.cancelAlarm(context, taskModel);
                    task.getResult().getDocuments().get(0).getReference().set(taskModel);
                    holder.checkBox.setEnabled (true);
                }
            });
        });

        // Click listener for speaker to read task text aloud
        holder.speaker.setOnClickListener(view -> readText(holder));
    }

    // Apply strike-through style to completed tasks


    // Get total item count
    @Override
    public int getItemCount() {
        return taskModels.size();
    }

    // Delete a task
    public TaskModel deleteTask(int position) {
        TaskModel taskModel = taskModels.get(position);

        taskModels.remove(taskModel);
        notifyItemRemoved(position);

        FirebaseUtils.getDailyTaskModel ().whereEqualTo("id", taskModel.getId()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FunctionsUtils.cancelAlarm(context, taskModel);
                task.getResult().getDocuments().get(0).getReference().delete().addOnSuccessListener(unused -> {
                });
            }
        });
        return taskModel;
    }

    // Read task text aloud
    public void readText(@NonNull TasksHolderView holder) {
        try {
            textToSpeech = new TextToSpeech(context, status -> {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.ENGLISH);
                    textToSpeech.speak(holder.checkBox.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, "1");
                } else {
                    Toast.makeText(context, "Initialization failed", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(context, "Not supporting that language", Toast.LENGTH_SHORT).show();
        }
    }
}
