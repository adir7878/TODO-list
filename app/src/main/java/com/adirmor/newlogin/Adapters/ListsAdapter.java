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

import com.adirmor.newlogin.Models.ListsModel;
import com.adirmor.newlogin.R;
import com.adirmor.newlogin.Utils.FirebaseUtils;
import com.adirmor.newlogin.inApp.insideFragments.ListDisplayActivity;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;


public class ListsAdapter extends RecyclerView.Adapter<ListsAdapter.HolderView> {

    private Context context;
    private List<ListsModel> listsModelList;

    public ListsAdapter(Context context, List<ListsModel> listsModelList) {
        this.context = context;
        this.listsModelList = listsModelList;
    }

    @NonNull
    @Override
    public HolderView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HolderView (LayoutInflater.from (context).inflate (R.layout.lists_ui_for_recycler_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HolderView holder, int position) {
        holder.description.setText (listsModelList.get (position).getName ());
        holder.date.setText (new SimpleDateFormat ("dd/MM/yyyy", Locale.US).format (listsModelList.get (position).getTime ().toDate ()));

        holder.itemView.setOnClickListener (v -> {
            Intent intent = new Intent (context, ListDisplayActivity.class);
            intent.putExtra ("id", listsModelList.get (position).getId ());
            intent.putExtra ("name", listsModelList.get (position).getName ());
            context.startActivity (intent);
        });
    }

    @Override
    public int getItemCount() {
        return listsModelList.size ();
    }

    public ListsModel deleteList(int position){
        ListsModel listsModel = listsModelList.get (position);

        FirebaseUtils.getSpecificList (listsModel.getId ()).get ().addOnCompleteListener (task -> {
            task.getResult ().getDocuments ().get (0).getReference ().delete ().addOnSuccessListener (unused -> {
                listsModelList.remove (position);
                notifyItemRemoved (position);
            });
        });

        return listsModel;
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
