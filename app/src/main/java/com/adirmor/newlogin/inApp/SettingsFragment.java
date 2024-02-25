package com.adirmor.newlogin.inApp;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.adirmor.newlogin.Models.UserModel;
import com.adirmor.newlogin.R;
import com.adirmor.newlogin.Utils.FirebaseUtils;
import com.adirmor.newlogin.loginAndRegister.Login;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class SettingsFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_PIC_REQUEST = 2;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 3;
    private TextInputEditText changeUsername;
    private TextView verifyEmail;
    private ImageView IVPreviewImage;


    public SettingsFragment() {

    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate (R.layout.fragment_settings, container, false);

        IVPreviewImage = view.findViewById (R.id.ProfileImage);
        IVPreviewImage.setOnClickListener(this::changeProfilePhoto);

        Button saveChanges = view.findViewById (R.id.saveChangesButton);
        saveChanges.setOnClickListener (this::SaveChanges);

        Button logout = view.findViewById (R.id.logoutButton);
        logout.setOnClickListener (this::logout);

        Button deleteAccount = view.findViewById (R.id.deleteAccountButton);
        deleteAccount.setOnClickListener (this::deleteAccountFromFirebase);

        TextView verifyEmail = view.findViewById (R.id.verifyButton);
        verifyEmail.setOnClickListener (this::verifyEmailButton);

        addValuesToInputs (view);
        if (FirebaseUtils.isUserEmailVerify ()) {
            verifyEmail.setText ("Verified.");
            verifyEmail.setEnabled (false);
        }
        return view;
    }
    public void addValuesToInputs(View view) {
        changeUsername = view.findViewById (R.id.changeUsername);
        verifyEmail = view.findViewById (R.id.verifyEmail);
        IVPreviewImage = view.findViewById (R.id.ProfileImage);

        printData ();

    }
    private void printData() {
        FirebaseUtils.getUserModel ().get ().addOnCompleteListener (task -> {
            if(task.isSuccessful ()){
                UserModel userModel = task.getResult ().toObject (UserModel.class);
                try {
                    changeUsername.setText (userModel.getUsername ());
                    verifyEmail.setText (userModel.getEmail ());
                } catch (Exception e) {
                    //ignore
                }
            }
        });
        FirebaseUtils.getProfilePictureReference ().getDownloadUrl ()
                .addOnSuccessListener (uri -> Picasso.get ().load (uri).into (IVPreviewImage));
    }
    public void SaveChanges(View view) {

        HashMap<String, Object> update = new HashMap<> ();
        update.put ("Username", changeUsername.getText ().toString ());

        FirebaseUtils.getUserModel ().update (update).addOnSuccessListener (unused -> {
            Toast.makeText (getContext (), "saved changes", Toast.LENGTH_SHORT).show ();
        }).addOnFailureListener (e -> {
            Toast.makeText (getContext (), "Failed to save changes", Toast.LENGTH_SHORT).show ();
        });
        printData ();
    }
    public void logout(View view) {

        AlertDialog.Builder logoutAccount = new AlertDialog.Builder (view.getContext ());
        logoutAccount.setTitle ("Logout?");
        logoutAccount.setMessage ("Are you sure?");

        logoutAccount.setPositiveButton ("Confirm", (dialogInterface, i) -> {

            SharedPreferences sharedPreferences = SettingsFragment.this.getActivity().getSharedPreferences ("isAlreadySignIn", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit ();
            editor.putBoolean ("remember", false);
            editor.apply ();

            FirebaseAuth.getInstance ().signOut ();
            Intent loginPage = new Intent (getContext (), Login.class);
            startActivity (loginPage);
            SettingsFragment.this.getActivity().finish ();
        });

        logoutAccount.show ();

    }
    public void verifyEmailButton(View view) {
        FirebaseAuth.getInstance ().getCurrentUser ().sendEmailVerification ()
                .addOnSuccessListener (unused -> Toast.makeText (getContext (), "Verification Email Has Been Sent.", Toast.LENGTH_SHORT).show ())
                .addOnFailureListener (e -> Toast.makeText (getContext (), "Email not sent " + e.getMessage (), Toast.LENGTH_SHORT).show ());
    }
    public void deleteAccountFromFirebase(View view) {

        AlertDialog.Builder deleteAccount = new AlertDialog.Builder (view.getContext ());
        deleteAccount.setTitle ("Delete account?");
        deleteAccount.setMessage ("After you confirm you cant undo, are you sure?");

        deleteAccount.setPositiveButton ("Confirm", (dialogInterface, i) -> FirebaseUtils.getUserModel ().delete ()
                .addOnSuccessListener (unused -> {
                    SharedPreferences sharedPreferences = SettingsFragment.this.getActivity().getSharedPreferences ("isAlreadySignIn", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit ();
                    editor.putBoolean ("remember", false);
                    editor.apply ();

                    FirebaseAuth.getInstance ().getCurrentUser ().delete ();
                    Toast.makeText (getContext (), "account has been deleted!", Toast.LENGTH_SHORT).show ();
                    startActivity (new Intent (getContext (), Login.class));
                    SettingsFragment.this.getActivity().finish ();
                }));

        deleteAccount.show ();
    }
    public void changeProfilePhoto(View view) {

        LinearLayout GalleryButton, CameraButton, RemoveButton;

        BottomSheetDialog dialog = new BottomSheetDialog (getContext ());
        dialog.setContentView (getLayoutInflater ().inflate (R.layout.bottom_popup_gallery_choose, null));

        GalleryButton = dialog.findViewById (R.id.openGalleryForProfilePic);
        CameraButton = dialog.findViewById (R.id.openCameraForProfilePic);
        RemoveButton = dialog.findViewById (R.id.removeProfilePic);

        dialog.getWindow ().getAttributes ().windowAnimations = R.style.DialogAnimation;

        GalleryButton.setOnClickListener (v -> {
            if (ContextCompat.checkSelfPermission (getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions (SettingsFragment.this.getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_REQUEST_CODE);
            } else {
                Intent pickImageIntent = new Intent (Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult (pickImageIntent, PICK_IMAGE_REQUEST);
            }
            dialog.dismiss ();
        });

        CameraButton.setOnClickListener (v -> {
            if (ContextCompat.checkSelfPermission (getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions (SettingsFragment.this.getActivity(),
                        new String[]{Manifest.permission.CAMERA},
                        1001);
            } else {
                try {
                    Intent cameraIntent = new Intent (MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult (cameraIntent, CAMERA_PIC_REQUEST);
                } catch (Exception e) {
                    Toast.makeText (getContext(), "you have to accept the permission first", Toast.LENGTH_SHORT).show ();
                }
            }
            dialog.dismiss ();
        });

        RemoveButton.setOnClickListener (v -> {
            FirebaseUtils.getProfilePictureReference ().delete ().addOnCompleteListener (task -> {
                if (task.isSuccessful ())
                    IVPreviewImage.setImageResource (R.drawable.avatar);

            });
            dialog.dismiss ();
        });

        dialog.show ();

    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult (requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            if (requestCode == PICK_IMAGE_REQUEST) {
                Uri selectedImageUri = data.getData ();
                IVPreviewImage.setImageURI (selectedImageUri);

                FirebaseUtils.getProfilePictureReference ().putFile (selectedImageUri)
                        .addOnSuccessListener (taskSnapshot -> FirebaseUtils.getProfilePictureReference ().getDownloadUrl ());
            }
        }
    }
}