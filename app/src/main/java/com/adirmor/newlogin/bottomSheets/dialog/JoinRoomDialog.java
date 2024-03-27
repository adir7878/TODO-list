package com.adirmor.newlogin.bottomSheets.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.adirmor.newlogin.Adapters.RoomAdapter;
import com.adirmor.newlogin.Models.RoomModel;
import com.adirmor.newlogin.Models.UserModel;
import com.adirmor.newlogin.R;
import com.adirmor.newlogin.Utils.FirebaseUtils;
import com.adirmor.newlogin.Utils.FunctionsUtils;

import java.util.List;

public class JoinRoomDialog extends Dialog {
    private final EditText editTextCode;
    private final RoomAdapter adapter;
    private final List<RoomModel> roomModels;

    public JoinRoomDialog(@NonNull Context context, RoomAdapter adapter, List<RoomModel> roomModels) {
        super (context);
        this.adapter = adapter;
        this.roomModels = roomModels;
        View view = LayoutInflater.from (context).inflate (R.layout.enter_room_code_dialog, null);
        setContentView (view);

        editTextCode = view.findViewById (R.id.code_edittext);

        Button join = view.findViewById (R.id.enter_room_code);
        join.setOnClickListener (this::checkRoomCode);
    }

    private void checkRoomCode(View view) {
        String code = editTextCode.getText ().toString ();

        if(code.length () != 10) {
            editTextCode.setError ("code is too shorter");
            return;
        }else
            editTextCode.setError (null);
        FunctionsUtils.addUserToRoomWithCode (code, getContext (), roomModels, adapter);
        dismiss ();
    }
}
