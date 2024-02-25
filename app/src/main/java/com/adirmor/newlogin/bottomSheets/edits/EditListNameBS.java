package com.adirmor.newlogin.bottomSheets.edits;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.adirmor.newlogin.Adapters.ListsAdapter;
import com.adirmor.newlogin.Models.ListsModel;
import com.adirmor.newlogin.R;
import com.adirmor.newlogin.Utils.FirebaseUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class EditListNameBS extends BottomSheetDialog {

    @NonNull
    private final List<ListsModel> listsModel;
    private final ListsAdapter adapter;
    private final int adapterPosition;
    private TextInputEditText textInputEditText;
    private Button edit;
    public EditListNameBS(@NonNull Context context, List<ListsModel> listsModel, ListsAdapter adapter, int adapterPosition) {
        super (context);

        this.listsModel = listsModel;
        this.adapter = adapter;
        this.adapterPosition = adapterPosition;

        View view = LayoutInflater.from (context).inflate (R.layout.edit_list_name, null);
        setContentView (view);

        textInputEditText = view.findViewById (R.id.list_new_description);
        textInputEditText.setText (listsModel.get (adapterPosition).getName ().toString ());
        textInputEditText.requestFocus ();

        edit = view.findViewById (R.id.edit_lists_name);
        edit.setOnClickListener (this::editName);
    }

    private void editName(View view) {
        if(textInputEditText.getText ().toString ().isEmpty ())
            return;

        FirebaseUtils.getSpecificList (listsModel.get (adapterPosition).getId ()).get ().addOnCompleteListener (task -> {
            listsModel.get (adapterPosition).setName (textInputEditText.getText ().toString ()); // set the name of the list in the exact position.
            task.getResult ().getDocuments ().get (0).getReference ().set (listsModel.get (adapterPosition)).addOnSuccessListener (unused -> {
                adapter.notifyItemChanged (adapterPosition); // notify in the adapter
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
