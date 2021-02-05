package com.morningwalk.ihome.explorer.expandablelistview;
// G. R. Akhtar, April 27, 2020

public class Member {
    private int id;
    private String name;
    private int status;

    public Member(int id, String name, int status) {
        assert(id > 0);
        this.id = id;
        this.name = name;
        this.status = status;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        assert(id > 0);
        this.id = id;
    }
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
