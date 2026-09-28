package com.library.model;

import java.time.LocalDate;

/**
 * A registered library member who can borrow books.
 */
public class Member extends Entity {

    private String name;
    private String email;
    private String phone;
    private LocalDate joinDate;

    public Member(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.joinDate = LocalDate.now();
    }

    public Member(int id, String name, String email, String phone, LocalDate joinDate) {
        super(id);
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.joinDate = joinDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    @Override
    public String getSummary() {
        return String.format("[#%d] %s <%s> - member since %s", id, name, email, joinDate);
    }
}
