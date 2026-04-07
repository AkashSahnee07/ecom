package com.ecommerce.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ShippingAddress {
    
    @Column(name = "shipping_first_name")
    private String firstName;
    
    @Column(name = "shipping_last_name")
    private String lastName;
    
    @Column(name = "shipping_company")
    private String company;
    
    @Column(name = "shipping_address_line1")
    private String addressLine1;
    
    @Column(name = "shipping_address_line2")
    private String addressLine2;
    
    @Column(name = "shipping_city")
    private String city;
    
    @Column(name = "shipping_state")
    private String state;
    
    @Column(name = "shipping_postal_code")
    private String postalCode;
    
    @Column(name = "shipping_country")
    private String country;
    
    @Column(name = "shipping_phone")
    private String phone;
    
    @Column(name = "shipping_email")
    private String email;
    
    @Column(name = "shipping_instructions")
    private String specialInstructions;
    
    // Constructors
    public ShippingAddress() {}
    
    public ShippingAddress(String firstName, String lastName, String addressLine1, 
                          String city, String state, String postalCode, String country) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.addressLine1 = addressLine1;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
    }
    
    // Helper methods
    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
    
    public String getFullAddress() {
        StringBuilder address = new StringBuilder();
        if (addressLine1 != null) address.append(addressLine1);
        if (addressLine2 != null && !addressLine2.trim().isEmpty()) {
            address.append(", ").append(addressLine2);
        }
        if (city != null) address.append(", ").append(city);
        if (state != null) address.append(", ").append(state);
        if (postalCode != null) address.append(" ").append(postalCode);
        if (country != null) address.append(", ").append(country);
        return address.toString();
    }
    
    // Getters and setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    
    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }
    
    public String getAddressLine2() { return addressLine2; }
    public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
}
