package com.example.giftyfy.friend;

import java.util.List;

public class Friend {
    private String name;
    private String birthday;
    private String relation;
    private List<String> interests;
    public Friend(String name, String birthday, String relation, List<String> interests) {
        this.name = name;
        this.birthday = birthday;
        this.relation = relation;
        this.interests = interests;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public void addInterest(String interest) {
        interests.add(interest);
    }

    public void removeInterest(String interest) {
        interests.remove(interest);
    }

    public String getName() {
        return name;
    }

    public String getBirthday() {
        return birthday;
    }

    public String getRelation() {
        return relation;
    }

    public List<String> getInterests() {
        return interests;
    }
}
