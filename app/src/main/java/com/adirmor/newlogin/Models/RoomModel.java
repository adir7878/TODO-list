package com.adirmor.newlogin.Models;

import com.adirmor.newlogin.Utils.FirebaseUtils;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class RoomModel {

    private String id = String.valueOf (UUID.randomUUID ());
    private String code = generateRoomCode ();
    private String name;
    private Timestamp time;
    private String hostId = FirebaseUtils.getCurrentUserId ();
    private List<String> participantIDs = new ArrayList<> ();
    private List<String> blockedUsersID = new ArrayList<> ();

    public RoomModel() {
        if(!participantIDs.contains (FirebaseUtils.getCurrentUserId ()))
            participantIDs.add (FirebaseUtils.getCurrentUserId ());
    }

    public RoomModel(String name, Timestamp time) {
        this.name = name;
        this.time = time;
        participantIDs.add (FirebaseUtils.getCurrentUserId ());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public List<String> getParticipantIDs() {
        return participantIDs;
    }

    public void setParticipantIDs(List<String> participantIDs) {
        this.participantIDs = participantIDs;
    }

    // Method to generate a random room code
    public static String generateRoomCode() {
        final String ALLOWED_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijkmnlopqrstuvwxyz0123456789";
        final int CODE_LENGTH = 10;
        StringBuilder codeBuilder = new StringBuilder();
        Random random = new Random ();

        // Generate random characters for the room code
        for (int i = 0; i < CODE_LENGTH; i++) {
            int randomIndex = random.nextInt(ALLOWED_CHARACTERS.length());
            char randomChar = ALLOWED_CHARACTERS.charAt(randomIndex);
            codeBuilder.append(randomChar);
        }

        return codeBuilder.toString();
    }

    public List<String> getBlockedUsersID() {
        return blockedUsersID;
    }

    public void setBlockedUsersID(List<String> blockedUsersID) {
        this.blockedUsersID = blockedUsersID;
    }
}
