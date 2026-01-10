package com.example.giftyfy.friend;

import java.util.List;

public class Friend {
    private String id; // Firebase 도큐먼트 ID를 저장할 변수
    private String name;
    private String birthday;
    private String relation;
    private List<String> interests;
    private boolean isExpanded = false;

    // ✅ Firebase 통신을 위해 반드시 필요한 빈 생성자
    public Friend() {}

    public Friend(String name, String birthday, String relation, List<String> interests) {
        this.name = name;
        this.birthday = birthday;
        this.relation = relation;
        this.interests = interests;
    }

    // Getter & Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }
    public String getRelation() { return relation; }
    public void setRelation(String relation) { this.relation = relation; }
    public List<String> getInterests() { return interests; }
    public void setInterests(List<String> interests) { this.interests = interests; }
    public boolean isExpanded() { return isExpanded; }
    public void setExpanded(boolean expanded) { isExpanded = expanded; }
}
