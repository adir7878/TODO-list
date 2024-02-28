package com.adirmor.newlogin.inApp;

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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.adirmor.newlogin.Adapters.RoomAdapter;
import com.adirmor.newlogin.Models.RoomModel;
import com.adirmor.newlogin.Models.UserModel;
import com.adirmor.newlogin.R;
import com.adirmor.newlogin.Utils.FirebaseUtils;
import com.adirmor.newlogin.bottomSheets.dialog.JoinRoomDialog;
import com.adirmor.newlogin.bottomSheets.edits.EditListNameBS;
import com.adirmor.newlogin.bottomSheets.creates.createListBottomSheet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MultiTasksFragment extends Fragment {

    private RecyclerView recyclerView;
    private RoomAdapter adapter;
    private List<RoomModel> roomModelList;


    public MultiTasksFragment(){
        roomModelList = new ArrayList<> ();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate (R.layout.fragment_multi_tasks, container, false);

        recyclerView = view.findViewById (R.id.lists_recycler_view);
        recyclerView.setLayoutManager (new LinearLayoutManager (getContext ()));
        adapter = new RoomAdapter (getContext (), roomModelList);
        recyclerView.setAdapter (adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper (new SwipeTo (getContext (), roomModelList, adapter));
        itemTouchHelper.attachToRecyclerView (recyclerView);

        printListsOfTasks (view);

        FloatingActionButton floatingActionButton = view.findViewById (R.id.create_list_floating_button);
        floatingActionButton.setOnClickListener (this::openCreateListBottomSheet);

        Button button = view.findViewById (R.id.join_room_open_dialog);
        button.setOnClickListener (this::openJoinRoomDialog);

        return view;
    }

    private void openJoinRoomDialog(View view) {
        new JoinRoomDialog (getContext (), roomModelList, adapter).show();
    }

    private void openCreateListBottomSheet(View view) {
        new createListBottomSheet (getContext (), adapter, roomModelList).show ();
    }

    private void printListsOfTasks(View view){
        (view.findViewById (R.id.progressBar)).setVisibility (View.VISIBLE);
        roomModelList.clear ();

        FirebaseUtils.getRoomsCollection ().whereArrayContains ("participantIDs", FirebaseUtils.getCurrentUserId ())
                .get ().addOnCompleteListener (task -> {
                    if(task.isSuccessful ()){
                        List<RoomModel> roomModels = task.getResult ().toObjects (RoomModel.class);
                        roomModelList.addAll (roomModels);
                        adapter.notifyDataSetChanged ();
                    }

                    (view.findViewById (R.id.progressBar)).setVisibility (View.GONE);
                });

/*
        FirebaseUtils.getUserModel ().get ().addOnCompleteListener (task -> {
            if(task.isSuccessful ()) {
                UserModel userModel = task.getResult ().toObject (UserModel.class);
                for (int i = 0; i < userModel.getRoomIDs ().size (); i++) {
                    FirebaseUtils.getRoomsCollection ().whereEqualTo ("id", userModel.getRoomIDs ().get (i)).get ().addOnCompleteListener (task1 -> {
                        if (task1.isSuccessful ()) {
                            RoomModel roomModel = task1.getResult ().toObjects (RoomModel.class).get (0);
                            roomModelList.add (roomModel);
                            adapter.notifyItemInserted (roomModelList.size ());
                        }
                    });
                }
            }
            (view.findViewById (R.id.progressBar)).setVisibility (View.GONE);
        });
*/
    }

    public class SwipeTo extends ItemTouchHelper.SimpleCallback{

        private final Context context;
        private final List<RoomModel> roomModelList;
        private final RoomAdapter adapter;

        public SwipeTo(Context context, List<RoomModel> roomModelList, RoomAdapter adapter) {
            super (0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT);
            this.context = context;
            this.roomModelList = roomModelList;
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
                    Snackbar.make (recyclerView, roomModel.getName ().toString (), Snackbar.LENGTH_LONG)
                            .setAction ("Undo", view -> {
                                roomModelList.add (position, roomModel);
                                FirebaseUtils.getRoomsCollection ().document ().set (roomModel).addOnSuccessListener (unused -> {
                                    FirebaseUtils.getUserModel ().get ().addOnCompleteListener (task1 -> {
                                        UserModel userModel = task1.getResult ().toObject (UserModel.class);
                                        userModel.getRoomIDs ().add (roomModel.getId ());
                                        task1.getResult ().getReference ().update ("roomIDs", userModel.getRoomIDs ());
                                    });
                                    adapter.notifyItemInserted (position);
                                });
                            }).show ();
                    break;
                case ItemTouchHelper.RIGHT:
                    new EditListNameBS (context, roomModelList, adapter, position).show ();
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