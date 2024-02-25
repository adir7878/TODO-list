package com.adirmor.newlogin.inApp;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.adirmor.newlogin.Adapters.PlannedTaskAdapter;
import com.adirmor.newlogin.Models.TaskModel;
import com.adirmor.newlogin.R;
import com.adirmor.newlogin.Utils.FirebaseUtils;
import com.adirmor.newlogin.bottomSheets.creates.createPlannedTaskBottomSheet;
import com.adirmor.newlogin.bottomSheets.edits.EditPlannedTaskBS;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class PlannedTasksFragment extends Fragment {

    private List<TaskModel> taskModels;
    private static RecyclerView recyclerView;
    private PlannedTaskAdapter plannedTaskAdapter;

    public PlannedTasksFragment() {
        taskModels = new ArrayList<> ();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate (R.layout.fragment_planned_tasks, container, false);

        recyclerView = view.findViewById (R.id.recyclerView_plannedTasks);
        recyclerView.setLayoutManager (new LinearLayoutManager (getContext ()));
        plannedTaskAdapter = new PlannedTaskAdapter (getContext (), taskModels,(view.findViewById (R.id.progressBar_plannedTasks)));
        recyclerView.setAdapter (plannedTaskAdapter);
        printPlannedTasks ();

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper (new SwipeTo (getContext (), taskModels, plannedTaskAdapter));
        itemTouchHelper.attachToRecyclerView (recyclerView);

        FloatingActionButton floatingActionButton = view.findViewById (R.id.addTask);
        floatingActionButton.setOnClickListener (this::createPlannedTask);

        return view;
    }

    private void createPlannedTask(View view) {
        new createPlannedTaskBottomSheet (getContext (), taskModels, plannedTaskAdapter).show ();
    }

    public void printPlannedTasks(){
        FirebaseUtils.getPlannedTaskModel ().get ().addOnCompleteListener (task -> {
            if(task.isSuccessful ()){
                taskModels.clear ();
                taskModels.addAll (task.getResult ().toObjects (TaskModel.class));
                plannedTaskAdapter.notifyDataSetChanged ();
            }
        }).addOnFailureListener (e -> {
            Toast.makeText (getContext (),"cannot load the tasks", Toast.LENGTH_SHORT).show ();
        });
    }

    private static class SwipeTo extends ItemTouchHelper.SimpleCallback {

        private final Context context;
        private final List<TaskModel> taskModels;
        private final PlannedTaskAdapter adapter;

        public SwipeTo(Context context, List<TaskModel> taskModels, PlannedTaskAdapter adapter) {
            super (0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT);
            this.context = context;
            this.taskModels = taskModels;
            this.adapter = adapter;
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition ();
            switch (direction) {
                case ItemTouchHelper.LEFT:
                    TaskModel taskModel = adapter.deleteTask (position);
                    Snackbar.make (recyclerView, taskModel.getDescription ().toString (), Snackbar.LENGTH_SHORT)
                            .setAction ("Undo", view -> {
                                taskModels.add (position, taskModel);
                                FirebaseUtils.getPlannedTaskModel ().document ().set (taskModel).addOnSuccessListener (unused -> adapter.notifyItemInserted (position));
                            }).show ();
                    break;
                case ItemTouchHelper.RIGHT:
                    new EditPlannedTaskBS (context, position ,taskModels, adapter).show ();
                    break;
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder (c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor (ContextCompat.getColor (context, R.color.lightRed))
                    .addSwipeLeftActionIcon (R.drawable.baseline_delete_24)
                    .addSwipeLeftLabel ("Delete")
                    .addSwipeRightBackgroundColor (ContextCompat.getColor (context, R.color.lightGreen))
                    .addSwipeRightActionIcon (R.drawable.edit_ic)
                    .addSwipeRightLabel ("Edit")
                    .create ()
                    .decorate ();

            super.onChildDraw (c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }

}