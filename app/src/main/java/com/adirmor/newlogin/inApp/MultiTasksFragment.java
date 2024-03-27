package com.adirmor.newlogin.inApp;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.adirmor.newlogin.Adapters.RoomAdapter;
import com.adirmor.newlogin.Adapters.ShowParticipantsAdapter;
import com.adirmor.newlogin.Models.RoomModel;
import com.adirmor.newlogin.Models.UserModel;
import com.adirmor.newlogin.R;
import com.adirmor.newlogin.Utils.FirebaseUtils;
import com.adirmor.newlogin.bottomSheets.dialog.JoinRoomDialog;
import com.adirmor.newlogin.bottomSheets.edits.EditListNameBS;
import com.adirmor.newlogin.bottomSheets.creates.createRoomBottomSheet;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MultiTasksFragment extends Fragment {

    List<RoomModel> roomModels;
    private RecyclerView recyclerView;
    private RoomAdapter adapter;


    public MultiTasksFragment(){
        roomModels = new ArrayList<> ();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate (R.layout.fragment_multi_tasks, container, false);

        recyclerView = view.findViewById (R.id.lists_recycler_view);
        recyclerView.setLayoutManager (new LinearLayoutManager (getContext ()));
        adapter = new RoomAdapter(getContext (), roomModels);
        recyclerView.setAdapter (adapter);

        printTasks ();

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper (new SwipeTo (getContext (), roomModels, adapter));
        itemTouchHelper.attachToRecyclerView (recyclerView);

        FloatingActionButton floatingActionButton = view.findViewById (R.id.create_list_floating_button);
        floatingActionButton.setOnClickListener (this::openCreateListBottomSheet);

        Button button = view.findViewById (R.id.join_room_open_dialog);
        button.setOnClickListener (this::openJoinRoomDialog);

        return view;
    }

    void printTasks(){
        roomModels.clear ();
        FirebaseUtils.getRoomsCollection ().whereArrayContains ("participantIDs", FirebaseUtils.getCurrentUserId ()).addSnapshotListener ((value, error) -> {
            if (error != null) {
                Log.w(TAG, "Listen failed.", error);
                return;
            }

            roomModels.clear ();
            roomModels.addAll (value.toObjects (RoomModel.class));
            adapter.notifyDataSetChanged ();

        });
    }

    private void openJoinRoomDialog(View view) {
        if (!FirebaseUtils.isUserEmailVerify ()) {
            Toast.makeText (getContext (), "Verify Your Email At Settings.", Toast.LENGTH_SHORT).show ();
            return;
        }
        new JoinRoomDialog (getContext (), adapter, roomModels).show();
    }

    private void openCreateListBottomSheet(View view) {
        if (!FirebaseUtils.isUserEmailVerify ()) {
            Toast.makeText (getContext (), "Verify Your Email At Settings.", Toast.LENGTH_SHORT).show ();
            return;
        }
        new createRoomBottomSheet (getContext (), adapter, roomModels).show ();
    }

    public class SwipeTo extends ItemTouchHelper.SimpleCallback{

        private final Context context;
        private final List<RoomModel> models;
        private final RoomAdapter adapter;

        public SwipeTo(Context context, List<RoomModel> models, RoomAdapter adapter) {
            super (0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT);
            this.context = context;
            this.models = models;
            this.adapter = adapter;
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
                    RoomModel roomModel = adapter.deleteList (position);
                    if(!roomModel.getHostId ().equals (FirebaseUtils.getCurrentUserId ()))
                        break;
                    Snackbar.make (recyclerView, roomModel.getName ().toString (), Snackbar.LENGTH_LONG)
                            .setAction ("Undo", view -> {
                                roomModels.add (position, roomModel);
                                adapter.notifyItemInserted (position);
                                FirebaseUtils.getRoomsCollection ().document (roomModel.getId ()).set (roomModel);
                            }).show ();
                    break;
                case ItemTouchHelper.RIGHT:
                    if(!roomModels.get (viewHolder.getAdapterPosition ()).getHostId ().equals (FirebaseUtils.getCurrentUserId ()))
                        break;
                    new EditListNameBS (context, models, adapter, position).show ();
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