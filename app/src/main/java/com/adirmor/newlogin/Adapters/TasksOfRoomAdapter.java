package com.adirmor.newlogin.Adapters;

import android.content.Context;
import android.graphics.Paint;
import android.speech.tts.TextToSpeech;
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

import java.util.List;
import java.util.Locale;

public class TasksOfRoomAdapter extends RecyclerView.Adapter<TasksOfRoomAdapter.ViewHolder>{

    private TextToSpeech textToSpeech;
    private final String id;
    private final Context context;
    private final List<TaskOfRoomModel> taskModels;


    public TasksOfRoomAdapter(Context context, List<TaskOfRoomModel> taskModels, String id) {
        this.context = context;
        this.taskModels = taskModels;
        this.id = id;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder (LayoutInflater.from (context).inflate (R.layout.tasks_of_list_ui_for_recycler_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.description.setText (taskModels.get (position).getDescription ().toString ());
        holder.description.setChecked (taskModels.get (position).isCompleted ());

        strikeThroughDescription (holder);
        holder.description.setOnClickListener (view -> {
            strikeThroughDescription (holder);
            FunctionsUtils.isCompleted_TasksOfList (position, taskModels, id);
        });
        holder.speaker.setOnClickListener (view -> {
            readText (holder);
        });
    }

    private static void strikeThroughDescription(@NonNull ViewHolder holder) {
        if(holder.description.isChecked ())
            holder.description.setPaintFlags (holder.description.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        else
            holder.description.setPaintFlags(holder.description.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
    }

    public TaskOfRoomModel deleteTask(int position){
        TaskOfRoomModel taskOfRoomModel = taskModels.get (position);

        FirebaseUtils.getSpecificRoom (id).get ().addOnCompleteListener (task -> {
            if (task.isSuccessful ()){
                RoomModel roomModelList = task.getResult ().toObjects (RoomModel.class).get (0);
                taskModels.remove (taskModels.get (position));
                roomModelList.setTasks (taskModels);
                task.getResult ().getDocuments ().get (0).getReference ().set (roomModelList).addOnSuccessListener (unused -> {
                    notifyItemRemoved (position);
                });
            }
        });
        return taskOfRoomModel;
    }
    private void readText(@NonNull ViewHolder holder) {
        try {
            textToSpeech = new TextToSpeech (context, status -> {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage (Locale.ENGLISH);
                    textToSpeech.speak (holder.description.getText ().toString (), TextToSpeech.QUEUE_FLUSH, null, "1");
                } else {
                    Toast.makeText (context, "Initialization failed", Toast.LENGTH_SHORT).show ();
                }
            });
        }catch (Exception e){
            Toast.makeText (context, "Not supporting that language", Toast.LENGTH_SHORT).show ();
        }
    }

    @Override
    public int getItemCount() {
        return taskModels.size ();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder{
        CheckBox description;
        ImageView speaker;
        public ViewHolder(@NonNull View itemView) {
            super (itemView);

            speaker = itemView.findViewById (R.id.task_of_list_menu);
            description = itemView.findViewById (R.id.task_of_list_description_check_box);
        }
    }
}
