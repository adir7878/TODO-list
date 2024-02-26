package com.adirmor.newlogin.Adapters;

import android.annotation.SuppressLint;
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
import com.adirmor.newlogin.inApp.TasksFragment;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CompletedDailyTaskAdapter extends RecyclerView.Adapter<TasksHolderView> {

    private TextToSpeech textToSpeech;
    private final Context context;
    private final List<TaskModel> taskModels;
    private final List<TaskModel> notCompTasksModels;

    // Constructor
    public CompletedDailyTaskAdapter(Context context, List<TaskModel> tasks, List<TaskModel> notCompTasksModels) {
        this.context = context;
        this.taskModels = tasks;
        this.notCompTasksModels = notCompTasksModels;
    }

    // Create ViewHolder
    @NonNull
    @Override
    public TasksHolderView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TasksHolderView(LayoutInflater.from(context).inflate(R.layout.task_ui_for_recycler_view, parent, false));
    }

    // Bind data to ViewHolder
    @Override
    public void onBindViewHolder(@NonNull TasksHolderView holder, @SuppressLint("RecyclerView") int position) {
        TaskModel taskModel = taskModels.get (holder.getAdapterPosition ());
        holder.checkBox.setText(taskModels.get(position).getDescription ());
        holder.checkBox.setChecked(true);
        if (taskModels.get(position).getSelectedTimestamp() != null)
            holder.dateDisplay.setText(new SimpleDateFormat("HH:mm", Locale.US)
                    .format(taskModels.get(position).getSelectedTimestamp().toDate()));

        strikeThroughDescription (holder);

        // Set task to true
        holder.checkBox.setOnClickListener(v -> {
            holder.checkBox.setEnabled (false);

            //notify to adapters
            taskModels.remove(holder.getAdapterPosition());
            notifyItemRemoved(holder.getAdapterPosition());

            taskModel.setChecked(!taskModel.isChecked());
            notCompTasksModels.add(taskModel);
            TasksFragment.adapterOfNotCompletedTask.notifyItemInserted(notCompTasksModels.size());

            FirebaseUtils.getDailyTaskModel ().whereEqualTo("id", taskModel.getId()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FunctionsUtils.cancelAlarm(context, taskModel);
                    task.getResult().getDocuments().get(0).getReference().set(taskModel);
                    holder.checkBox.setEnabled (true);
                }
            });

            holder.itemView.setAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_out_animation));
            new Handler().postDelayed(() -> {

            }, 500);
        });

        // Speak task text
        holder.speaker.setOnClickListener(view -> {
            readText(holder);
        });

    }
    private static void strikeThroughDescription(@NonNull TasksHolderView holder) {
        holder.checkBox.setPaintFlags(holder.checkBox.getPaintFlags() | (holder.checkBox.isChecked() ? Paint.STRIKE_THRU_TEXT_FLAG : 0));
    }

    // Get item count
    @Override
    public int getItemCount() {
        return taskModels.size();
    }

    // Delete a task
    public TaskModel deleteTask(int position) {
        TaskModel taskModel = taskModels.get(position);

        FirebaseUtils.getDailyTaskModel ().whereEqualTo("id", taskModels.get(position).getId()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                task.getResult().getDocuments().get(0).getReference().delete();
                taskModels.remove(position);
                notifyItemRemoved(position);
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
