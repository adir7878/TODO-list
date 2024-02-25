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

import com.adirmor.newlogin.Models.ListsModel;
import com.adirmor.newlogin.Models.TaskOfListModel;
import com.adirmor.newlogin.R;
import com.adirmor.newlogin.Utils.FirebaseUtils;
import com.adirmor.newlogin.Utils.FunctionsUtils;

import java.util.List;
import java.util.Locale;

public class TasksOfListAdapter extends RecyclerView.Adapter<TasksOfListAdapter.ViewHolder>{

    private TextToSpeech textToSpeech;
    private final String id;
    private final Context context;
    private final List<TaskOfListModel> taskModels;


    public TasksOfListAdapter(Context context, List<TaskOfListModel> taskModels, String id) {
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

    public TaskOfListModel deleteTask(int position){
        TaskOfListModel taskOfListModel = taskModels.get (position);

        FirebaseUtils.getSpecificList (id).get ().addOnCompleteListener (task -> {
            if (task.isSuccessful ()){
                ListsModel listsModelList = task.getResult ().toObjects (ListsModel.class).get (0);
                taskModels.remove (taskModels.get (position));
                listsModelList.setList (taskModels);
                task.getResult ().getDocuments ().get (0).getReference ().set (listsModelList).addOnSuccessListener (unused -> {
                    notifyItemRemoved (position);
                });
            }
        });
        return taskOfListModel;
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
