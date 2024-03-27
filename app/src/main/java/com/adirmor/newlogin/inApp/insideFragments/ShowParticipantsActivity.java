package com.adirmor.newlogin.inApp.insideFragments;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.adirmor.newlogin.Adapters.ShowParticipantsAdapter;
import com.adirmor.newlogin.Adapters.TasksOfRoomAdapter;
import com.adirmor.newlogin.Models.TaskOfRoomModel;
import com.adirmor.newlogin.Models.UserModel;
import com.adirmor.newlogin.R;
import com.adirmor.newlogin.Utils.FirebaseUtils;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

public class ShowParticipantsActivity extends AppCompatActivity {

    private ShowParticipantsAdapter adapter;
    private RecyclerView recyclerView;
    private boolean isAdapterSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_show_participants);

        recyclerView = findViewById (R.id.recycler_view);
        recyclerView.setLayoutManager (new LinearLayoutManager (this));

        String roomID = getIntent ().getExtras ().getString ("roomID");
        String roomName = getIntent ().getExtras ().getString ("roomName");

        adapter = new ShowParticipantsAdapter (new FirestoreRecyclerOptions.Builder<UserModel>()
                .setQuery(FirebaseUtils.getUserModelOfRoomCollection(roomID).orderBy ("id", Query.Direction.DESCENDING), UserModel.class).build(), getApplicationContext (), roomID);
        recyclerView.setAdapter(adapter);

        adapter.startListening ();
        isAdapterSet = true;
        Log.e(TAG, "Collection reference is found. roomID: " + roomID);

        TextView title = findViewById (R.id.room_name_participants_page);
        title.setText(roomName);

        ImageView backButton = findViewById (R.id.backButton);
        backButton.setOnClickListener (view -> {
            finish ();
        });
    }

    @Override
    protected void onStart() {
        super.onStart ();
        String roomID = getIntent ().getExtras ().getString ("id");


    }

    @Override
    public void onStop() {
        super.onStop ();
        if(adapter != null && isAdapterSet) {
            adapter.stopListening ();
            isAdapterSet = false;
        }
    }
}