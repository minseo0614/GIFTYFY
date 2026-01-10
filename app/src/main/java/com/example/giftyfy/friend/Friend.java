package com.example.giftyfy.friend;

import java.util.List;

public class Friend {
    private String name;
    private String birthday;
    private String relation;
    private List<String> interests;
    private boolean isExpanded = false; // 확장 상태 저장용 변수 추가

    public Friend(String name, String birthday, String relation, List<String> interests) {
        this.name = name;
        this.birthday = birthday;
        this.relation = relation;
        this.interests = interests;
    }

    public String getName() { return name; }
    public String getBirthday() { return birthday; }
    public String getRelation() { return relation; }
    public void setRelation(String relation) { this.relation = relation; }
    public List<String> getInterests() { return interests; }
    
    public boolean isExpanded() { return isExpanded; }
    public void setExpanded(boolean expanded) { isExpanded = expanded; }
}
