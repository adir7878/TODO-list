package com.adirmor.newlogin.inApp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adirmor.newlogin.Adapters.CompletedDailyTaskAdapter;
import com.adirmor.newlogin.Adapters.DailyTaskAdapter;
import com.adirmor.newlogin.Models.TaskModel;
import com.adirmor.newlogin.R;
import com.adirmor.newlogin.Utils.FirebaseUtils;
import com.adirmor.newlogin.Utils.FunctionsUtils;
import com.adirmor.newlogin.bottomSheets.edits.EditDailyTaskBS;
import com.adirmor.newlogin.bottomSheets.creates.createDailyTaskBottomSheet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class TasksFragment extends Fragment {

    private List<TaskModel> tasks;
    private List<TaskModel> completedTasks;
    private static RecyclerView recyclerView;
    private static RecyclerView recyclerViewOfCompletedTasks;
    public static DailyTaskAdapter adapterOfNotCompletedTask;
    public static CompletedDailyTaskAdapter adapterOfCompletedTasks;

    public TasksFragment(){
        tasks = new ArrayList<> ();
        completedTasks = new ArrayList<> ();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate (R.layout.fragment_tasks, container, false);

        //set title to current date
        TextView titleOfPage = view.findViewById (R.id.tasksPageTitle);
        titleOfPage.setText (new SimpleDateFormat ("EEE, MMM d", Locale.US).format (Calendar.getInstance ().getTime ()));

        //initialize ui
        initializeRecyclerViews (view);

        //add task button
        FloatingActionButton openBottomSheetPopupToAddTask = view.findViewById (R.id.addTask);
        openBottomSheetPopupToAddTask.setOnClickListener (this::openBottomSheetPopupToAddTask);

        //ask permissions from user
        FunctionsUtils.askPermissions (getActivity ());

        return view;
    }

    @SuppressLint("CutPasteId")
    private void initializeRecyclerViews(View view) {
        recyclerView = view.findViewById (R.id.recyclerView);
        recyclerView.setLayoutManager (new LinearLayoutManager (getContext ()));
        adapterOfNotCompletedTask = new DailyTaskAdapter (getContext (), tasks, completedTasks);
        recyclerView.setAdapter (adapterOfNotCompletedTask);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper (new SwipeTo (getContext (), tasks, adapterOfNotCompletedTask));
        itemTouchHelper.attachToRecyclerView (recyclerView);
        printNotCheckedTasks (view);

        recyclerViewOfCompletedTasks = view.findViewById (R.id.recyclerView_completed_tasks);
        recyclerViewOfCompletedTasks.setLayoutManager (new LinearLayoutManager (getContext ()));
        adapterOfCompletedTasks = new CompletedDailyTaskAdapter (getContext (), completedTasks, tasks);
        recyclerViewOfCompletedTasks.setAdapter (adapterOfCompletedTasks);
        ItemTouchHelper itemTouchHelperComp = new ItemTouchHelper (new SwipeToComp (getContext (), completedTasks, adapterOfCompletedTasks));
        itemTouchHelperComp.attachToRecyclerView (recyclerViewOfCompletedTasks);
        printCheckedTasks (view);
    }

    private void printNotCheckedTasks(View view) {
        (view.findViewById (R.id.progressBar)).setVisibility (View.VISIBLE);
        tasks.clear ();
        FirebaseUtils.getDailyTaskModel ().whereEqualTo ("checked", false).get ().addOnCompleteListener (task -> {
            if (task.isSuccessful ()) {
                tasks.addAll (task.getResult ().toObjects (TaskModel.class));
                adapterOfNotCompletedTask.notifyDataSetChanged ();
                (view.findViewById (R.id.progressBar)).setVisibility (View.GONE);
            }
        });
    }

    private void printCheckedTasks(View view) {
        (view.findViewById (R.id.progressBar)).setVisibility (View.VISIBLE);
        completedTasks.clear ();
        FirebaseUtils.getDailyTaskModel ().whereEqualTo ("checked", true).get ().addOnCompleteListener (task -> {
            if (task.isSuccessful ()) {
                completedTasks.addAll (task.getResult ().toObjects (TaskModel.class));
                adapterOfCompletedTasks.notifyDataSetChanged ();
                (view.findViewById (R.id.progressBar)).setVisibility (View.GONE);
            }
        });
    }

    private void openBottomSheetPopupToAddTask (View view){
        if (!FirebaseUtils.isUserEmailVerify ()) {
            Toast.makeText (getContext (), "Verify Your Email At Settings.", Toast.LENGTH_SHORT).show ();
            return;
        }
        new createDailyTaskBottomSheet (getContext (), tasks, adapterOfNotCompletedTask).show ();
    }

    //delete daily tasks in 00:00.
    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext ());
        int lastTimeStarted = settings.getInt("last_time_started", -1);
        int today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        if (today != lastTimeStarted) {
            FirebaseUtils.getDailyTaskModel().get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    documentSnapshot.getReference().delete();
                }
            });

            settings.edit().putInt("last_time_started", today).apply();
        }
    }

    private static class SwipeTo extends ItemTouchHelper.SimpleCallback {

        private final Context context;
        private final List<TaskModel> taskModels;
        private final DailyTaskAdapter adapter;

        public SwipeTo(Context context, List<TaskModel> taskModels, DailyTaskAdapter adapter) {
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
                                FirebaseUtils.getDailyTaskModel ().document ().set (taskModel).addOnSuccessListener (unused -> adapter.notifyItemInserted (position));
                            }).show ();
                    break;
                case ItemTouchHelper.RIGHT:
                    new EditDailyTaskBS (context, position, taskModels, adapter).show ();
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
    private static class SwipeToComp extends ItemTouchHelper.SimpleCallback {

        private final Context context;
        private final List<TaskModel> taskModels;
        private final CompletedDailyTaskAdapter adapter;

        public SwipeToComp(Context context, List<TaskModel> taskModels, CompletedDailyTaskAdapter adapter) {
            super (0, ItemTouchHelper.LEFT);
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
                    Snackbar.make (recyclerViewOfCompletedTasks, taskModel.getDescription ().toString (), Snackbar.LENGTH_SHORT)
                            .setAction ("Undo", view -> {
                                taskModels.add (position, taskModel);
                                FirebaseUtils.getDailyTaskModel ().document ().set (taskModel).addOnSuccessListener (unused -> adapter.notifyItemInserted (position));
                            }).show ();
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