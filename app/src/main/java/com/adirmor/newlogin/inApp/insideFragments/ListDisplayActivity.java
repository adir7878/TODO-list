package com.adirmor.newlogin.inApp.insideFragments;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.adirmor.newlogin.Adapters.TasksOfListAdapter;
import com.adirmor.newlogin.Models.ListsModel;
import com.adirmor.newlogin.Models.TaskOfListModel;
import com.adirmor.newlogin.R;
import com.adirmor.newlogin.Utils.FirebaseUtils;
import com.adirmor.newlogin.bottomSheets.edits.EditTaskOfListBS;
import com.adirmor.newlogin.bottomSheets.creates.createTaskForListBottomSheet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class ListDisplayActivity extends AppCompatActivity {

    private List<TaskOfListModel> tasks;
    private TasksOfListAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_list_displayer);

        String roomID = getIntent ().getExtras ().getString ("id");
        String roomName = getIntent ().getExtras ().getString ("name");

        tasks = new ArrayList<> ();
        recyclerView = findViewById (R.id.recycler_view_tasks_of_list);
        recyclerView.setLayoutManager (new LinearLayoutManager (this));
        adapter = new TasksOfListAdapter (this, tasks, roomID);
        recyclerView.setAdapter (adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper (new SwipeTo (this, tasks, adapter, roomID));
        itemTouchHelper.attachToRecyclerView (recyclerView);

        printTasks (roomID);

        TextView title = findViewById (R.id.list_title_name);
        title.setText(roomName);

        ImageView backButton = findViewById (R.id.backButton);
        backButton.setOnClickListener (view -> {
           finish ();
        });

        FloatingActionButton addButton = findViewById (R.id.add_task_to_list);
        addButton.setOnClickListener (view -> {
            new createTaskForListBottomSheet(this, tasks, adapter, roomID).show ();
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void printTasks(String roomID){
        tasks.clear ();
        FirebaseUtils.getSpecificList (roomID).get ().addOnCompleteListener (task -> {
            if(task.isSuccessful ()){
                List<ListsModel> listsModelList = task.getResult ().toObjects (ListsModel.class);
                tasks.addAll (listsModelList.get (0).getList ());
                adapter.notifyDataSetChanged ();
            }
        });
    }
    public class SwipeTo extends ItemTouchHelper.SimpleCallback{

        private final Context context;
        private final List<TaskOfListModel> taskModels;
        private final TasksOfListAdapter adapter;
        private final String id;

        public SwipeTo(Context context, List<TaskOfListModel> taskModels, TasksOfListAdapter adapter, String id) {
            super (0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT);
            this.context = context;
            this.taskModels = taskModels;
            this.adapter = adapter;
            this.id = id;
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @SuppressLint("ShowToast")
        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            int position = viewHolder.getAdapterPosition ();

            switch (direction){
                case ItemTouchHelper.LEFT:
                    TaskOfListModel taskOfListModel = adapter.deleteTask (position);
                    Snackbar.make (recyclerView, taskOfListModel.getDescription ().toString (), Snackbar.LENGTH_LONG)
                            .setAction ("Undo", view -> {
                                taskModels.add (position, taskOfListModel);
                                FirebaseUtils.getSpecificList (id).get ().addOnCompleteListener (task -> {
                                    ListsModel listsModel = task.getResult ().toObjects (ListsModel.class).get (0);
                                    listsModel.setList (taskModels);
                                    task.getResult ().getDocuments ().get (0).getReference ().set (listsModel).addOnSuccessListener (unused -> {
                                        adapter.notifyItemInserted (position);
                                    });
                                });
                            }).show ();

                    break;
                case ItemTouchHelper.RIGHT:
                    new EditTaskOfListBS (context, position, taskModels, adapter, id).show ();
                    break;
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor (ContextCompat.getColor(context, R.color.lightRed))
                    .addSwipeLeftActionIcon (R.drawable.baseline_delete_24)
                    .addSwipeLeftLabel ("Delete")
                    .addSwipeRightBackgroundColor (ContextCompat.getColor(context, R.color.lightGreen))
                    .addSwipeRightActionIcon (R.drawable.edit_ic)
                    .addSwipeRightLabel ("Edit")
                    .create()
                    .decorate();

            super.onChildDraw (c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }

}