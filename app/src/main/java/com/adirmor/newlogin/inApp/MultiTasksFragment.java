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

import com.adirmor.newlogin.Adapters.ListsAdapter;
import com.adirmor.newlogin.Models.ListsModel;
import com.adirmor.newlogin.R;
import com.adirmor.newlogin.Utils.FirebaseUtils;
import com.adirmor.newlogin.bottomSheets.edits.EditListNameBS;
import com.adirmor.newlogin.bottomSheets.creates.createListBottomSheet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MultiTasksFragment extends Fragment {

    private RecyclerView recyclerView;
    private ListsAdapter adapter;
    private List<ListsModel> listsModelList;


    public MultiTasksFragment(){
        listsModelList = new ArrayList<> ();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate (R.layout.fragment_multi_tasks, container, false);

        recyclerView = view.findViewById (R.id.lists_recycler_view);
        recyclerView.setLayoutManager (new LinearLayoutManager (getContext ()));
        adapter = new ListsAdapter (getContext (), listsModelList);
        recyclerView.setAdapter (adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper (new SwipeTo (getContext (), listsModelList, adapter));
        itemTouchHelper.attachToRecyclerView (recyclerView);

        printListsOfTasks ();

        FloatingActionButton floatingActionButton = view.findViewById (R.id.create_list_floating_button);
        floatingActionButton.setOnClickListener (this::openCreateListBottomSheet);

        return view;
    }

    private void openCreateListBottomSheet(View view) {
        new createListBottomSheet (getContext (), adapter, listsModelList).show ();
    }

    private void printListsOfTasks(){
        FirebaseUtils.getListsCollection ().get ().addOnCompleteListener (new OnCompleteListener<QuerySnapshot> () {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful ()){
                    listsModelList.clear ();
                    List<ListsModel> listsModel = task.getResult ().toObjects (ListsModel.class);
                    listsModelList.addAll(listsModel);
                    adapter.notifyDataSetChanged ();
                }
            }
        });
    }

    public class SwipeTo extends ItemTouchHelper.SimpleCallback{

        private final Context context;
        private final List<ListsModel> listsModelList;
        private final ListsAdapter adapter;

        public SwipeTo(Context context, List<ListsModel> listsModelList, ListsAdapter adapter) {
            super (0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT);
            this.context = context;
            this.listsModelList = listsModelList;
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
                    ListsModel listsModel = adapter.deleteList (position);
                    Snackbar.make (recyclerView, listsModel.getName ().toString (), Snackbar.LENGTH_LONG)
                            .setAction ("Undo", view -> {
                                listsModelList.add (position, listsModel);
                                FirebaseUtils.getListsCollection ().document ().set (listsModel).addOnSuccessListener (unused -> {
                                    adapter.notifyItemInserted (position);
                                });
                            }).show ();
                    break;
                case ItemTouchHelper.RIGHT:
                    new EditListNameBS (context, listsModelList, adapter, position).show ();
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