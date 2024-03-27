package com.adirmor.newlogin.Adapters;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.graphics.Paint;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.adirmor.newlogin.Models.RoomModel;
import com.adirmor.newlogin.Models.TaskOfRoomModel;
import com.adirmor.newlogin.R;
import com.adirmor.newlogin.Utils.FirebaseUtils;
import com.adirmor.newlogin.Utils.FunctionsUtils;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;

import java.util.List;
import java.util.Locale;

public class TasksOfRoomAdapter extends FirestoreRecyclerAdapter<TaskOfRoomModel, TasksOfRoomAdapter.ViewHolder> {

    private TextToSpeech textToSpeech;
    private final String id;
    private final Context context;

    public TasksOfRoomAdapter(@NonNull FirestoreRecyclerOptions<TaskOfRoomModel> options, Context context, String id) {
        super(options);
        this.context = context;
        this.id = id;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.tasks_of_list_ui_for_recycler_view, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull TaskOfRoomModel model) {
            holder.description.setText(model.getDescription());
            holder.description.setChecked(model.isCompleted());

            strikeThroughDescription(holder);
            holder.description.setOnClickListener(view -> {
                strikeThroughDescription(holder);
                FunctionsUtils.isCompleted_TasksOfList(model, id);
            });
            holder.speaker.setOnClickListener(view -> {
                readText(holder);
            });
        }

    private static void strikeThroughDescription(@NonNull ViewHolder holder) {
        if (holder.description.isChecked())
            holder.description.setPaintFlags(holder.description.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        else
            holder.description.setPaintFlags(holder.description.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
    }

    public TaskOfRoomModel deleteTask(TaskOfRoomModel model){
        FirebaseUtils.getTasksOfRoomCollection (id).whereEqualTo ("id", model.getId ()).get ().addOnCompleteListener (task -> {
            if(task.isSuccessful ()){
                task.getResult ().getDocuments ().get (0).getReference ().delete ();
            }
        });
        return model;
    }


    private void readText(@NonNull ViewHolder holder) {
        try {
            textToSpeech = new TextToSpeech(context, status -> {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.ENGLISH);
                    textToSpeech.speak(holder.description.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, "1");
                } else {
                    Toast.makeText(context, "Initialization failed", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(context, "Not supporting that language", Toast.LENGTH_SHORT).show();
        }
    }


    public static class ViewHolder extends RecyclerView.ViewHolder{
        CheckBox description;
        ImageView speaker;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            speaker = itemView.findViewById(R.id.task_of_list_menu);
            description = itemView.findViewById(R.id.task_of_list_description_check_box);
        }
    }
}
