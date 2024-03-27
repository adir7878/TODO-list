package com.adirmor.newlogin.bottomSheets.edits;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.adirmor.newlogin.Adapters.RoomAdapter;
import com.adirmor.newlogin.Models.RoomModel;
import com.adirmor.newlogin.R;
import com.adirmor.newlogin.Utils.FirebaseUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class EditListNameBS extends BottomSheetDialog {
    @NonNull
    private final List<RoomModel> roomModel;
    private final RoomAdapter adapter;
    private final int adapterPosition;
    private final TextInputEditText textInputEditText;

    public EditListNameBS(@NonNull Context context, List<RoomModel> roomModel, RoomAdapter adapter, int adapterPosition) {
        super (context);
        this.roomModel = roomModel;

        this.adapter = adapter;
        this.adapterPosition = adapterPosition;

        View view = LayoutInflater.from (context).inflate (R.layout.edit_list_name, null);
        setContentView (view);

        textInputEditText = view.findViewById (R.id.list_new_description);
        textInputEditText.setText (roomModel.get (adapterPosition).getName ().toString ());
        textInputEditText.requestFocus ();

        Button edit = view.findViewById (R.id.edit_lists_name);
        edit.setOnClickListener (this::editName);
    }

    private void editName(View view) {
        if(textInputEditText.getText ().toString ().isEmpty ())
            return;

        FirebaseUtils.getSpecificRoom (roomModel.get (adapterPosition).getId ()).get ().addOnCompleteListener (task -> {
            roomModel.get (adapterPosition).setName (textInputEditText.getText ().toString ()); // set the name of the list in the exact position.
            task.getResult ().getReference ().update ("name", roomModel.get (adapterPosition).getName ())
                    .addOnSuccessListener (unused -> {
                        adapter.notifyItemChanged (adapterPosition);
                    });
        });
        dismiss ();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setOnDismissListener(dialogInterface -> {
            adapter.notifyItemChanged (adapterPosition);
        });
    }
}
