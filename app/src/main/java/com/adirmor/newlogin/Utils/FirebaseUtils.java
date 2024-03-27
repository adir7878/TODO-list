package com.adirmor.newlogin.Utils;

import com.adirmor.newlogin.Models.RoomModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
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
    public static StorageReference getProfilePictureReference(String id){
        return FirebaseStorage.getInstance ().getReference ().child ("Profile_pics").child (id);
    }
    public static CollectionReference getRoomsCollection(){
        return FirebaseFirestore.getInstance ().collection ("Rooms");
    }
    public static DocumentReference getSpecificRoom(String roomId){
        return getRoomsCollection ().document (roomId);
    }
    public static Query getSpecificRoomByCode(String code){
        return getRoomsCollection ().whereEqualTo ("code", code);
    }

    public static CollectionReference getTasksOfRoomCollection(String id){
        return FirebaseUtils.getSpecificRoom (id).collection ("tasks");
    }

    public static CollectionReference getUserModelOfRoomCollection(String roomID){
        return FirebaseUtils.getSpecificRoom(roomID).collection ("participants");
    }
}
