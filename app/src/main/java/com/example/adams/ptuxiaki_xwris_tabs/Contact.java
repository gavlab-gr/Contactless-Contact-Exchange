package com.example.adams.ptuxiaki_xwris_tabs;



public class Contact {



    String firstName;
    String email;
    String company;
    String number ;
    String address;



    public Contact(String firstName, String email, String company, String number , String address) {

        this.firstName = firstName;
        this.email = email;
        this.address= address;
        this.company = company;
        this.number = number;
    }



    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return this.firstName;
    }
}
