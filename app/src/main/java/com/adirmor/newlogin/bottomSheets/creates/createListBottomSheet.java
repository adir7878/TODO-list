package com.adirmor.newlogin.bottomSheets.creates;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import androidx.annotation.NonNull;

import com.adirmor.newlogin.Adapters.RoomAdapter;
import com.adirmor.newlogin.Models.RoomModel;
import com.adirmor.newlogin.Models.UserModel;
import com.adirmor.newlogin.R;
import com.adirmor.newlogin.Utils.FirebaseUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class createListBottomSheet extends BottomSheetDialog {

    private TextInputEditText Description;
    private DatePicker datePicker;
    private Button create;
    private RoomAdapter adapter;
    private List<RoomModel> roomModelList;

    public createListBottomSheet(@NonNull Context context, RoomAdapter adapter, List<RoomModel> roomModelList) {
        super (context);
        View view  = LayoutInflater.from (context).inflate (R.layout.add_list_of_tasks, null);
        setContentView (view);
        getWindow ().getAttributes ().windowAnimations = R.style.DialogAnimation;

        Description = view.findViewById (R.id.list_name);
        datePicker = view.findViewById (R.id.DatePickerForList);
        datePicker.setMinDate (Calendar.getInstance ().getTimeInMillis ());
        create = view.findViewById (R.id.createList);
        this.adapter = adapter;
        this.roomModelList = roomModelList;
        
        create.setOnClickListener (this::createList);

    }

    private void createList(View view) {
        if(Description.getText ().toString ().isEmpty ())
            return;
        RoomModel roomModel = new RoomModel (Description.getText ().toString (), convertToFirebaseTimestamp (datePicker));
        FirebaseUtils.getRoomsCollection ().add (roomModel).addOnSuccessListener (unused -> {
           FirebaseUtils.getUserModel ().get ().addOnCompleteListener (task -> {
               if (task.isSuccessful ()){
                   UserModel userModel = task.getResult ().toObject (UserModel.class);
                   userModel.getRoomIDs ().add (roomModel.getId ());
                   task.getResult ().getReference ().update ("roomIDs", userModel.getRoomIDs ());
               }
           });
            roomModelList.add (roomModel);
            adapter.notifyItemInserted (roomModelList.size ());
        });
        dismiss ();
    }
    public static Timestamp convertToFirebaseTimestamp(DatePicker datePicker) {
        // Extracting the selected date from the DatePicker
        int year = datePicker.getYear();
        int month = datePicker.getMonth();
        int dayOfMonth = datePicker.getDayOfMonth();

        // Creating a Calendar instance and setting the selected date
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);

        // Getting the time in milliseconds from the Calendar instance
        long selectedDateInMillis = calendar.getTimeInMillis();

        // Creating a Firebase Timestamp object from the selected date

        return new Timestamp(new Date (selectedDateInMillis));
    }

}
