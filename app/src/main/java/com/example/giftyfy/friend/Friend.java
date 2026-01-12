package com.example.giftyfy.friend;

import java.util.List;

public class Friend {
    public String id;
    public String userId;
    public String name;
    public String birthday;
    public String relation;
    public List<String> interests;
    public boolean isExpanded = false;

    public Friend() {}

    public Friend(String name, String birthday, String relation, List<String> interests) {
        this.name = name;
        this.birthday = birthday;
        this.relation = relation;
        this.interests = interests;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }
    public String getRelation() { return relation; }
    public void setRelation(String relation) { this.relation = relation; }
    public List<String> getInterests() { return interests; }
    public void setInterests(List<String> interests) { this.interests = interests; }
    public boolean isExpanded() { return isExpanded; }
    public void setExpanded(boolean expanded) { this.isExpanded = expanded; }
}
