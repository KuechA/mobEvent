package fr.eurecom.Ready2Meet.database;


import java.util.Map;

public class User {
    public String DisplayName;
    public String ParticipatingEvents;
    //public Map<String, Boolean> ParticipatingEvents;
    public String ProfilePictureURL;

    public User() {}

    public User(String displayName, String participatingEvents, String profilePictureUrl) {
        this.DisplayName = displayName;
        this.ParticipatingEvents = participatingEvents;
        this.ProfilePictureURL = profilePictureUrl;
    }
}
