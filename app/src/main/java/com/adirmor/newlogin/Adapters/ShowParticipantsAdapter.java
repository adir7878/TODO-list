package com.adirmor.newlogin.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.adirmor.newlogin.Models.RoomModel;
import com.adirmor.newlogin.Models.UserModel;
import com.adirmor.newlogin.R;
import com.adirmor.newlogin.Utils.FirebaseUtils;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;

public class ShowParticipantsAdapter extends FirestoreRecyclerAdapter<UserModel, ShowParticipantsAdapter.ViewHolder> {

    private final Context context;
    private final String roomID;

    public ShowParticipantsAdapter(@NonNull FirestoreRecyclerOptions<UserModel> options, Context context, String roomID) {
        super(options);
        this.context = context;
        this.roomID = roomID;
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull UserModel model) {
        // Bind user data to views
        holder.bind(model, roomID);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.participant_ui_for_recycler_view, parent, false);
        return new ViewHolder(view, roomID);
    }


    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {

        ShapeableImageView profilePic;
        TextView Username;
        ImageButton kick, block;
        String roomID;

        public ViewHolder(@NonNull View itemView, String roomID) {
            super(itemView);
            this.roomID = roomID;
            // Initialize views
            profilePic = itemView.findViewById(R.id.ProfileImage_ShowParticipants);
            Username = itemView.findViewById(R.id.participant_username);
            kick = itemView.findViewById(R.id.kick_from_room_button);
            block = itemView.findViewById(R.id.block_from_room_button);
        }

        // Method to bind user data to views
        public void bind(UserModel model, String roomID) {
            if (model.getId().equals(FirebaseUtils.getCurrentUserId())) {
                Username.setText (model.getUsername () + " (Me)");
                kick.setVisibility (View.GONE);
                block.setVisibility (View.GONE);
            }
            else
                Username.setText(model.getUsername());

            // Load profile picture using Picasso library
            FirebaseUtils.getProfilePictureReference(model.getId()).getDownloadUrl().addOnSuccessListener(uri -> {
                Picasso.get().load(uri).into(profilePic);
            }).addOnFailureListener(e -> {});

            // Kick user from the room
            kick.setOnClickListener(view -> {
                FirebaseUtils.getSpecificRoom(roomID).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        RoomModel roomModel = task.getResult().toObject(RoomModel.class);
                        roomModel.getParticipantIDs().remove(model.getId());
                        task.getResult().getReference().update("participantIDs", roomModel.getParticipantIDs());
                    }
                });
                FirebaseUtils.getUserModelOfRoomCollection(roomID).document (model.getId ()).get ().addOnCompleteListener (task -> {
                    task.getResult().getReference().delete();
                });
            });

            // Block user from the room
            block.setOnClickListener(view -> {
                FirebaseUtils.getSpecificRoom(roomID).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        RoomModel roomModel = task.getResult().toObject(RoomModel.class);

                        roomModel.getParticipantIDs().remove(model.getId());
                        task.getResult().getReference().update("participantIDs", roomModel.getParticipantIDs());

                        roomModel.getBlockedUsersID().add(model.getId());
                        task.getResult().getReference().update("blockedUsersID", roomModel.getBlockedUsersID());
                    }
                });
                FirebaseUtils.getUserModelOfRoomCollection(roomID).document (model.getId ()).get ().addOnCompleteListener (task -> {
                    task.getResult().getReference().delete();
                });
            });
        }
    }
}
