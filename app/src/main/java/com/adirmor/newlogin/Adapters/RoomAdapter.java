package com.adirmor.newlogin.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.adirmor.newlogin.Models.RoomModel;
import com.adirmor.newlogin.Models.UserModel;
import com.adirmor.newlogin.R;
import com.adirmor.newlogin.Utils.FirebaseUtils;
import com.adirmor.newlogin.inApp.insideFragments.ListDisplayActivity;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;


public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.HolderView> {

    private Context context;
    private List<RoomModel> roomModelList;

    public RoomAdapter(Context context, List<RoomModel> roomModelList) {
        this.context = context;
        this.roomModelList = roomModelList;
    }

    @NonNull
    @Override
    public HolderView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HolderView (LayoutInflater.from (context).inflate (R.layout.lists_ui_for_recycler_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HolderView holder, int position) {
        holder.description.setText (roomModelList.get (position).getName ());
        holder.date.setText (new SimpleDateFormat ("dd/MM/yyyy", Locale.US).format (roomModelList.get (position).getTime ().toDate ()));

        holder.itemView.setOnClickListener (v -> {
            Intent intent = new Intent (context, ListDisplayActivity.class);
            intent.putExtra ("id", roomModelList.get (position).getId ());
            intent.putExtra ("name", roomModelList.get (position).getName ());
            context.startActivity (intent);
        });
    }

    @Override
    public int getItemCount() {
        return roomModelList.size ();
    }

    public RoomModel deleteList(int position){
        RoomModel roomModel = roomModelList.get (position);

        roomModelList.remove (position);
        notifyItemRemoved (position);

        FirebaseUtils.getSpecificRoom (roomModel.getId ()).get ().addOnCompleteListener (task -> {
            task.getResult ().getDocuments ().get (0).getReference ().delete ().addOnSuccessListener (unused -> {
                FirebaseUtils.getUserModel ().get ().addOnCompleteListener (task1 -> {
                    UserModel userModel = task1.getResult ().toObject (UserModel.class);
                    userModel.getRoomIDs ().remove (roomModel.getId ());
                    task1.getResult ().getReference ().update ("roomIDs", userModel.getRoomIDs ());
                });
            });
        });

        return roomModel;
    }

    public static class HolderView extends RecyclerView.ViewHolder{

        ImageView menu;
        TextView description, date;

        public HolderView(@NonNull View itemView) {
            super (itemView);
            menu = itemView.findViewById (R.id.tasks_list_icon);
            description = itemView.findViewById (R.id.list_tasks_description);
            date = itemView.findViewById (R.id.date_display_list_tasks);
        }
    }
}
