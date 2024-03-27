package com.adirmor.newlogin.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.adirmor.newlogin.Models.RoomModel;
import com.adirmor.newlogin.R;
import com.adirmor.newlogin.Utils.FirebaseUtils;
import com.adirmor.newlogin.Utils.FunctionsUtils;
import com.adirmor.newlogin.inApp.insideFragments.RoomDisplayActivity;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.HolderView> {

    private Context context;
    private final List<RoomModel> roomModels;

    public RoomAdapter(Context context, List<RoomModel> roomModels) {
        this.context = context;
        this.roomModels = roomModels;
    }

    @NonNull
    @Override
    public HolderView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HolderView(LayoutInflater.from(context).inflate(R.layout.lists_ui_for_recycler_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HolderView holder, int position) {
        // Bind data to views
        holder.bind(roomModels.get(position));
    }

    @Override
    public int getItemCount() {
        return roomModels.size();
    }

    // Method to delete a room
    public RoomModel deleteList(int position) {

        RoomModel roomModel = roomModels.get(position);
        roomModels.remove(position);
        notifyItemRemoved(position);

        // Check if the host deleted the room
        if (roomModel.getHostId().equals(FirebaseUtils.getCurrentUserId())) {
            FirebaseUtils.getSpecificRoom(roomModel.getId()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    task.getResult().getReference().delete();
                    FunctionsUtils.deleteUserFromRoomBySwiping (roomModel.getId ());
                }
            });
        } else {
            // If user deleted the room
            FirebaseUtils.getSpecificRoom(roomModel.getId()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    AlertDialog.Builder ExitRoom = new AlertDialog.Builder (context);
                    ExitRoom.setTitle ("Exit Room");
                    ExitRoom.setMessage ("Are you sure?");

                    ExitRoom.setPositiveButton ("Confirm", (dialogInterface, i) -> {
                        RoomModel roomModelRemoveUser = task.getResult().toObject(RoomModel.class);
                        roomModelRemoveUser.getParticipantIDs().remove(FirebaseUtils.getCurrentUserId());
                        task.getResult().getReference().update("participantIDs", roomModelRemoveUser.getParticipantIDs());
                        FunctionsUtils.deleteUserFromRoomBySwiping (FirebaseUtils.getCurrentUserId ());
                    });
                    ExitRoom.setOnDismissListener (dialogInterface -> {
                       notifyItemChanged (position);
                    });
                    ExitRoom.show ();
                }
            });
        }
        return roomModel;
    }

    // ViewHolder class
    public static class HolderView extends RecyclerView.ViewHolder {

        ImageView menu;
        TextView description, date;

        public HolderView(@NonNull View itemView) {
            super(itemView);
            menu = itemView.findViewById(R.id.tasks_list_icon);
            description = itemView.findViewById(R.id.list_tasks_description);
            date = itemView.findViewById(R.id.date_display_list_tasks);
        }

        // Bind data to views
        public void bind(RoomModel roomModel) {
            description.setText(roomModel.getName());
            date.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.US).format(roomModel.getTime().toDate()));

            // Add click listener to the item view
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), RoomDisplayActivity.class);
                intent.putExtra("id", roomModel.getId());
                intent.putExtra("name", roomModel.getName());
                intent.putExtra("hostID", roomModel.getHostId());
                intent.putExtra("roomCode", roomModel.getCode());
                itemView.getContext().startActivity(intent);
            });
        }
    }
}
