package fr.eurecom.Ready2Meet.database;

import java.util.Map;

public class Event {
    public String title;
    public String description;
    public String owner;
    public Long current; // TODO: Change to int
    public Map<String, Boolean> categories; // TODO: Change this to List
    public Long capacity; // TODO: Change to int
    public String picture;
    public String place;
    public String startTime;
    public String endTime;
    public Map<String, Boolean> Participants; // Map seems to be more scalable according to firebase documentation
    public Map<String, Boolean> WhoReported; // TODO: Change to List

    public Event() {}

    public Event(String title, String description, String owner, Long current, Map<String, Boolean> categories,
                 Long capacity, String picture, String place, String startTime, String endTime,
                 Map<String, Boolean> participants, Map<String, Boolean> whoReported) {
        this.title = title;
        this.description = description;
        this.owner = owner;
        this.current = current;
        this.categories = categories;
        this.capacity = capacity;
        this.picture = picture;
        this.place = place;
        this.startTime = startTime;
        this.endTime = endTime;
        Participants = participants;
        WhoReported = whoReported;
    }

    @Override
    public String toString() {
        return title + " : " + place + ", " + startTime + " -- " + endTime + " created by " + owner + ".\n\tAttending: " + Participants.keySet().toString();
    }
}
