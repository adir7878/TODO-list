package com.adirmor.newlogin.inApp.insideFragments;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.adirmor.newlogin.Adapters.TasksOfRoomAdapter;
import com.adirmor.newlogin.Models.RoomModel;
import com.adirmor.newlogin.Models.TaskOfRoomModel;
import com.adirmor.newlogin.Models.UserModel;
import com.adirmor.newlogin.R;
import com.adirmor.newlogin.Utils.FirebaseUtils;
import com.adirmor.newlogin.bottomSheets.edits.EditTaskOfListBS;
import com.adirmor.newlogin.bottomSheets.creates.createTaskForListBottomSheet;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class ListDisplayActivity extends AppCompatActivity {

    private List<TaskOfRoomModel> tasks;
    private TasksOfRoomAdapter adapter;
    private RecyclerView recyclerView;
    private boolean isAdapterSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_list_displayer);

        TextView showParticipants = findViewById (R.id.show_participants_only_to_host);
        TextView showCode = findViewById (R.id.show_code_for_every_one);
        recyclerView = findViewById(R.id.recycler_view_tasks_of_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext ()));

        String roomID = getIntent ().getExtras ().getString ("id");
        String roomName = getIntent ().getExtras ().getString ("name");
        String hostID = getIntent ().getExtras ().getString ("hostID");
        String roomCode = getIntent ().getExtras ().getString ("roomCode");

        if(!hostID.equals (FirebaseUtils.getCurrentUserId ()))
            showParticipants.setVisibility (View.GONE);
        showCode.setText(roomCode);

        tasks = new ArrayList<> ();
        printTasks (roomID);

        TextView title = findViewById (R.id.list_title_name);
        title.setText(roomName);

        ImageView backButton = findViewById (R.id.backButton);
        backButton.setOnClickListener (view -> {
           finish ();
        });
        showCode.setOnClickListener (view -> {
            ClipboardManager clipboard = (ClipboardManager) this.getSystemService (Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", showCode.getText ().toString ());
            clipboard.setPrimaryClip(clip);
            Toast.makeText (getApplicationContext (), "Code Copied.", Toast.LENGTH_SHORT).show ();
        });

        FloatingActionButton addButton = findViewById (R.id.add_task_to_list);
        addButton.setOnClickListener (view -> {
            new createTaskForListBottomSheet(this, tasks, adapter, roomID).show ();
        });
    }

    void printTasks(String RoomID){
        FirebaseUtils.getTasksOfRoomCollection (RoomID, collectionReference -> {
            if(collectionReference == null)
                return;
            collectionReference.get ().addOnCompleteListener (task -> {
               List<TaskOfRoomModel> taskOfRoomModels = task.getResult ().toObjects (TaskOfRoomModel.class);
               tasks.addAll (taskOfRoomModels);
            });
        });
    }

    public class SwipeTo extends ItemTouchHelper.SimpleCallback{

        private final Context context;
        private final List<TaskOfRoomModel> taskModels;
        private final TasksOfRoomAdapter adapter;
        private final String id;

        public SwipeTo(Context context, List<TaskOfRoomModel> taskModels, TasksOfRoomAdapter adapter, String id) {
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
                    TaskOfRoomModel taskOfRoomModel = adapter.getItem (position);
                    adapter.deleteTask (taskOfRoomModel);
                    Snackbar.make (recyclerView, taskOfRoomModel.getDescription ().toString (), Snackbar.LENGTH_LONG)
                            .setAction ("Undo", view -> {
                                FirebaseUtils.getTasksOfRoomCollection (id, collectionReference -> {
                                    if(collectionReference == null)
                                        return;
                                    collectionReference.add (taskOfRoomModel);
                                });
                            }).show ();

                    break;
                case ItemTouchHelper.RIGHT:
                    new EditTaskOfListBS (context, position, adapter.getItem (position), id, adapter).show ();
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
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

        }


    @Override
    protected void onStart() {
        super.onStart ();
        String roomID = getIntent ().getExtras ().getString ("id");
        FirebaseUtils.getTasksOfRoomCollection(roomID, collectionReference -> {
            if (collectionReference != null) {
                adapter = new TasksOfRoomAdapter(new FirestoreRecyclerOptions.Builder<TaskOfRoomModel>()
                        .setQuery(collectionReference.orderBy("creationTime", Query.Direction.DESCENDING), TaskOfRoomModel.class).build(), this, roomID);
                recyclerView.setAdapter(adapter);

                adapter.startListening ();
                isAdapterSet = true;

                ItemTouchHelper itemTouchHelper = new ItemTouchHelper (new SwipeTo (this, tasks, adapter, roomID));
                itemTouchHelper.attachToRecyclerView (recyclerView);

                Log.e(TAG, "Collection reference is found. roomID: " + roomID);
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop ();
        if(adapter != null)
            adapter.stopListening ();
    }

    @Override
    protected void onResume() {
        super.onResume ();
        if(adapter != null && !isAdapterSet) {
            adapter.startListening ();
            isAdapterSet = true;
        }
    }
}