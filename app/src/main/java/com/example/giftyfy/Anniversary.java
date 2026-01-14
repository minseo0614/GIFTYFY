package com.example.giftyfy;

import com.google.firebase.firestore.DocumentId;

public class Anniversary {
    @DocumentId
    private String id;
    private String title;
    private int month;
    private int day;
    private String personUid;
    private String memo;
    private boolean isRepeat;

    public Anniversary() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public int getDay() { return day; }
    public void setDay(int day) { this.day = day; }
    public String getPersonUid() { return personUid; }
    public void setPersonUid(String personUid) { this.personUid = personUid; }
    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }
    public boolean isRepeat() { return isRepeat; }
    public void setRepeat(boolean repeat) { isRepeat = repeat; }
}
