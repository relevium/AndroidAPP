package com.example.android.releviumfinal;

public class Messages
{
    private String from, message, type, to, messageID, time, date, name, fromName;

    public Messages()
    {

    }

    public Messages(String from, String message, String time, String date, String fromName) {
        this.from = from;
        this.message = message;
        this.type = "text";
        this.date = date;
        this.time = time;
        this.fromName = fromName;
    }

    public String getFromName() {
        return fromName;
    }

    public void getFromName(String fromName) {
        this.from = fromName;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}