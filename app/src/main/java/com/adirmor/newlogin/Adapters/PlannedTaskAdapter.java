package com.adirmor.newlogin.Adapters;

import android.content.Context;
import android.graphics.Paint;
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
import com.adirmor.newlogin.Utils.FirebaseUtils;
import com.adirmor.newlogin.Utils.FunctionsUtils;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PlannedTaskAdapter extends RecyclerView.Adapter<TasksHolderView> {

    // Text to speech engine
    private TextToSpeech textToSpeech;

    // Context of the adapter
    private final Context context;

    // List of task models
    private final List<TaskModel> taskModels;

    // Progress bar to show during operations
    private final ProgressBar progressBar;

    // Constructor
    public PlannedTaskAdapter(Context context, List<TaskModel> tasks, ProgressBar progressBar) {
        this.context = context;
        this.taskModels = tasks;
        this.progressBar = progressBar;
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
            holder.dateDisplay.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(taskModel.getSelectedTimestamp().toDate()));

        // Click listener for checkbox to mark task as completed
        holder.checkBox.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            FirebaseUtils.getPlannedTaskModel ().whereEqualTo("id", taskModel.getId()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    taskModel.setChecked(!taskModel.isChecked());
                    FunctionsUtils.cancelAlarm(context, taskModel);
                    task.getResult().getDocuments().get(0).getReference().set(taskModel).addOnSuccessListener (unused -> {
                        strikeThroughDescription(holder);
                        progressBar.setVisibility (View.GONE);
                    });
                }
            });
        });

        // Click listener for speaker to read task text aloud
        holder.speaker.setOnClickListener(view -> readText(holder));
    }

    private static void strikeThroughDescription(@NonNull TasksHolderView holder) {
        holder.checkBox.setPaintFlags(holder.checkBox.getPaintFlags() | (holder.checkBox.isChecked() ? Paint.STRIKE_THRU_TEXT_FLAG : 0));
    }

    // Get total item count
    @Override
    public int getItemCount() {
        return taskModels.size();
    }

    // Delete a task
    public TaskModel deleteTask(int position) {
        TaskModel taskModel = taskModels.get(position);
        FirebaseUtils.getPlannedTaskModel ().whereEqualTo("id", taskModel.getId()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FunctionsUtils.cancelAlarm(context, taskModel);
                task.getResult().getDocuments().get(0).getReference().delete().addOnSuccessListener(unused -> {
                    taskModels.remove(taskModel);
                    notifyItemRemoved(position);
                });
            }
        });
        return taskModel;
    }

    // Read task text aloud
    private void readText(@NonNull TasksHolderView holder) {
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
