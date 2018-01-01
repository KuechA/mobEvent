package fr.eurecom.Ready2Meet.database;

public class Message {
    public String message;
    public String senderId;
    public String time;

    public Message() {
    }

    public Message(String message, String senderId, String time) {
        this.message = message;
        this.senderId = senderId;
        this.time = time;
    }
}
