package com.ecommerce.shipping.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Address {
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String addressLine1;
    
    @Column
    private String addressLine2;
    
    @Column(nullable = false)
    private String city;
    
    @Column(nullable = false)
    private String state;
    
    @Column(nullable = false)
    private String zipCode;
    
    @Column(nullable = false)
    private String country;
    
    @Column
    private String phoneNumber;
    
    // Default constructor
    public Address() {}
    
    // Constructor with required fields
    public Address(String name, String addressLine1, String city, String state, String zipCode, String country) {
        this.name = name;
        this.addressLine1 = addressLine1;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
    }
    
    // Full constructor
    public Address(String name, String addressLine1, String addressLine2, String city, 
                  String state, String zipCode, String country, String phoneNumber) {
        this.name = name;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
        this.phoneNumber = phoneNumber;
    }
    
    // Business methods
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(addressLine1);
        if (addressLine2 != null && !addressLine2.trim().isEmpty()) {
            sb.append(", ").append(addressLine2);
        }
        sb.append(", ").append(city);
        sb.append(", ").append(state);
        sb.append(" ").append(zipCode);
        sb.append(", ").append(country);
        return sb.toString();
    }
    
    public boolean isComplete() {
        return name != null && !name.trim().isEmpty() &&
               addressLine1 != null && !addressLine1.trim().isEmpty() &&
               city != null && !city.trim().isEmpty() &&
               state != null && !state.trim().isEmpty() &&
               zipCode != null && !zipCode.trim().isEmpty() &&
               country != null && !country.trim().isEmpty();
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAddressLine1() {
        return addressLine1;
    }
    
    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }
    
    public String getAddressLine2() {
        return addressLine2;
    }
    
    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getZipCode() {
        return zipCode;
    }
    
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}