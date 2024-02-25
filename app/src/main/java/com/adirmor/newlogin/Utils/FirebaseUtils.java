package com.adirmor.newlogin.Utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseUtils {

    public static String getCurrentUserId(){
        return FirebaseAuth.getInstance ().getCurrentUser ().getUid ();
    }
    public static CollectionReference getDailyTaskModel(){
        return FirebaseFirestore.getInstance ().collection ("users").document (getCurrentUserId()).collection ("daily_tasks");
    }
    public static CollectionReference getPlannedTaskModel(){
        return FirebaseFirestore.getInstance ().collection ("users").document (getCurrentUserId()).collection ("planned_tasks");
    }
    public static DocumentReference getUserModel(){
        return FirebaseFirestore.getInstance ().collection ("users").document (getCurrentUserId());
    }
    public static boolean isUserEmailVerify(){
        return FirebaseAuth.getInstance ().getCurrentUser ().isEmailVerified ();
    }
    public static StorageReference getProfilePictureReference(){
        return FirebaseStorage.getInstance ().getReference ().child (getCurrentUserId ()).child ("Profile_pic");
    }
    public static CollectionReference getListsCollection(){
        return getUserModel ().collection ("Lists");
    }
    public static Query getSpecificList(String id){
        return getListsCollection ().whereEqualTo ("id", id);
    }
}
