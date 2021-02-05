package com.morningwalk.ihome.explorer.expandablelistview;
// G. R. Akhtar, April 27, 2020
// updated April 30, 2020

import java.util.ArrayList;

public class Group {
    private String title;
    private ArrayList<Member> items;
    private String admin;

    public Group(String title, String admin, ArrayList<Member> items) {
        this.title = title;
        this.items = items;
        this.admin = admin;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    ArrayList<Member> getItems() {
        return items;
    }
    void setItems(ArrayList<Member> items) {
        this.items = items;
    }
    public String getAdmin() {
        return admin;
    }
    public void setAdmin(String admin) {
        this.admin = admin;
    }
    public int getAdminId() {
        for(Member member:getItems()) {
            if(admin.equals(member.getName())) return member.getId();
        }
        return -1;
    }
}
